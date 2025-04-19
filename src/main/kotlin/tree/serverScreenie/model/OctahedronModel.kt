package tree.serverScreenie.model

import org.bukkit.block.Block
import org.bukkit.util.Vector
import tree.serverScreenie.utils.Intersection
import tree.serverScreenie.utils.MathUtil
import kotlin.math.floor

class OctahedronModel(
    texture: Array<IntArray>,
    transparencyFactor: Double,
    reflectionFactor: Double,
    occluding: Boolean
) : AbstractModel(texture, transparencyFactor, reflectionFactor, occluding) {

    companion object {
        private const val RADIUS = 0.5

        private val NORMALS = arrayOf(
            Vector(-1.0, -1.0, -1.0), Vector(-1.0, -1.0, 1.0),
            Vector(-1.0, 1.0, -1.0), Vector(-1.0, 1.0, 1.0),
            Vector(1.0, -1.0, -1.0), Vector(1.0, -1.0, 1.0),
            Vector(1.0, 1.0, -1.0), Vector(1.0, 1.0, 1.0)
        )
    }

    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        val linePoint = currentIntersection._point
        val lineDirection = currentIntersection._direction
        val blockPoint = block.location.toVector()
        val centerPoint = blockPoint.clone().add(Vector(0.5, 0.5, 0.5))

        var lastIntersection: Vector? = null
        var lastDistance = Double.POSITIVE_INFINITY
        for (i in 0 until 8) {
            val planePoint = if (i < 4) Vector(-0.5, 0.0, 0.0) else Vector(0.5, 0.0, 0.0)
            val planeNormal = NORMALS[i]
            val intersection = MathUtil.getLinePlaneIntersection(
                linePoint, lineDirection, planePoint.add(centerPoint), planeNormal, false
            )

            if (!isInsideBlock(blockPoint, planeNormal, intersection!!)) {
                continue
            }

            val distance = intersection.distance(linePoint)
            if (distance < lastDistance) {
                lastIntersection = intersection
                lastDistance = distance
            }
        }

        if (lastIntersection == null) {
            return currentIntersection
        }

        val dist = linePoint.distance(centerPoint)
        val minDist = dist - RADIUS
        val maxDist = dist + RADIUS
        val factor = (lastDistance - minDist) / (maxDist - minDist)

        val yOffset = lastIntersection.x - lastIntersection.x.toInt()
        val xOffset = lastIntersection.z - lastIntersection.z.toInt()

        val pixelY = (floor((if (yOffset < 0) yOffset + 1 else yOffset) * textureSize)).toInt()
        val pixelX = (floor((if (xOffset < 0) xOffset + 1 else xOffset) * textureSize)).toInt()

        return Intersection.of(
            currentIntersection._normal, lastIntersection, lineDirection,
            (0xFF000000 or MathUtil.weightedColorSum(texture[pixelY][pixelX], 0, 1 - factor, factor).toLong()).toInt()
        )
    }

    private fun isInsideBlock(blockPoint: Vector, planeNormal: Vector, intersection: Vector): Boolean {
        var intersection = intersection.clone().subtract(blockPoint)

        if (intersection.x < 0 || intersection.x >= 1 || intersection.y < 0 || intersection.y >= 1
            || intersection.z < 0 || intersection.z >= 1
        ) {
            return false
        }

        val posX = planeNormal.x >= 0
        val posY = planeNormal.y >= 0
        val posZ = planeNormal.z >= 0

        val blockX = intersection.x >= 0.5
        val blockY = intersection.y >= 0.5
        val blockZ = intersection.z >= 0.5

        return posX == blockX && posY == blockY && posZ == blockZ
    }

    class OctahedronModelBuilder(texture: Array<IntArray>) : Builder(texture) {

        override fun build(): Model {
            return OctahedronModel(texture, transparencyFactor, reflectionFactor, occluding)
        }
    }
}
