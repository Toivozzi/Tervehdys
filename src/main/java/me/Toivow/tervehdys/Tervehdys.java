package me.Toivow.tervehdys;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Tervehdys extends JavaPlugin implements Listener, CommandExecutor {

    private FileConfiguration lang;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    private String joinEnabledPath;
    private String joinMessagePath;
    private String quitEnabledPath;
    private String quitMessagePath;

    private LuckPerms luckPerms;
    private boolean mukautettuChatKaytossa;
    private String mukautettuChatFormaatti;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        saveResourceIfMissing("languages/fi.yml");
        saveResourceIfMissing("languages/en.yml");

        loadLanguage();
        loadMukautettuChat();
        setupLuckPerms();

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
            loadMukautettuChat();

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

    private void loadMukautettuChat() {
        mukautettuChatKaytossa = getConfig().getBoolean("Mukautettu-Chat.Käytössä", false);
        mukautettuChatFormaatti = getConfig().getString("Mukautettu-Chat.Formaatti", "%lprank% %player% &f%message%");
    }

    private void setupLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return;
        }

        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
    }

    private String getLuckPermsRank(Player player) {
        if (luckPerms == null) {
            return "";
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return "";
        }

        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
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

        String raw = lang.getString(joinMessagePath, "&a%player% liittyi peliin.");
        String parsed = raw.replace("%player%", event.getPlayer().getName())
                .replace("%lprank%", getLuckPermsRank(event.getPlayer()));
        Component message = legacySerializer.deserialize(parsed);

        event.joinMessage(message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!lang.getBoolean(quitEnabledPath, true)) {
            event.quitMessage(null);
            return;
        }

        String raw = lang.getString(quitMessagePath, "&7%player% poistui pelistä.");
        String parsed = raw.replace("%player%", event.getPlayer().getName())
                .replace("%lprank%", getLuckPermsRank(event.getPlayer()));
        Component message = legacySerializer.deserialize(parsed);

        event.quitMessage(message);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (!mukautettuChatKaytossa) {
            return;
        }

        Player player = event.getPlayer();
        String rank = getLuckPermsRank(player);
        String format = mukautettuChatFormaatti
                .replace("%lprank%", rank)
                .replace("%player%", player.getName());

        int messageIndex = format.indexOf("%message%");
        String prefixPart = messageIndex >= 0 ? format.substring(0, messageIndex) : format;
        String suffixPart = messageIndex >= 0 ? format.substring(messageIndex + "%message%".length()) : "";

        Component prefixComponent = legacySerializer.deserialize(prefixPart);
        Component suffixComponent = legacySerializer.deserialize(suffixPart);

        event.renderer((source, sourceDisplayName, message, viewer) ->
                Component.empty().append(prefixComponent).append(message).append(suffixComponent));
    }
}