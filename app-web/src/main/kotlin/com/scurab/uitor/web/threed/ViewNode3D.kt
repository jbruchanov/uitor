package com.scurab.uitor.web.threed

import THREE.BoxGeometry
import THREE.EdgesGeometry
import THREE.LineSegments
import THREE.Mesh
import THREE.MeshBasicMaterial
import THREE.Object3D
import THREE.Scene
import THREE.TextureLoader
import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.util.obj

private const val DEPTH = 0.01
private const val HALF = 0.5
private const val COLOR_TEXTURE_DEFAULT = "#FFFFFF"
private const val COLOR_TEXTURE_SELECTED = "#808080"
private const val COLOR_EDGE_LEAF_DEFAULT = "#005B00"
private const val COLOR_EDGE_LEAF_SELECTED = "#00FF00"
private const val COLOR_EDGE_NODE_DEFAULT = "#5B0000"
private const val COLOR_EDGE_NODE_SELECTED = "#FF0000"

/**
 * Help class to compose ViewNode related threejs objects
 */
class ViewNode3D(val viewNode: ViewNode) : IViewNode by viewNode {

    private val geometry = BoxGeometry(rect.width, rect.height, DEPTH)
    private val lineMaterial = THREE.LineBasicMaterial(obj {
        color = viewNode.edgeColor(false)
    })

    private val lineSegments = LineSegments(EdgesGeometry(geometry), lineMaterial)
        .withPosition(viewNode)
        .withName(viewNode.elementName("Edge"))

    private val textureMaterial = MeshBasicMaterial(obj {
        transparent = true
        color = COLOR_TEXTURE_DEFAULT
    }).withTexture(viewNode)

    @Suppress("DuplicatedCode")
    private val mesh = Mesh(geometry, textureMaterial)
        .withPosition(viewNode)
        .withName(viewNode.elementName("Mesh"))

    init {
        idsToViewNode3D[mesh.uuid] = this
        idsToViewNode3D[lineSegments.uuid] = this
    }

    fun add(scene: Scene) {
        scene.add(lineSegments)
        scene.add(mesh)
    }

    fun remove(scene: Scene) {
        scene.remove(lineSegments)
        scene.remove(mesh)
    }

    fun setSelected(selected: Boolean) {
        lineMaterial.color = viewNode.edgeColor(selected).threeColor
        textureMaterial.color = (if (selected) COLOR_TEXTURE_SELECTED else COLOR_TEXTURE_DEFAULT).threeColor
    }

    companion object {
        internal val textureLoader = TextureLoader()
        private val idsToViewNode3D = mutableMapOf<String, ViewNode3D>()

        fun fromObject(item: Object3D?): ViewNode3D? {
            return item?.let { idsToViewNode3D[it.uuid] }
        }
    }
}

//region ext methods
private fun ViewNode.edgeColor(selected: Boolean): String {
    return when {
        selected && isLeaf -> COLOR_EDGE_LEAF_SELECTED
        selected && !isLeaf -> COLOR_EDGE_NODE_SELECTED
        isLeaf -> COLOR_EDGE_LEAF_DEFAULT
        !isLeaf -> COLOR_EDGE_NODE_DEFAULT
        else -> COLOR_TEXTURE_DEFAULT
    }
}
private fun ViewNode.elementName(type: String) = "ViewNode-$type:${position}"

private fun <T : Object3D> T.withPosition(viewNode: ViewNode): T {
    val rect = viewNode.rect
    val x = rect.left
    val y = rect.top
    val z = 100 * viewNode.level + (viewNode.position / 5.0)

    val w = rect.width
    val h = rect.height

    translateX((+w * HALF) + x)
    translateY((-h * HALF) - y)
    translateZ((-DEPTH * HALF) + z)
    return this
}

private fun <T : Object3D> T.withName(name: String): T {
    this.name = name
    return this
}

private fun MeshBasicMaterial.withTexture(viewNode: ViewNode): MeshBasicMaterial {
    transparent = !viewNode.shouldRender
    if (viewNode.shouldRender) {
        //this expects to server handle the hammering
        map = ViewNode3D.textureLoader.load("view.png?position=${viewNode.position}").apply {
            minFilter = THREE.NearestFilter
            magFilter = THREE.NearestFilter
        }
    } else {
        opacity = 0
    }
    return this
}
//endregion ext methods