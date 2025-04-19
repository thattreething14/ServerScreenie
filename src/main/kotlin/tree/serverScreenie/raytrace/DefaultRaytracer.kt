package tree.serverScreenie.raytrace
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.util.Vector
import tree.serverScreenie.registry.DefaultModelRegistry
import tree.serverScreenie.utils.BlockRaytracer
import tree.serverScreenie.utils.Intersection
import tree.serverScreenie.utils.MathUtil
import kotlin.math.*

class DefaultRaytracer : Raytracer {

    private val textureRegistry = DefaultModelRegistry()
    private var reflectedBlock: Block? = null

    companion object {
        private const val MAX_DISTANCE = 300
        private const val REFLECTION_DEPTH = 10

        private val LIGHT_DIRECTION = Vector(-1.0, -1.0, -1.0).normalize()
        private const val AMBIENT_LIGHT = 0.2
    }

    init {
        textureRegistry.initialize()
        reflectedBlock = null
    }

    override fun trace(world: World, point: Vector, direction: Vector): Int {
        return trace(world, point, direction, REFLECTION_DEPTH)
    }

    private fun trace(world: World, point: Vector, direction: Vector, reflectionDepth: Int): Int {
        var loc = point.toLocation(world)
        loc.direction = direction
        val iterator = BlockRaytracer(loc)
        var baseColor = Color.fromRGB(65, 89, 252).asRGB()
        var finalIntersection: Vector? = null

        var reflectionColor = 0
        var reflectionFactor = 0.0
        var reflected = false

        var transparencyStart: Vector? = null
        var transparencyColor = 0
        var transparencyFactor = 0.0

        var occlusionMaterial: Material? = null
        var occlusionData: BlockData? = null

        for (i in 0 until MAX_DISTANCE) {
            if (!iterator.hasNext()) break

            val block = iterator.next()

            if (reflectedBlock != null && reflectedBlock == block) continue
            reflectedBlock = null

            val material = block.type
            if (material == Material.AIR) {
                occlusionMaterial = null
                occlusionData = null
                continue
            }

            val textureModel = textureRegistry.getModel(block)
            val currentIntersection = Intersection.of(
                MathUtil.toVector(iterator.intersectionFace),
                if (i == 0) point else iterator.intersectionPoint,
                direction
            )
            val newIntersection = textureModel?.intersect(block, currentIntersection) ?: continue

            var color = newIntersection._color

            // Reflections
            if (!reflected && textureModel.reflectionFactor > 0 && reflectionDepth > 0 && (color shr 24) != 0) {
                reflectedBlock = block
                val reflectedDir = MathUtil.reflectVector(point, direction, newIntersection._point, newIntersection._normal)
                reflectionColor = trace(world, newIntersection._point, reflectedDir, reflectionDepth - 1)
                reflectionFactor = textureModel.reflectionFactor
                reflected = true
            }

            // Transparency
            if (transparencyStart == null && textureModel.transparencyFactor > 0) {
                transparencyStart = newIntersection._point
                transparencyColor = newIntersection._color
                transparencyFactor = textureModel.transparencyFactor
            }

            // Occlusion tracking
            if (textureModel.isOccluding) {
                val data = block.blockData
                if (material == occlusionMaterial && data == occlusionData) continue
                occlusionMaterial = material
                occlusionData = data
            } else {
                occlusionMaterial = null
                occlusionData = null
            }

            if (transparencyStart != null && textureModel.transparencyFactor > 0) continue
            if ((color shr 24) == 0) continue

            // ===== Improved Lighting with Smooth Shadows =====
            val normal = newIntersection._normal.normalize()
            val lightDir = LIGHT_DIRECTION.normalize()
            val lightDot = normal.dot(lightDir).coerceAtLeast(0.0)
            val isShadowed = isInShadow(world, newIntersection._point, lightDir)

// Smooth shading calculation
            val diffuseStrength = 0.7
            val diffuse = if (isShadowed) 0.0 else lightDot * diffuseStrength
            val brightness = (AMBIENT_LIGHT + diffuse).coerceIn(0.0, 1.0)

// Apply brightness to color
            val r = ((color shr 16) and 0xFF) * brightness
            val g = ((color shr 8) and 0xFF) * brightness
            val b = (color and 0xFF) * brightness

            color = Color.fromRGB(
                min(255, r.toInt()),
                min(255, g.toInt()),
                min(255, b.toInt())
            ).asRGB()


            baseColor = color
            finalIntersection = newIntersection._point
            break
        }

        // Final transparency and reflection blending
        if (transparencyStart != null) {
            baseColor = MathUtil.weightedColorSum(
                baseColor,
                transparencyColor,
                transparencyFactor,
                (1 - transparencyFactor) * (1 + transparencyStart.distance(finalIntersection ?: transparencyStart) / 5.0)
            )
        }
        if (reflected) {
            baseColor = MathUtil.weightedColorSum(baseColor, reflectionColor, 1 - reflectionFactor, reflectionFactor)
        }

        return baseColor and 0xFFFFFF
    }

    private fun isInShadow(world: World, point: Vector, directionToLight: Vector): Boolean {
        val shadowOrigin = point.clone().add(directionToLight.clone().multiply(0.1)) // Avoid self-shadow
        val shadowLocation = shadowOrigin.toLocation(world).apply {
            direction = directionToLight
        }
        val iterator = BlockRaytracer(shadowLocation)
        for (i in 0 until 20) {
            if (!iterator.hasNext()) break
            val block = iterator.next()
            if (block.type != Material.AIR) return true
        }
        return false
    }

}
