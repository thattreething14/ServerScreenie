package tree.serverScreenie.render

import org.bukkit.entity.Player
import java.awt.image.BufferedImage


interface Renderer {
    fun render(player: Player, resolution: Resolution): BufferedImage
}