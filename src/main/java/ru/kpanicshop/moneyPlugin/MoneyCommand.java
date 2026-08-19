package ru.kpanicshop.moneyPlugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Проверяем право (чтобы обычные игроки не могли релоадить плагин)
            if (!player.hasPermission("moneyplugin.admin")) {
                player.sendMessage(ChatColor.RED + "У вас нет прав на перезагрузку плагина!");
                return true;
            }

            MoneyPlugin.getInstance().reloadConfig();

            player.sendMessage(ChatColor.GREEN + "[MoneyPlugin] Конфигурация успешно перезагружена!");
            return true;
        }
        // =========================================================================

        FileConfiguration config = MoneyPlugin.getInstance().getConfig();

        String materialName = config.getString("coin-settings.material", "GOLD_NUGGET");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            material = Material.GOLD_NUGGET;
        }

        String rawName = config.getString("coin-settings.display-name", "&6Монета");
        String displayName = ChatColor.translateAlternateColorCodes('&', rawName);

        String msgSuccess = ChatColor.translateAlternateColorCodes('&',
                config.getString("coin-settings.messages.success", "&aВыдана одна монета."));
        String msgFull = ChatColor.translateAlternateColorCodes('&',
                config.getString("coin-settings.messages.inventory-full", "&eВаш инвентарь был полон! Монета сброшена под ноги."));

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            NamespacedKey key = new NamespacedKey(MoneyPlugin.getInstance(), "GOLD");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }

        HashMap<Integer, ItemStack> notFit = player.getInventory().addItem(item);

        if (!notFit.isEmpty()) {
            for (ItemStack leftItem : notFit.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftItem);
            }
            player.sendMessage(msgFull);
        } else {
            player.sendMessage(msgSuccess);
        }

        return true;
    }
}
