package com.scurab.uitor.web.threed

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.dlog
import com.scurab.uitor.common.util.vlog
import com.scurab.uitor.web.addMouseMoveListener
import com.scurab.uitor.web.inspector.InspectorViewModel
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.ui.HtmlView
import com.scurab.uitor.web.util.obj
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
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

class ThreeD(private val viewModel: InspectorViewModel) : HtmlView() {
    private val TAG = "ThreeD"
    override var element: HTMLElement? = null; private set

    private lateinit var camera: PerspectiveCamera
    private lateinit var renderer: WebGLRenderer
    private lateinit var scene: Scene
    private lateinit var controls: Any/*TrackballControls*/
    private val rayCaster = Raycaster()
    private val mouse = Vector2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)

    private val resizeAction = EventListener { e: Event ->
        camera.aspect = window.innerWidth / window.innerHeight.toDouble()
        camera.updateProjectionMatrix()
        renderer.setSize(window.innerWidth, window.innerHeight)
    }

    private val requestAnimationFrameAction: () -> Unit = { window.requestAnimationFrame(renderAction) }
    private val renderAction = { d: Double ->
        requestAnimationFrameAction()
        renderer.render(scene, camera)
        controls.asDynamic().update()
        raycastObjects()
    }

    private val mouseMoveAction = { event: MouseEvent ->
        mouse.x = (event.clientX / window.innerWidth.toDouble()) * 2 - 1
        mouse.y = 1 - 2 * (event.clientY / window.innerHeight.toDouble())
        dlog(TAG) { "MouseMove:${mouse.d}" }
    }

    override fun buildContent() {
        initScene()
    }

    override fun onAttached() {
        super.onAttached()
        initControls()
        window.addEventListener("resize", resizeAction)
        window.addMouseMoveListener(mouseMoveAction)
        requestAnimationFrameAction()
        viewModel.rootNode.observe {
            it?.let { buildLayout(it, scene) }
        }
    }

    private fun buildLayout(root: ViewNode, scene: Scene) {
        root.all()
            .map { ViewNode3D(it) }
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

    var selectedObject: ViewNode3D? = null
    private fun raycastObjects() {
        rayCaster.setFromCamera(mouse, camera)
        val intersectObjects = rayCaster.intersectObjects(scene.children)
        val item = intersectObjects.firstOrNull()?.item
        val n = ViewNode3D.fromObject(item)
        if (selectedObject != n) {
            selectedObject?.setSelected(false)
            selectedObject = n
            selectedObject?.setSelected(true)
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
