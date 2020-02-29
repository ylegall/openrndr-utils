package org.ygl.openrndr.utils

import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget

/**
 * Pushes style, view- and projection matrix, calls function and pops.
 * @param block the function that is called in the isolation
 */
inline fun Drawer.isolated(block: Drawer.() -> Unit) {
    pushTransforms()
    pushStyle()
    block()
    popStyle()
    popTransforms()
}

/**
 * Pushes style, view- and projection matrix, sets render target, calls function and pops,
 * @param block the function that is called in the isolation
 */
inline fun Drawer.isolatedWithTarget(target: RenderTarget, block: Drawer.() -> Unit) {
    target.bind()
    isolated(block)
    target.unbind()
}
