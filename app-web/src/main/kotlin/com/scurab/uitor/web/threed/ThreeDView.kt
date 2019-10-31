package com.scurab.uitor.web.threed

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.r2
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.common.util.vlog
import com.scurab.uitor.web.common.addMouseClickListener
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.ColumnsLayout
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.SCROLL_BAR_WIDTH
import com.scurab.uitor.web.util.obj
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import threejs.BoxBufferGeometry
import threejs.GridHelper
import threejs.Mesh
import threejs.MeshBasicMaterial
import threejs.PerspectiveCamera
import threejs.Raycaster
import threejs.Scene
import threejs.TrackballControls
import threejs.Vector2
import threejs.WebGLRenderer
import kotlin.browser.window

//stop rendering with no events in 5s
private const val RENDER_PAUSE_TIMEOUT = 5000L

class ThreeDView(private val viewModel: InspectorViewModel) : HtmlView() {
    private val TAG = "ThreeD"
    override var element: HTMLElement? = null; private set

    private lateinit var camera: PerspectiveCamera
    private lateinit var renderer: WebGLRenderer
    private lateinit var scene: Scene
    private lateinit var controls: TrackballControls
    private lateinit var pauseRenderingChannel: Channel<Boolean>
    private val rayCaster = Raycaster()
    private val mouse = Vector2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
    private var pointingObject: ViewNode3D? = null
    private var pausedRendering = false

    private val resizeAction = { e: Event ->
        val size = element.ref.getBoundingClientRect()
        dlog("SIZE") {"${size.width.r2}x${size.height.r2}"}
        dispatchContainerSizeChanged(size.width, window.innerHeight.toDouble())
    }

    private val requestAnimationFrameAction: () -> Unit = { window.requestAnimationFrame(renderAction) }
    private val renderAction = { d: Double ->
        if (isAttached && !pausedRendering) {
            requestAnimationFrameAction()
            renderer.render(scene, camera)
            controls.asDynamic().update()
            raycastObjects()
        }
    }

    private val mouseMoveAction = { event: MouseEvent ->
        resetRenderingPause()
        //TODO, check this
        //mouse.x = (event.clientX / window.innerWidth.toDouble()) * 2 - 1
        mouse.x = (event.clientX / element.ref.getBoundingClientRect().width.toDouble()) * 2 - 1
        mouse.y = 1 - 2 * (event.clientY / window.innerHeight.toDouble())
        dlog(TAG) { "MouseMove:${mouse.d}" }
    }

    override fun buildContent() {
        initScene()
    }

    override fun onAttached() {
        super.onAttached()
        initControls()
        document.addWindowResizeListener(resizeAction)
        document.addMouseMoveListener(mouseMoveAction)
        requestAnimationFrameAction()
        viewModel.rootNode.observe {
            it?.let { buildLayout(it, scene) }
        }

        pauseRenderingChannel = Channel<Boolean>().also { channel ->
            GlobalScope.launch {
                channel.consumeAsFlow().debounce(RENDER_PAUSE_TIMEOUT).collect {
                    vlog(TAG) { "Rendering Paused" }
                    pausedRendering = true
                }
            }
        }

        element.ref.addMouseClickListener {
            pointingObject?.let {
//                val node = pickNodeForNotification(viewModel.selectedNode.item, it.viewNode)
                viewModel.selectedNode.post(it.viewNode)
//                resetRenderingPause()
            }
        }
    }

    fun dispatchContainerSizeChanged(width: Double, height: Double) {
        camera.aspect = width / height
        camera.updateProjectionMatrix()
        renderer.setSize(width, height - ColumnsLayout.UNKNOWNGAP)
    }

    private fun resetRenderingPause() {
        vlog { "Rendering Pause reset" }
        if (pausedRendering) {
            requestAnimationFrameAction()
        }
        pausedRendering = false
        pauseRenderingChannel.offer(true)
    }

    override fun onDetached() {
        controls.dispose()
        pauseRenderingChannel.cancel()
        super.onDetached()
    }

    private fun buildLayout(root: ViewNode, scene: Scene) {
        root.all()
            .map { ViewNode3D(it, viewModel.screenIndex) }
            .forEach {
                it.add(scene)
            }
    }

    private fun initScene() {
        scene = Scene()
        renderer = WebGLRenderer(obj {
            antialias = true
        })
        renderer.setSize(window.innerWidth, window.innerHeight)
        element = renderer.domElement

        camera = PerspectiveCamera(
            50,
            window.innerWidth / window.innerHeight.toDouble(),
            1,
            1000000
        )
        camera.position.apply {
            x = 3000.0
            y = 1500.0
            z = 7000.0
        }

        //debugAddOriginBox(scene)
        addGrid(scene)
    }

    private fun initControls() {
        check(element?.parentElement != null)
        { "TrackballControls have to be initialized after attaching the element to DOM" }
        //has to be created after the element has been added to parent

        controls = TrackballControls(camera, renderer.domElement).apply {
            maxDistance = 100000.0
            addEventListener("change") {
                dlog("${TAG}Controls") { "Camera: Position:${camera.position.d} Euler:${camera.rotation.d}" }
            }
        }

        document.addKeyDownListener { keyboardEvent ->
            vlog(TAG) { "KeyEvent:${keyboardEvent.keyCode} => '${keyboardEvent.key}'(${keyboardEvent.code})" }
            when (keyboardEvent.keyCode) {
                106/*'*'*/ -> camera.rotation.x = 0.0
                107/*+*/ -> camera.rotation.y = 0.0
                109/*-*/ -> camera.rotation.z = 0.0
                else -> {/*none*/
                }
            }
        }
    }

    private fun raycastObjects() {
        rayCaster.setFromCamera(mouse, camera)
        val intersectObjects = rayCaster.intersectObjects(scene.children)
        val item = intersectObjects.firstOrNull()?.item
        val n = ViewNode3D.fromObject(item)
        if (pointingObject != n) {
            pointingObject?.setSelected(false)
            pointingObject = n
            pointingObject?.setSelected(true)
            vlog(TAG) { "Pointing at:ViewNode=${n?.viewNode?.position}" }
        }
        dlog(TAG) { "raycastObjects:${mouse.d}, children:${scene.children.size} found:${item?.name}" }
    }

    private fun debugAddOriginBox(scene: Scene) {
        val geometry = BoxBufferGeometry(50, 50, 50)
        val material = MeshBasicMaterial(obj {
            color = Color.Red.htmlRGB
        })
        val mesh = Mesh(geometry, material)
        mesh.name = "Box"
        scene.add(mesh)
    }

    private fun addGrid(scene: Scene) {
        val color = Color.Gray20.threeColor
        scene.add(GridHelper(100000, 50, color, color).apply {
            position.y = -8000.0
        })
    }
}
