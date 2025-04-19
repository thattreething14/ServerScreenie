package tree.serverScreenie.model

import org.bukkit.block.Block
import tree.serverScreenie.utils.Intersection


interface Model {

    fun intersect(block: Block, currentIntersection: Intersection): Intersection

    val transparencyFactor: Double

    val reflectionFactor: Double

    val isOccluding: Boolean
}