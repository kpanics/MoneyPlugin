package ru.kpanicshop.moneyPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class MoneyPlugin extends JavaPlugin {

    // Создаем статическую переменную, чтобы легко брать конфиг из класса команды
    private static MoneyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // Автоматически создает папку и config.yml, если их еще нет
        saveDefaultConfig();

        if (getCommand("money") != null) {
            getCommand("money").setExecutor(new MoneyCommand());
        }
        if (getCommand("trade") !=null)  {
            getCommand("trade").setExecutor(new TradeCommand());
        }
    }

    @Override
    public void onDisable() {
    }

    // Метод, чтобы получить доступ к плагину из других классов
    public static MoneyPlugin getInstance() {
        return instance;
    }
}
