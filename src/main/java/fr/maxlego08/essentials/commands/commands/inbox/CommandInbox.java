package fr.maxlego08.essentials.commands.commands.inbox;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.InboxModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandInbox extends VCommand {
    public CommandInbox(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(InboxModule.class);
        this.setPermission(Permission.ESSENTIALS_INBOX);
        this.setDescription(Message.DESCRIPTION_INBOX);
        this.onlyPlayers();

        this.addSubCommand(new CommandInboxSend(plugin));
        this.addSubCommand(new CommandInboxSendAll(plugin));
        this.addSubCommand(new CommandInboxSendTemp(plugin));
        this.addSubCommand(new CommandInboxRead(plugin));
        this.addSubCommand(new CommandInboxClear(plugin));
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        syntaxMessage();
        return CommandResultType.SUCCESS;
    }
}
