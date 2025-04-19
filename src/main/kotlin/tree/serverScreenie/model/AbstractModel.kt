package tree.serverScreenie.model

import com.google.common.base.Preconditions
import tree.serverScreenie.model.StaticModel.StaticModelBuilder




abstract class AbstractModel(
    texture: Array<IntArray>,
    override val transparencyFactor: Double,
    override val reflectionFactor: Double,
    override val isOccluding: Boolean
) : Model {

    val textureSize: Int
    val texture: Array<IntArray>

    init {
        // Preconditions for validating texture
        Preconditions.checkNotNull(texture)
        Preconditions.checkArgument(texture.isNotEmpty(), "texture cannot be empty")
        Preconditions.checkArgument(texture.size == texture[0].size, "texture must be a square array")

        this.textureSize = texture.size
        this.texture = texture
    }

    // Abstract Builder class
    abstract class Builder(val texture: Array<IntArray>) {

        var transparencyFactor: Double = 0.0
        var reflectionFactor: Double = 0.0
        var occluding: Boolean = false

        companion object {
            fun createMulti(topTexture: Array<IntArray>, sideTexture: Array<IntArray>, bottomTexture: Array<IntArray>): MultiModel.MultiModelBuilder? = MultiModel.MultiModelBuilder(topTexture, sideTexture, bottomTexture)
            fun createSimple(texture: Array<IntArray>): SimpleModel.SimpleModelBuilder = SimpleModel.SimpleModelBuilder(texture)
            fun createStatic(color: Int): StaticModelBuilder = StaticModelBuilder(color)
            fun createCarpet(texture: Array<IntArray>): CarpetModel.CarpetModelBuilder = CarpetModel.CarpetModelBuilder(texture)
            fun createCross(texture: Array<IntArray>): CrossModel.CrossModelBuilder = CrossModel.CrossModelBuilder(texture)
            fun createSphere(texture: Array<IntArray>): SphereModel.SphereModelBuilder = SphereModel.SphereModelBuilder(texture)
            fun createOctahedron(texture: Array<IntArray>): OctahedronModel.OctahedronModelBuilder = OctahedronModel.OctahedronModelBuilder(texture)

        }
        fun transparency(transparencyFactor: Double): Builder {
            this.transparencyFactor = transparencyFactor
            return this
        }

        fun reflection(reflectionFactor: Double): Builder {
            this.reflectionFactor = reflectionFactor
            return this
        }

        fun occlusion(): Builder {
            this.occluding = true
            return this
        }

        abstract fun build(): Model
    }
}

