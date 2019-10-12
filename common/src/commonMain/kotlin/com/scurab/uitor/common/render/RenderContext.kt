package com.scurab.uitor.common.render

interface IFillRenderContext {
    val fillColor: Color
}

interface IStrokeRenderContext {
    val strokeColor: Color
    val strokeWidth: Double
    val strokeDash: Array<Double>? get() = null
}

interface IRectangleRenderContext : IFillRenderContext, IStrokeRenderContext

class FillRenderContext(override val fillColor: Color) : IFillRenderContext

class StrokeRenderContext(
    override val strokeColor: Color,
    override val strokeWidth: Double = 1.0,
    override val strokeDash: Array<Double>? = null
) : IStrokeRenderContext

class RectangleRenderContext(
    override val strokeColor: Color,
    override val fillColor: Color,
    override val strokeWidth: Double = 1.0
) : IRectangleRenderContext
