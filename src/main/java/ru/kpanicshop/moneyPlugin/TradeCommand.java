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

        // 1. Сначала объявляем игрока (делаем каст из sender)
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду могут использовать только игроки!");
            return true;
        }

        // 2. Проверяем длину аргументов (исправлено на length)
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /trade [ник игрока]");
            return true;
        }

        // 3. Берем первый аргумент из массива — это ник цели args[0]
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Игрок отсутствует либо вы ошиблись ником");
            return true; // Не забудь тут return true, чтобы код остановился, если игрока нет!
        }

        // 4. Проверяем, чтобы игрок не торговал сам с собой
        if (target.equals(player)) {
            player.sendMessage(ChatColor.GOLD + "Вы не можете торговать с этим игроком");
            return true;
        }

        // 5. Отправляем сообщения об успешном запросе
        player.sendMessage(ChatColor.GREEN + "Вы отправили запрос на обмен игроку с ником: " + target.getName());
        target.sendMessage(ChatColor.GREEN + "Игрок " + player.getName() + " хочет обменяться с вами!");

        return true;
    } // <--- Вот эта скобка должна закрывать метод в самом конце!
}
