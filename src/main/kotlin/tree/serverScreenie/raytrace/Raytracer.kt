package tree.serverScreenie.raytrace

import org.bukkit.World
import org.bukkit.util.Vector


interface Raytracer {
    fun trace(world: World, point: Vector, direction: Vector): Int
}