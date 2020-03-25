package software.bigbade.enchantmenttokens;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.entity.Player;
import software.bigbade.enchantmenttokens.currency.CurrencyHandler;

public class MongoCurrencyHandler implements CurrencyHandler {
    private MongoCollection<Document> collection;
    private long gems = 0;
    public MongoCurrencyHandler(MongoCollection<Document> collection, long gems) {
        setAmount(gems);
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
    public void savePlayer(Player player, boolean async) {
        collection.updateOne(Filters.eq("uuid", player.getUniqueId()), Updates.set("gems", getAmount()));
    }

    @Override
    public String name() {
        return "mongodb";
    }
}
