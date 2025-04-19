package tree.serverScreenie.render

import com.google.common.base.Preconditions
import java.util.*
import kotlin.math.roundToInt


class Resolution {
    val width: Int
    val height: Int

    constructor(pixels: Pixels?, aspectRatio: AspectRatio?) {
        Preconditions.checkNotNull<Pixels?>(pixels)
        Preconditions.checkNotNull<AspectRatio?>(aspectRatio)

        this.height = pixels!!.height
        this.width = (pixels.height * aspectRatio!!.ratio).roundToInt().toInt()
    }

    enum class Pixels(val height: Int, vararg aliases: String?) {
        //TODO: Make it so scales above 1080p don't make the server completely tweak the fuck out!
        _128P(128, "128p"),
        _144P(144, "144p"),
        _240P(240, "240p"),
        _360P(360, "360p"),
        _480P(480, "480p"),
        _720P(720, "720p"),
        _1080P(1080, "1080p", "fullhd", "full-hd"),
        _4K(2160, "4k", "ultrahd", "ultra-hd"),
        _8K(4320, "8k"),
        _16K(8640, "16k");

        val aliases: MutableList<String> = Collections.unmodifiableList<String?>(listOf<String?>(*aliases))

        companion object {
            private val BY_ALIAS: MutableMap<String?, Pixels?>

            init {
                val byAlias: MutableMap<String?, Pixels?> = HashMap<String?, Pixels?>()
                for (pixels in entries) {
                    for (alias in pixels.aliases) {
                        byAlias.put(alias.lowercase(Locale.getDefault()), pixels)
                    }
                }
                BY_ALIAS = Collections.unmodifiableMap<String?, Pixels?>(byAlias)
            }

            fun getPixels(alias: String?): Pixels? {
                Preconditions.checkNotNull<String?>(alias)

                return BY_ALIAS[alias!!.lowercase(Locale.getDefault())]
            }

            val allAliases: MutableSet<String?>
                get() = BY_ALIAS.keys
        }
    }

    enum class AspectRatio(val ratio: Double, vararg aliases: String?) {
        _4_3(4 / 3.0, "4:3"),
        _16_9(16 / 9.0, "16:9", "wide", "widescreen", "wide-screen"),
        ULTRAWIDE(21 / 9.0, "21:9", "ultrawide", "ultra-wide");

        val aliases: MutableList<String> = Collections.unmodifiableList<String?>(listOf<String?>(*aliases))

        companion object {
            private val BY_ALIAS: MutableMap<String?, AspectRatio?>

            init {
                val byAlias: MutableMap<String?, AspectRatio?> = HashMap<String?, AspectRatio?>()
                for (aspectRatio in entries) {
                    for (alias in aspectRatio.aliases) {
                        byAlias.put(alias.lowercase(Locale.getDefault()), aspectRatio)
                    }
                }
                BY_ALIAS = Collections.unmodifiableMap<String?, AspectRatio?>(byAlias)
            }

            fun getAspectRatio(alias: String?): AspectRatio? {
                Preconditions.checkNotNull<String?>(alias)

                return BY_ALIAS[alias!!.lowercase(Locale.getDefault())]
            }

            val allAliases: MutableSet<String?>
                get() = BY_ALIAS.keys
        }
    }
}