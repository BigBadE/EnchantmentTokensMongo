/*
 * Addons for the Custom Enchantment API in Minecraft
 * Copyright (C) 2020 BigBadE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import software.bigbade.enchantmenttokens.configuration.ConfigurationType;
import software.bigbade.enchantmenttokens.currency.CurrencyFactory;
import software.bigbade.enchantmenttokens.currency.CurrencyHandler;

import java.util.Locale;
import java.util.logging.Level;

public class MongoCurrencyFactory implements CurrencyFactory {
    private final MongoClient client;
    private MongoCollection<Document> collection;
    private final boolean loaded;

    public MongoCurrencyFactory(ConfigurationSection section) {
        EnchantmentTokens.getEnchantLogger().log(Level.INFO, "Loading MongoDB database");

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
                    EnchantmentTokens.getEnchantLogger().log(Level.WARNING, "PLAIN VERIFICATION IS ENABLED. NOT SUGGESTED!");
                    break;
                case "SSH":
                    builder.credential(MongoCredential.createMongoX509Credential());
                    break;
                case "DEFAULT":
                default:
                    builder.credential(MongoCredential.createCredential(username, EnchantmentTokens.NAME, password.toCharArray()));
            }


        client = MongoClients.create(builder.build());
        String collectionName = new ConfigurationType<>("players").getValue("section", section);
        collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);

        if (collection == null) {
            EnchantmentTokens.getEnchantLogger().log(Level.INFO, "Creating new database section");
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
            document.put("locale", Locale.getDefault().toLanguageTag());
            collection.insertOne(document);
        }
        return new MongoCurrencyHandler(collection, document.getLong("gems"), Locale.forLanguageTag(document.getString("locale")));
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
