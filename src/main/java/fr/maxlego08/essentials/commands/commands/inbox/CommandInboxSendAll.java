package fr.maxlego08.essentials.commands.commands.inbox;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.InboxModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

import java.util.ArrayList;

public class CommandInboxSendAll extends VCommand {
    public CommandInboxSendAll(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(InboxModule.class);
        this.setPermission(Permission.ESSENTIALS_INBOX_SENDALL);
        this.setDescription(Message.DESCRIPTION_INBOX_SENDALL);
        this.addSubCommand("sendall");
        this.addRequireArg("message", (a, b) -> new ArrayList<>());
        this.setExtendedArgs(true);
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        String message = getArgs(1);

        InboxModule inboxModule = plugin.getModuleManager().getModule(InboxModule.class);
        inboxModule.sendAllMail(this.sender, message);

        return CommandResultType.SUCCESS;
    }
}
