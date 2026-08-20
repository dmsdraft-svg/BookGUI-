package com.bookgui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BookCommand implements CommandExecutor, TabCompleter {
    private final BookGUI plugin;
    private final BookManager bookManager;

    public BookCommand(BookGUI plugin, BookManager bookManager) {
        this.plugin = plugin;
        this.bookManager = bookManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /bookgui reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bookgui.reload")) {
                sender.sendMessage("Нет прав!");
                return true;
            }
            plugin.reload();
            sender.sendMessage("BookGUI перезапущен!");
            return true;
        }

        // /bookgui <книга> [игрок]
        if (args.length < 1) {
            sender.sendMessage("Использование: /bookgui <книга> [игрок]");
            sender.sendMessage("Доступные книги: " + String.join(", ", bookManager.getBookNames()));
            return true;
        }

        String bookName = args[0];

        if (!bookManager.hasBook(bookName)) {
            sender.sendMessage("Книга не найдена: " + bookName);
            return true;
        }

        boolean bypassPermissions = plugin.getConfig().getBoolean("settings.bypass-permissions", false);

        if (args.length >= 2) {
            // Открыть книгу другому игроку
            if (!bypassPermissions && !sender.hasPermission("bookgui.open.others")) {
                sender.sendMessage("Нет прав открывать книги другим!");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("Игрок не найден: " + args[1]);
                return true;
            }
            bookManager.openBook(target, bookName);
            sender.sendMessage("Книга '" + bookName + "' открыта игроку " + target.getName());
        } else {
            // Открыть книгу себе
            if (!(sender instanceof Player)) {
                sender.sendMessage("Эту команду может использовать только игрок");
                return true;
            }
            if (!bypassPermissions && !sender.hasPermission("bookgui.open.self")) {
                sender.sendMessage("Нет прав открывать книги!");
                return true;
            }
            bookManager.openBook((Player) sender, bookName);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            for (String bookName : bookManager.getBookNames()) {
                if (bookName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(bookName);
                }
            }
            return completions;
        }
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
