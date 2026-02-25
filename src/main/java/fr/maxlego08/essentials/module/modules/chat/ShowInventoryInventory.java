package fr.maxlego08.essentials.module.modules.chat;

import fr.maxlego08.essentials.api.chat.ShowInventory;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Displays a player's full inventory snapshot (36 main + 4 armor + 1 off-hand = 41 slots)
 * inside a 45-slot chest inventory (5 rows × 9 columns).
 *
 * <p>Slot layout:
 * <ul>
 *   <li>Slots  0–8 : Hotbar</li>
 *   <li>Slots  9–35: Main inventory</li>
 *   <li>Slot  36   : Boots</li>
 *   <li>Slot  37   : Leggings</li>
 *   <li>Slot  38   : Chestplate</li>
 *   <li>Slot  39   : Helmet</li>
 *   <li>Slot  40   : Off-hand</li>
 *   <li>Slots 41–44: Empty (padding to reach a multiple of 9)</li>
 * </ul>
 */
public class ShowInventoryInventory implements InventoryHolder {

    // Total inventory size must be a multiple of 9. We need 41 slots, so we use 45.
    private static final int INVENTORY_SIZE = 45;

    // Armor slot indices inside our display inventory (after the 36 main slots)
    private static final int SLOT_BOOTS = 36;
    private static final int SLOT_LEGGINGS = 37;
    private static final int SLOT_CHESTPLATE = 38;
    private static final int SLOT_HELMET = 39;
    private static final int SLOT_OFF_HAND = 40;

    private final Inventory inventory;

    public ShowInventoryInventory(ShowInventory snapshot, String title, Player viewer) {

        this.inventory = Bukkit.createInventory(
                this,
                INVENTORY_SIZE,
                LegacyComponentSerializer.legacySection().deserialize(title)
        );

        // ── Main inventory (hotbar 0-8 + main 9-35) ──────────────────────────
        ItemStack[] contents = snapshot.contents();
        if (contents != null) {
            int limit = Math.min(contents.length, 36);
            for (int i = 0; i < limit; i++) {
                if (contents[i] != null) {
                    this.inventory.setItem(i, contents[i].clone());
                }
            }
        }

        // ── Armor (boots → leggings → chestplate → helmet) ───────────────────
        // Bukkit getArmorContents() returns [boots(0), leggings(1), chestplate(2), helmet(3)]
        ItemStack[] armor = snapshot.armor();
        if (armor != null) {
            setIfPresent(SLOT_BOOTS, armor, 0);
            setIfPresent(SLOT_LEGGINGS, armor, 1);
            setIfPresent(SLOT_CHESTPLATE, armor, 2);
            setIfPresent(SLOT_HELMET, armor, 3);
        }

        // ── Off-hand ──────────────────────────────────────────────────────────
        ItemStack offHand = snapshot.offHand();
        if (offHand != null && !offHand.isEmpty()) {
            this.inventory.setItem(SLOT_OFF_HAND, offHand.clone());
        }

        viewer.openInventory(this.inventory);
    }

    private void setIfPresent(int slot, ItemStack[] array, int index) {
        if (index < array.length && array[index] != null && !array[index].isEmpty()) {
            this.inventory.setItem(slot, array[index].clone());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
