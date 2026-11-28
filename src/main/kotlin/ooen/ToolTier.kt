package ooen

internal enum class ToolTier(val idPrefix: String, val material: String?) {
    WOOD("wooden", "plankWood"),
    STONE("stone", "cobblestone"),
    IRON("iron", "ingotIron"),
    DIAMOND("diamond", "gemDiamond"),
    EMERALD("emerald", "gemEmerald"),
    NETHERITE("netherite", null),
}
