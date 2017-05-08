package org.jglr.voxlin.math

fun easeOutCircular(t: Double, start: Double, end: Double, max: Double): Double {
    var t_ = t / max
    t_--
    return (end-start) * Math.sqrt(1 - t_*t_) + start
}

fun easeInCircular(t: Double, start: Double, end: Double, max: Double): Double {
    val t_ = t / max
    return -(end-start) * (Math.sqrt(1 - t_*t_) -1) + start
}

fun easeInLinear(t: Double, start: Double, end: Double, max: Double): Double {
    return (end-start)*t/max + start
}