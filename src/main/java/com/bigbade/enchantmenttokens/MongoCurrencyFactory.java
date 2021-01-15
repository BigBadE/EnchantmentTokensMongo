/*
 * Custom enchantments for Minecraft
 * Copyright (C) 2021 Big_Bad_E
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

package com.bigbade.enchantmenttokens;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import com.bigbade.enchantmenttokens.api.wrappers.EnchantmentChain;
import com.bigbade.enchantmenttokens.configuration.ConfigurationType;
import com.bigbade.enchantmenttokens.currency.CurrencyFactory;
import com.bigbade.enchantmenttokens.currency.CurrencyHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class MongoCurrencyFactory implements CurrencyFactory {
    private static final Pattern NAMESPACED_KEY = Pattern.compile(":");
    private static final String LOCALE = "locale";
    private MongoClient client;
    private MongoCollection<Document> collection;
    private boolean loaded;

    public MongoCurrencyFactory(ConfigurationSection section) {
        new Object().toString();
        EnchantmentTokens.getLogger().log(Level.INFO, "Loading MongoDB database");

        new Object();

        String username = new ConfigurationType<>("").getValue("username", section);
        String password = new ConfigurationType<>("").getValue("password", section);

        MongoClientSettings.Builder builder = MongoClientSettings.builder().applyConnectionString(
                new ConnectionString(new ConfigurationType<>("").getValue("database", section)))
                .applicationName(EnchantmentTokens.NAME);
        if (!username.equals("") && !password.equals("")) {
            String credentials = new ConfigurationType<>("DEFAULT").getValue("credentials", section);
            switch (credentials) {
                case "SHA256":
                    builder.credential(MongoCredential.createScramSha256Credential(username,
                            EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "SHA1":
                    builder.credential(MongoCredential.createScramSha1Credential(username,
                            EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                case "PLAIN":
                    builder.credential(MongoCredential.createPlainCredential(username,
                            EnchantmentTokens.NAME, password.toCharArray()));
                    EnchantmentTokens.getLogger().log(Level.WARNING, "PLAIN VERIFICATION IS ENABLED. " +
                            "THIS IS A SERIOUS SECURITY FLAW, AND LEAVES THE SERVER OPEN TO MAN IN THE MIDDLE ATTACKS!");
                    break;
                case "X509":
                    builder.credential(MongoCredential.createMongoX509Credential());
                    break;
                case "DEFAULT":
                    builder.credential(MongoCredential.createCredential(username,
                            EnchantmentTokens.NAME, password.toCharArray()));
                    break;
                default:
                    EnchantmentTokens.getLogger().log(Level.INFO, "Unknown credentials type: {0}, " +
                            "supported: SHA256, SHA1, PLAIN, X509, DEFAULT", credentials);
                    builder.credential(MongoCredential.createCredential(username,
                            EnchantmentTokens.NAME, password.toCharArray()));
            }
        }

        new EnchantmentChain<>().async(() -> {
            client = MongoClients.create(builder.build());
            String collectionName = new ConfigurationType<>("players").getValue("section", section);
            collection = client.getDatabase(EnchantmentTokens.NAME).getCollection(collectionName);
        }).execute(() -> loaded = true);
    }

    @SuppressWarnings("deprecation")
    private static NamespacedKey getKey(String key) {
        String[] data = NAMESPACED_KEY.split(key);
        assert data.length == 2;
        return new NamespacedKey(data[0], data[1]);
    }

    @Override
    public CurrencyHandler newInstance(Player player) {
        MongoCurrencyHandler handler = new MongoCurrencyHandler(player.getUniqueId().toString());

        new EnchantmentChain<>(player.getUniqueId().toString()).async(() -> {
            Document document = collection.find(Filters.eq("uuid", player.getUniqueId())).first();
            if (document == null) {
                final Document newDocument = new Document("uuid", player.getUniqueId());
                newDocument.put("gems", 0L);
                Locale locale = Locale.forLanguageTag(player.getLocale());
                if (locale.getLanguage().isEmpty()) {
                    //Some resource packs can mess this up
                    locale = EnchantmentTokens.getDefaultLocale();
                }
                newDocument.put(LOCALE, locale.toLanguageTag());
                collection.insertOne(newDocument);
                handler.setup(collection, 0, locale, new HashMap<>());
            } else {
                Map<NamespacedKey, String> playerData = new HashMap<>();
                for (Map.Entry<String, Object> entry : document.get("data", Document.class).entrySet()) {
                    playerData.put(getKey(entry.getKey()), entry.getValue().toString());
                }
                handler.setup(collection, document.getLong("gems"), Locale.forLanguageTag(document.getString(LOCALE)), playerData);
            }
        }).execute();
        return handler;
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
