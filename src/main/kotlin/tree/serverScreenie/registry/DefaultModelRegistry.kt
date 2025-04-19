package tree.serverScreenie.registry


import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import java.awt.image.BufferedImage
import java.io.IOException
import  tree.serverScreenie.model.AbstractModel.Builder
import tree.serverScreenie.model.Blocks
import tree.serverScreenie.model.Model
import javax.imageio.ImageIO

class DefaultModelRegistry : ModelRegistry {
    private val modelMap: MutableMap<Material?, MutableMap<BlockData?, Model?>?> = HashMap<Material?, MutableMap<BlockData?, Model?>?>()
    private var textures: BufferedImage? = null

    public override fun initialize() {
        val url = this.javaClass.getClassLoader().getResource(IMAGE_RESOURCE)
        if (url == null) {
            throw RuntimeException("Default resource \"terrain.png\" is missing")
        }
        try {
            url.openConnection().getInputStream().use { input ->
                this.textures = ImageIO.read(input)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        registerModel(Material.SHORT_GRASS, Builder.createCross(Blocks.SHORT_GRASS.toTexture()).build())
        registerModel(Material.SUGAR_CANE, Builder.createCross(Blocks.SUGAR_CANE.toTexture()).build())
        registerModel(Material.OAK_LOG, Builder.createMulti(Blocks.OAK_LOG_TOP.toTexture(), Blocks.OAK_LOG.toTexture(), Blocks.OAK_LOG_TOP.toTexture())?.build())
        registerModel(Material.STONE, Builder.createSimple(Blocks.STONE.toTexture()).build())
        registerModel(Material.GRASS_BLOCK, Builder.createMulti(Blocks.GRASS_BLOCK_TOP.toTexture(), Blocks.GRASS_BLOCK_SIDE.toTexture(), Blocks.DIRT.toTexture())?.build())
        registerModel(Material.SAND, Builder.createSimple(Blocks.SAND.toTexture()).build())
        registerModel(Material.OAK_WOOD, Builder.createSimple(Blocks.OAK_LOG.toTexture()).build())
        registerModel(Material.GLASS, Builder.createSimple(Blocks.GLASS.toTexture()).transparency(0.5).build())
        registerModel(Material.COAL_ORE, Builder.createSimple(Blocks.COAL_ORE.toTexture()).build())
        registerModel(Material.IRON_ORE, Builder.createSimple(Blocks.IRON_ORE.toTexture()).build())
        registerModel(Material.GOLD_ORE, Builder.createSimple(Blocks.GOLD_ORE.toTexture()).build())
        registerModel(Material.REDSTONE_ORE, Builder.createSimple(Blocks.REDSTONE_ORE.toTexture()).build())
        registerModel(Material.LAPIS_ORE, Builder.createSimple(Blocks.LAPIS_ORE.toTexture()).build())
        registerModel(Material.DIAMOND_ORE, Builder.createSimple(Blocks.DIAMOND_ORE.toTexture()).build())
        registerModel(Material.EMERALD_ORE, Builder.createSimple(Blocks.EMERALD_ORE.toTexture()).build())
        // You get the point
}

    public override fun getModel(material: Material?, blockData: BlockData?): Model? {
        return modelMap.computeIfAbsent(material) { key: Material? -> HashMap<BlockData?, Model?>() }!!.getOrDefault(
            blockData,
            if (blockData == null)
                this.defaultModel
            else
                modelMap[material]!!.getOrDefault(null, this.defaultModel)
        )
    }

    override val defaultModel: Model
        get() = Builder.createStatic(Color.PURPLE.asRGB()).build()

    private fun registerModel(material: Material?, blockModel: Model?) {
        modelMap.computeIfAbsent(material) { key: Material? -> HashMap<BlockData?, Model?>() }!!
            .put(null, blockModel)
    }

    private fun textureIndex(verticalIndex: Int, horizontalIndex: Int): Array<IntArray?> {
        val texture = Array<IntArray?>(TEXTURE_SIZE) { IntArray(TEXTURE_SIZE) }

        val offsetY = verticalIndex * TEXTURE_SIZE + (TEXTURE_SIZE - 1)
        val offsetX = horizontalIndex * TEXTURE_SIZE

        for (pixelY in 0..<TEXTURE_SIZE) {
            for (pixelX in 0..<TEXTURE_SIZE) {
                texture[pixelY]!![pixelX] = textures!!.getRGB(offsetX + pixelX, offsetY - pixelY)
            }
        }

        return texture
    }

    companion object {
        private const val IMAGE_RESOURCE = "terrain.png"
        private const val TEXTURE_SIZE = 16
    }
}