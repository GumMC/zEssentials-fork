package fr.maxlego08.essentials.commands.commands.inbox;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.InboxModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandInboxRead extends VCommand {
    public CommandInboxRead(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(InboxModule.class);
        this.setPermission(Permission.ESSENTIALS_INBOX_READ);
        this.setDescription(Message.DESCRIPTION_INBOX_READ);
        this.addSubCommand("read");
        this.addOptionalArg("page", (a, b) -> java.util.List.of("1", "2", "3"));
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        int page = this.argAsInteger(0, 1);

        InboxModule inboxModule = plugin.getModuleManager().getModule(InboxModule.class);
        inboxModule.readInbox(this.player, page);

        return CommandResultType.SUCCESS;
    }
}
