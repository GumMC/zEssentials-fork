package fr.maxlego08.essentials.commands.commands.inbox;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.InboxModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

import java.util.ArrayList;

public class CommandInboxSend extends VCommand {
    public CommandInboxSend(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(InboxModule.class);
        this.setPermission(Permission.ESSENTIALS_INBOX_SEND);
        this.setDescription(Message.DESCRIPTION_INBOX_SEND);
        this.addSubCommand("send");
        this.addRequirePlayerNameArg();
        this.addRequireArg("message", (a, b) -> new ArrayList<>());
        this.setExtendedArgs(true);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        String userName = this.argAsString(0);
        String message = getArgs(2);

        InboxModule inboxModule = plugin.getModuleManager().getModule(InboxModule.class);
        fetchUniqueId(userName, uuid -> inboxModule.sendMail(this.sender, uuid, userName, message));

        return CommandResultType.SUCCESS;
    }
}
