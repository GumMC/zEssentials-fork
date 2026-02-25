package fr.maxlego08.essentials.migrations.update;

import fr.maxlego08.sarah.SchemaBuilder;
import fr.maxlego08.sarah.database.Migration;

public class UpdateUserTableAddVoteFlyColumn extends Migration {
    @Override
    public void up() {
        SchemaBuilder.alter(this, "%prefix%users", schema -> schema.bigInt("vote_fly_seconds").defaultValue(0));
    }
}
