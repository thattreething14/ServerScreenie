package tree.serverScreenie.registry


import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import tree.serverScreenie.model.Model

interface ModelRegistry {
    fun initialize()

    fun getModel(block: Block): Model? {
        return getModel(block.type, block.blockData)
    }

    fun getModel(material: Material?): Model? {
        return getModel(material, null)
    }

    fun getModel(material: Material?, blockData: BlockData?): Model?

    val defaultModel: Model?
}