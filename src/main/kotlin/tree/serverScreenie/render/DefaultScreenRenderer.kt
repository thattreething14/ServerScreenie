package tree.serverScreenie.render

import org.bukkit.entity.Player
import org.bukkit.util.Vector
import tree.serverScreenie.raytrace.DefaultRaytracer
import tree.serverScreenie.raytrace.Raytracer
import tree.serverScreenie.utils.MathUtil.doubleYawPitchRotation
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import kotlin.math.atan2
import kotlin.math.sqrt


class DefaultScreenRenderer : Renderer {
    private val raytracer: Raytracer = DefaultRaytracer()

    override fun render(player: Player, resolution: Resolution): BufferedImage {
        val width: Int = resolution.width
        val height: Int = resolution.height

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val imageData = (image.raster.getDataBuffer() as DataBufferInt).getData()

        val world = player.world
        val linePoint = player.eyeLocation.toVector()
        val rayMap = buildRayMap(player, resolution)
        for (i in rayMap.indices) {
            imageData[i] = raytracer.trace(world, linePoint, rayMap[i]!!)
        }

        return image
    }

    private fun buildRayMap(p: Player, resolution: Resolution): MutableList<Vector?> {
        val eyeLocation = p.eyeLocation
        val lineDirection = eyeLocation.direction

        val x = lineDirection.x
        val y = lineDirection.y
        val z = lineDirection.z

        val angleYaw = atan2(z, x)
        val anglePitch = atan2(y, sqrt(x * x + z * z))

        val lowerLeftCorner = doubleYawPitchRotation(BASE_VEC, -FOV_YAW_RAD, -FOV_PITCH_RAD, angleYaw, anglePitch)
        val upperLeftCorner = doubleYawPitchRotation(BASE_VEC, -FOV_YAW_RAD, FOV_PITCH_RAD, angleYaw, anglePitch)
        val lowerRightCorner = doubleYawPitchRotation(BASE_VEC, FOV_YAW_RAD, -FOV_PITCH_RAD, angleYaw, anglePitch)
        val upperRightCorner = doubleYawPitchRotation(BASE_VEC, FOV_YAW_RAD, FOV_PITCH_RAD, angleYaw, anglePitch)

        val width: Int = resolution.width
        val height: Int = resolution.height
        val rayMap: MutableList<Vector?> = ArrayList<Vector?>(width * height)

        val leftFraction = upperLeftCorner.clone().subtract(lowerLeftCorner).multiply(1.0 / (height - 1))
        val rightFraction = upperRightCorner.clone().subtract(lowerRightCorner).multiply(1.0 / (height - 1))

        for (pitch in 0..<height) {
            val leftPitch = upperLeftCorner.clone().subtract(leftFraction.clone().multiply(pitch))
            val rightPitch = upperRightCorner.clone().subtract(rightFraction.clone().multiply(pitch))
            val yawFraction = rightPitch.clone().subtract(leftPitch).multiply(1.0 / (width - 1))

            for (yaw in 0..<width) {
                val ray = leftPitch.clone().add(yawFraction.clone().multiply(yaw)).normalize()
                rayMap.add(ray)
            }
        }

        return rayMap
    }

    companion object {
        private const val FOV_YAW_DEG = 53.0
        private const val FOV_PITCH_DEG = 23.0

        private val FOV_YAW_RAD = Math.toRadians(FOV_YAW_DEG)
        private val FOV_PITCH_RAD = Math.toRadians(FOV_PITCH_DEG)

        private val BASE_VEC = Vector(1, 0, 0)
    }
}