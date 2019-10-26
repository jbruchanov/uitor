package com.scurab.uitor.web.threed

import com.scurab.uitor.common.render.Color
import com.scurab.uitor.common.render.r2
import threejs.Euler
import threejs.Vector2
import threejs.Vector3

val String.threeColor get() = threejs.Color(this)
val Color.threeColor get() = threejs.Color(this.value)
val Vector3.d get() = "[${x.r2}, ${y.r2}, ${z.r2}]"
val Vector2.d get() = "[${x.r2}, ${y.r2}]"
val Euler.d get() = "[${x.r2}, ${y.r2}, ${z.r2}]"
