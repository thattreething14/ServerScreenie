package tree.serverScreenie.utils

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import tree.serverScreenie.utils.MathUtil.getLinePlaneIntersection


class BlockRaytracer(loc: Location) : BlockIterator(loc) {
    private val position: Vector = loc.toVector()
    private val direction: Vector = loc.direction

    private var lastBlock: Block? = null
    private var currentFace: BlockFace? = null

    val intersectionFace: BlockFace
        get() {
            checkNotNull(currentFace) { "Called before next()" }

            return currentFace!!
        }

    val intersectionPoint: Vector
        get() {
            val lastFace = this.intersectionFace
            val planeNormal = Vector(lastFace.modX, lastFace.modY, lastFace.modZ)
            val planePoint = lastBlock!!.location.add(0.5, 0.5, 0.5).toVector()
                .add(planeNormal.clone().multiply(0.5))

            return getLinePlaneIntersection(position, direction, planePoint, planeNormal, true)!!
        }

    override fun next(): Block {
        val currentBlock = super.next()
        currentFace = if (lastBlock == null) BlockFace.SELF else currentBlock.getFace(lastBlock!!)

        return (currentBlock.also { lastBlock = it })
    }
}