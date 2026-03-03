package fr.maxlego08.essentials.module.modules.chat;

import com.tcoded.folialib.impl.PlatformScheduler;
import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.chat.BadWordsConfig;
import fr.maxlego08.essentials.api.chat.ChatDisplay;
import fr.maxlego08.essentials.api.utils.component.AdventureComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optimized bad words filter - uses single compiled regex pattern for maximum performance
 */
public class BadWordsFilter implements ChatDisplay {

    private final BadWordsConfig config;
    private final PlatformScheduler scheduler;
    private final Pattern filterPattern;
    private final ConcurrentHashMap<UUID, WarnEntry> warnCounts = new ConcurrentHashMap<>();

    private record WarnEntry(int count, long timestamp) {}

    public BadWordsFilter(ZEssentialsPlugin plugin, BadWordsConfig config) {
        this.config = config;
        this.scheduler = plugin.getScheduler();

        // Build optimized single regex pattern from all bad words
        if (config.badWords() != null && !config.badWords().isEmpty()) {
            StringBuilder sb = new StringBuilder("(?i)\\b(");
            for (int i = 0; i < config.badWords().size(); i++) {
                if (i > 0) sb.append('|');
                sb.append(Pattern.quote(config.badWords().get(i).toLowerCase()));
            }
            sb.append(")\\b");
            this.filterPattern = Pattern.compile(sb.toString());
        } else {
            this.filterPattern = null;
        }

        // Cleanup old warns every 5 minutes if warn system enabled
        if (config.maxWarns() > 0) {
            plugin.getScheduler().runTimerAsync(() -> {
                long cutoff = System.currentTimeMillis() - 1800000L; // 30 min
                warnCounts.entrySet().removeIf(e -> e.getValue().timestamp() < cutoff);
            }, 6000L, 6000L);
        }
    }

    @Override
    public String display(AdventureComponent adventureComponent, TagResolver.Builder builder, Player sender, Player receiver, String message) {
        // Filter the message
        String filtered = filter(message, sender);

        // Handle warn system
        if (config.maxWarns() > 0 && filtered != message) {
            processWarn(sender);
        }

        return filtered;
    }

    /**
     * Filter bad words from message without triggering warns (for Discord webhook)
     */
    public String filter(String message, Player player) {
        if (message == null || filterPattern == null) return message;

        // Bypass check
        String bypassPerm = config.bypassPermission();
        if (bypassPerm != null && !bypassPerm.isEmpty() && player != null && player.hasPermission(bypassPerm)) {
            return message;
        }

        // Single regex match - O(n) where n = message length
        Matcher matcher = filterPattern.matcher(message);
        if (!matcher.find()) return message;

        // Replace all bad words with replacement
        return matcher.replaceAll(config.replacement());
    }

    private void processWarn(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        WarnEntry entry = warnCounts.get(uuid);
        int warns = (entry == null) ? 1 : entry.count() + 1;
        warnCounts.put(uuid, new WarnEntry(warns, now));

        // Execute punishment if max reached
        if (warns >= config.maxWarns() && config.punishments() != null) {
            String playerName = player.getName();
            for (String cmd : config.punishments()) {
                String command = cmd.replace("%player%", playerName);
                scheduler.runNextTick(wrappedTask -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
            warnCounts.remove(uuid);
        }
    }

    @Override
    public boolean hasPermission(Permissible permissible) {
        return true;
    }
}
