package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.entity.Player;

public class CommandVoteFlyOff extends VCommand {
    public CommandVoteFlyOff(EssentialsPlugin plugin) {
        super(plugin);
        this.addSubCommand("off");
        this.setPermission(Permission.ESSENTIALS_VOTEFLY);
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        Player player = this.player;
        if (player == null) return CommandResultType.SYNTAX_ERROR;

        player.setAllowFlight(false);
        player.setFlying(false);
        message(sender, Message.COMMAND_VOTEFLY_DISABLE);

        return CommandResultType.SUCCESS;
    }
}
