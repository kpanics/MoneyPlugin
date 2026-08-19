package ru.kpanicshop.moneyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class TradeSession {

    private final UUID p1UUID;
    private final UUID p2UUID;
    private final Inventory inventory;
    private boolean isFinished = false; // Главный предохранитель от дюпа

    public TradeSession(Player p1, Player p2) {
        this.p1UUID = p1.getUniqueId();
        this.p2UUID = p2.getUniqueId();
        this.inventory = Bukkit.createInventory(null, 9, "§8Безопасный Обмен");
    }

    public UUID getP1UUID() { return p1UUID; }
    public UUID getP2UUID() { return p2UUID; }
    public Inventory getInventory() { return inventory; }
    public boolean isFinished() { return isFinished; }

    // Успешный обмен
    public synchronized void completeTrade() {
        if (isFinished) return;
        isFinished = true;

        Player p1 = Bukkit.getPlayer(p1UUID);
        Player p2 = Bukkit.getPlayer(p2UUID);

        ItemStack[] p1Items = { inventory.getItem(0), inventory.getItem(1) };
        ItemStack[] p2Items = { inventory.getItem(7), inventory.getItem(8) };

        inventory.clear(); // Чистим GUI до выдачи шмоток

        if (p2 != null && p2.isOnline()) giveItems(p2, p1Items);
        else dropItems(p1UUID, p1Items);

        if (p1 != null && p1.isOnline()) giveItems(p1, p2Items);
        else dropItems(p2UUID, p2Items);

        closeInventories();
    }

    // Отмена обмена (если закрыли GUI)
    public synchronized void cancelTrade() {
        if (isFinished) return;
        isFinished = true;

        ItemStack[] p1Items = { inventory.getItem(0), inventory.getItem(1) };
        ItemStack[] p2Items = { inventory.getItem(7), inventory.getItem(8) };

        inventory.clear();

        giveItemsOrDrop(p1UUID, p1Items);
        giveItemsOrDrop(p2UUID, p2Items);

        closeInventories();
    }

    private void giveItemsOrDrop(UUID uuid, ItemStack[] items) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) giveItems(player, items);
        else dropItems(uuid, items);
    }

    private void giveItems(Player player, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item).values().forEach(
                        rem -> player.getWorld().dropItemNaturally(player.getLocation(), rem)
                );
            }
        }
    }

    private void dropItems(UUID uuid, ItemStack[] items) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) giveItems(player, items);
    }

    private void closeInventories() {
        Bukkit.getScheduler().runTask(MoneyPlugin.getInstance(), () -> {
            Player p1 = Bukkit.getPlayer(p1UUID);
            Player p2 = Bukkit.getPlayer(p2UUID);
            if (p1 != null) p1.closeInventory();
            if (p2 != null) p2.closeInventory();
        });
    }
}
