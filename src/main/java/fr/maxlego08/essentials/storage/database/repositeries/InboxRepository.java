package fr.maxlego08.essentials.storage.database.repositeries;

import fr.maxlego08.essentials.api.EssentialsPlugin;
import fr.maxlego08.essentials.api.dto.InboxDTO;
import fr.maxlego08.essentials.storage.database.Repository;
import fr.maxlego08.sarah.DatabaseConnection;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class InboxRepository extends Repository {

    public InboxRepository(EssentialsPlugin plugin, DatabaseConnection connection) {
        super(plugin, connection, "inbox");
    }

    public List<InboxDTO> selectByReceiver(UUID receiverId) {
        return this.select(InboxDTO.class, table -> {
            table.where("receiver_id", receiverId);
            table.orderByDesc("created_at");
        });
    }

    public long countUnread(UUID receiverId) {
        return this.select(table -> {
            table.where("receiver_id", receiverId);
            table.where("is_read", false);
        });
    }

    public long countTotal(UUID receiverId) {
        return this.select(table -> table.where("receiver_id", receiverId));
    }

    public void insert(UUID receiverId, UUID senderId, String senderName, String message, Date expireAt) {
        this.insert(table -> {
            table.uuid("receiver_id", receiverId);
            if (senderId != null) {
                table.uuid("sender_id", senderId);
            }
            table.string("sender_name", senderName);
            table.string("message", message);
            table.bool("is_read", false);
            if (expireAt != null) {
                table.object("expire_at", expireAt);
            }
        });
    }

    public void markAllAsRead(UUID receiverId) {
        this.update(table -> {
            table.where("receiver_id", receiverId);
            table.bool("is_read", true);
        });
    }

    public void deleteByReceiver(UUID receiverId) {
        this.delete(table -> table.where("receiver_id", receiverId));
    }

    public void deleteExpired() {
        this.delete(table -> table.where("expire_at", "<", new Date()));
    }

    public void deleteOldest(UUID receiverId) {
        List<InboxDTO> oldest = this.select(InboxDTO.class, table -> {
            table.where("receiver_id", receiverId);
            table.orderByDesc("created_at");
        });
        if (!oldest.isEmpty()) {
            // Delete the last (oldest) entry since results are ordered by created_at DESC
            this.delete(table -> table.where("id", oldest.get(oldest.size() - 1).id()));
        }
    }
}
