package com.scurab.uitor.web.threed

import THREE.Euler
import THREE.Vector2
import THREE.Vector3
import com.scurab.uitor.common.render.r2

val String.threeColor get() = THREE.Color(this)
val Vector3.d get() = "[${x.r2}, ${y.r2}, ${z.r2}]"
val Vector2.d get() = "[${x.r2}, ${y.r2}]"
val Euler.d get() = "[${x.r2}, ${y.r2}, ${z.r2}]"
