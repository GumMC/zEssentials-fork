package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.entity.Player;

public class CommandVoteFlyOn extends VCommand {
    public CommandVoteFlyOn(EssentialsPlugin plugin) {
        super(plugin);
        this.addSubCommand("on");
        this.setPermission(Permission.ESSENTIALS_VOTEFLY);
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        Player player = this.player;
        if (player == null) return CommandResultType.SYNTAX_ERROR;

        // Conflict check
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

        player.setAllowFlight(true);
        player.setFlying(true);
        message(sender, Message.COMMAND_VOTEFLY_ENABLE);

        return CommandResultType.SUCCESS;
    }
}
