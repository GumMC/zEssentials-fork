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
        this.addOptionalArg("player");
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        String userName = this.argAsString(0, null);

        if (userName == null) {
            // Self check
            if (this.player == null) return CommandResultType.SYNTAX_ERROR;

            var user = plugin.getUser(this.player.getUniqueId());
            if (user == null) return CommandResultType.SYNTAX_ERROR;

            long flySeconds = user.getFlySeconds();
            String time = TimerBuilder.getStringTime(flySeconds * 1000);

            if (this.player.getAllowFlight() && !this.player.hasPermission(Permission.ESSENTIALS_FLY_UNLIMITED.asPermission())) {
                message(sender, Message.COMMAND_VOTEFLY_ACTIVE, "%time%", time);
            } else {
                message(sender, Message.COMMAND_VOTEFLY_INACTIVE, "%time%", time);
            }

            return CommandResultType.SUCCESS;
        }

        // Check other player - requires ESSENTIALS_VOTEFLY_GET permission
        if (!hasPermission(sender, Permission.ESSENTIALS_VOTEFLY_GET)) {
            return CommandResultType.NO_PERMISSION;
        }

        fetchUniqueId(userName, uniqueId -> {
            var storage = plugin.getStorageManager().getStorage();
            var user = plugin.getUser(uniqueId);
            long flySeconds = user == null ? storage.getFlySeconds(uniqueId) : user.getFlySeconds();

            message(sender, Message.COMMAND_VOTEFLY_GET, "%player%", userName, "%time%", TimerBuilder.getStringTime(flySeconds * 1000));
        });

        return CommandResultType.SUCCESS;
    }
}
