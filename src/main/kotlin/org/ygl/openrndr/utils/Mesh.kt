package org.ygl.openrndr.utils

import org.openrndr.draw.BufferWriter
import org.openrndr.draw.VertexBuffer
import org.openrndr.extras.meshgenerators.GeneratorBuffer
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder

private typealias VertexWriter = (position: Vector3, normal: Vector3, texCoord: Vector2) -> Unit

private fun vertexWriter(bufferWriter: BufferWriter): VertexWriter {
    return { p, n, t ->
        bufferWriter.write(p)
        bufferWriter.write(n)
        bufferWriter.write(t)
    }
}

fun generateSurfaceMesh(
        vertices: List<List<Vector3>>,
        buffer: VertexBuffer
) {

    buffer.put {
        val writer = vertexWriter(this)

        val step = Vector2(1.0 / vertices[0].size, 1.0 / vertices.size)

        for (r in 0 until vertices.size - 1) {
            val widthSegments = vertices[0].size
            for (c in 0 until widthSegments - 1) {

                val c00 = vertices[r][c]
                val c01 = vertices[r + 1][c]
                val c10 = vertices[r][c + 1]
                val c11 = vertices[r + 1][c + 1]

                val uv00 = Vector2(c + 0.0, r + 0.0) * step
                val uv01 = Vector2(c + 0.0, r + 1.0) * step
                val uv10 = Vector2(c + 1.0, r + 0.0) * step
                val uv11 = Vector2(c + 1.0, r + 1.0) * step

                val normal1 = (c11 - c00).cross(c10 - c00).normalized
                writer(c11, normal1, uv11)
                writer(c10, normal1, uv10)
                writer(c00, normal1, uv00)

                val normal2 = (c00 - c11).cross(c01 - c11).normalized
                writer(c00, normal2, uv00)
                writer(c01, normal2, uv01)
                writer(c11, normal2, uv11)
            }
        }
    }
}

fun generateSurfaceMesh(
        vertices: List<List<Vector3>>,
        buffer: VertexBuffer,
        textureScale: Vector2,
        textureOffset: Vector2 = Vector2.ZERO
) {

    buffer.put {
        val writer = vertexWriter(this)

        val stepX = 1.0 / textureScale.x
        val stepY = 1.0 / textureScale.y

        for (r in 0 until vertices.size - 1) {
            val widthSegments = vertices[0].size

            val r0 = (textureOffset.y + (r * stepY)) % 1.0
            val r1 = (r0 + stepY)

            for (c in 0 until widthSegments - 1) {

                val c00 = vertices[r][c]
                val c01 = vertices[r + 1][c]
                val c10 = vertices[r][c + 1]
                val c11 = vertices[r + 1][c + 1]

                val c0 = (textureOffset.x + (c * stepX)) % 1.0
                val c1 = (c0 + stepX)

                val uv00 = Vector2(c0, r0)
                val uv01 = Vector2(c0, r1)
                val uv10 = Vector2(c1, r0)
                val uv11 = Vector2(c1, r1)

                val normal1 = (c11 - c00).cross(c10 - c00).normalized
                writer(c11, normal1, uv11)
                writer(c10, normal1, uv10)
                writer(c00, normal1, uv00)

                val normal2 = (c00 - c11).cross(c01 - c11).normalized
                writer(c00, normal2, uv00)
                writer(c01, normal2, uv01)
                writer(c11, normal2, uv11)
            }
        }
    }
}

fun GeneratorBuffer.fillByteBuffer(bb: ByteBuffer) {
    bb.order(ByteOrder.nativeOrder())
    bb.rewind()
    for (d in data) {
        bb.putFloat(d.position.x.toFloat())
        bb.putFloat(d.position.y.toFloat())
        bb.putFloat(d.position.z.toFloat())

        bb.putFloat(d.normal.x.toFloat())
        bb.putFloat(d.normal.y.toFloat())
        bb.putFloat(d.normal.z.toFloat())

        bb.putFloat(d.texCoord.x.toFloat())
        bb.putFloat(d.texCoord.y.toFloat())
    }
}

fun VertexBuffer.loadFrom(generatorBuffer: GeneratorBuffer) {
    val byteBuffer = generatorBuffer.toByteBuffer()
    loadFrom(byteBuffer)
}

fun VertexBuffer.loadFrom(byteBuffer: ByteBuffer) {
    byteBuffer.rewind()
    this.write(byteBuffer)
}
