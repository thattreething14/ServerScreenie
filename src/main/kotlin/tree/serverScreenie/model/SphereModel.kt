package tree.serverScreenie.model

import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.util.Vector
import tree.serverScreenie.utils.Intersection
import tree.serverScreenie.utils.MathUtil
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SphereModel(
    texture: Array<IntArray>,
    transparencyFactor: Double,
    reflectionFactor: Double,
    occluding: Boolean,
    private val radius: Double,
    private val offset: Vector
) : AbstractModel(texture, transparencyFactor, reflectionFactor, occluding) {

    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        val linePoint = currentIntersection._point
        val lineDirection = currentIntersection._direction
        val blockPoint = block.location.toVector()
        val centerPoint = block.location.add(0.5, 0.5, 0.5).add(offset).toVector()

        val a = lineDirection.dot(lineDirection)
        val b = 2 * (linePoint.dot(lineDirection) - centerPoint.dot(lineDirection))
        val c = linePoint.dot(linePoint) - 2 * centerPoint.dot(linePoint) + centerPoint.dot(centerPoint)
        -radius.pow(2.0)

        val delta = b.pow(2.0) - 4 * a * c
        if (delta < 0) {
            return Intersection.of(currentIntersection._normal, linePoint, lineDirection)
        }

        val dist = linePoint.distance(centerPoint)
        val minDist = dist - radius
        val maxDist = dist + radius

        if (delta == 0.0) {
            val t = -b / (2 * a)
            val intersection = lineDirection.clone().add(lineDirection.clone().multiply(t))
            if (!isInsideBlock(blockPoint, intersection)) {
                return currentIntersection
            }
            val currentDist = intersection.distance(linePoint)
            val factor = (currentDist - minDist) / (maxDist - minDist)
            val normal = intersection.clone().subtract(centerPoint).normalize()
            return Intersection.of(normal, intersection, lineDirection, getColor(centerPoint, intersection, factor))
        }

        val deltaSqrt = sqrt(delta)

        val tOne = (-b + deltaSqrt) / (2 * a)
        val tTwo = (-b - deltaSqrt) / (2 * a)

        val intersectionOne = linePoint.clone().add(lineDirection.clone().multiply(tOne))
        val intersectionTwo = linePoint.clone().add(lineDirection.clone().multiply(tTwo))

        val first = intersectionOne.distanceSquared(linePoint) < intersectionTwo.distanceSquared(linePoint)
        val currentDist = if (first) intersectionOne else intersectionTwo
        val factor = (currentDist.distance(linePoint) - minDist) / (maxDist - minDist)

        return when {
            first && isInsideBlock(blockPoint, intersectionOne) -> {
                val normal = intersectionOne.clone().subtract(centerPoint).normalize()
                Intersection.of(normal, intersectionOne, lineDirection, getColor(centerPoint, intersectionOne, factor))
            }
            isInsideBlock(blockPoint, intersectionTwo) -> {
                val normal = intersectionTwo.clone().subtract(centerPoint).normalize()
                Intersection.of(normal, intersectionTwo, lineDirection, getColor(centerPoint, intersectionTwo, factor))
            }
            else -> currentIntersection
        }
    }

    private fun getColor(base: Vector, intersection: Vector, factor: Double): Int {
        val world = Bukkit.getServer().getWorld("world")
        val loc = base.toLocation(world!!)
        loc.direction = intersection.clone().subtract(base).normalize()

        val perimeter = (2 * Math.PI * radius).roundToInt().toDouble()
        val yawDiv = 360.0 / perimeter
        val pitchDiv = 180.0 / perimeter

        val pixelX = (loc.yaw % yawDiv / (yawDiv / textureSize)).toInt()
        val pixelY = ((loc.pitch + 90) % pitchDiv / (pitchDiv / textureSize)).toInt()

        return 0xFF000000.toInt() or MathUtil.weightedColorSum(texture[pixelY][pixelX], 0, 1 - factor, factor)
    }

    private fun isInsideBlock(blockPoint: Vector, intersection: Vector): Boolean {
        var intersection = intersection.clone().subtract(blockPoint)
        return intersection.x in 0.0..1.0 && intersection.y in 0.0..1.0 && intersection.z in 0.0..1.0
    }

    class SphereModelBuilder(texture: Array<IntArray>) : Builder(texture) {

        private var radius: Double = 0.5
        private var offset: Vector = Vector()

        fun radius(radius: Double): SphereModelBuilder {
            this.radius = radius
            return this
        }

        fun offset(offset: Vector): SphereModelBuilder {
            this.offset = offset.clone()
            return this
        }

        override fun build(): SphereModel {
            return SphereModel(texture, transparencyFactor, reflectionFactor, occluding, radius, offset)
        }
    }
}
