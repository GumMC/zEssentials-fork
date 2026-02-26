package fr.maxlego08.essentials.commands.commands.booster;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.module.modules.BoosterModule;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;

public class CommandBooster extends VCommand {
    public CommandBooster(EssentialsPlugin plugin) {
        super(plugin);
        this.setModule(BoosterModule.class);
        this.setPermission(Permission.ESSENTIALS_BOOSTER);
        this.setDescription(Message.DESCRIPTION_BOOSTER);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {
        BoosterModule boosterModule = plugin.getModuleManager().getModule(BoosterModule.class);
        boosterModule.displayBoosters(this.player);
        return CommandResultType.SUCCESS;
    }
}
