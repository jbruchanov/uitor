package com.scurab.uitor.web.threed

import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.util.obj
import threejs.BackSide
import threejs.BoxGeometry
import threejs.Color
import threejs.EdgesGeometry
import threejs.FrontSide
import threejs.LineSegments
import threejs.Mesh
import threejs.MeshBasicMaterial
import threejs.Object3D
import threejs.Scene
import threejs.Side
import threejs.TextureLoader

private const val DEPTH = 1
private const val HALF = 0.5
private const val COLOR_TEXTURE_DEFAULT = "#FFFFFF"
private const val COLOR_TEXTURE_SELECTED = "#808080"

/**
 * Help class to compose ViewNode related threejs objects
 */
class ViewNode3D(val viewNode: ViewNode, private val screenIndex: Int) : IViewNode by viewNode {

    override val rect = viewNode.renderAreaRelative?.let { viewNode.rect.addRelative(it) } ?: viewNode.rect
    private val hasCustomRenderArea = rect != viewNode.rect
    private val geometry = BoxGeometry(rect.width, rect.height, DEPTH)
    private val lineMaterial = threejs.LineBasicMaterial(obj {
        color = viewNode.edgeColor(false)
    })

    private val lineSegments = LineSegments(EdgesGeometry(geometry), lineMaterial)
        .withPosition(viewNode)
        .withName(viewNode.elementName("Edge"))

    private val textureSideMaterial = MeshBasicMaterial(obj {
        transparent = true
        opacity = 1
        color = "#000000"
    })

    private val textures = arrayOf<MeshBasicMaterial?>(
        //can't be nulls for raycast
        textureSideMaterial,//right
        textureSideMaterial,//left
        textureSideMaterial,//top
        textureSideMaterial,//bottom
        viewTexture(FrontSide),//front
        viewTexture(BackSide)//back
    )

    private val mesh = Mesh(geometry, textures)
        .withPosition(viewNode)
        .withName(viewNode.elementName("Mesh"))

    init {
        mesh.userData = this
        lineSegments.userData = this
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
        lineMaterial.color = viewNode.edgeColor(selected)
        textures.forEach {
            if (textureSideMaterial != it) {
                it?.color = (if (selected) COLOR_TEXTURE_SELECTED else COLOR_TEXTURE_DEFAULT).threeColor
            }
        }
    }

    private fun viewTexture(side: Side): MeshBasicMaterial {
        return MeshBasicMaterial(obj {
            transparent = true
            opacity = 1
            color = COLOR_TEXTURE_DEFAULT
            depthWrite = false
            this.side = FrontSide
        }).withTexture(viewNode, side, screenIndex)
    }

    private fun ViewNode.edgeColor(selected: Boolean): Color {
        return colorMatcher[StateMatcher(
            isLeaf, selected, hasCustomRenderArea
        )] ?: throw IllegalStateException("Invalid state for color, " +
                "isLeaf:$isLeaf, selected:$selected, hasCustomRenderArea:$hasCustomRenderArea")
    }

    companion object {
        internal val textureLoader = TextureLoader()
        private val colorMatcher = mutableMapOf<StateMatcher, Color>(
            StateMatcher(isLeaf = false, selected = false, hasCustomRenderArea = false) to "#5B0000".threeColor,
            StateMatcher(isLeaf = false, selected = true, hasCustomRenderArea = false) to "#FF0000".threeColor,
            StateMatcher(isLeaf = true, selected = false, hasCustomRenderArea = false) to "#005B00".threeColor,
            StateMatcher(isLeaf = true, selected = true, hasCustomRenderArea = false) to "#00FF00".threeColor,
            StateMatcher(isLeaf = false, selected = false, hasCustomRenderArea = true) to "#5B5B00".threeColor,
            StateMatcher(isLeaf = false, selected = true, hasCustomRenderArea = true) to "#FFFF00".threeColor,
            StateMatcher(isLeaf = true, selected = false, hasCustomRenderArea = true) to "#5B5B00".threeColor,
            StateMatcher(isLeaf = true, selected = true, hasCustomRenderArea = true) to "#FFFF00".threeColor
        )
        fun fromObject(item: Object3D?): ViewNode3D? {
            return item?.userData as? ViewNode3D
        }
    }
}

//region ext methods
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

private fun MeshBasicMaterial.withTexture(viewNode: ViewNode, side: Side, screenIndex: Int): MeshBasicMaterial {
    transparent = !viewNode.shouldRender
    if (viewNode.shouldRender) {
        //this expects to server handle the hammering
        map = ViewNode3D.textureLoader.load("view.png?position=${viewNode.position}&screenIndex=$screenIndex").apply {
            minFilter = threejs.LinearFilter
            magFilter = threejs.LinearFilter
            transparent = true
            opacity = 1
            premultiplyAlpha = true
            wrapS = threejs.RepeatWrapping;
            repeat.x = when (side) {
                BackSide -> -1
                else -> 1
            }
        }
    } else {
        opacity = 0
    }
    return this
}
//endregion ext methods

private data class StateMatcher(
    val isLeaf: Boolean,
    val selected: Boolean,
    val hasCustomRenderArea: Boolean
)