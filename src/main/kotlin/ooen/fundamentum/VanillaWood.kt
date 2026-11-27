package ooen.fundamentum

internal enum class VanillaWood(val suffix: String) {
    OAK(""),
    SPRUCE("_spruce"),
    BIRCH("_birch"),
    JUNGLE("_jungle"),
    ACACIA("_acacia"),
    DARK_OAK("_dark_oak");

    // The vanilla planks and wooden slabs share this metadata order.
    val metadata: Int
        get() = ordinal
}
