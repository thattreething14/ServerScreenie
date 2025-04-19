package tree.serverScreenie.model

import org.bukkit.block.Block
import tree.serverScreenie.utils.Intersection
import kotlin.math.floor

class CarpetModel(
    texture: Array<IntArray>,
    transparencyFactor: Double,
    reflectionFactor: Double,
    isOccluding: Boolean
) : SimpleModel(texture, transparencyFactor, reflectionFactor, isOccluding) {

    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        val normal = currentIntersection._normal
        val point = currentIntersection._point
        val direction = currentIntersection._direction

        var yOffset = point.y - point.y.toInt()
        var xOffset = point.x - point.x.toInt()

        val pixelY: Int
        val pixelX: Int

        when (normal) {
            UP -> {
                pixelY = floor((if (yOffset < 0) yOffset + 1 else yOffset) * textureSize).toInt()
                pixelX = floor((if (xOffset < 0) xOffset + 1 else xOffset) * textureSize).toInt()
            }
            DOWN -> {
                pixelY = floor((if (yOffset < 0) yOffset + 1 else yOffset) * textureSize).toInt()
                pixelX = floor((if (xOffset < 0) xOffset + 1 else xOffset) * textureSize).toInt()
            }
            else -> {
                pixelY = floor((if (yOffset < 0) yOffset + 1 else yOffset) * textureSize).toInt()
                pixelX = floor((if (xOffset < 0) xOffset + 1 else xOffset) * textureSize).toInt()

                if (pixelY == 0 || pixelY == textureSize - 1 || pixelX == 0 || pixelX == textureSize - 1) {

                    return Intersection.of(normal, point, direction, texture[0][0])
                }
            }
        }

        return Intersection.of(normal, point, direction, texture[pixelY][pixelX])
    }

    class CarpetModelBuilder(texture: Array<IntArray>) : SimpleModelBuilder(texture) {

        override fun build(): Model {
            return CarpetModel(texture, transparencyFactor, reflectionFactor, occluding)
        }
    }
}
