package fr.maxlego08.essentials.api.chat;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a snapshot of a player's inventory for display in chat via [inv] tag.
 *
 * @param player     The player who owns this inventory snapshot.
 * @param contents   Slots 0-35: hotbar (0-8) + main inventory (9-35).
 * @param armor      Slots 0-3: boots, leggings, chestplate, helmet (index order matches getArmorContents()).
 * @param offHand    The item in the player's off-hand slot.
 * @param expiredAt  Timestamp (ms) when this snapshot expires.
 * @param code       Random code used to reference this snapshot via command.
 */
public record ShowInventory(Player player, ItemStack[] contents, ItemStack[] armor, ItemStack offHand,
                            long expiredAt, String code) {

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiredAt;
    }
}
