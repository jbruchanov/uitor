@file:JsQualifier("THREE")
package threejs

external var REVISION: String

external enum class MOUSE {
    LEFT,
    MIDDLE,
    RIGHT,
    ROTATE,
    DOLLY,
    PAN
}

external enum class TOUCH {
    ROTATE,
    PAN,
    DOLLY_PAN,
    DOLLY_ROTATE
}

external enum class CullFace {

}

external var CullFaceNone: CullFace

external var CullFaceBack: CullFace

external var CullFaceFront: CullFace

external var CullFaceFrontBack: CullFace

external enum class FrontFaceDirection {

}

external var FrontFaceDirectionCW: FrontFaceDirection

external var FrontFaceDirectionCCW: FrontFaceDirection

external enum class ShadowMapType {

}

external var BasicShadowMap: ShadowMapType

external var PCFShadowMap: ShadowMapType

external var PCFSoftShadowMap: ShadowMapType

external var VSMShadowMap: ShadowMapType

external enum class Side {

}

external var FrontSide: Side

external var BackSide: Side

external var DoubleSide: Side

external enum class Shading {

}

external var FlatShading: Shading

external var SmoothShading: Shading

external enum class Colors {

}

external var NoColors: Colors

external var FaceColors: Colors

external var VertexColors: Colors

external enum class Blending {

}

external var NoBlending: Blending

external var NormalBlending: Blending

external var AdditiveBlending: Blending

external var SubtractiveBlending: Blending

external var MultiplyBlending: Blending

external var CustomBlending: Blending

external enum class BlendingEquation {

}

external var AddEquation: BlendingEquation

external var SubtractEquation: BlendingEquation

external var ReverseSubtractEquation: BlendingEquation

external var MinEquation: BlendingEquation

external var MaxEquation: BlendingEquation

external enum class BlendingDstFactor {

}

external var ZeroFactor: BlendingDstFactor

external var OneFactor: BlendingDstFactor

external var SrcColorFactor: BlendingDstFactor

external var OneMinusSrcColorFactor: BlendingDstFactor

external var SrcAlphaFactor: BlendingDstFactor

external var OneMinusSrcAlphaFactor: BlendingDstFactor

external var DstAlphaFactor: BlendingDstFactor

external var OneMinusDstAlphaFactor: BlendingDstFactor

external var DstColorFactor: BlendingDstFactor

external var OneMinusDstColorFactor: BlendingDstFactor

external enum class BlendingSrcFactor {

}

external var SrcAlphaSaturateFactor: BlendingSrcFactor

external enum class DepthModes {

}

external var NeverDepth: DepthModes

external var AlwaysDepth: DepthModes

external var LessDepth: DepthModes

external var LessEqualDepth: DepthModes

external var EqualDepth: DepthModes

external var GreaterEqualDepth: DepthModes

external var GreaterDepth: DepthModes

external var NotEqualDepth: DepthModes

external enum class Combine {

}

external var MultiplyOperation: Combine

external var MixOperation: Combine

external var AddOperation: Combine

external enum class ToneMapping {

}

external var NoToneMapping: ToneMapping

external var LinearToneMapping: ToneMapping

external var ReinhardToneMapping: ToneMapping

external var Uncharted2ToneMapping: ToneMapping

external var CineonToneMapping: ToneMapping

external enum class Mapping {

}

external var UVMapping: Mapping

external var CubeReflectionMapping: Mapping

external var CubeRefractionMapping: Mapping

external var EquirectangularReflectionMapping: Mapping

external var EquirectangularRefractionMapping: Mapping

external var SphericalReflectionMapping: Mapping

external var CubeUVReflectionMapping: Mapping

external var CubeUVRefractionMapping: Mapping

external enum class Wrapping {

}

external var RepeatWrapping: Wrapping

external var ClampToEdgeWrapping: Wrapping

external var MirroredRepeatWrapping: Wrapping

external enum class TextureFilter {

}

external var NearestFilter: TextureFilter

external var NearestMipmapNearestFilter: TextureFilter

external var NearestMipMapNearestFilter: TextureFilter

external var NearestMipmapLinearFilter: TextureFilter

external var NearestMipMapLinearFilter: TextureFilter

external var LinearFilter: TextureFilter

external var LinearMipmapNearestFilter: TextureFilter

external var LinearMipMapNearestFilter: TextureFilter

external var LinearMipmapLinearFilter: TextureFilter

external var LinearMipMapLinearFilter: TextureFilter

external enum class TextureDataType {

}

external var UnsignedByteType: TextureDataType

external var ByteType: TextureDataType

external var ShortType: TextureDataType

external var UnsignedShortType: TextureDataType

external var IntType: TextureDataType

external var UnsignedIntType: TextureDataType

external var FloatType: TextureDataType

external var HalfFloatType: TextureDataType

external enum class PixelType {

}

external var UnsignedShort4444Type: PixelType

