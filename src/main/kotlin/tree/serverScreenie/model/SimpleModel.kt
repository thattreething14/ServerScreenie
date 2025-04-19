package tree.serverScreenie.model

import org.bukkit.block.Block
import org.bukkit.util.Vector
import tree.serverScreenie.utils.Intersection
import kotlin.math.floor

open class SimpleModel(
    texture: Array<IntArray>,
    override val transparencyFactor: Double,
    override val reflectionFactor: Double,
    override val isOccluding: Boolean
) : AbstractModel(texture, transparencyFactor, reflectionFactor, isOccluding) {

    companion object {
        val UP = Vector(0.0, 1.0, 0.0)
        val DOWN = Vector(0.0, -1.0, 0.0)
        val NORTH = Vector(0.0, 0.0, -1.0)
        val SOUTH = Vector(0.0, 0.0, 1.0)
        val EAST = Vector(1.0, 0.0, 0.0)
        val WEST = Vector(-1.0, 0.0, 0.0)
    }

    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        var yOffset: Double
        var xOffset: Double

        val normal = currentIntersection._normal
        val point = currentIntersection._point
        val direction = currentIntersection._direction

        when (normal) {
            NORTH, SOUTH -> {
                yOffset = point.y - point.y.toInt()
                xOffset = point.x - point.x.toInt()
            }
            EAST, WEST -> {
                yOffset = point.y - point.y.toInt()
                xOffset = point.z - point.z.toInt()
            }
            else -> {
                yOffset = point.x - point.x.toInt()
                xOffset = point.z - point.z.toInt()
            }
        }

        val pixelY = floor((if (yOffset < 0) yOffset + 1 else yOffset) * textureSize).toInt()
        val pixelX = floor((if (xOffset < 0) xOffset + 1 else xOffset) * textureSize).toInt()

        return Intersection.of(normal, point, direction, texture[pixelY][pixelX])
    }

    open class SimpleModelBuilder(texture: Array<IntArray>) : Builder(texture) {

        override fun build(): Model {
            return SimpleModel(texture, transparencyFactor, reflectionFactor, occluding)
        }
    }
}
