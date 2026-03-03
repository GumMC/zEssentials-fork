package fr.maxlego08.essentials.module.modules.chat;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.chat.ChatDisplay;
import fr.maxlego08.essentials.api.utils.component.AdventureComponent;
import fr.maxlego08.essentials.zutils.utils.ZUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemDisplay extends ZUtils implements ChatDisplay {

    private final EssentialsPlugin plugin;
    private final Pattern pattern;
    private final String result;
    private final String permission;

    public ItemDisplay(EssentialsPlugin plugin, String regex, String result, String permission, String itemNameRegex) {
        this.plugin = plugin;
        this.pattern = Pattern.compile(regex);
        this.result = result;
        this.permission = permission;
    }

    @Override
    public String display(AdventureComponent adventureComponent, TagResolver.Builder builder, Player sender, Player receiver, String message) {

        ItemStack itemStack = sender.getInventory().getItemInMainHand();
        if (itemStack.isEmpty()) {
            return message;
        }

        int amount = itemStack.getAmount();

        Component itemName;
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            itemName = itemStack.getItemMeta().displayName();
        } else {
            itemName = Component.translatable(itemStack.translationKey());
        }

        Matcher matcher = this.pattern.matcher(message);
        if (!matcher.find()) return message;
        matcher.reset();

        StringBuilder formattedMessage = new StringBuilder();
        String name = "item";

        Component component = adventureComponent.getComponent(this.result, TagResolver.builder()
                .resolver(Placeholder.component("item", itemName))
                .resolver(Placeholder.component("amount", Component.text(amount)))
                .build());

        component = component.hoverEvent(itemStack.asHoverEvent());
        builder.resolver(Placeholder.component(name, component));

        while (matcher.find()) matcher.appendReplacement(formattedMessage, "<" + name + ">");
        matcher.appendTail(formattedMessage);
        return formattedMessage.toString();
    }

    @Override
    public boolean hasPermission(Permissible permissible) {
        return permissible.hasPermission(this.permission);
    }
}
