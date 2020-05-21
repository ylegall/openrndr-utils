package org.ygl.openrndr.utils

import org.openrndr.draw.BufferMultisample
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.RenderTargetBuilder
import org.openrndr.draw.Session
import org.openrndr.draw.renderTarget


class PingPongBuffer(
        val renderTarget1: RenderTarget,
        val renderTarget2: RenderTarget
) {
    private var prev = renderTarget1
    private var curr = renderTarget2

    fun swap() {
        if (prev === renderTarget1) {
            prev = renderTarget2
            curr = renderTarget1
        } else {
            prev = renderTarget1
            curr = renderTarget2
        }
    }

    fun current() = curr

    fun previous() = prev
}

fun pingPongBuffer(width: Int, height: Int,
                   contentScale: Double = 1.0,
                   multisample: BufferMultisample = BufferMultisample.Disabled,
                   session: Session? = Session.active,
                   builder: RenderTargetBuilder.() -> Unit
): PingPongBuffer {
    return PingPongBuffer(
            renderTarget(width, height, contentScale, multisample, session, builder),
            renderTarget(width, height, contentScale, multisample, session, builder)
    )
}

