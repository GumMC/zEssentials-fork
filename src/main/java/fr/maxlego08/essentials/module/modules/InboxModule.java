package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.configuration.NonLoadable;
import fr.maxlego08.essentials.api.dto.InboxDTO;
import fr.maxlego08.essentials.api.messages.Message;

import fr.maxlego08.essentials.module.ZModule;
import fr.maxlego08.essentials.storage.database.repositeries.InboxRepository;
import fr.maxlego08.essentials.storage.storages.SqlStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InboxModule extends ZModule {

    private int maxMessageLength;
    private int messagesPerPage;
    private int rateLimitSeconds;
    private boolean enableTempMail;
    private boolean notifyOnReceive;
    private boolean notifyOnLogin;
    private int notifyOnLoginDelayTicks;
    private int cleanupIntervalMinutes;
    private String dateFormat;
    private int maxInboxSize;
    private boolean autoDeleteOldestWhenFull;
    private boolean allowSelfMail;

    @NonLoadable
    private final Map<UUID, Long> rateLimitMap = new ConcurrentHashMap<>();

    @NonLoadable
    private SimpleDateFormat simpleDateFormat;

    @NonLoadable
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([smhdw])$", Pattern.CASE_INSENSITIVE);

    public InboxModule(ZEssentialsPlugin plugin) {
        super(plugin, "inbox");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        var configuration = getConfiguration();
        this.maxMessageLength = configuration.getInt("max-message-length", 500);
        this.messagesPerPage = configuration.getInt("messages-per-page", 10);
        this.rateLimitSeconds = configuration.getInt("rate-limit-seconds", 60);
        this.enableTempMail = configuration.getBoolean("enable-temp-mail", true);
        this.notifyOnReceive = configuration.getBoolean("notify-on-receive", true);
        this.notifyOnLogin = configuration.getBoolean("notify-on-login", true);
        this.notifyOnLoginDelayTicks = configuration.getInt("notify-on-login-delay-ticks", 40);
        this.cleanupIntervalMinutes = configuration.getInt("cleanup-interval-minutes", 60);
        this.dateFormat = configuration.getString("date-format", "dd/MM/yyyy HH:mm");
        this.maxInboxSize = configuration.getInt("max-inbox-size", 100);
        this.autoDeleteOldestWhenFull = configuration.getBoolean("auto-delete-oldest-when-full", true);
        this.allowSelfMail = configuration.getBoolean("allow-self-mail", false);

        this.simpleDateFormat = new SimpleDateFormat(this.dateFormat);

        // Schedule cleanup task for expired mails
        if (this.cleanupIntervalMinutes > 0) {
            long ticks = this.cleanupIntervalMinutes * 60L * 1000L;
            this.plugin.getScheduler().runTimerAsync(wrappedTask -> cleanupExpiredMails(), ticks, ticks, TimeUnit.MILLISECONDS);
        }
    }

    private InboxRepository getRepository() {
        var storage = this.plugin.getStorageManager().getStorage();
        if (storage instanceof SqlStorage sqlStorage) {
            return sqlStorage.with(InboxRepository.class);
        }
        return null;
    }

    public void sendMail(CommandSender sender, UUID receiverUUID, String receiverName, String messageText) {
        InboxRepository repo = getRepository();
        if (repo == null) return;

        UUID senderUUID = sender instanceof Player player ? player.getUniqueId() : null;
        String senderName = sender.getName();

        // Self-mail check
        if (!this.allowSelfMail && senderUUID != null && senderUUID.equals(receiverUUID)) {
            message(sender, Message.COMMAND_INBOX_SELF_MAIL);
            return;
        }

        // Message length check
        if (messageText.length() > this.maxMessageLength) {
            message(sender, Message.COMMAND_INBOX_SEND_TOO_LONG, "%max%", this.maxMessageLength);
            return;
        }

        // Rate limit check
        if (senderUUID != null && !sender.hasPermission(Permission.ESSENTIALS_INBOX_BYPASS_RATELIMIT.asPermission())) {
            Long lastSent = this.rateLimitMap.get(senderUUID);
            if (lastSent != null) {
                long elapsed = (System.currentTimeMillis() - lastSent) / 1000;
                if (elapsed < this.rateLimitSeconds) {
                    message(sender, Message.COMMAND_INBOX_SEND_RATE_LIMIT, "%time%", (this.rateLimitSeconds - elapsed));
                    return;
                }
            }
        }

        this.plugin.getScheduler().runAsync(wrappedTask -> {

            // Inbox size check
            long totalMails = repo.countTotal(receiverUUID);
            if (this.maxInboxSize > 0 && totalMails >= this.maxInboxSize) {
                if (this.autoDeleteOldestWhenFull) {
                    repo.deleteOldest(receiverUUID);
                } else {
                    message(sender, Message.COMMAND_INBOX_FULL, "%player%", receiverName);
                    return;
                }
            }

            repo.insert(receiverUUID, senderUUID, senderName, messageText, null);

            // Update rate limit
            if (senderUUID != null) {
                this.rateLimitMap.put(senderUUID, System.currentTimeMillis());
            }

            message(sender, Message.COMMAND_INBOX_SEND_SUCCESS, "%player%", receiverName);

            // Notify online receiver
            if (this.notifyOnReceive) {
                Player receiver = Bukkit.getPlayer(receiverUUID);
                if (receiver != null && receiver.isOnline()) {
                    message(receiver, Message.COMMAND_INBOX_SEND_NOTIFY, "%sender%", senderName);
                }
            }
        });
    }

    public void sendTempMail(CommandSender sender, UUID receiverUUID, String receiverName, String durationStr, String messageText) {
        if (!this.enableTempMail) {
            message(sender, Message.COMMAND_INBOX_TEMP_DISABLED);
            return;
        }

        long durationMs = parseDuration(durationStr);
        if (durationMs <= 0) {
            message(sender, Message.COMMAND_INBOX_INVALID_DURATION, "%time%", durationStr);
            return;
        }

        InboxRepository repo = getRepository();
        if (repo == null) return;

        UUID senderUUID = sender instanceof Player player ? player.getUniqueId() : null;
        String senderName = sender.getName();

        // Self-mail check
        if (!this.allowSelfMail && senderUUID != null && senderUUID.equals(receiverUUID)) {
            message(sender, Message.COMMAND_INBOX_SELF_MAIL);
            return;
        }

        // Message length check
        if (messageText.length() > this.maxMessageLength) {
            message(sender, Message.COMMAND_INBOX_SEND_TOO_LONG, "%max%", this.maxMessageLength);
            return;
        }

        // Rate limit check
        if (senderUUID != null && !sender.hasPermission(Permission.ESSENTIALS_INBOX_BYPASS_RATELIMIT.asPermission())) {
            Long lastSent = this.rateLimitMap.get(senderUUID);
            if (lastSent != null) {
                long elapsed = (System.currentTimeMillis() - lastSent) / 1000;
                if (elapsed < this.rateLimitSeconds) {
                    message(sender, Message.COMMAND_INBOX_SEND_RATE_LIMIT, "%time%", (this.rateLimitSeconds - elapsed));
                    return;
                }
            }
        }

        Date expireAt = new Date(System.currentTimeMillis() + durationMs);

        this.plugin.getScheduler().runAsync(wrappedTask -> {

            // Inbox size check
            long totalMails = repo.countTotal(receiverUUID);
            if (this.maxInboxSize > 0 && totalMails >= this.maxInboxSize) {
                if (this.autoDeleteOldestWhenFull) {
                    repo.deleteOldest(receiverUUID);
                } else {
                    message(sender, Message.COMMAND_INBOX_FULL, "%player%", receiverName);
                    return;
                }
            }

            repo.insert(receiverUUID, senderUUID, senderName, messageText, expireAt);

            if (senderUUID != null) {
                this.rateLimitMap.put(senderUUID, System.currentTimeMillis());
            }

            message(sender, Message.COMMAND_INBOX_SEND_SUCCESS, "%player%", receiverName);

            if (this.notifyOnReceive) {
                Player receiver = Bukkit.getPlayer(receiverUUID);
                if (receiver != null && receiver.isOnline()) {
                    message(receiver, Message.COMMAND_INBOX_SEND_NOTIFY, "%sender%", senderName);
                }
            }
        });
    }

    public void sendAllMail(CommandSender sender, String messageText) {
        InboxRepository repo = getRepository();
        if (repo == null) return;

        if (messageText.length() > this.maxMessageLength) {
            message(sender, Message.COMMAND_INBOX_SEND_TOO_LONG, "%max%", this.maxMessageLength);
            return;
        }

        message(sender, Message.COMMAND_INBOX_SENDALL_START);

        UUID senderUUID = sender instanceof Player player ? player.getUniqueId() : null;
        String senderName = sender.getName();

        this.plugin.getScheduler().runAsync(wrappedTask -> {
            int count = 0;

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                UUID receiverUUID = onlinePlayer.getUniqueId();
                if (senderUUID != null && senderUUID.equals(receiverUUID)) continue;

                repo.insert(receiverUUID, senderUUID, senderName, messageText, null);
                count++;

                if (this.notifyOnReceive) {
                    message(onlinePlayer, Message.COMMAND_INBOX_SEND_NOTIFY, "%sender%", senderName);
                }
            }

            message(sender, Message.COMMAND_INBOX_SENDALL_COMPLETE, "%count%", count);
        });
    }

    public void readInbox(Player player, int page) {
        InboxRepository repo = getRepository();
        if (repo == null) return;

        UUID playerUUID = player.getUniqueId();

        this.plugin.getScheduler().runAsync(wrappedTask -> {
            List<InboxDTO> allMails = repo.selectByReceiver(playerUUID);

            if (allMails.isEmpty()) {
                message(player, Message.COMMAND_INBOX_EMPTY);
                return;
            }

            int totalMails = allMails.size();
            int maxPage = (int) Math.ceil((double) totalMails / this.messagesPerPage);

            if (page < 1 || page > maxPage) {
                message(player, Message.COMMAND_INBOX_INVALID_PAGE);
                return;
            }

            long unreadCount = allMails.stream().filter(m -> !m.is_read()).count();

            // Header
            message(player, Message.COMMAND_INBOX_INFORMATION_MULTI_LINE_HEADER,
                    "%count%", totalMails,
                    "%unread%", unreadCount,
                    "%page%", page,
                    "%maxPage%", maxPage);

            // Content
            int startIndex = (page - 1) * this.messagesPerPage;
            int endIndex = Math.min(startIndex + this.messagesPerPage, totalMails);

            for (int i = startIndex; i < endIndex; i++) {
                InboxDTO mail = allMails.get(i);
                String dateStr = this.simpleDateFormat.format(mail.created_at());
                Message msgType = mail.is_read()
                        ? Message.COMMAND_INBOX_INFORMATION_MULTI_LINE_CONTENT
                        : Message.COMMAND_INBOX_INFORMATION_MULTI_LINE_CONTENT_UNREAD;

                message(player, msgType,
                        "%date%", dateStr,
                        "%sender%", mail.sender_name(),
                        "%message%", mail.message());
            }

            // Footer
            message(player, Message.COMMAND_INBOX_INFORMATION_MULTI_LINE_FOOTER);

            // Mark all as read after viewing
            repo.markAllAsRead(playerUUID);
        });
    }

    public void clearInbox(Player player) {
        InboxRepository repo = getRepository();
        if (repo == null) return;

        this.plugin.getScheduler().runAsync(wrappedTask -> {
            repo.deleteByReceiver(player.getUniqueId());
            message(player, Message.COMMAND_INBOX_CLEAR_SUCCESS);
        });
    }

    public void clearInboxOther(CommandSender sender, UUID targetUUID, String targetName) {
        InboxRepository repo = getRepository();
        if (repo == null) return;

        this.plugin.getScheduler().runAsync(wrappedTask -> {
            repo.deleteByReceiver(targetUUID);
            message(sender, Message.COMMAND_INBOX_CLEAR_OTHER_SUCCESS, "%player%", targetName);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.notifyOnLogin) return;

        Player player = event.getPlayer();
        InboxRepository repo = getRepository();
        if (repo == null) return;

        // Delay thông báo để gửi sau join motd, tránh lag
        this.plugin.getScheduler().runLater(() -> {
            if (!player.isOnline()) return;

            this.plugin.getScheduler().runAsync(asyncTask -> {
                long unreadCount = repo.countUnread(player.getUniqueId());
                if (unreadCount > 0) {
                    message(player, Message.COMMAND_INBOX_LOGIN_NOTIFY, "%count%", unreadCount);
                }
            });
        }, this.notifyOnLoginDelayTicks);
    }

    private void cleanupExpiredMails() {
        InboxRepository repo = getRepository();
        if (repo != null) {
            repo.deleteExpired();
        }
    }

    /**
     * Parses a duration string like "1h", "7d", "30m", "24h", "1w" into milliseconds.
     */
    private long parseDuration(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input);
        if (!matcher.matches()) return -1;

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        return switch (unit) {
            case "s" -> value * 1000;
            case "m" -> value * 60 * 1000;
            case "h" -> value * 60 * 60 * 1000;
            case "d" -> value * 24 * 60 * 60 * 1000;
            case "w" -> value * 7 * 24 * 60 * 60 * 1000;
            default -> -1;
        };
    }
}
