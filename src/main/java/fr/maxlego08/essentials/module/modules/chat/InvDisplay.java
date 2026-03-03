package fr.maxlego08.essentials.module.modules.chat;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.chat.ChatDisplay;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.utils.component.AdventureComponent;
import fr.maxlego08.essentials.zutils.utils.ZUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChatDisplay implementation that replaces [inv] tags in chat messages with a
 * clickable component that opens the sender's full inventory snapshot for the viewer.
 *
 * <p>Uses the same code-based caching pattern as {@link ItemDisplay}:
 * a random code is generated, stored in {@link ChatModule}, and redeemed via
 * {@code /showinv <code>} when the viewer clicks the component.
 */
public class InvDisplay extends ZUtils implements ChatDisplay {

    private final EssentialsPlugin plugin;
    private final Pattern pattern;
    private final String result;
    private final String permission;

    public InvDisplay(EssentialsPlugin plugin, String regex, String result, String permission) {
        this.plugin = plugin;
        this.pattern = Pattern.compile(regex);
        this.result = result;
        this.permission = permission;
    }

    @Override
    public String display(AdventureComponent adventureComponent, TagResolver.Builder builder,
                          Player sender, Player receiver, String message) {

        Matcher matcher = this.pattern.matcher(message);
        if (!matcher.find()) return message;

        // Check that the player's inventory is not completely empty
        boolean hasContent = hasAnyContent(sender);
        if (!hasContent) {
            throw new fr.maxlego08.essentials.api.chat.ChatDisplayException(Message.CHAT_INV_EMPTY);
        }

        // Reset matcher to start from beginning again
        matcher.reset();

        StringBuilder formattedMessage = new StringBuilder();
        String tagName = "inv";

        // Take a snapshot of the sender's inventory right now (async-safe copy)
        ItemStack[] rawContents = sender.getInventory().getContents(); // all 41 slots
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            ItemStack item = (i < rawContents.length) ? rawContents[i] : null;
            contents[i] = (item != null && !item.isEmpty()) ? item.clone() : null;
        }

        ItemStack[] rawArmor = sender.getInventory().getArmorContents();
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack item = (i < rawArmor.length) ? rawArmor[i] : null;
            armor[i] = (item != null && !item.isEmpty()) ? item.clone() : null;
        }

        ItemStack rawOffHand = sender.getInventory().getItemInOffHand();
        ItemStack offHand = (!rawOffHand.isEmpty()) ? rawOffHand.clone() : null;

        // Register snapshot and get click code
        String code = plugin.getModuleManager().getModule(ChatModule.class)
                .createHoverInventory(sender, contents, armor, offHand);

        // Build the display component
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss");
        String time = format.format(new java.util.Date());
        net.kyori.adventure.text.Component component = adventureComponent.getComponent(
                this.result.replace("%player%", sender.getName())
                        .replace("%time%", time),
                TagResolver.builder()
                        .resolver(Placeholder.parsed("player", sender.getName()))
                        .resolver(Placeholder.parsed("time", time))
                        .build()
        );
        component = component.clickEvent(
                net.kyori.adventure.text.event.ClickEvent.clickEvent(
                        net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND,
                        "/showinv " + code
                )
        );

        builder.resolver(Placeholder.component(tagName, component));

        while (matcher.find()) {
            matcher.appendReplacement(formattedMessage, "<" + tagName + ">");
        }
        matcher.appendTail(formattedMessage);

        return formattedMessage.toString();
    }

    @Override
    public boolean hasPermission(Permissible permissible) {
        return permissible.hasPermission(this.permission);
    }

    private boolean hasAnyContent(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.isEmpty()) return true;
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.isEmpty()) return true;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return !offHand.isEmpty();
    }
}
