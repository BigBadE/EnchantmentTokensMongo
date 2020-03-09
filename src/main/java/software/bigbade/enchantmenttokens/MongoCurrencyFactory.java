package software.bigbade.enchantmenttokens;

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
import software.bigbade.enchantmenttokens.utils.EnchantLogger;
import software.bigbade.enchantmenttokens.utils.configuration.ConfigurationType;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyFactory;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyHandler;

import java.util.logging.Level;

public class MongoCurrencyFactory extends CurrencyFactory {
    private MongoClient client;
    private MongoCollection<Document> collection;
    private boolean loaded;

    public MongoCurrencyFactory(ConfigurationSection section) {
        super("mongo");
        EnchantLogger.log(Level.INFO, "Loading MongoDB database");

        String username = new ConfigurationType<>("").getValue("username", section);
        String password = new ConfigurationType<>("").getValue("password", section);

        MongoClientSettings.Builder builder = MongoClientSettings.builder().applyConnectionString(new ConnectionString(new ConfigurationType<>("").getValue("database", section))).applicationName(EnchantmentTokens.NAME);
        if (!username.equals("") && !password.equals(""))
            switch (new ConfigurationType<>("DEFAULT").getValue("security", section)) {
                case "SHA256":
                    builder.credential(MongoCredential.createScramSha256Credential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "SHA1":
                    builder.credential(MongoCredential.createScramSha1Credential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "PLAIN":
                    builder.credential(MongoCredential.createPlainCredential(username, EnchantmentTokens.NAME, password.toCharArray()));
                    EnchantLogger.log(Level.WARNING, "PLAIN VERIFICATION IS ENABLED. NOT SUGGESTED!");
                    break;
                case "DEFAULT":
                default:
                    builder.credential(MongoCredential.createCredential(username, EnchantmentTokens.NAME, password.toCharArray()));
            }


        client = MongoClients.create(builder.build());
        String collectionName = new ConfigurationType<>("players").getValue("section", section);
        collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);

        if (collection == null) {
            EnchantLogger.log(Level.INFO, "Creating new database section");
            client.getDatabase(EnchantmentTokens.NAME).createCollection(collectionName);
            collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);
        }

        loaded = true;
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

    @Override
    public boolean loaded() {
        return loaded;
    }
}
