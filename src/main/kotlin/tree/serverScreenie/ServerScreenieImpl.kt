package tree.serverScreenie
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import tree.serverScreenie.render.DefaultScreenRenderer
import tree.serverScreenie.render.Renderer
import tree.serverScreenie.render.Resolution
import javax.imageio.ImageIO
import java.io.File
import java.util.regex.Pattern

class ServerScreenieImpl : JavaPlugin(), CommandExecutor {

    private lateinit var screenRenderer: Renderer


    companion object {
        private val INVALID_FILE_NAME: Pattern = Pattern.compile("[^a-zA-Z0-9_]")
    }

    override fun onEnable() {
        ServerScreenie.instance = this
        screenRenderer = DefaultScreenRenderer()
       getCommand("screenshot")?.setExecutor(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String?>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(ChatColor.RED.toString() + "Only a player may use this command!")
            return true
        }

        val player = sender
        var fileName: String? = null
        var pixels: Resolution.Pixels? = Resolution.Pixels._1080P
        var aspectRatio: Resolution.AspectRatio? = Resolution.AspectRatio._16_9

        if (args.isNotEmpty()) {
            fileName = args[0]
            if (INVALID_FILE_NAME.matcher(fileName).find()) {
                player.sendMessage(ChatColor.RED.toString() + "Enter a file name containing only alphanumeric characters and underscore")
                return true
            }
        } else {
            player.sendMessage(ChatColor.RED.toString() + "Syntax: /screenshot <file name> [pixels] [aspect ratio]")
            return true
        }

        if (args.size > 1) {
            pixels = Resolution.Pixels.getPixels(args[1])
            if (pixels == null) {
                player.sendMessage(ChatColor.RED.toString() + "Invalid option \"" + args[1] + "\"")
                player.sendMessage(
                    ChatColor.YELLOW.toString() + "Available options: " + ChatColor.GRAY.toString() +
                            Resolution.Pixels.allAliases.joinToString(", ")
                )
                return true
            }
        }

        if (args.size > 2) {
            aspectRatio = Resolution.AspectRatio.getAspectRatio(args[2])
            if (aspectRatio == null) {
                player.sendMessage(ChatColor.RED.toString() + "Invalid option \"" + args[2] + "\"")
                player.sendMessage(
                    ChatColor.YELLOW.toString() + "Available options: " + ChatColor.GRAY.toString() +
                            Resolution.AspectRatio.allAliases.joinToString(", ")
                )
                return true
            }
        }

        val resolution = Resolution(pixels, aspectRatio)
        player.sendMessage(ChatColor.RED.toString() + "Starting rendering process, this may take a while...")

        val image = screenRenderer?.render(player, resolution)

        if (image == null) {
            try {
                throw IllegalStateException("ServerScreenie: render() returned null for player ${player.name}")
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            player.sendMessage(ChatColor.RED.toString() + "Rendering failed — the screenshot image is null.")
            return true
        }

        player.sendMessage(ChatColor.GREEN.toString() + "Rendering complete, saving image...")

        val file = File(dataFolder, "$fileName.png")

        try {
            // Ensure directory exists
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
            }
            ImageIO.write(image, "png", file)
        } catch (e: Exception) {
            player.sendMessage(ChatColor.RED.toString() + "An error occurred while trying to save the image file. Check the console for more information")
            e.printStackTrace()
            return true
        }

        player.sendMessage(ChatColor.GREEN.toString() + "Image saved to the file \"" + file.path + "\" with success")
        return true
    }
}
