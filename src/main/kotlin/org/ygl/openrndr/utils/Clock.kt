package org.ygl.openrndr.utils



class IntervalClock(
        val intervals: List<Int>
) {
    val totalFrames = intervals.sum()

    fun getTime(frameCount: Int) = ((frameCount - 1) % totalFrames) / totalFrames.toDouble()

    fun getIntervalTime(frameCount: Int): Pair<Int, Double> {
        var frames = (frameCount - 1) % totalFrames
        for (i in intervals.indices) {
            val intervalFrames = intervals[i]
            val intervalSize = if (intervalFrames == 1) 1.0 else intervalFrames - 1.0
            if (frames < intervalFrames) {
                return i to (frames / intervalSize)
            }
            frames -= intervalFrames
        }
        throw IllegalStateException("should not have gotten here")
    }
}


fun main() {
    val ic = IntervalClock(listOf(2, 4, 3, 1))
    println(ic.intervals)
    for (i in 1 .. ic.totalFrames) {
        println("$i: " + ic.getIntervalTime(i))
    }
}