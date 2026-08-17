package ru.kpanicshop.moneyPlugin;

import ru.kpanicshop.moneyPlugin.MoneyCommand;

import org.bukkit.plugin.java.JavaPlugin;

public final class MoneyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("money").setExecutor(new MoneyCommand());

    }

    @Override
    public void onDisable() {
    }
}
