package tree.serverScreenie.utils

import org.bukkit.util.Vector

class Intersection private constructor(val _normal: Vector, val _point: Vector, val _direction: Vector, val _color: Int) {

    fun getNormal(): Vector {
        return _normal
    }

    fun getPoint(): Vector {
        return _point
    }

    fun getDirection(): Vector {
        return _direction
    }

    companion object {
        fun of(normal: Vector, point: Vector, direction: Vector): Intersection {
            return of(normal, point, direction, 0)
        }

        fun of(normal: Vector, point: Vector, direction: Vector, color: Int): Intersection {
            return Intersection(normal, point, direction, color)
        }
        val EMPTY = of(
            Vector(0.0, 1.0, 0.0), // normal = up
            Vector(0.0, 0.0, 0.0), // point
            Vector(0.0, 0.0, 0.0), // direction
            0 // color
        )
    }
}