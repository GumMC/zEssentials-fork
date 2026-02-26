package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.entity.Player;

public class CommandVoteFly extends VCommand {
    public CommandVoteFly(EssentialsPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ESSENTIALS_VOTEFLY);
        this.setDescription(Message.DESCRIPTION_VOTEFLY);
        this.addSubCommand(new CommandVoteFlyOn(plugin));
        this.addSubCommand(new CommandVoteFlyOff(plugin));
        this.addSubCommand(new CommandVoteFlyCheck(plugin));
        this.addSubCommand(new CommandVoteFlyAdd(plugin));
        this.addSubCommand(new CommandVoteFlyTake(plugin));
        this.addSubCommand(new CommandVoteFlySet(plugin));
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        Player player = this.player;
        if (player == null) return CommandResultType.SYNTAX_ERROR;

        // Conflict check: if player has fly unlimited AND fly is currently enabled
        if (player.hasPermission(Permission.ESSENTIALS_FLY_UNLIMITED.asPermission()) && player.getAllowFlight()) {
            message(sender, Message.COMMAND_VOTEFLY_DISABLED_BY_FLY);
            return CommandResultType.DEFAULT;
        }

        User user = plugin.getUser(player.getUniqueId());
        if (user == null) return CommandResultType.SYNTAX_ERROR;

        if (user.getFlySeconds() <= 0) {
            message(sender, Message.COMMAND_VOTEFLY_NO_TIME);
            return CommandResultType.DEFAULT;
        }

        // Toggle flight
        boolean newState = !player.getAllowFlight();
        player.setAllowFlight(newState);
        player.setFlying(newState);
        message(sender, newState ? Message.COMMAND_VOTEFLY_ENABLE : Message.COMMAND_VOTEFLY_DISABLE);

        return CommandResultType.SUCCESS;
    }
}
