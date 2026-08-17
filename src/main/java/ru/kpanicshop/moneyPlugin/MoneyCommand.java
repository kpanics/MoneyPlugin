package ru.kpanicshop.moneyPlugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class MoneyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.YELLOW + "Only KPlayers can use this command.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Монета");
            item.setItemMeta(meta);
        }

        HashMap<Integer, ItemStack> notFit = player.getInventory().addItem(item);

        if (!notFit.isEmpty()) {
            for (ItemStack leftItem : notFit.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftItem);
            }
            player.sendMessage(ChatColor.YELLOW + "Ваш инвентарь был полон! Монета сброшена под ноги.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Выдана одна монета.");
        }

        return true;
    }
}

