package fr.maxlego08.essentials.api.dto;

import java.util.Date;
import java.util.UUID;

public record InboxDTO(int id, UUID receiver_id, UUID sender_id, String sender_name, String message, boolean is_read,
                       Date expire_at, Date created_at, Date updated_at) {
}
