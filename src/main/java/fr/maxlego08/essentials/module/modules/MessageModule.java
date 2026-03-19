package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.chat.ChatResult;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.server.EssentialsServer;
import fr.maxlego08.essentials.api.storage.IStorage;
import fr.maxlego08.essentials.api.user.Option;
import fr.maxlego08.essentials.api.user.PrivateMessage;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.module.ZModule;
import fr.maxlego08.essentials.module.modules.chat.ChatModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class MessageModule extends ZModule {

    private boolean soundEnable;
    private boolean soundReceiveEnable;
    private String soundReceiveType;
    private float soundReceiveVolume;
    private float soundReceivePitch;

    private boolean soundSendEnable;
    private String soundSendType;
    private float soundSendVolume;
    private float soundSendPitch;

    public MessageModule(ZEssentialsPlugin plugin) {
        super(plugin, "messages");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        var configuration = this.getConfiguration();
        this.soundEnable = configuration.getBoolean("sound.enable", true);
        this.soundReceiveEnable = configuration.getBoolean("sound.receive.enable", true);
        this.soundReceiveType = configuration.getString("sound.receive.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        this.soundReceiveVolume = (float) configuration.getDouble("sound.receive.volume", 1.0);
        this.soundReceivePitch = (float) configuration.getDouble("sound.receive.pitch", 1.0);

        this.soundSendEnable = configuration.getBoolean("sound.send.enable", true);
        this.soundSendType = configuration.getString("sound.send.sound", "ENTITY_PLAYER_LEVELUP");
        this.soundSendVolume = (float) configuration.getDouble("sound.send.volume", 0.5);
        this.soundSendPitch = (float) configuration.getDouble("sound.send.pitch", 2.0);
    }

    public boolean isSoundEnable() { return soundEnable; }
    public boolean isSoundReceiveEnable() { return soundReceiveEnable; }
    public String getSoundReceiveType() { return soundReceiveType; }
    public float getSoundReceiveVolume() { return soundReceiveVolume; }
    public float getSoundReceivePitch() { return soundReceivePitch; }

    public boolean isSoundSendEnable() { return soundSendEnable; }
    public String getSoundSendType() { return soundSendType; }
    public float getSoundSendVolume() { return soundSendVolume; }
    public float getSoundSendPitch() { return soundSendPitch; }

    protected boolean isVanished(UUID uuid, Map<Option, Boolean> options) {
        Player player = Bukkit.getPlayer(uuid);
        return player == null ? options.getOrDefault(Option.VANISH, false) : isVanished(player);
    }

    public void sendMessage(User user, UUID receiverUUID, String userName, String message) {

        EssentialsServer essentialsServer = this.plugin.getEssentialsServer();
        IStorage iStorage = this.plugin.getStorageManager().getStorage();

        if (user.getUniqueId().equals(receiverUUID)) {
            message(user, Message.COMMAND_MESSAGE_SELF);
            return;
        }

        Map<Option, Boolean> options = iStorage.getOptions(receiverUUID);
        if (options == null) options = new java.util.HashMap<>();
        User targetUser = iStorage.getUser(receiverUUID);

        // Vanish check
        if (isVanished(receiverUUID, options)) {
            message(user, Message.PLAYER_NOT_FOUND, "%player%", userName);
            return;
        }

        if (options.getOrDefault(Option.PRIVATE_MESSAGE_DISABLE, false)) {
            message(user, Message.COMMAND_MESSAGE_DISABLE, "%player%", userName);
            return;
        }

        if (targetUser != null && targetUser.isAfk()) {
            message(user, Message.COMMAND_MESSAGE_TARGET_AFK, "%player%", userName);
        }

        if (user.isMute()) {
            message(user, Message.COMMAND_MESSAGE_MUTE);
            return;
        }

        ChatModule chatModule = plugin.getModuleManager().getModule(ChatModule.class);
        ChatResult chatResult = chatModule.analyzeMessage(user, message);
        if (!chatResult.isValid()) {
            message(user, chatResult.message(), chatResult.arguments());
            return;
        }

        PrivateMessage privateMessage = user.setPrivateMessage(receiverUUID, userName);
        this.plugin.getUtils().sendPrivateMessage(user, privateMessage, Message.COMMAND_MESSAGE_ME, message);
        essentialsServer.sendPrivateMessage(user, privateMessage, message);
        essentialsServer.broadcastMessage(Option.SOCIAL_SPY, Message.COMMAND_MESSAGE_SOCIAL_SPY, "%sender%", user.getName(), "%receiver%", userName, "%message%", message);

        iStorage.insertPrivateMessage(user.getUniqueId(), receiverUUID, message);
    }
}
