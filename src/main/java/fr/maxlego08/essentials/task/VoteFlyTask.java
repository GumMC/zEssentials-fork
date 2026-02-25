package fr.maxlego08.essentials.task;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.commands.Permission;
import fr.maxlego08.essentials.api.messages.Message;
import fr.maxlego08.essentials.api.user.User;
import fr.maxlego08.essentials.zutils.utils.ZUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class VoteFlyTask extends ZUtils {

    public VoteFlyTask(EssentialsPlugin plugin) {
        plugin.getScheduler().runTimer(task -> {

            if (!plugin.isEnabled()) {
                task.cancel();
                return;
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // Only process players who are flying, in survival/adventure, and do NOT have unlimited fly
                if (player.isFlying()
                        && !player.hasPermission(Permission.ESSENTIALS_FLY_UNLIMITED.asPermission())
                        && (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)) {

                    User user = plugin.getUser(player.getUniqueId());
                    if (user == null) continue;

                    long seconds = user.getVoteFlySeconds() - 1;

                    if (seconds <= 0) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        message(player, Message.COMMAND_VOTEFLY_END);
                        user.setVoteFlySeconds(0);
                    } else {
                        user.setVoteFlySeconds(seconds);
                    }
                }
            }

        }, 1, 1, TimeUnit.SECONDS);
    }
}
