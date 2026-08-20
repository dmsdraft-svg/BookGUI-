package com.bookgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.WritableBookMeta;

import java.io.File;
import java.util.*;

public class BookManager {
    private final BookGUI plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, BookData> books = new HashMap<>();

    public BookManager(BookGUI plugin) {
        this.plugin = plugin;
    }

    public void loadBooks() {
        books.clear();
        File booksFolder = new File(plugin.getDataFolder(), "books");
        
        if (!booksFolder.exists()) {
            booksFolder.mkdirs();
            createDefaultBook(booksFolder);
        }

        File[] files = booksFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                loadBook(file);
            }
        }
    }

    public void reload() {
        loadBooks();
    }

    private void createDefaultBook(File booksFolder) {
        File rulesFile = new File(booksFolder, "rules.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("title", "Правила Сервера");
        config.set("author", "Администрация");
        
        List<String> pages = new ArrayList<>();
        pages.add("<red><bold>⚡ ПРАВИЛА СЕРВЕРА ⚡</bold></red>\n\nЗдарова. Короче, расклад такой, читай внимательно:\n\n1. <gold>Гриферство</gold> — это для слабых. Трогать чужие постройки запрещено.\n2. <gold>Воровство и скам</gold> — кидать своих не ок. Хочешь играть нормально — играй по честному.");
        pages.add("3. <red>Читы = Бан</red>. Даже не думай. Античит не спит, а админы не любят шутников.\n\n4. <dark_purple>Токсичность и оскорбления</dark_purple>. Будешь вести себя неадекватно — получишь мут. Да-да, мы тут умеем отправлять в молчанку тех, кто не умеет формулировать мысли без мата. По приколу? Вполне.");
        pages.add("5. <green>Видишь нарушителя?</green>\n\nНе надо решать вопросы топором или спамить в общий чат. Просто используй:\n<click:run_command:/report><hover:show_text:'Нажми, чтобы написать /report'>/report <ник> <причина></hover></click>\n\n<gray><i>Уважай других, и всё будет чётко. Хорошей игры!</i></gray>");
        
        config.set("pages", pages);
        try {
            config.save(rulesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBook(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String bookName = file.getName().replace(".yml", "");

            String title = config.getString("title", "Книга");
            String author = config.getString("author", "Неизвестно");
            List<String> pages = config.getStringList("pages");

            if (pages.isEmpty()) {
                plugin.getLogger().warning("Книга " + bookName + " не имеет страниц!");
                return;
            }

            books.put(bookName, new BookData(title, author, pages));
            plugin.getLogger().info("Загружена книга: " + bookName);
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки книги " + file.getName() + ": " + e.getMessage());
        }
    }

    public void openBook(Player player, String bookName) {
        BookData book = books.get(bookName);
        if (book == null) {
            player.sendMessage("<red>Книга не найдена: " + bookName + "</red>");
            return;
        }

        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        WritableBookMeta meta = (WritableBookMeta) bookItem.getItemMeta();

        meta.setTitle(Component.text(book.getTitle()));
        meta.setAuthor(Component.text(book.getAuthor()));

        List<Component> components = new ArrayList<>();
        for (String page : book.getPages()) {
            try {
                components.add(miniMessage.deserialize(page));
            } catch (Exception e) {
                components.add(Component.text(page));
            }
        }
        meta.pages(components);
        bookItem.setItemMeta(meta);

        player.openBook(bookItem);
    }

    public boolean hasBook(String bookName) {
        return books.containsKey(bookName);
    }

    public Set<String> getBookNames() {
        return books.keySet();
    }

    public int getBookCount() {
        return books.size();
    }

    public static class BookData {
        private final String title;
        private final String author;
        private final List<String> pages;

        public BookData(String title, String author, List<String> pages) {
            this.title = title;
            this.author = author;
            this.pages = pages;
        }

        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public List<String> getPages() { return pages; }
    }
}
