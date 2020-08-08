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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bukkit.NamespacedKey;
import software.bigbade.enchantmenttokens.api.EnchantmentPlayer;
import software.bigbade.enchantmenttokens.api.wrappers.EnchantmentChain;
import software.bigbade.enchantmenttokens.currency.CurrencyHandler;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MongoCurrencyHandler implements CurrencyHandler {
    private final String uuid;
    private MongoCollection<Document> collection;
    private long gems;
    private Locale locale;

    void setup(MongoCollection<Document> collection, long gems, Locale locale) {
        setAmount(gems);
        this.collection = collection;
        this.locale = locale;
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
        collection.updateOne(Filters.eq("uuid", uuid), Updates.combine(Updates.set("gems", getAmount()), Updates.set("locale", locale.toLanguageTag())));
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
    public void storePlayerData(NamespacedKey namespacedKey, String s) {

    }

    @Override
    public String getPlayerData(NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public void removePlayerData(NamespacedKey namespacedKey) {

    }
}
