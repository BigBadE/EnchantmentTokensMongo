package software.bigbade.enchantmenttokensmongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import software.bigbade.enchantmenttokens.EnchantmentTokens;
import software.bigbade.enchantmenttokens.api.ExternalCurrencyData;
import software.bigbade.enchantmenttokens.utils.ConfigurationManager;
import software.bigbade.enchantmenttokens.utils.EnchantLogger;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyFactory;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyHandler;

import java.util.logging.Level;

public class MongoCurrencyFactory implements CurrencyFactory {
    private MongoClient client;
    private MongoCollection<Document> collection;

    private ExternalCurrencyData data;

    public MongoCurrencyFactory(EnchantmentTokens main, ConfigurationSection section) {
        EnchantLogger.LOGGER.log(Level.INFO, "Loading MongoDB database");
        client = MongoClients.create((String) ConfigurationManager.getValueOrDefault("database", section, null));
        String collectionName = (String) ConfigurationManager.getValueOrDefault("section", section, "players");
        collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);
        if(collection == null) {
            EnchantLogger.LOGGER.log(Level.INFO, "Creating new database section");
            client.getDatabase(EnchantmentTokens.NAME).createCollection(collectionName);
            collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);
        }
    }

    @Override
    public CurrencyHandler newInstance(Player player) {
        Document document;
        Document query = new Document();
        query.put("uuid", player.getUniqueId());
        document = collection.find(query).first();
        if(document == null) {
            document = new Document();
            document.put("uuid", player.getUniqueId());
            document.put("gems", 0L);
        }
        return new MongoCurrencyHandler(collection, document.getLong("gems"));
    }

    @Override
    public String name() {
        return "mongo";
    }

    @Override
    public void setData(ExternalCurrencyData data) {
        this.data = data;
    }

    @Override
    public ExternalCurrencyData getData() {
        return data;
    }

    @Override
    public void shutdown() {
        client.close();
    }
}
