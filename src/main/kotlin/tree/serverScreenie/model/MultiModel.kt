package tree.serverScreenie.model

import org.bukkit.block.Block
import org.bukkit.util.Vector
import tree.serverScreenie.utils.Intersection
import kotlin.math.floor

class MultiModel(
    private val topTexture: Array<IntArray>,
    texture: Array<IntArray>,
    private val bottomTexture: Array<IntArray>,
    transparencyFactor: Double,
    reflectionFactor: Double,
    occluding: Boolean
) : SimpleModel(texture, transparencyFactor, reflectionFactor, occluding) {

    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        if (currentIntersection._normal != UP && currentIntersection._normal != DOWN) {
            return super.intersect(block, currentIntersection)
        }

        val normal = currentIntersection._normal
        val point = currentIntersection._point
        val direction = currentIntersection._direction

        val yOffset = point.x - point.x.toInt()
        val xOffset = point.z - point.z.toInt()

        val pixelY = floor((if (yOffset < 0) yOffset + 1 else yOffset) * textureSize).toInt()
        val pixelX = floor((if (xOffset < 0) xOffset + 1 else xOffset) * textureSize).toInt()

        return if (normal == UP) {
            Intersection.of(normal, point, direction, topTexture[pixelY][pixelX])
        } else {
            Intersection.of(normal, point, direction, bottomTexture[pixelY][pixelX])
        }
    }

    companion object {
        val UP = Vector(0.0, 1.0, 0.0)
        val DOWN = Vector(0.0, -1.0, 0.0)
    }

    class MultiModelBuilder(
        private val topTexture: Array<IntArray>,
        texture: Array<IntArray>,
        private val bottomTexture: Array<IntArray>
    ) : SimpleModelBuilder(texture) {

        override fun build(): MultiModel {
            return MultiModel(topTexture, texture, bottomTexture, transparencyFactor, reflectionFactor, occluding)
        }
    }
}
