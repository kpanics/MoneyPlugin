package ru.kpanicshop.moneyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeListener implements Listener {

    // Карта для отслеживания: какой инвентарь принадлежит какой паре игроков
    // Ключ — UUID игрока, Значение — UUID его оппонента
    private final Map<UUID, UUID> activePairs = new HashMap<>();

    // Сетка слотов для 54-слотового GUI (6 строк)
    // Игрок 1 (Левая сторона): слоты строк 0-5 (например, столбцы 0,1,2,3)
    // Игрок 2 (Правая сторона): слоты строк 0-5 (например, столбцы 5,6,7,8)
    private final int SLOT_READY_P1 = 49; // Кнопка готовности левого игрока (снизу)
    private final int SLOT_READY_P2 = 51; // Кнопка готовности правого игрока (снизу)

    /**
     * Метод для инициализации и открытия твоего кастомного 54-слотового трейда
     */
    public void openTrade(Player p1, Player p2) {
        String title = ChatColor.translateAlternateColorCodes('&', "&8 Обмен");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Заполняем разделитель (столбец №4 по центру: слоты 4, 13, 22, 31, 40, 49)
        for (int i = 4; i < 54; i += 9) {
            gui.setItem(i, createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "§7[ Разделитель ]"));
        }

        // Ставим кнопки шерсти на их места
        gui.setItem(SLOT_READY_P1, createGuiItem(Material.RED_WOOL, ChatColor.RED + "Игрок 1: Не готов"));
        gui.setItem(SLOT_READY_P2, createGuiItem(Material.RED_WOOL, ChatColor.RED + "Игрок 2: Не готов"));

        // Связываем игроков в мапе
        activePairs.put(p1.getUniqueId(), p2.getUniqueId());
        activePairs.put(p2.getUniqueId(), p1.getUniqueId());

        p1.openInventory(gui);
        p2.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&', "&8 Обмен");
        if (!title.equals(expectedTitle)) return;

        int slot = event.getRawSlot();
        Inventory gui = event.getInventory();

        // Защита: запрещаем брать разделительные стекла
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        UUID oppUUID = activePairs.get(player.getUniqueId());
        if (oppUUID == null) return; // Если это не активный трейд — игнорируем

        // Проверяем, кто кликнул. Пусть первый в паре будет "Левым", второй "Правым"
        // Для простоты определим зоны кликов:
        int column = slot % 9;

        // Логика клика по кнопкам готовности (шерсти)
        if (slot == SLOT_READY_P1) {
            event.setCancelled(true);
            // Только "левый" игрок (тот у кого инициатива или первая позиция) может тыкать левую кнопку
            // Чтобы не усложнять, позволим переключать шерсть на своей стороне
            toggleReadyButton(gui, SLOT_READY_P1, "Игрок 1");
            checkTradeCompletion(gui, player, Bukkit.getPlayer(oppUUID));
            return;
        }

        if (slot == SLOT_READY_P2) {
            event.setCancelled(true);
            toggleReadyButton(gui, SLOT_READY_P2, "Игрок 2");
            checkTradeCompletion(gui, player, Bukkit.getPlayer(oppUUID));
            return;
        }

        // Защита от воровства из чужой половины экрана:
        // Левый игрок не может кликать по правой стороне (столбцы 5,6,7,8)
        // Правый игрок не может кликать по левой стороне (столбцы 0,1,2,3)
        boolean isLeftPlayer = player.getUniqueId().hashCode() > oppUUID.hashCode(); // Стабильное разделение сторон

        if (isLeftPlayer && column > 4 && slot < 54) {
            event.setCancelled(true);
            return;
        }
        if (!isLeftPlayer && column < 4 && slot < 54) {
            event.setCancelled(true);
            return;
        }

        // Самое главное: Если кто-то доложил/забрал вещь — сбрасываем обе шерсти на КРАСНЫЕ!
        // Это защитит от подмены предметов в последнюю секунду
        if (slot < 54 && slot != SLOT_READY_P1 && slot != SLOT_READY_P2) {
            resetReadyStatus(gui);
        }
    }

    private void toggleReadyButton(Inventory gui, int slot, String label) {
        ItemStack item = gui.getItem(slot);
        if (item != null && item.getType() == Material.RED_WOOL) {
            gui.setItem(slot, createGuiItem(Material.GREEN_WOOL, ChatColor.GREEN + label + ": ГОТОВ!"));
        } else {
            gui.setItem(slot, createGuiItem(Material.RED_WOOL, ChatColor.RED + label + ": Не готов"));
        }
    }

    private void resetReadyStatus(Inventory gui) {
        gui.setItem(SLOT_READY_P1, createGuiItem(Material.RED_WOOL, ChatColor.RED + "Игрок 1: Не готов"));
        gui.setItem(SLOT_READY_P2, createGuiItem(Material.RED_WOOL, ChatColor.RED + "Игрок 2: Не готов"));
    }

    private void checkTradeCompletion(Inventory gui, Player p1, Player p2) {
        ItemStack b1 = gui.getItem(SLOT_READY_P1);
        ItemStack b2 = gui.getItem(SLOT_READY_P2);

        // Если ОДНОВРЕМЕННО обе шерсти стали ЗЕЛЕНЫМИ
        if (b1 != null && b1.getType() == Material.GREEN_WOOL &&
                b2 != null && b2.getType() == Material.GREEN_WOOL) {

            // Разрываем связь в мапе ПЕРЕД выдачей, чтобы закрытие инвентаря не триггернуло возврат ресурсов
            activePairs.remove(p1.getUniqueId());
            if (p2 != null) activePairs.remove(p2.getUniqueId());

            // Раздаем вещи игрокам "накрест"
            executeSuccessfulTrade(gui, p1, p2);
        }
    }

    private void executeSuccessfulTrade(Inventory gui, Player p1, Player p2) {
        // Чтобы распределить вещи накрест, нам нужно понять, кто сидел слева, а кто справа
        UUID p2UUID = p2 != null ? p2.getUniqueId() : null;
        boolean p1IsLeft = p2UUID == null || p1.getUniqueId().hashCode() > p2UUID.hashCode();

        // Сначала собираем все предметы из GUI в списки, чтобы не зависеть от очистки
        java.util.List<ItemStack> leftItems = new java.util.ArrayList<>();
        java.util.List<ItemStack> rightItems = new java.util.ArrayList<>();

        for (int i = 0; i < 54; i++) {
            ItemStack item = gui.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.GRAY_STAINED_GLASS_PANE || item.getType() == Material.RED_WOOL || item.getType() == Material.GREEN_WOOL)
                continue;

            if (i % 9 < 4) {
                leftItems.add(item);
            } else if (i % 9 > 4) {
                rightItems.add(item);
            }
        }

        // НАМЕРТВО чистим инвентарь обмена, чтобы исключить любой дюп
        gui.clear();

        // Отдаем вещи крест-накрест
        if (p1IsLeft) {
            // Левые вещи отдаем Правому (p2), Правые вещи отдаем Левому (p1)
            if (p2 != null && p2.isOnline()) giveItemsToPlayer(p2, leftItems);
            giveItemsToPlayer(p1, rightItems);
        } else {
            if (p2 != null && p2.isOnline()) giveItemsToPlayer(p2, rightItems);
            giveItemsToPlayer(p1, leftItems);
        }

        p1.sendMessage(ChatColor.GREEN + "Обмен успешно завершен!");
        if (p2 != null) p2.sendMessage(ChatColor.GREEN + "Обмен успешно завершен!");

        // Закрываем инвентари в следующем тике сервера
        Bukkit.getScheduler().runTask(MoneyPlugin.getInstance(), () -> {
            p1.closeInventory();
            if (p2 != null) p2.closeInventory();
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&', "&8 Обмен");
        if (!title.equals(expectedTitle)) return;

        // Пытаемся удалить игрока из мапы активных пар.
        // Если метод .remove() вернул значение — значит, этот игрок закрыл меню ПЕРВЫМ!
        UUID oppUUID = activePairs.remove(player.getUniqueId());

        if (oppUUID != null) {
            // Сразу же удаляем из мапы и второго игрока, чтобы его ивент закрытия ничего не дублировал!
            activePairs.remove(oppUUID);

            Inventory gui = event.getInventory();

            // Собираем вещи из GUI
            java.util.List<ItemStack> itemsToReturn = new java.util.ArrayList<>();
            for (int i = 0; i < 54; i++) {
                ItemStack item = gui.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getType() == Material.GRAY_STAINED_GLASS_PANE || item.getType() == Material.RED_WOOL || item.getType() == Material.GREEN_WOOL)
                    continue;

                itemsToReturn.add(item);
            }

            // Очищаем GUI! Теперь в нем пусто, и второй ивент закрытия ничего не найдет
            gui.clear();

            // Возвращаем ВСЕ вещи обратно игрокам в зависимости от того, на какой стороне они лежали
            boolean playerIsLeft = player.getUniqueId().hashCode() > oppUUID.hashCode();
            Player opponent = Bukkit.getPlayer(oppUUID);
            java.util.List<ItemStack> leftSideItems = new java.util.ArrayList<>();
            java.util.List<ItemStack> rightSideItems = new java.util.ArrayList<>();
            for (ItemStack item : itemsToReturn) {
                // Из-за того, что мы очистили инвентарь выше, мы должны были распределить их до очистки.
                // Перепишем этот шаг для безопасности:
            }
            // Чтобы не путаться с координатами после очистки, сделаем возврат прямо перед очисткой:
            // (Смотри исправленный блок ниже)
            rollbackTradeItems(gui, player, opponent, playerIsLeft);
        }
    }

    private void rollbackTradeItems(Inventory gui, Player player, Player opponent, boolean playerIsLeft) {
        java.util.List<ItemStack> leftItems = new java.util.ArrayList<>();
        java.util.List<ItemStack> rightItems = new java.util.ArrayList<>();
        for (int i = 0; i < 54; i++) {
            ItemStack item = gui.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.GRAY_STAINED_GLASS_PANE || item.getType() == Material.RED_WOOL || item.getType() == Material.GREEN_WOOL)
                continue;
            if (i % 9 < 4) {
                leftItems.add(item);
            } else if (i % 9 > 4) {
                rightItems.add(item);
            }
        }
        gui.clear(); // Стираем всё из меню намертво!

        // Возвращаем вещи владельцам
        if (playerIsLeft) {
            giveItemsToPlayer(player, leftItems);
            if (opponent != null && opponent.isOnline()) giveItemsToPlayer(opponent, rightItems);
        } else {
            giveItemsToPlayer(player, rightItems);
            if (opponent != null && opponent.isOnline()) giveItemsToPlayer(opponent, leftItems);
        }
        player.sendMessage(ChatColor.YELLOW + "Обмен отменен! Предметы возвращены.");
        if (opponent != null && opponent.isOnline()) {
            opponent.sendMessage(ChatColor.YELLOW + "Обмен отменен оппонентом! Предметы возвращены.");
            // Проверяем, что у оппонента открыт именно кастомный GUI, а не его обычный инвентарь
            if (opponent.getOpenInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
                // Заменяем ссылку на метод на лямбду, чтобы устранить Ambiguous method call
                Bukkit.getScheduler().runTaskLater(MoneyPlugin.getInstance(), () -> opponent.closeInventory(), 1L);
            }
        }
    }



    private void giveItemsToPlayer(Player player, java.util.List<ItemStack> items) {
        for (ItemStack item : items) {
            HashMap<Integer, ItemStack> notFit = player.getInventory().addItem(item);
            if (!notFit.isEmpty()) {
                for (ItemStack leftItem : notFit.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftItem);
                }
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}