package me.Toivow.tervehdys;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Tervehdys extends JavaPlugin implements Listener, CommandExecutor {

    private FileConfiguration lang;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private String joinEnabledPath;
    private String joinMessagePath;
    private String quitEnabledPath;
    private String quitMessagePath;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        saveResourceIfMissing("languages/fi.yml");
        saveResourceIfMissing("languages/en.yml");

        loadLanguage();

        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("tervehdys") != null) {
            getCommand("tervehdys").setExecutor(this);
        }

        getLogger().info("Tervehdys käynnistyi! Kieli: " + getConfig().getString("language", "fi"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("tervehdys")) {
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tervehdys.reload")) {
                sender.sendMessage(miniMessage.deserialize("<red>Ei oikeuksia käyttää tätä komentoa."));
                return true;
            }

            reloadConfig();
            loadLanguage();

            sender.sendMessage(miniMessage.deserialize(
                    "<green>Tervehdys ladattu uudelleen! Kieli: <white>" + getConfig().getString("language", "fi")));
            return true;
        }

        sender.sendMessage(miniMessage.deserialize("<yellow>Käyttö: /" + label + " reload"));
        return true;
    }

    private void loadLanguage() {
        String language = getConfig().getString("language", "fi");
        File langFile = new File(getDataFolder(), "languages/" + language + ".yml");

        if (!langFile.exists()) {
            getLogger().warning("Kielitiedostoa '" + language + ".yml' ei löytynyt, käytetään suomea.");
            langFile = new File(getDataFolder(), "languages/fi.yml");
        }

        lang = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = getResource("languages/" + langFile.getName());
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            lang.setDefaults(defaultConfig);
        }

        String actualLanguage = langFile.getName().replace(".yml", "");
        if (actualLanguage.equalsIgnoreCase("fi")) {
            joinEnabledPath = "liittyminen.käytössä";
            joinMessagePath = "liittyminen.viesti";
            quitEnabledPath = "poistuminen.käytössä";
            quitMessagePath = "poistuminen.viesti";
        } else {
            joinEnabledPath = "joined.enabled";
            joinMessagePath = "joined.message";
            quitEnabledPath = "left.enabled";
            quitMessagePath = "left.message";
        }
    }

    private void saveResourceIfMissing(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            try {
                saveResource(resourcePath, false);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Resurssia " + resourcePath + " ei löytynyt jarista.");
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!lang.getBoolean(joinEnabledPath, true)) {
            event.joinMessage(null);
            return;
        }

        String raw = lang.getString(joinMessagePath, "<green>{pelaaja} liittyi peliin.");
        String parsed = raw.replace("{pelaaja}", event.getPlayer().getName());
        Component message = miniMessage.deserialize(parsed);

        event.joinMessage(message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!lang.getBoolean(quitEnabledPath, true)) {
            event.quitMessage(null);
            return;
        }

        String raw = lang.getString(quitMessagePath, "<gray>{pelaaja} poistui pelistä.");
        String parsed = raw.replace("{pelaaja}", event.getPlayer().getName());
        Component message = miniMessage.deserialize(parsed);

        event.quitMessage(message);
    }
}