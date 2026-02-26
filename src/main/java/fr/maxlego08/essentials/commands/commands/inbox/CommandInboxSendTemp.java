package fr.maxlego08.essentials.commands.commands.inbox;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.InboxModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandInboxSendTemp extends VCommand {
    public CommandInboxSendTemp(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(InboxModule.class);
        this.setPermission(Permission.ESSENTIALS_INBOX_SENDTEMP);
        this.setDescription(Message.DESCRIPTION_INBOX_SENDTEMP);
        this.addSubCommand("sendtemp");
        this.addRequirePlayerNameArg();
        this.addRequireArg("duration", (a, b) -> Arrays.asList("1h", "12h", "1d", "7d", "30d"));
        this.addRequireArg("message", (a, b) -> new ArrayList<>());
        this.setExtendedArgs(true);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        String userName = this.argAsString(0);
        String duration = this.argAsString(1);
        String message = getArgs(3);

        InboxModule inboxModule = plugin.getModuleManager().getModule(InboxModule.class);
        fetchUniqueId(userName, uuid -> inboxModule.sendTempMail(this.sender, uuid, userName, duration, message));

        return CommandResultType.SUCCESS;
    }
}
