package software.bigbade.enchantmenttokensmongo;

import com.mongodb.client.MongoCollection;
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
        Document query = new Document();
        query.put("uuid", player.getUniqueId());
        collection.find(query).first().replace("gems", gems);
    }

    @Override
    public String name() {
        return "mongo";
    }
}
