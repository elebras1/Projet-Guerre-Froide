package com.populaire.projetguerrefroide.util;

public class BuildingUtils {
    public static String getColor(String building) {
        return switch (building) {
            case "refinery", "cement_factory", "steel_factory", "glass_factory" -> "brown";
            case "coal_powerplant", "renewable_powerplant", "nuclear_powerplant" -> "red";
            case "fabric_factory", "paper_factory", "clothes_factory" -> "green";
            case "inorganic_chemicals_factory", "plastics_factory", "pharmaceuticals_factory" -> "purple";
            case "integrated_circuits_factory", "electronic_components_factory", "computers_factory",
                 "communication_factory" -> "blue";
            case "machine_parts_factory", "automobiles_factory" -> "darkblue";
            case "transport_factory", "ships_factory", "aeroplanes_factory" -> "yellow";
            case "ammunition_factory", "weaponry_factory", "armoured_vehicles_factory", "military_satellites_factory",
                 "centrifuges" -> "cyan";
            case "aluminium_factory"        -> "orange";
            default -> null;
        };
    }
}
