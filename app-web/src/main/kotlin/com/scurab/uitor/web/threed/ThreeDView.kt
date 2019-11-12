package com.scurab.uitor.web.threed

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.r2
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.ref
import com.scurab.uitor.common.util.vlog
import com.scurab.uitor.web.common.addMouseClickListener
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.ui.PageProgressBar
import com.scurab.uitor.web.util.obj
import js.threejs.BoxBufferGeometry
import js.threejs.GridHelper
import js.threejs.LoadingManager
import js.threejs.Mesh
import js.threejs.MeshBasicMaterial
import js.threejs.PerspectiveCamera
import js.threejs.Raycaster
import js.threejs.Scene
import js.threejs.TextureLoader
import js.threejs.TrackballControls
import js.threejs.Vector2
import js.threejs.WebGLRenderer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import kotlin.browser.window

//stop rendering with no events in 5s
private const val RENDER_PAUSE_TIMEOUT = 2000L
const val UNKNOWN_GAP = 4

class ThreeDView(private val viewModel: InspectorViewModel) : HtmlView() {
    private val TAG = "ThreeD"
    override var element: HTMLElement? = null; private set

    private lateinit var camera: PerspectiveCamera
    private lateinit var renderer: WebGLRenderer
    private lateinit var scene: Scene
    private lateinit var controls: TrackballControls
    private lateinit var pauseRenderingChannel: Channel<Boolean>
    private lateinit var resizeWindowChannel: Channel<Unit>
    private val rayCaster = Raycaster()
    private val mouse = Vector2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
    private var pointingObject: ViewNode3D? = null
    private var pausedRendering = false
    var renderAreaSizeProvider: () -> Pair<Double, Double> = {
        Pair(window.innerWidth.toDouble(), window.innerHeight.toDouble())
    }

    private val resizeWindowAction = {
        val (width, height) = renderAreaSizeProvider()
        dispatchContainerSizeChanged(width, height)
    }

    private val requestAnimationFrameAction: () -> Unit = { window.requestAnimationFrame(renderAction) }
    private val renderAction = { d: Double ->
        if (isAttached && !pausedRendering) {
            requestAnimationFrameAction()
            renderer.render(scene, camera)
            controls.asDynamic().update()
            raycast()
        }
    }

    private val mouseMoveAction = { event: MouseEvent ->
        resetRenderingPause()
        val (w, h) = renderAreaSizeProvider()
        mouse.x = (event.clientX / w) * 2 - 1
        mouse.y = 1 - 2 * (event.clientY / h)
        dlog(TAG) { "MouseMove:${mouse.d}" }
    }

    override fun buildContent() {
        initScene()
    }

    override fun onAttached() {
        super.onAttached()
        initControls()
        document.addMouseMoveListener(mouseMoveAction)
        requestAnimationFrameAction()
        viewModel.rootNode.observe {
            it?.let { buildLayout(it, scene) }
        }

        pauseRenderingChannel = Channel<Boolean>().also { channel ->
            launch {
                channel.consumeAsFlow().debounce(RENDER_PAUSE_TIMEOUT).collect {
                    vlog(TAG) { "Rendering Paused" }
                    pausedRendering = true
                }
            }
        }

        resizeWindowChannel = Channel<Unit>().also { channel ->
            document.addWindowResizeListener {
                channel.offer(Unit)
            }

            launch {
                channel.consumeAsFlow().debounce(1000).collect {
                    resizeWindowAction()
                }
            }
        }

        element.ref.addMouseClickListener {
            findObject()?.viewNode?.let {
                //ignore clicking outside, just keep selected the last one
                viewModel.selectedNode.post(it)
            }
            resetRenderingPause()
        }
    }

    fun dispatchContainerSizeChanged(width: Double, height: Double) {
        dlog(TAG) { "dispatchContainerSizeChanged:${width.r2}x${height.r2}" }
        camera.aspect = width / height
        camera.updateProjectionMatrix()
        val height = height - UNKNOWN_GAP//looks like necessary, unclear why 4px
        renderer.setSize(width, height)
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
        resizeWindowChannel.cancel()
        super.onDetached()
    }

    private fun buildLayout(root: ViewNode, scene: Scene) {
        val token = PageProgressBar.show()
        val textureLoader = TextureLoader(manager = LoadingManager(
            onLoad = { PageProgressBar.hide(token) },
            onProgress = { _, _, _ -> resetRenderingPause() },
            onError = { PageProgressBar.hide(token) }
        ))
        root.all()
            .map { ViewNode3D(ViewNode3DContext(it, viewModel.screenIndex, textureLoader, viewModel.clientConfig)) }
            .toList().let {
                it.forEach { n -> n.add(scene) }
                val toRender = it.count { n -> n.viewNode.shouldRender }
                if (toRender == 0) {
                    PageProgressBar.hide(token)
                }
            }
    }

    private fun initScene() {
        scene = Scene()
        renderer = WebGLRenderer(obj {
            antialias = true
        })
        element = renderer.domElement
        camera = PerspectiveCamera(
            50,
            window.innerWidth / window.innerHeight.toDouble(),
            1,
            1000000
        )
        resizeWindowAction()

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

    private fun raycast() {
        val n = findObject()
        if (pointingObject != n) {
            pointingObject?.setSelected(false)
            pointingObject = n
            pointingObject?.setSelected(true)
            vlog(TAG) { "Pointing at:ViewNode=${n?.viewNode?.position}" }
        }
    }

    private fun findObject(mouse: Vector2 = this.mouse): ViewNode3D? {
        rayCaster.setFromCamera(mouse, camera)
        val intersectObjects = rayCaster.intersectObjects(scene.children)
        val item = intersectObjects.firstOrNull()?.item
        val n = ViewNode3D.fromObject(item)

        dlog(TAG) { "raycastObjects:${mouse.d}, children:${scene.children.size} found:${item?.name}" }
        return n
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
