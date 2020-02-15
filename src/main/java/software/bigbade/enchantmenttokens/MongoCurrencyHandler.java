package software.bigbade.enchantmenttokens;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.entity.Player;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyHandler;

public class MongoCurrencyHandler implements CurrencyHandler {
    private long gems;
    private MongoCollection<Document> collection;

    public MongoCurrencyHandler(MongoCollection<Document> collection, long gems) {
        this.gems = gems;
        this.collection = collection;
    }

    @Override
    public long getAmount() {
        return gems;
    }

    @Override
    public void setAmount(long amount) {
        gems = amount;
    }

    @Override
    public void addAmount(long amount) {
        gems += amount;
    }

    @Override
    public void savePlayer(Player player) {
        collection.updateOne(Filters.eq("uuid", player.getUniqueId()), Updates.set("gems", gems));
    }

    @Override
    public String name() {
        return "mongo";
    }
}
