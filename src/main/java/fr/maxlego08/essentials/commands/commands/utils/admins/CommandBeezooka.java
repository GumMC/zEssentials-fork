package fr.maxlego08.essentials.commands.commands.utils.admins;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.CommandResultType;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.zutils.utils.commands.VCommand;
import org.bukkit.Location;
import org.bukkit.entity.Bee;

import java.util.concurrent.TimeUnit;

public class CommandBeezooka extends VCommand {

    public CommandBeezooka(EssentialsPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ESSENTIALS_BEEZOOKA);
        this.setDescription(Message.DESCRIPTION_BEEZOOKA);
        this.onlyPlayers();
    }

    @Override
    protected CommandResultType perform(EssentialsPlugin plugin) {

        Location location = player.getEyeLocation();
        location.getWorld().spawn(location, Bee.class, bee -> {
            bee.setVelocity(location.getDirection().multiply(2));

            plugin.getScheduler().runAtLocationLater(location, () -> {
                Location beeLoc = bee.getLocation();
                bee.remove();
                beeLoc.getWorld().createExplosion(beeLoc, 0F);
            }, 1, TimeUnit.SECONDS);
        });

        return CommandResultType.SUCCESS;
    }
}