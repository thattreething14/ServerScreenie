package tree.serverScreenie.model

import org.bukkit.block.Block
import org.bukkit.util.Vector
import tree.serverScreenie.utils.Intersection
import tree.serverScreenie.utils.MathUtil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

class CrossModel(
    texture: Array<IntArray>,
    transparencyFactor: Double,
    reflectionFactor: Double,
    occluding: Boolean
) : AbstractModel(texture, transparencyFactor, reflectionFactor, occluding) {

    companion object {
        private val NORMAL_ONE = Vector(1.0, 0.0, 1.0).normalize()
        private val NORMAL_TWO = Vector(-1.0, 0.0, 1.0).normalize()

        private val POINT_ONE = Vector(1.0, 0.0, 0.0)
        private val POINT_TWO = Vector(1.0, 0.0, 1.0)
    }

    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        val linePoint = currentIntersection._point
        val lineDirection = currentIntersection._direction

        val blockPoint = block.location.toVector()
        val planePoint = block.location.add(0.5, 0.0, 0.5).toVector()

        var distance = Double.POSITIVE_INFINITY
        var color = 0
        var target: Vector? = null

        var intersectionOne = MathUtil.getLinePlaneIntersection(linePoint, lineDirection, planePoint, NORMAL_ONE, true)
        intersectionOne.let {
            it?.subtract(blockPoint)
            if (isInsideBlock(it!!)) {
                color = getColor(it, POINT_ONE)
                distance = linePoint.distanceSquared(it.add(blockPoint))
                target = it
            }
        }

        var intersectionTwo = MathUtil.getLinePlaneIntersection(linePoint, lineDirection, planePoint, NORMAL_TWO, true)
        intersectionTwo.let {
            it?.subtract(blockPoint)
            if (isInsideBlock(it!!)) {
                val colorTwo = getColor(it, POINT_TWO)
                val distanceTwo = linePoint.distanceSquared(it.add(blockPoint))
                if ((distanceTwo < distance && (colorTwo shr 24) != 0) || (color shr 24) == 0) {
                    target = it
                    color = colorTwo
                }
            }
        }

        if (target == null) {
            target = linePoint
        }

        return Intersection.of(currentIntersection._normal, target, lineDirection, color)
    }

    private fun isInsideBlock(vec: Vector): Boolean {
        return vec.x >= 0 && vec.z < 1 && vec.y >= 0 && vec.y < 1 && vec.z >= 0 && vec.z < 1
    }

    private fun getColor(vec: Vector, base: Vector): Int {
        val xOffset = sqrt((vec.x - base.x).pow(2.0) + (vec.z - base.z).pow(2.0))
        val yOffset = vec.y

        val pixelY = floor(yOffset * textureSize).toInt()
        val pixelX = floor(xOffset / sqrt(2.0) * textureSize).toInt()

        return texture[pixelY][pixelX]
    }

    class CrossModelBuilder(texture: Array<IntArray>) : Builder(texture) {
        override fun build(): CrossModel {
            return CrossModel(texture, transparencyFactor, reflectionFactor, occluding)
        }
    }
}
