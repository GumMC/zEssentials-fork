package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.TimerBuilder;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandVoteFlyGet extends VCommand {
    public CommandVoteFlyGet(EssentialsPlugin plugin) {
        super(plugin);
        this.addSubCommand("get");
        this.setPermission(Permission.ESSENTIALS_VOTEFLY_GET);
        this.setDescription(Message.DESCRIPTION_VOTEFLY_GET);
        this.addRequireOfflinePlayerNameArg();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        String userName = this.argAsString(0);

        fetchUniqueId(userName, uniqueId -> {
            var storage = plugin.getStorageManager().getStorage();
            var target = plugin.getUser(uniqueId);
            long voteFlySeconds = target == null ? storage.getVoteFlySeconds(uniqueId) : target.getVoteFlySeconds();

            message(sender, Message.COMMAND_VOTEFLY_GET, "%player%", userName, "%time%", TimerBuilder.getStringTime(voteFlySeconds * 1000));
        });

        return CommandResultType.SUCCESS;
    }
}
