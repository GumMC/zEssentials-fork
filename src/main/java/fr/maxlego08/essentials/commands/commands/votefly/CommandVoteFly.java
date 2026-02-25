package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandVoteFly extends VCommand {
    public CommandVoteFly(EssentialsPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ESSENTIALS_VOTEFLY);
        this.setDescription(Message.DESCRIPTION_VOTEFLY);
        this.onlyPlayers();
        this.addSubCommand(new CommandVoteFlyAdd(plugin));
        this.addSubCommand(new CommandVoteFlyRemove(plugin));
        this.addSubCommand(new CommandVoteFlySet(plugin));
        this.addSubCommand(new CommandVoteFlyGet(plugin));
        this.addSubCommand(new CommandVoteFlyCheck(plugin));
        this.addOptionalArg("action", (sender, args) -> java.util.Arrays.asList("on", "off", "check"));
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        String arg = this.argAsString(0);

        // Handle check command
        if ("check".equalsIgnoreCase(arg)) {
            long time = user.getVoteFlySeconds();
            message(sender, Message.COMMAND_VOTEFLY_CHECK, "%time%", formatTime(time));
            return CommandResultType.SUCCESS;
        }

        // Handle explicit on/off commands
        if ("on".equalsIgnoreCase(arg)) {
            if (player.hasPermission(Permission.ESSENTIALS_FLY_UNLIMITED.asPermission())) {
                message(sender, Message.COMMAND_VOTEFLY_DISABLED_BY_FLY);
                return CommandResultType.DEFAULT;
            }

            if (user.getVoteFlySeconds() <= 0) {
                message(sender, Message.COMMAND_VOTEFLY_NO_TIME);
                return CommandResultType.DEFAULT;
            }

            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(true);
                message(sender, Message.COMMAND_VOTEFLY_ENABLE);
            }
            return CommandResultType.SUCCESS;
        }

        if ("off".equalsIgnoreCase(arg)) {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                message(sender, Message.COMMAND_VOTEFLY_DISABLE);
            }
            return CommandResultType.SUCCESS;
        }

        // Default: toggle behavior when no args or unknown arg
        if (arg != null) {
            message(sender, Message.COMMAND_SYNTAX_ERROR, "%syntax%", "/votefly [on|off|check]");
            return CommandResultType.DEFAULT;
        }

        // Block if normal fly (unlimited) is active
        if (player.hasPermission(Permission.ESSENTIALS_FLY_UNLIMITED.asPermission())) {
            message(sender, Message.COMMAND_VOTEFLY_DISABLED_BY_FLY);
            return CommandResultType.DEFAULT;
        }

        if (user.getVoteFlySeconds() <= 0) {
            message(sender, Message.COMMAND_VOTEFLY_NO_TIME);
            return CommandResultType.DEFAULT;
        }

        player.setAllowFlight(!player.getAllowFlight());
        player.setFlying(player.getAllowFlight());
        message(sender, player.getAllowFlight() ? Message.COMMAND_VOTEFLY_ENABLE : Message.COMMAND_VOTEFLY_DISABLE);

        return CommandResultType.SUCCESS;
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "0s";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("s");

        return sb.toString().trim();
    }
}
