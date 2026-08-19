package ru.kpanicshop.moneyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

public class TradeGui {
    public void openTradeMenu(Player player1, Player player2) {
        String title = ChatColor.translateAlternateColorCodes('&', "&8 Обмен");
        Inventory gui = Bukkit.createInventory(null, 54, title);
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(ChatColor.GRAY + "Разделитель");
        }
        for (int i = 4; i < 54; i+=9) {
            gui.setItem(i, glass);
        }
        ItemStack notReady = new ItemStack(Material.RED_WOOL);
        ItemMeta woolMeta = notReady.getItemMeta();
        if (woolMeta != null) {
            woolMeta.setDisplayName(ChatColor.RED + "Не готов");
            notReady.setItemMeta(woolMeta);
        }
        gui.setItem(45, notReady);
        gui.setItem(53, notReady);
        player1.openInventory(gui);
        player2.openInventory(gui);
    }
}