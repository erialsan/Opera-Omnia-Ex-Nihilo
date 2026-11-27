package ooen.materia;

import com.gtnewhorizon.gtnhlib.config.Config;

// GTNHLib treats each declared field as a setting, so this class only contains config fields.
@Config(modid = "ooen_materia", category = "materials", categoryCulling = false)
final class MateriaConfig {

    @Config.Comment("JAOPCA material names allowed in Materia. Names are case-sensitive.")
    @Config.DefaultStringList({ "Iron", "Gold", "Copper", "Tin", "Aluminum", "Aluminium", "Lead", "Nickel", "Silver",
        "Cobalt", "Ardite", "Osmium", "Iridium", "Tungsten", "Boron", "Uranium", "Platinum", "Manganese", "Titanium",
        "Mithril", "Thorium" })
    static String[] metalWhitelist;
}
