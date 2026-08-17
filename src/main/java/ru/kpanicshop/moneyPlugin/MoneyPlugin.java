package ru.kpanicshop.moneyPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class MoneyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Сервер при запуске будет искать команду тут
        getCommand("money").setExecutor(new MoneyCommand());
    }

    @Override
    public void onDisable() {
    }
}

