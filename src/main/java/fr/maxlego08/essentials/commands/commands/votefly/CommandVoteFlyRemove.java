package fr.maxlego08.essentials.commands.commands.votefly;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.TimerBuilder;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

import java.util.stream.Stream;

public class CommandVoteFlyRemove extends VCommand {
    public CommandVoteFlyRemove(EssentialsPlugin plugin) {
        super(plugin);
        this.addSubCommand("remove");
        this.setPermission(Permission.ESSENTIALS_VOTEFLY_REMOVE);
        this.setDescription(Message.DESCRIPTION_VOTEFLY_REMOVE);
        this.addRequireOfflinePlayerNameArg();
        this.addRequireArg("seconds", (a, b) -> Stream.of(10, 20, 30, 40, 50, 60, 70, 80, 90).map(String::valueOf).toList());
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        String userName = this.argAsString(0);
        long seconds = this.argAsLong(1);

        fetchUniqueId(userName, uniqueId -> {
            var storage = plugin.getStorageManager().getStorage();
            var target = plugin.getUser(uniqueId);
            if (target == null) {
                long voteFlySeconds = storage.getVoteFlySeconds(uniqueId) - seconds;
                storage.upsertVoteFlySeconds(uniqueId, Math.max(0, voteFlySeconds));
            } else {
                target.removeVoteFlySeconds(seconds);
            }

            message(sender, Message.COMMAND_VOTEFLY_REMOVE, "%player%", userName, "%time%", TimerBuilder.getStringTime(seconds * 1000));
        });

        return CommandResultType.SUCCESS;
    }
}
