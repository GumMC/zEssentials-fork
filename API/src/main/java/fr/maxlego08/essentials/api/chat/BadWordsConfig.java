package fr.maxlego08.essentials.api.chat;

import fr.maxlego08.essentials.api.modules.Loadable;

import java.util.List;

/**
 * Configuration for bad words filtering system
 */
public record BadWordsConfig(
    boolean enabled,
    String replacement,
    int maxWarns,
    List<String> punishments,
    List<String> badWords,
    String bypassPermission
) implements Loadable {

    public static BadWordsConfig enabled(String replacement, int maxWarns, List<String> punishments, List<String> badWords, String bypassPermission) {
        return new BadWordsConfig(true, replacement, maxWarns, punishments, badWords, bypassPermission);
    }

    public static BadWordsConfig disabled() {
        return new BadWordsConfig(false, "***", -1, List.of(), List.of(), "");
    }

    public boolean isEnabled() {
        return this.enabled && !this.badWords.isEmpty();
    }
}
