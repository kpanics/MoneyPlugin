package ru.kpanicshop.moneyPlugin;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TradeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // 1. Сначала проверяем, что команду ввёл именно игрок
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду могут использовать только игроки!");
            return true;
        }

        // 2. Проверяем длину аргументов (введён ли ник игрока)
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /trade [ник игрока]");
            return true;
        }

        // 3. Берем первый аргумент из массива — это ник цели обмена
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Игрок отсутствует либо вы ошиблись ником");
            return true;
        }

        // 4. Проверяем, чтобы игрок не торговал сам с собой
        if (target.equals(player)) {
            player.sendMessage(ChatColor.GOLD + "Вы не можете торговать с этим игроком");
            return true;
        }

        // 5. Создаем объект нашего GUI и открываем его для обоих игроков!
        TradeGui tradeGui = new TradeGui();
        tradeGui.openTradeMenu(player, target);

        return true;
    }
}