external var UnsignedShort5551Type: PixelType

external var UnsignedShort565Type: PixelType

external var UnsignedInt248Type: PixelType

external enum class PixelFormat {

}

external var AlphaFormat: PixelFormat

external var RGBFormat: PixelFormat

external var RGBAFormat: PixelFormat

external var LuminanceFormat: PixelFormat

external var LuminanceAlphaFormat: PixelFormat

external var RGBEFormat: PixelFormat

external var DepthFormat: PixelFormat

external var DepthStencilFormat: PixelFormat

external var RedFormat: PixelFormat

external enum class CompressedPixelFormat {

}

external var RGB_S3TC_DXT1_Format: CompressedPixelFormat

external var RGBA_S3TC_DXT1_Format: CompressedPixelFormat

external var RGBA_S3TC_DXT3_Format: CompressedPixelFormat

external var RGBA_S3TC_DXT5_Format: CompressedPixelFormat

external var RGB_PVRTC_4BPPV1_Format: CompressedPixelFormat

external var RGB_PVRTC_2BPPV1_Format: CompressedPixelFormat

external var RGBA_PVRTC_4BPPV1_Format: CompressedPixelFormat

external var RGBA_PVRTC_2BPPV1_Format: CompressedPixelFormat

external var RGB_ETC1_Format: CompressedPixelFormat

external var RGBA_ASTC_4x4_Format: CompressedPixelFormat

external var RGBA_ASTC_5x4_Format: CompressedPixelFormat

external var RGBA_ASTC_5x5_Format: CompressedPixelFormat

external var RGBA_ASTC_6x5_Format: CompressedPixelFormat

external var RGBA_ASTC_6x6_Format: CompressedPixelFormat

external var RGBA_ASTC_8x5_Format: CompressedPixelFormat

external var RGBA_ASTC_8x6_Format: CompressedPixelFormat

external var RGBA_ASTC_8x8_Format: CompressedPixelFormat

external var RGBA_ASTC_10x5_Format: CompressedPixelFormat

external var RGBA_ASTC_10x6_Format: CompressedPixelFormat

external var RGBA_ASTC_10x8_Format: CompressedPixelFormat

external var RGBA_ASTC_10x10_Format: CompressedPixelFormat

external var RGBA_ASTC_12x10_Format: CompressedPixelFormat

external var RGBA_ASTC_12x12_Format: CompressedPixelFormat

external enum class AnimationActionLoopStyles {

}

external var LoopOnce: AnimationActionLoopStyles

external var LoopRepeat: AnimationActionLoopStyles

external var LoopPingPong: AnimationActionLoopStyles

external enum class InterpolationModes {

}

external var InterpolateDiscrete: InterpolationModes

external var InterpolateLinear: InterpolationModes

external var InterpolateSmooth: InterpolationModes

external enum class InterpolationEndingModes {

}

external var ZeroCurvatureEnding: InterpolationEndingModes

external var ZeroSlopeEnding: InterpolationEndingModes

external var WrapAroundEnding: InterpolationEndingModes

external enum class TrianglesDrawModes {

}

external var TrianglesDrawMode: TrianglesDrawModes

external var TriangleStripDrawMode: TrianglesDrawModes

external var TriangleFanDrawMode: TrianglesDrawModes

external enum class TextureEncoding {

}

external var LinearEncoding: TextureEncoding

external var sRGBEncoding: TextureEncoding

external var GammaEncoding: TextureEncoding

external var RGBEEncoding: TextureEncoding

external var LogLuvEncoding: TextureEncoding

external var RGBM7Encoding: TextureEncoding

external var RGBM16Encoding: TextureEncoding

external var RGBDEncoding: TextureEncoding

external enum class DepthPackingStrategies {

}

external var BasicDepthPacking: DepthPackingStrategies

external var RGBADepthPacking: DepthPackingStrategies

external enum class NormalMapTypes {

}

external var TangentSpaceNormalMap: NormalMapTypes

external var ObjectSpaceNormalMap: NormalMapTypes

external enum class StencilOp {

}

external var ZeroStencilOp: StencilOp

external var KeepStencilOp: StencilOp

external var ReplaceStencilOp: StencilOp

external var IncrementStencilOp: StencilOp

external var DecrementStencilOp: StencilOp

external var IncrementWrapStencilOp: StencilOp

external var DecrementWrapStencilOp: StencilOp

external var InvertStencilOp: StencilOp

external enum class StencilFunc {

}

external var NeverStencilFunc: StencilFunc

external var LessStencilFunc: StencilFunc

external var EqualStencilFunc: StencilFunc

external var LessEqualStencilFunc: StencilFunc

external var GreaterStencilFunc: StencilFunc

external var NotEqualStencilFunc: StencilFunc

external var GreaterEqualStencilFunc: StencilFunc

external var AlwaysStencilFunc: StencilFunc