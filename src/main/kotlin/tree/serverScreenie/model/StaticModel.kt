package tree.serverScreenie.model

import org.bukkit.block.Block
import tree.serverScreenie.utils.Intersection


class StaticModel private constructor(
    private val color: Int,
    override val transparencyFactor: Double,
    override val reflectionFactor: Double,
    override val isOccluding: Boolean
) : Model {
    override fun intersect(block: Block, currentIntersection: Intersection): Intersection {
        return Intersection.of(
            currentIntersection.getNormal(), currentIntersection.getPoint(),
            currentIntersection.getDirection(), color
        )
    }

    class StaticModelBuilder internal constructor(private val color: Int) :
        AbstractModel.Builder(Array<IntArray>(1) { IntArray(1) }) {
        public override fun build(): StaticModel {
            return StaticModel(color, transparencyFactor, reflectionFactor, occluding)
        }
    }
}