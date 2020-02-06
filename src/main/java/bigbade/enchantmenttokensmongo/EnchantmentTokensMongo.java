package bigbade.enchantmenttokensmongo;

import org.bukkit.entity.Player;
import software.bigbade.enchantmenttokens.utils.currency.CurrencyHandler;

public class EnchantmentTokensMongo implements CurrencyHandler {
    private long gems;

    public EnchantmentTokensMongo() {

    }

    public EnchantmentTokensMongo(Player player) {

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

    }

    @Override
    public String name() {
        return "mongo";
    }
}
