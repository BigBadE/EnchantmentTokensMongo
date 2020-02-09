package software.bigbade.enchantmenttokensmongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import software.bigbade.enchantmenttokens.EnchantmentTokens;
import software.bigbade.enchantmenttokens.utils.ConfigurationManager;
import software.bigbade.enchantmenttokens.utils.EnchantLogger;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyFactory;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyHandler;

import java.util.logging.Level;

public class MongoCurrencyFactory implements CurrencyFactory {
    private MongoClient client;
    private MongoCollection<Document> collection;

    public MongoCurrencyFactory(EnchantmentTokens main, ConfigurationSection section) {
        EnchantLogger.log(Level.INFO, "Loading MongoDB database");

        String username = (String) ConfigurationManager.getValueOrDefault("username", section, null);
        String password = (String) ConfigurationManager.getValueOrDefault("password", section, null);

        MongoClientSettings.Builder builder = MongoClientSettings.builder().applyConnectionString(new ConnectionString((String) ConfigurationManager.getValueOrDefault("database", section, ""))).applicationName(EnchantmentTokens.NAME);
        if (username != null && password != null)
            switch ((String) ConfigurationManager.getValueOrDefault("security", section, "DEFAULT")) {
                case "DEFAULT":
                    builder.credential(MongoCredential.createCredential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "SHA256":
                    builder.credential(MongoCredential.createScramSha256Credential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "SHA1":
                    builder.credential(MongoCredential.createScramSha1Credential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "PLAIN":
                    builder.credential(MongoCredential.createPlainCredential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    EnchantLogger.log(Level.WARNING, "PLAIN VERIFICATION IS ENABLED. NOT SUGGESTED!");
            }


        client = MongoClients.create(builder.build());
        String collectionName = (String) ConfigurationManager.getValueOrDefault("section", section, "players");
        collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);

        if (collection == null) {
            EnchantLogger.log(Level.INFO, "Creating new database section");
            client.getDatabase(EnchantmentTokens.NAME).createCollection(collectionName);
            collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);
        }
    }

    @Override
    public CurrencyHandler newInstance(Player player) {
        Document document = collection.find(Filters.eq("uuid", player.getUniqueId())).first();
        if (document == null) {
            document = new Document("uuid", player.getUniqueId());
            document.put("gems", 0L);
            collection.insertOne(document);
        }
        return new MongoCurrencyHandler(collection, document.getLong("gems"));
    }

    @Override
    public String name() {
        return "mongo";
    }

    @Override
    public void shutdown() {
        client.close();
    }
}
