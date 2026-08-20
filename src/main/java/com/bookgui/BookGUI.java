package com.bookgui;

import org.bukkit.plugin.java.JavaPlugin;

public class BookGUI extends JavaPlugin {
    private static BookGUI instance;
    private BookManager bookManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        bookManager = new BookManager(this);
        bookManager.loadBooks();
        
        BookCommand command = new BookCommand(this, bookManager);
        getCommand("bookgui").setExecutor(command);
        getCommand("bookgui").setTabCompleter(command);
        
        getLogger().info("BookGUI включен! Загружено книг: " + bookManager.getBookCount());
    }

    @Override
    public void onDisable() {
        getLogger().info("BookGUI выключен!");
    }

    public static BookGUI getInstance() {
        return instance;
    }

    public BookManager getBookManager() {
        return bookManager;
    }

    public void reload() {
        reloadConfig();
        bookManager.reload();
        getLogger().info("BookGUI перезапущен!");
    }
}
