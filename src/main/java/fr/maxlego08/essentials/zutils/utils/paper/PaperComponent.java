package fr.maxlego08.essentials.zutils.utils.paper;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.cache.SimpleCache;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.messages.messages.BossBarMessage;
import fr.maxlego08.essentials.api.messages.messages.TitleMessage;
import fr.maxlego08.essentials.api.utils.TagPermission;
import fr.maxlego08.essentials.api.utils.component.AdventureComponent;
import fr.maxlego08.essentials.zutils.utils.BossBarAnimation;
import fr.maxlego08.essentials.zutils.utils.MessageUtils;
import fr.maxlego08.essentials.zutils.utils.PlaceholderUtils;
import fr.maxlego08.menu.api.utils.Placeholders;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PaperComponent extends PlaceholderUtils implements AdventureComponent {

    private final List<TagPermission> tagPermissions = List.of(
            // Chat
            new TagPermission(Permission.ESSENTIALS_CHAT_COLOR, StandardTags.color()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_CLICK, StandardTags.clickEvent()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_HOVER, StandardTags.hoverEvent()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_GRADIENT, StandardTags.gradient()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_RAINBOW, StandardTags.rainbow()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_NEWLINE, StandardTags.newline()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_RESET, StandardTags.reset()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_FONT, StandardTags.font()), //
            new TagPermission(Permission.ESSENTIALS_CHAT_KEYBIND, StandardTags.keybind()),  //
            new TagPermission(Permission.ESSENTIALS_CHAT_DECORATION, StandardTags.decorations()), //
            // Sign
            new TagPermission(Permission.ESSENTIALS_SIGN_COLOR, StandardTags.color()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_CLICK, StandardTags.clickEvent()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_HOVER, StandardTags.hoverEvent()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_GRADIENT, StandardTags.gradient()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_RAINBOW, StandardTags.rainbow()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_NEWLINE, StandardTags.newline()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_RESET, StandardTags.reset()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_FONT, StandardTags.font()), //
            new TagPermission(Permission.ESSENTIALS_SIGN_KEYBIND, StandardTags.keybind()),  //
            new TagPermission(Permission.ESSENTIALS_SIGN_DECORATION, StandardTags.decorations()) //
    );

    private final MiniMessage defaultMiniMessage;
    private final SimpleCache<String, Component> cache = new SimpleCache<>();

    public PaperComponent() {
        // Pre-build MiniMessage with defaults for better performance
        this.defaultMiniMessage = MiniMessage.builder()
                .tags(TagResolver.builder().resolver(StandardTags.defaults()).build())
                .build();
    }

    private TextDecoration.State getState(String text) {
        return text.contains("&o") || text.contains("<i>") || text.contains("<em>") || text.contains("<italic>") ? TextDecoration.State.TRUE : TextDecoration.State.FALSE;
    }

    private void updateDisplayName(ItemMeta itemMeta, String text) {
        Component component = this.cache.get(text, () -> {
            return this.defaultMiniMessage.deserialize(colorMiniMessage(text)).decoration(TextDecoration.ITALIC, getState(text)); // We will force the italics in false, otherwise it will activate for no reason
        });
        itemMeta.displayName(component);
    }

    public void updateDisplayName(ItemMeta itemMeta, String text, Player player) {
        updateDisplayName(itemMeta, papi(text, player));
    }

    public void updateLore(ItemMeta itemMeta, List<String> lore, Player offlinePlayer) {
        update(itemMeta, lore, offlinePlayer);
    }

    public void update(ItemMeta itemMeta, List<String> lore, Player offlinePlayer) {
        List<Component> components = lore.stream().map(text -> {
            String result = papi(text, offlinePlayer);
            return this.cache.get(result, () -> {
                return this.defaultMiniMessage.deserialize(colorMiniMessage(result)).decoration(TextDecoration.ITALIC, getState(result)); // We will force the italics in false, otherwise it will activate for no reason
            });
        }).collect(Collectors.toList());
        itemMeta.lore(components);
    }

    @Override
    public Inventory createInventory(String inventoryName, int size, InventoryHolder inventoryHolder) {
        Component component = this.cache.get(inventoryName, () -> this.defaultMiniMessage.deserialize(colorMiniMessage(inventoryName)));
        return Bukkit.createInventory(inventoryHolder, size, component);
    }

    // Converts legacy hex §x§R§G§B§r§g§b → <#RRGGBB>
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile(
            "\u00a7x\u00a7([0-9a-fA-F])\u00a7([0-9a-fA-F])\u00a7([0-9a-fA-F])\u00a7([0-9a-fA-F])\u00a7([0-9a-fA-F])\u00a7([0-9a-fA-F])"
    );

    // Converts bare #RRGGBB → <#RRGGBB>, skips already-wrapped ones
    private static final Pattern BARE_HEX_PATTERN = Pattern.compile("(?<!<)(?<!:)#([a-fA-F0-9]{6})");

    // Single pattern to replace all & and § color codes at once - MUCH faster
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("[&§]([0-9a-fA-Fk-lm-no-r])", Pattern.CASE_INSENSITIVE);

    // Pre-build lookup table for color codes
    private static final Map<String, String> COLOR_CODE_MAP;
    static {
        COLOR_CODE_MAP = Map.ofEntries(
                Map.entry("0", "black"), Map.entry("1", "dark_blue"), Map.entry("2", "dark_green"),
                Map.entry("3", "dark_aqua"), Map.entry("4", "dark_red"), Map.entry("5", "dark_purple"),
                Map.entry("6", "gold"), Map.entry("7", "gray"), Map.entry("8", "dark_gray"),
                Map.entry("9", "blue"), Map.entry("a", "green"), Map.entry("b", "aqua"),
                Map.entry("c", "red"), Map.entry("d", "light_purple"), Map.entry("e", "yellow"),
                Map.entry("f", "white"), Map.entry("k", "obfuscated"), Map.entry("l", "bold"),
                Map.entry("m", "strikethrough"), Map.entry("n", "underlined"), Map.entry("o", "italic"),
                Map.entry("r", "reset")
        );
    }

    private String colorMiniMessage(String message) {
        if (message == null || message.isEmpty()) return message;

        // Step 1: §x§f§a§5§1§7§d → <#FA517D> (must run before §-code replacements)
        message = LEGACY_HEX_PATTERN.matcher(message).replaceAll("<#$1$2$3$4$5$6>");

        // Step 2: bare #RRGGBB → <#RRGGBB>
        Matcher hexMatcher = BARE_HEX_PATTERN.matcher(message);
        if (!hexMatcher.find()) {
            // No bare hex found, skip StringBuilder
            return convertColorCodes(message);
        }
        // Reset and process with StringBuilder
        hexMatcher.reset();
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) hexMatcher.appendReplacement(sb, "<$0>");
        hexMatcher.appendTail(sb);
        return convertColorCodes(sb.toString());
    }

    // Single regex pass for all color codes - O(n) instead of O(72n)
    private String convertColorCodes(String message) {
        Matcher matcher = COLOR_CODE_PATTERN.matcher(message);
        if (!matcher.find()) return message;

        matcher.reset();
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String code = matcher.group(1).toLowerCase();
            String replacement = COLOR_CODE_MAP.get(code);
            if (replacement != null) {
                matcher.appendReplacement(sb, "<" + replacement + ">");
            } else {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    @Override
    public Component getComponent(String message) {
        return this.cache.get(message, () -> this.defaultMiniMessage.deserialize(colorMiniMessage(message)));
    }

    @Override
    public Component getComponent(String message, TagResolver tagResolver) {
        return this.defaultMiniMessage.deserialize(colorMiniMessage(message), tagResolver);
    }

    @Override
    public BossBar createBossBar(String message, BossBar.Color barColor, BossBar.Overlay barStyle) {
        return BossBar.bossBar(getComponent(message), 0, barColor, barStyle);
    }

    private TagResolver getTagResolver(Player player, TagResolver... tagResolvers) {
        TagResolver.Builder builder = TagResolver.builder();

        if (!player.isOp()) {

            Set<TagResolver> resolvers = this.tagPermissions.stream().filter(tagPermission -> player.hasPermission(tagPermission.permission().asPermission())).map(TagPermission::tagResolver).collect(Collectors.toSet());
            builder.resolvers(resolvers);
        } else {

            builder.resolver(StandardTags.defaults());
        }

        builder.resolvers(tagResolvers);
        return builder.build();
    }

    public Component translateText(Player player, String message, TagResolver... tagResolvers) {
        var tagResolver = getTagResolver(player, tagResolvers);
        return MiniMessage.builder().tags(tagResolver).build().deserialize(colorMiniMessage(message));
    }

    @Override
    public void sendActionBar(Player sender, String message) {
        Component component = this.cache.get(message, () -> this.defaultMiniMessage.deserialize(colorMiniMessage(message)));
        sender.sendActionBar(component);
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            sender.sendMessage(this.defaultMiniMessage.deserialize(colorMiniMessage(papi(message, player))));
        } else {
            Component component = this.cache.get(message, () -> this.defaultMiniMessage.deserialize(colorMiniMessage(message)));
            sender.sendMessage(component);
        }
    }

    public Component getComponentMessage(Message message, Object... args) {
        List<String> strings = message.getMessageAsStringList();
        if (!strings.isEmpty()) {
            TextComponent.Builder component = Component.text();
            strings.forEach(currentMessage -> {
                component.append(getComponent(getMessage(currentMessage, args)));
                component.append(Component.text("\n"));
            });
            return component.build();
        }
        return getComponent(getMessage(message.getMessageAsString(), args));
    }

    public Component getComponentMessage(String message, TagResolver tagResolver, Object... args) {
        return getComponent(getMessage(message, args), tagResolver);
    }

    protected String getMessage(String message, Object... args) {
        return MessageUtils.getString(message, args);
    }

    @Override
    public void addToLore(ItemStack itemStack, List<String> lore, Placeholders placeholders) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<Component> currentLore = itemMeta.hasLore() ? itemMeta.lore() : new ArrayList<>();
        if (currentLore == null) currentLore = new ArrayList<>();
        currentLore.addAll(lore.stream().map(placeholders::parse).map(this::getComponent).map(e -> e.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList());
        itemMeta.lore(currentLore);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public void sendTitle(Player player, TitleMessage titleMessage, Object... args) {

        Component title = getComponent(papi(getMessage(titleMessage.title(), args), player));
        Component subtitle = getComponent(papi(getMessage(titleMessage.subtitle(), args), player));

        player.showTitle(Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(titleMessage.start()), Duration.ofMillis(titleMessage.time()), Duration.ofMillis(titleMessage.end()))));
    }

    @Override
    public void sendBossBar(EssentialsPlugin plugin, Player player, BossBarMessage bossBarMessage) {
        BossBar bossBar = BossBar.bossBar(getComponent(papi(bossBarMessage.text(), player)), 1f, bossBarMessage.getColor(), bossBarMessage.getOverlay(), bossBarMessage.getFlags());
        player.showBossBar(bossBar);

        new BossBarAnimation(plugin, player, bossBar, bossBarMessage.duration());
    }

    @Override
    public void kick(Player player, String message) {
        player.kick(getComponent(message));
    }

    @Override
    public String getItemStackName(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) return "";
        var meta = itemStack.getItemMeta();
        if (!meta.hasDisplayName()) return "";
        return PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));
    }

    @Override
    public void changeSignColor(SignChangeEvent event) {
        var player = event.getPlayer();

        var miniMessage = MiniMessage.builder().tags(getTagResolver(player)).build();
        var plainTextSerializer = PlainTextComponentSerializer.plainText();

        for (int i = 0; i < event.lines().size(); i++) {
            var line = event.line(i);
            if (line == null) continue;

            var plainText = plainTextSerializer.serialize(line);
            event.line(i, miniMessage.deserialize(colorMiniMessage(plainText)));
        }
    }
}
