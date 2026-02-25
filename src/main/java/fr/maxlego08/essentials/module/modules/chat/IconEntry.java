package fr.maxlego08.essentials.module.modules.chat;

public record IconEntry(
        String name,
        String tab,
        String value,
        String permission,
        String category
) {

    public String completionValue() {
        String display = (tab == null || tab.isBlank()) ? "" : tab;
        return ":" + name + ": " + display;
    }
}
