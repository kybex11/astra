package astra.mod.client;

public enum Category {
    COMBAT("Combat", "Сражения"),
    BUILD("Build", "Строительство"),
    MOVEMENT("Movement", "Перемещение"),
    RENDER("Render", "Визуальные"),
    HUD("HUD", "HUD Интерфейс"),
    PLAYER("Player", "Игрок"),
    MISC("Misc", "Разное");

    private final String displayName;
    private final String description;

    Category(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
