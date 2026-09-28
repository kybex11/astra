package astra.mod.client;

public enum Category {
    COMBAT("Combat", "Сражения"),
    MOVEMENT("Movement", "Перемещение"),
    RENDER("Render", "Визуальные"),
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
