package site.zvolcan.kuClans;

import com.zaxxer.hikari.HikariDataSource;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import site.zvolcan.kuClans.commands.ClanChatCommand;
import site.zvolcan.kuClans.commands.ClanCommand;
import site.zvolcan.kuClans.commands.MainCommand;
import site.zvolcan.kuClans.commands.impl.CommandImpl;
import site.zvolcan.kuClans.listener.PlayerChatListener;
import site.zvolcan.kuClans.listener.PlayerCombatListener;
import site.zvolcan.kuClans.listener.PlayerConnectListener;
import site.zvolcan.kuClans.manager.ClansManager;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.manager.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public final class KuClans extends JavaPlugin {

    private static KuClans instance;
    private HikariDataSource hikari;
    private ClansManager clansManager;
    private LanguageManager languageManager;
    private DataManager dataManager;
    private PlaceholdersKuClans placeholdersKuClans;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        languageManager = new LanguageManager(this);

        setupHakari(getConfig());
        clansManager = new ClansManager(this);
        dataManager = new DataManager(clansManager);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholdersKuClans = new PlaceholdersKuClans(this, dataManager, clansManager);
            placeholdersKuClans.register();
        }

        registerCommands();
        registerListeners();
    }

    // Función hecha por la IA
    private void setupHakari(FileConfiguration config) {
        hikari = new HikariDataSource();
        String databaseType = config.getString("database.type", "MYSQL").toUpperCase();

        if (databaseType.equals("SQLITE")) {
            // Configuración para SQLite
            hikari.setDriverClassName("org.sqlite.JDBC");
            // La base de datos será un archivo llamado "clans.db" dentro de la carpeta de tu plugin.
            String dbPath = this.getDataFolder().getAbsolutePath() + "/clans.db";
            hikari.setJdbcUrl("jdbc:sqlite:" + dbPath);
        } else { // Por defecto o si es MYSQL
            // Configuración para MySQL (la que ya tenías)
            String host = config.getString("database.host", "localhost");
            int port = config.getInt("database.port", 3306);
            String dbName = config.getString("database.database", "kudb");
            String user = config.getString("database.username", "root");
            String pass = config.getString("database.password", "");

            hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false");
            hikari.setUsername(user);
            hikari.setPassword(pass);
        }

        // Propiedades comunes para mejorar el rendimiento
        hikari.addDataSourceProperty("cachePrepStmts", "true"); // Habilitar caché de statements
        hikari.addDataSourceProperty("prepStmtCacheSize", "250"); // Tamaño de la caché
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // Límite de tamaño del SQL en caché
        hikari.setMaximumPoolSize(10); // Número máximo de conexiones en el pool
    }

    @Override
    public void onDisable() {
        clansManager.shutDown();
        dataManager.shutDown();

        if (hikari != null) {
            hikari.close();
        }
    }

    public static KuClans getPlugin() {
        return instance;
    }

    public HikariDataSource getHikari() {
        return hikari;
    }

    private void registerCommands() {
        final List<CommandImpl> commands = new ArrayList<>();
        commands.add(new MainCommand(languageManager, placeholdersKuClans, this));
        commands.add(new ClanChatCommand(dataManager, languageManager));
        commands.add(new ClanCommand(this, clansManager, languageManager));

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, (event) -> {
            for (CommandImpl cmd : commands) {
                event.registrar().register(cmd.command());
            }
        });
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectListener(dataManager), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(dataManager, this), this);
        getServer().getPluginManager().registerEvents(new PlayerCombatListener(dataManager), this);
    }

    public ClansManager getClansManager() {
        return clansManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
