package com.scurab.uitor.web.threed

import com.scurab.uitor.common.model.IViewNode
import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.util.ise
import com.scurab.uitor.web.App
import com.scurab.uitor.web.model.IClientConfig
import com.scurab.uitor.web.model.ViewNode
import com.scurab.uitor.web.util.obj
import js.threejs.BackSide
import js.threejs.BoxGeometry
import js.threejs.EdgesGeometry
import js.threejs.FrontSide
import js.threejs.LineSegments
import js.threejs.Mesh
import js.threejs.MeshBasicMaterial
import js.threejs.Object3D
import js.threejs.Scene
import js.threejs.Side
import js.threejs.TextureLoader
import js.threejs.Color as JsColor

private const val DEPTH = 1
private const val HALF = 0.5
private const val COLOR_TEXTURE_DEFAULT = "#FFFFFF"
private const val COLOR_TEXTURE_SELECTED = "#808080"

/**
 * Help class to compose ViewNode related threejs objects
 */
class ViewNode3D(val context: ViewNode3DContext) : IViewNode by context.viewNode {
    val viewNode = context.viewNode

    override val rect = viewNode.renderAreaRelative?.let { viewNode.rect.addRelative(it) } ?: viewNode.rect
    private val hasCustomRenderArea = rect != viewNode.rect
    private val geometry = BoxGeometry(rect.width, rect.height, DEPTH)
    private val lineMaterial = js.threejs.LineBasicMaterial(obj {
        color = viewNode.edgeColor(false, context.clientConfig)
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
        viewTexture(FrontSide, context.textureLoader),//front
        viewTexture(BackSide, context.textureLoader)//back
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
        lineMaterial.color = viewNode.edgeColor(selected, context.clientConfig)
        textures.forEach {
            if (textureSideMaterial != it) {
                it?.color = (if (selected) COLOR_TEXTURE_SELECTED else COLOR_TEXTURE_DEFAULT).threeColor
            }
        }
    }

    private fun viewTexture(side: Side, textureLoader: TextureLoader): MeshBasicMaterial {
        return MeshBasicMaterial(obj {
            transparent = true
            opacity = 1
            color = COLOR_TEXTURE_DEFAULT
            depthWrite = false
            this.side = FrontSide
        }).withTexture(viewNode, side, context.screenIndex, textureLoader)
    }

    private fun ViewNode.edgeColor(selected: Boolean, clientConfig: IClientConfig): JsColor {
        return (clientConfig.typeHighlights[type] ?: colorMatcher[StateMatcher(isLeaf, hasCustomRenderArea)])
            ?.let { if (!selected) it.halfLightness() else it }?.threeColor
            ?: ise(
                "Invalid state for color, isLeaf:$isLeaf, selected:$selected, hasCustomRenderArea:$hasCustomRenderArea"
            )
    }

    companion object {
        //internal val textureLoader = TextureLoader()
        private val colorMatcher = mutableMapOf(
            StateMatcher(isLeaf = false, hasCustomRenderArea = false) to Color.Red,
            StateMatcher(isLeaf = true, hasCustomRenderArea = false) to Color.Green,
            StateMatcher(isLeaf = false, hasCustomRenderArea = true) to Color.Yellow,
            StateMatcher(isLeaf = true, hasCustomRenderArea = true) to Color.Yellow
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

private fun MeshBasicMaterial.withTexture(viewNode: ViewNode, side: Side, screenIndex: Int, textureLoader: TextureLoader): MeshBasicMaterial {
    transparent = !viewNode.shouldRender
    if (viewNode.shouldRender) {
        //this expects to server handle the hammering
        //TODO: remove direct reference to App.serverApi
        val uri = App.serverApi.viewShotUrl(screenIndex, viewNode.position)
        map = textureLoader.load(uri).apply {
            minFilter = js.threejs.LinearFilter
            magFilter = js.threejs.LinearFilter
            transparent = true
            opacity = 1
            premultiplyAlpha = true
            wrapS = js.threejs.RepeatWrapping
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
    val hasCustomRenderArea: Boolean
)

class ViewNode3DContext(
    val viewNode: ViewNode,
    val screenIndex: Int,
    val textureLoader: TextureLoader,
    val clientConfig: IClientConfig
)