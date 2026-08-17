package ru.kpanicshop.moneyPlugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration; // Новый импорт для работы с конфигом
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class MoneyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.YELLOW + "Only KPlayers can use this command.");
            return true;
        }

        // 1. Получаем файл конфигурации через наш главный класс
        FileConfiguration config = MoneyPlugin.getInstance().getConfig();

        // 2. Читаем название материала из конфига. Если там ошибка — по дефолту берем GOLD_NUGGET
        String materialName = config.getString("coin-settings.material", "GOLD_NUGGET");
        Material material = Material.getMaterial(materialName.toUpperCase()); // Метод toUpperCase() сам сделает буквы КАПСОМ, если ты забудешь!
        if (material == null) {
            material = Material.GOLD_NUGGET;
        }

        // 3. Читаем название и сообщения, переводя цвета из формата &
        String rawName = config.getString("coin-settings.display-name", "&6Монета");
        String displayName = ChatColor.translateAlternateColorCodes('&', rawName);

        String msgSuccess = ChatColor.translateAlternateColorCodes('&',
                config.getString("coin-settings.messages.success", "&aВыдана одна монета."));
        String msgFull = ChatColor.translateAlternateColorCodes('&',
                config.getString("coin-settings.messages.inventory-full", "&eВаш инвентарь был полон! Монета сброшена под ноги."));

        // 4. Создаем предмет на основе данных из конфига
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }

        // Наша рабочая проверка инвентаря
        HashMap<Integer, ItemStack> notFit = player.getInventory().addItem(item);

        if (!notFit.isEmpty()) {
            for (ItemStack leftItem : notFit.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftItem);
            }
            player.sendMessage(msgFull); // Сообщение из конфига
        } else {
            player.sendMessage(msgSuccess); // Сообщение из конфига
        }

        return true;
    }
}
