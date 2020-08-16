package org.ygl.openrndr.utils

import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.Shape
import org.openrndr.shape.shape


interface FieldValueProvider {
    fun getThreshold(x: Double, y: Double): Double
    operator fun get(x: Double, y: Double): Double
}

private data class Vertex(
        val pos: Vector2,
        val value: Double
)

/**
 * https://en.wikipedia.org/wiki/Marching_squares
 */
fun marchingSquaresThreshold(
        field: FieldValueProvider,
        rows: Int,
        cols: Int,
        width: Double = 100.0,
        height: Double = 100.0,
        rowStart: Double = 0.0,
        colStart: Double = 0.0
): List<Shape> {
    val shapes = mutableListOf<Shape>()
    val colStep = width / cols
    val rowStep = height / rows

    for (r in 0 until rows) {
        for (c in 0 until cols) {

            val left = colStart + colStep * c
            val right = left + colStep
            val top = rowStart + rowStep * r
            val bottom = top + rowStep
            val centerRow = (top + bottom) / 2
            val centerCol = (left + right) / 2
            val threshold = field.getThreshold(centerCol, centerRow)

            val tlv = Vertex(Vector2(left, top), field[left, top])
            val trv = Vertex(Vector2(right, top), field[right, top])
            val blv = Vertex(Vector2(left, bottom), field[left, bottom])
            val brv = Vertex(Vector2(right, bottom), field[right, bottom])

            fun interp(v1: Vertex, v2: Vertex): Vector2 {
                return mix(v2.pos, v1.pos, (threshold - v2.value)/(v1.value - v2.value))
            }

            var key = 0
            key = key or if (blv.value > threshold) 1 else 0
            key = key or if (brv.value > threshold) 2 else 0
            key = key or if (trv.value > threshold) 4 else 0
            key = key or if (tlv.value > threshold) 8 else 0

            when (key) {
                // 0
                //!tl && !tr && !bl && !br -> {}
                0 -> {}

                // 15
                //tl && tr && bl && br -> {
                15 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(tlv.pos)
                            lineTo(trv.pos)
                            lineTo(brv.pos)
                            lineTo(blv.pos)
                            close()
                        }
                    })
                }

                // 1
                //!tl && !tr && bl && !br -> {
                1 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(blv.pos)
                            lineTo(interp(blv, tlv))
                            lineTo(interp(blv, brv))
                            close()
                        }
                    })
                }

                // 2
                //!tl && !tr && !bl && br -> {
                2 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(brv.pos)
                            lineTo(interp(brv, blv))
                            lineTo(interp(brv, trv))
                            close()
                        }
                    })
                }

                // 4
                //!tl && tr && !bl && !br -> {
                4 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(trv.pos)
                            lineTo(interp(trv, brv))
                            lineTo(interp(trv, tlv))
                            close()
                        }
                    })
                }

                // 8
                //tl && !tr && !bl && !br -> {
                8 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(tlv.pos)
                            lineTo(interp(tlv, trv))
                            lineTo(interp(tlv, blv))
                            close()
                        }
                    })
                }

                // 3
                //!tl && !tr && bl && br -> {
                3 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(blv.pos)
                            lineTo(interp(blv, tlv))
                            lineTo(interp(brv, trv))
                            lineTo(brv.pos)
                            close()
                        }
                    })
                }

                // 6
                //!tl && tr && !bl && br -> {
                6 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(trv.pos)
                            lineTo(brv.pos)
                            lineTo(interp(brv, blv))
                            lineTo(interp(trv, tlv))
                            close()
                        }
                    })
                }

                // 9
                //tl && !tr && bl && !br -> {
                9 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(blv.pos)
                            lineTo(tlv.pos)
                            lineTo(interp(tlv, trv))
                            lineTo(interp(blv, brv))
                            close()
                        }
                    })
                }

                // 12
                //tl && tr && !bl && !br -> {
                12 -> {
                    shapes.add(shape {
                        contour {
                            moveTo(tlv.pos)
                            lineTo(trv.pos)
                            lineTo(interp(trv, brv))
                            lineTo(interp(tlv, blv))
                            close()
                        }
                    })
                }

                // 5
                // !tl && tr && bl && !br -> {
                5 -> {
                    val center = field[centerCol, centerRow]
                    if (center < threshold) {
                        shapes.add(shape {
                            contour {
                                moveTo(blv.pos)
                                lineTo(interp(blv, tlv))
                                lineTo(interp(blv, brv))
                                close()
                            }
                        })
                        shapes.add(shape {
                            contour {
                                moveTo(trv.pos)
                                lineTo(interp(trv, brv))
                                lineTo(interp(trv, tlv))
                                close()
                            }
                        })
                    } else {
                        shapes.add(shape { contour {
                            moveTo(interp(trv, tlv))
                            lineTo(trv.pos)
                            lineTo(interp(trv, brv))
                            lineTo(interp(blv, brv))
                            lineTo(blv.pos)
                            lineTo(interp(blv, tlv))
                            close()
                        }})
                    }
                }

                // 10
                //tl && !tr && !bl && br -> {
                10 -> {
                    val center = field[centerCol, centerRow]
                    if (center < threshold) {
                        shapes.add(shape {
                            contour {
                                moveTo(brv.pos)
                                lineTo(interp(brv, blv))
                                lineTo(interp(brv, trv))
                                close()
                            }
                        })
                        shapes.add(shape {
                            contour {
                                moveTo(tlv.pos)
                                lineTo(interp(tlv, trv))
                                lineTo(interp(tlv, blv))
                                close()
                            }
                        })
                    } else {
                        shapes.add(shape { contour {
                            moveTo(tlv.pos)
                            lineTo(interp(tlv, trv))
                            lineTo(interp(brv, trv))
                            lineTo(brv.pos)
                            lineTo(interp(brv, blv))
                            lineTo(interp(tlv, blv))
                            close()
                        }})
                    }
                }

                // 7
                //!tl && tr && bl && br -> {
                7 -> {
                    shapes.add(shape { contour {
                        moveTo(interp(trv, tlv))
                        lineTo(trv.pos)
                        lineTo(brv.pos)
                        lineTo(blv.pos)
                        lineTo(interp(blv, tlv))
                        close()
                    }})
                }

                // 11
                //tl && !tr && bl && br -> {
                11 -> {
                    shapes.add(shape { contour {
                        moveTo(tlv.pos)
                        lineTo(interp(tlv, trv))
                        lineTo(interp(brv, trv))
                        lineTo(brv.pos)
                        lineTo(blv.pos)
                        close()
                    }})
                }

                // 13
                //tl && tr && bl && !br -> {
                13 -> {
                    shapes.add(shape { contour {
                        moveTo(tlv.pos)
                        lineTo(trv.pos)
                        lineTo(interp(trv, brv))
                        lineTo(interp(blv, brv))
                        lineTo(blv.pos)
                        close()
                    }})
                }

                // 14
                //tl && tr && !bl && br -> {
                14 -> {
                    shapes.add(shape { contour {
                        moveTo(tlv.pos)
                        lineTo(trv.pos)
                        lineTo(brv.pos)
                        lineTo(interp(brv, blv))
                        lineTo(interp(tlv, blv))
                        close()
                    }})
                }
            }
        }
    }
    return shapes
}
