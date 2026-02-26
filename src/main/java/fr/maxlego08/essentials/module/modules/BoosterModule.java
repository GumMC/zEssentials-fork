package fr.maxlego08.essentials.module.modules;

import fr.maxlego08.essentials.ZEssentialsPlugin;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.ZModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BoosterModule extends ZModule {

    // Config fields
    private String timeFormat;

    // AxBoosters API classes (reflection-based to avoid hard dependency)
    private Class<?> boosterManagerClass;
    private Class<?> activeBoosterClass;
    private Class<?> boosterClass;
    private Class<?> boosterTypeClass;
    private Class<?> audienceClass;
    private boolean axBoostersAvailable = false;

    // Cached reflection methods for performance optimization
    private Method getAllBoostersMethod;
    private Method getBoosterFromActiveMethod;
    private Method getAudienceMethod;
    private Method getTypeMethod;
    private Method getDisplayNameMethod;
    private Method getMultiplierMethod;
    private Method getStartedMethod;
    private Method getLengthMethod;

    // Cached enum values
    private Object personalAudience;
    private Object globalAudience;

    public BoosterModule(ZEssentialsPlugin plugin) {
        super(plugin, "booster");
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();

        if (!this.isEnable()) return;

        // Load config values
        YamlConfiguration config = getConfiguration();
        this.timeFormat = config.getString("time-format", "short");

        // Check if AxBoosters plugin is loaded
        Plugin axBoostersPlugin = Bukkit.getPluginManager().getPlugin("AxBoosters");
        if (axBoostersPlugin == null || !axBoostersPlugin.isEnabled()) {
            plugin.getLogger().warning("BoosterModule requires AxBoosters plugin. Module disabled.");
            this.isEnable = false;
            return;
        }

        // Try to load AxBoosters API classes via reflection using plugin's class loader
        try {
            ClassLoader pluginClassLoader = axBoostersPlugin.getClass().getClassLoader();

            this.boosterManagerClass = Class.forName("com.artillexstudios.axboosters.boosters.BoosterManager", true, pluginClassLoader);
            this.activeBoosterClass = Class.forName("com.artillexstudios.axboosters.boosters.types.activated.ActiveBooster", true, pluginClassLoader);
            this.boosterClass = Class.forName("com.artillexstudios.axboosters.boosters.types.activated.Booster", true, pluginClassLoader);
            this.boosterTypeClass = Class.forName("com.artillexstudios.axboosters.boosters.BoosterType", true, pluginClassLoader);
            this.audienceClass = Class.forName("com.artillexstudios.axboosters.enums.Audience", true, pluginClassLoader);

            // Cache all reflection methods - only lookup once during initialization
            this.getAllBoostersMethod = boosterManagerClass.getMethod("getAllBoosters", Player.class);
            this.getBoosterFromActiveMethod = activeBoosterClass.getMethod("getBooster");
            this.getAudienceMethod = boosterClass.getMethod("getAudience");
            this.getTypeMethod = activeBoosterClass.getMethod("getType");
            this.getDisplayNameMethod = boosterTypeClass.getMethod("getDisplayName");
            this.getMultiplierMethod = boosterClass.getMethod("getMultiplier");
            this.getStartedMethod = activeBoosterClass.getMethod("getStarted");
            this.getLengthMethod = boosterClass.getMethod("getLength");

            // Cache enum values - they never change
            @SuppressWarnings("unchecked")
            Class<Enum> audienceEnumClass = (Class<Enum>) audienceClass;
            this.personalAudience = Enum.valueOf(audienceEnumClass, "PERSONAL");
            this.globalAudience = Enum.valueOf(audienceEnumClass, "GLOBAL");

            this.axBoostersAvailable = true;
            plugin.getLogger().info("Booster module loaded with AxBoosters integration!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("AxBoosters API classes not found. Make sure AxBoosters plugin is up to date. Module disabled.");
            plugin.getLogger().warning("Missing class: " + e.getMessage());
            this.isEnable = false;
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("AxBoosters API method not found. Make sure AxBoosters plugin is up to date. Module disabled.");
            plugin.getLogger().warning("Missing method: " + e.getMessage());
            this.isEnable = false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize AxBoosters integration: " + e.getMessage());
            e.printStackTrace();
            this.isEnable = false;
        }
    }

    public void displayBoosters(Player player) {
        if (!this.isEnable() || !axBoostersAvailable) {
            message(player, Message.COMMAND_BOOSTER_PLUGIN_NOT_FOUND);
            return;
        }

        try {
            // Get all active boosters for player using cached method
            @SuppressWarnings("unchecked")
            ArrayList<Object> activeBoosters = (ArrayList<Object>) getAllBoostersMethod.invoke(null, player);

            if (activeBoosters == null || activeBoosters.isEmpty()) {
                message(player, Message.COMMAND_BOOSTER_EMPTY);
                return;
            }

            // Separate PERSONAL and GLOBAL boosters using cached enum values
            List<Object> personalBoosters = new ArrayList<>();
            List<Object> globalBoosters = new ArrayList<>();

            for (Object activeBooster : activeBoosters) {
                Object booster = getBoosterFromActiveMethod.invoke(activeBooster);
                Object audience = getAudienceMethod.invoke(booster);

                if (audience.equals(personalAudience)) {
                    personalBoosters.add(activeBooster);
                } else if (audience.equals(globalAudience)) {
                    globalBoosters.add(activeBooster);
                }
            }

            int totalCount = activeBoosters.size();

            // Display header
            message(player, Message.COMMAND_BOOSTER_INFORMATION_MULTI_LINE_HEADER, "%count%", totalCount);

            // Display PERSONAL boosters
            for (Object activeBooster : personalBoosters) {
                displayBooster(player, activeBooster);
            }

            // Display separator if both types exist
            if (!personalBoosters.isEmpty() && !globalBoosters.isEmpty()) {
                message(player, Message.COMMAND_BOOSTER_INFORMATION_MULTI_LINE_SEPARATOR);
            }

            // Display GLOBAL boosters
            for (Object activeBooster : globalBoosters) {
                displayBooster(player, activeBooster);
            }

            // Display footer
            message(player, Message.COMMAND_BOOSTER_INFORMATION_MULTI_LINE_FOOTER, "%count%", totalCount);

        } catch (Exception e) {
            plugin.getLogger().severe("Error displaying boosters: " + e.getMessage());
            e.printStackTrace();
            message(player, Message.COMMAND_BOOSTER_PLUGIN_NOT_FOUND);
        }
    }

    private void displayBooster(Player player, Object activeBooster) {
        try {
            Object boosterType = getTypeMethod.invoke(activeBooster);
            String displayName = (String) getDisplayNameMethod.invoke(boosterType);
            Object booster = getBoosterFromActiveMethod.invoke(activeBooster);
            float multiplier = (Float) getMultiplierMethod.invoke(booster);

            String multiplierFormatted = String.format("%.1fx", multiplier);

            long started = (Long) getStartedMethod.invoke(activeBooster);
            int length = (Integer) getLengthMethod.invoke(booster);

            long remaining = (started + length) - System.currentTimeMillis();
            if (remaining < 0) remaining = 0;

            String remainingFormatted = formatRemainingTime(remaining);

            message(player, Message.COMMAND_BOOSTER_INFORMATION_MULTI_LINE_CONTENT,
                    "%name%", displayName,
                    "%multiplier%", multiplierFormatted,
                    "%remaining%", remainingFormatted);

        } catch (Exception e) {
            plugin.getLogger().warning("Error displaying booster: " + e.getMessage());
        }
    }

    private String formatRemainingTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "Expired";
        }

        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if ("long".equals(timeFormat)) {
            List<String> parts = new ArrayList<>();
            if (days > 0) parts.add(days + (days == 1 ? " day" : " days"));
            if (hours > 0) parts.add(hours + (hours == 1 ? " hour" : " hours"));
            if (minutes > 0) parts.add(minutes + (minutes == 1 ? " minute" : " minutes"));
            if (seconds > 0 || parts.isEmpty()) parts.add(seconds + (seconds == 1 ? " second" : " seconds"));
            return String.join(" ", parts);
        } else {
            List<String> parts = new ArrayList<>();
            if (days > 0) parts.add(days + "d");
            if (hours > 0) parts.add(hours + "h");
            if (minutes > 0) parts.add(minutes + "m");
            if (seconds > 0 || parts.isEmpty()) parts.add(seconds + "s");
            return String.join(" ", parts);
        }
    }
}
