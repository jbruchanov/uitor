package d3

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.Rect

fun svg() = create("svg")
fun group() = create("g")

fun Selection.group(): Selection = append("g")
fun Selection.viewBox(rect: Rect): Selection = attr("viewBox", rect.toIntArray())
fun Selection.fontFamily(fontFamily: String): Selection = attr("font-family", fontFamily)
fun Selection.fontSize(fontSize: Double): Selection = attr("font-size", fontSize)
fun Selection.transform(transform: String): Selection = attr("transform", transform)
fun Selection.transform(block: (Node<*>) -> String): Selection = attr("transform", block)
fun Selection.fill(fill: String): Selection = attr("fill", fill)
fun Selection.fill(fill: Color): Selection = attr("fill", fill.htmlRGBA)
fun Selection.fill(fill: (Node<*>) -> String): Selection = attr("fill", fill)
fun Selection.stroke(stroke: Color): Selection = attr("stroke", stroke.htmlRGBA)
fun Selection.strokeOpacity(strokeOpacity: Float): Selection = attr("stroke-opacity", strokeOpacity)
fun Selection.strokeWidth(strokeWidth: Double): Selection = attr("stroke-width", strokeWidth)
fun Selection.strokeLineJoin(strokeLineJoin: String): Selection = attr("strokeLineJoin", strokeLineJoin)
fun Selection.radius(radius: Double): Selection = attr("r", radius)
fun Selection.dx(dx: String): Selection = attr("dx", dx)
fun Selection.dy(dy: String): Selection = attr("dy", dy)
fun Selection.dy(block: (Node<*>) -> String): Selection = attr("dy", block)
fun Selection.x(block: (Node<*>) -> Double): Selection = attr("x", block)
fun Selection.y(block: (Node<*>) -> Double): Selection = attr("y", block)
fun Selection.textAnchor(block: (Node<*>) -> String): Selection = attr("text-anchor", block)
fun Selection.style(style: String): Selection = attr("style", style)
fun Selection.width(width: Double): Selection = attr("width", "${width}px")
fun Selection.height(height: Double): Selection = attr("height", "${height}px")
fun Selection.onClick(block: (Node<*>) -> Unit): Selection = on("click", block)
fun Selection.classes(classes: Set<String>): Selection = attr("class", (classes.joinToString(" ")))
fun Selection.classes(classes: String): Selection = attr("class", classes)


fun translate(x: Double, y: Double): String = "translate($x, $y)"

/**
const link = g.append("g")
.attr("fill", "none")
.attr("stroke", "#555")
.attr("stroke-opacity", 0.4)
.attr("stroke-width", 1.5)
.selectAll("path")
.data(root.links())
.join("path")
.attr("d", d3.linkHorizontal()
.x(d => d.y)
.y(d => d.x));
 */