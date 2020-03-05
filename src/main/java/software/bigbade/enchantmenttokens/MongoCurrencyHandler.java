package software.bigbade.enchantmenttokens;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.entity.Player;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyHandler;

public class MongoCurrencyHandler extends CurrencyHandler {
    private MongoCollection<Document> collection;

    public MongoCurrencyHandler(MongoCollection<Document> collection, long gems) {
        super("mongo");
        setAmount(gems);
        this.collection = collection;
    }

    @Override
    public void savePlayer(Player player, boolean async) {
        collection.updateOne(Filters.eq("uuid", player.getUniqueId()), Updates.set("gems", getAmount()));
    }
}
