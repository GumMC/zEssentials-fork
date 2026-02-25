package fr.maxlego08.essentials.commands.commands.chat;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.chat.ChatModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandShowInv extends VCommand {

    public CommandShowInv(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(ChatModule.class);
        this.setPermission(Permission.ESSENTIALS_SHOW_INV);
        this.setDescription(Message.DESCRIPTION_SHOW_INV);
        this.addRequireArg("code");
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        String code = this.argAsString(0);
        plugin.getModuleManager().getModule(ChatModule.class).openShowInventory(player, code);
        return CommandResultType.SUCCESS;
    }
}
