package fr.maxlego08.essentials.commands.commands.inbox;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.InboxModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandInboxClear extends VCommand {
    public CommandInboxClear(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(InboxModule.class);
        this.setPermission(Permission.ESSENTIALS_INBOX_CLEAR);
        this.setDescription(Message.DESCRIPTION_INBOX_CLEAR);
        this.addSubCommand("clear");
        this.addOptionalArg("player");
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        InboxModule inboxModule = plugin.getModuleManager().getModule(InboxModule.class);

        String targetName = this.argAsString(0, null);

        if (targetName == null) {
            // Clear own inbox
            inboxModule.clearInbox(this.player);
        } else {
            // Clear another player's inbox (admin)
            if (!hasPermission(this.sender, Permission.ESSENTIALS_INBOX_CLEAR_OTHER)) return CommandResultType.DEFAULT;
            fetchUniqueId(targetName, uuid -> inboxModule.clearInboxOther(this.sender, uuid, targetName));
        }

        return CommandResultType.SUCCESS;
    }
}
