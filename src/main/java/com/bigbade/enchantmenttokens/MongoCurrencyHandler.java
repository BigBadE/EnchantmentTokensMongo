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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bukkit.NamespacedKey;
import com.bigbade.enchantmenttokens.api.EnchantmentPlayer;
import com.bigbade.enchantmenttokens.api.wrappers.EnchantmentChain;
import com.bigbade.enchantmenttokens.currency.CurrencyHandler;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MongoCurrencyHandler implements CurrencyHandler {
    private final String uuid;
    private long gems;
    private Map<NamespacedKey, String> playerData;
    private MongoCollection<Document> collection;
    private Locale locale;

    void setup(MongoCollection<Document> collection, long gems, Locale locale, Map<NamespacedKey, String> playerData) {
        setAmount(gems);
        this.collection = collection;
        this.locale = locale;
        this.playerData = playerData;
    }

    @Override
    public CompletableFuture<Long> getAmount() {
        CompletableFuture<Long> future = new CompletableFuture<>();
        new EnchantmentChain<>(uuid).execute(() -> future.complete(gems));
        return future;
    }

    @Override
    public void setAmount(long amount) {
        new EnchantmentChain<>(uuid).execute(() -> gems = amount);
    }

    @Override
    public void addAmount(long amount) {
        new EnchantmentChain<>(uuid).execute(() -> gems += amount);
    }

    @Override
    public void savePlayer(EnchantmentPlayer player) {
        save();
    }

    private void save() {
        Document bson = new Document();
        for (Map.Entry<NamespacedKey, String> data : playerData.entrySet()) {
            bson.put(data.getKey().toString(), data.getValue());
        }
        collection.updateOne(Filters.eq("uuid", uuid), Updates.combine(Updates.set("gems", getAmount()), Updates.set("locale", locale.toLanguageTag()), Updates.set("data", bson)));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale language) {
        locale = language;
    }

    @Override
    public String name() {
        return "mongodb";
    }

    @Override
    public void storePlayerData(NamespacedKey namespacedKey, String value) {
        playerData.put(namespacedKey, value);
    }

    @Override
    public String getPlayerData(NamespacedKey namespacedKey) {
        return playerData.get(namespacedKey);
    }

    @Override
    public void removePlayerData(NamespacedKey namespacedKey) {
        playerData.remove(namespacedKey);
    }
}
