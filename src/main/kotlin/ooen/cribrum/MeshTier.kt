package ooen.cribrum

// Ordinals are stored in item metadata and saved sieves. Add new tiers at the end.
internal enum class MeshTier(val key: String, val color: Int) {
    STRING("string_mesh", 0xFFFFFF),
    FLINT("flint_mesh", 0x777C84),
    IRON("iron_mesh", 0xCDD3DA),
    GOLD("gold_mesh", 0xFFCB50),
    DIAMOND("diamond_mesh", 0x62E7EE),
    EMERALD("emerald_mesh", 0x5DE47F),
    NETHERITE("netherite_mesh", 0x665565);

    fun isAvailable(): Boolean = this != NETHERITE || Cribrum.netheriteAvailable
}
