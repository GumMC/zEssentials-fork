package fr.maxlego08.essentials.migrations.create;

import fr.maxlego08.sarah.database.Migration;

public class CreateInboxTableMigration extends Migration {

    @Override
    public void up() {
        create("%prefix%inbox", table -> {
            table.autoIncrement("id");
            table.uuid("receiver_id").foreignKey("%prefix%users", "unique_id", true);
            table.uuid("sender_id").nullable();
            table.string("sender_name", 64);
            table.text("message");
            table.bool("is_read").defaultValue(0);
            table.timestamp("expire_at").nullable();
            table.timestamps();
        });
    }
}
