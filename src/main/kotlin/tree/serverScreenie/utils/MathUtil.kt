package tree.serverScreenie.utils

import org.bukkit.Color
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin


object MathUtil {
    fun yawPitchRotation(base: Vector, angleYaw: Double, anglePitch: Double): Vector {
        val oldX = base.getX()
        val oldY = base.getY()
        val oldZ = base.getZ()

        val sinOne = sin(angleYaw)
        val sinTwo = sin(anglePitch)
        val cosOne = cos(angleYaw)
        val cosTwo = cos(anglePitch)

        val newX = oldX * cosOne * cosTwo - oldY * cosOne * sinTwo - oldZ * sinOne
        val newY = oldX * sinTwo + oldY * cosTwo
        val newZ = oldX * sinOne * cosTwo - oldY * sinOne * sinTwo + oldZ * cosOne

        return Vector(newX, newY, newZ)
    }

    fun doubleYawPitchRotation(
        base: Vector, firstYaw: Double, firstPitch: Double, secondYaw: Double,
        secondPitch: Double
    ): Vector {
        return yawPitchRotation(yawPitchRotation(base, firstYaw, firstPitch), secondYaw, secondPitch)
    }

    fun reflectVector(linePoint: Vector, lineDirection: Vector, planePoint: Vector, planeNormal: Vector): Vector {
        return lineDirection.clone().subtract(planeNormal.clone().multiply(2 * lineDirection.dot(planeNormal)))
    }

    fun toVector(face: BlockFace): Vector {
        return Vector(face.getModX(), face.getModY(), face.getModZ())
    }

    fun weightedColorSum(rgbOne: Int, rgbTwo: Int, weightOne: Double, weightTwo: Double): Int {
        val colorOne = Color.fromRGB(rgbOne and 0xFFFFFF)
        val colorTwo = Color.fromRGB(rgbTwo and 0xFFFFFF)

        val total = weightOne + weightTwo
        val newRed = ((colorOne.getRed() * weightOne + colorTwo.getRed() * weightTwo) / total).toInt()
        val newGreen = ((colorOne.getGreen() * weightOne + colorTwo.getGreen() * weightTwo) / total).toInt()
        val newBlue = ((colorOne.getBlue() * weightOne + colorTwo.getBlue() * weightTwo) / total).toInt()

        return Color.fromRGB(newRed, newGreen, newBlue).asRGB()
    }

    fun getLinePlaneIntersection(
        linePoint: Vector, lineDirection: Vector, planePoint: Vector,
        planeNormal: Vector, allowBackwards: Boolean
    ): Vector? {
        val d = planePoint.dot(planeNormal)
        val t = (d - planeNormal.dot(linePoint)) / planeNormal.dot(lineDirection)

        if (t < 0 && !allowBackwards) {
            return null
        }

        val x = linePoint.getX() + lineDirection.getX() * t
        val y = linePoint.getY() + lineDirection.getY() * t
        val z = linePoint.getZ() + lineDirection.getZ() * t

        return Vector(x, y, z)
    }
}