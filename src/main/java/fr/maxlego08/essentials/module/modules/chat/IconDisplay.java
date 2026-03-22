package fr.maxlego08.essentials.module.modules.chat;

import fr.maxlego08.essentials.api.chat.ChatDisplay;
import fr.maxlego08.essentials.api.utils.component.AdventureComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IconDisplay implements ChatDisplay {

    private static final Pattern ICON_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+):");
    private static final Pattern GLYPH_PATTERN = Pattern.compile("<glyph:[^>]+>");

    private final Map<String, IconEntry> icons;
    private final Map<String, String> categoryPermissions;

    public IconDisplay(Map<String, IconEntry> icons, Map<String, String> categoryPermissions) {
        this.icons = icons;
        this.categoryPermissions = categoryPermissions;
    }

    @Override
    public String display(AdventureComponent adventureComponent, TagResolver.Builder builder,
                          Player sender, Player receiver, String message) {

        Matcher matcher = ICON_PATTERN.matcher(message);
        if (!matcher.find()) return message;
        matcher.reset();

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String iconName = matcher.group(1);
            IconEntry entry = icons.get(iconName);

            if (entry == null || !canUse(sender, entry)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }

            String tagName = "icon_" + iconName;
            builder.resolver(Placeholder.component(tagName, adventureComponent.getComponent(entry.value())));
            String replacement = "<" + tagName + ">";
            if (GLYPH_PATTERN.matcher(entry.value()).find()) {
                replacement += "<font:default>";
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @Override
    public boolean hasPermission(Permissible permissible) {
        return true;
    }

    private boolean canUse(Player player, IconEntry entry) {
        String catPerm = categoryPermissions.get(entry.category());
        if (catPerm != null && !catPerm.isBlank() && player.hasPermission(catPerm)) return true;
        if (entry.permission() != null && !entry.permission().isBlank()) return player.hasPermission(entry.permission());
        return true;
    }
}
