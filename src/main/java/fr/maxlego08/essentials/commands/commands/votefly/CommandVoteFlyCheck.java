package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.TimerBuilder;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandVoteFlyCheck extends VCommand {
    public CommandVoteFlyCheck(EssentialsPlugin plugin) {
        super(plugin);
        this.addSubCommand("check");
        this.setPermission(Permission.ESSENTIALS_VOTEFLY);
        this.setDescription(Message.DESCRIPTION_VOTEFLY);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        long voteFlySeconds = user.getVoteFlySeconds();
        boolean isActive = player.isFlying() && !player.hasPermission(Permission.ESSENTIALS_FLY_UNLIMITED.asPermission());

        if (isActive) {
            message(sender, Message.COMMAND_VOTEFLY_ACTIVE, "%time%", TimerBuilder.getStringTime(voteFlySeconds * 1000));
        } else {
            message(sender, Message.COMMAND_VOTEFLY_INACTIVE, "%time%", TimerBuilder.getStringTime(voteFlySeconds * 1000));
        }

        return CommandResultType.SUCCESS;
    }
}
