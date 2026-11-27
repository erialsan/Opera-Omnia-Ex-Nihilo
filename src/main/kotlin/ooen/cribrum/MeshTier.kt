package ooen.cribrum

// Item metadata and saved sieves use enum ordinals.
internal enum class MeshTier(val key: String, val color: Int, val oreTier: Int) {
    STRING("string_mesh", 0xFFFFFF, 0),
    FLINT("flint_mesh", 0x777C84, 1),
    IRON("iron_mesh", 0xCDD3DA, 2),
    GOLD("gold_mesh", 0xFFCB50, 3),
    DIAMOND("diamond_mesh", 0x62E7EE, 4),
    EMERALD("emerald_mesh", 0x5DE47F, 4),
    NETHERITE("netherite_mesh", 0x665565, 4);

    fun isAvailable(): Boolean = this != NETHERITE || Cribrum.netheriteAvailable
}
