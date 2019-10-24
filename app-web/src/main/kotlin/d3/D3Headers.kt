@file:JsQualifier("d3")

package d3

import org.w3c.dom.svg.SVGElement

external fun create(type: String): Selection

open external class Selection {
    /*
    const svg = d3.create("svg")
        .attr("viewBox", [0, 0, width, x1 - x0 + root.dx * 2]);

        const g = svg.append("g")
            .attr("font-family", "sans-serif")
            .attr("font-size", 10)
        .attr("transform", `translate(${root.dy / 3},${root.dx - x0})`);
     */
    fun attr(name: String, value: dynamic): Selection

    fun append(name: String): Selection

    fun node(): SVGElement

    fun join(name: String): Selection

    fun selectAll(name: String): Selection

    fun data(arg: Array<Node<*>>): UpdateSelection

    fun text(text: (Node<*>) -> String): Selection

    fun clone(deep: Boolean): Selection

    fun lower(): Selection

    fun <T> on(name: String, block: (T) -> Unit) : Selection
}

external class UpdateSelection : Selection {

}

external fun <T> hierarchy(data: T, children: (T) -> Array<T>): Node<*>

external class Node<T>(data: T) {
    var data: T
    var depth: Int
    var height: Int
    var parent: Node<T>?
    var dx: Double
    var dy: Double
    var x: Double
    var y: Double
    var children: Array<Node<T>>?

    fun descendants(): Array<Node<*>>
    fun each(block: (Node<T>) -> Unit)
    fun links(): Array<Node<*>>
    /*
    function Node(data) {
  this.data = data;
  this.depth =
  this.height = 0;
  this.parent = null;
}
     */
}

external fun tree() : Tree

external interface Tree {
    fun nodeSize(size: DoubleArray): (Node<*>) -> Node<*>
}

external fun linkHorizontal() : Link

external interface Link {
    fun x(block: (Node<*>) -> Double): Link
    fun y(block: (Node<*>) -> Double): Link
}

external interface ITreeItem {
    val children: Array<out ITreeItem>?
}