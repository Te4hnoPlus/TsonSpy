package plus.tsonspy;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import plus.tson.*;
import plus.tson.security.ClassManager;
import plus.tsonspy.listener.TsonEventExecutor;
import plus.tsonspy.listener.TsonListener;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;


/**
 * Base class for all Tson plugins
 */
public class TsonPlugin extends JavaPlugin implements TsonObj {
    private Metrics metrics;
    private static boolean engineInstalled = false;
    private TsonMap map;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        //use groovy script engine defaults
        try {
            loadConfig();
        } catch (Exception e){
            map = new TsonMap();
            getLogger().warning("Error in config: " + e.getMessage());
            e.printStackTrace();
        }
        installEngine(map.getStrSafe("engine"));
    }


    /**
     * Install engine if not installed yet
     * @param name Engine name, if null use default
     */
    private static void installEngine(String name){
        if(!engineInstalled) {
            if(name != null){
                ScriptEngine engine = new ScriptEngineManager().getEngineByName(name);
                if(engine != null){
                    TsonFunc.COMPILER.setEngine(engine);
                    engineInstalled = true;
                    return;
                } else {
                    System.err.println("Script engine [" + name + "] not found, use default");
                }
            }
            TsonFunc.COMPILER.setEngine(new GroovyScriptEngineFactory().getScriptEngine());
            engineInstalled = true;
        }
    }


    /**
     * Register Function in Bukkit event bus
     */
    public static <T extends Event> void registerCustomListener(
            TsonPlugin plugin,
            Class<T> type,
            Function<T,Boolean> listener,
            EventPriority priority
    ){
        registerTsonListener(
                plugin,
                type,
                TsonListener.wrap(listener),
                priority
        );
    }


    /**
     * Register TsonListener in Bukkit event bus
     * @param plugin TsonPlugin instance
     * @param type Event type
     */
    public static <T extends Event> void registerTsonListener(
            TsonPlugin plugin,
            Class<T> type,
            TsonListener<T> listener,
            EventPriority priority
    ){
        if(priority == null)priority = EventPriority.NORMAL;

        plugin.getServer().getPluginManager().registerEvent(
                type,
                listener,
                priority,
                TsonEventExecutor.INSTANCE, plugin
        );
    }


    /**
     * Safe calculate event priority
     */
    protected static EventPriority priorityOf(int i){
        EventPriority[] values = EventPriority.values();
        if(i >= values.length)i = values.length-1;
        if(i < 0)i = 0;
        return values[i];
    }


    /**
     * BStats metrics
     * @param id Plugin id
     */
    protected void addMetric(int id){
        try {
            metrics = new Metrics(this, id);
        } catch (Throwable e){
            getLogger().warning("Metrics error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Load Tson from file. Use STsonParser
     * @param name Config file name
     */
    protected TsonMap readSTson(String name){
        byte[] bytes = TsonFile.read(new File(getDataFolder(), name), "")
                .getBytes(StandardCharsets.UTF_8);
        fixBadChars(bytes);

        STsonParser parser = new STsonParser(bytes, new ClassManager.Def()).readImports();
        return parser.getMap();
    }


    /**
     * Replace bad bytes from win-style line endings
     */
    private static void fixBadChars(byte[] src){
        for (int i = 0, s = src.length; i < s; i++){
            byte b = src[i];
            if(b == '\r' || b == '\t') src[i] = ' ';
        }
    }


    /**
     * Load default config
     */
    private void loadConfig(){
        this.map = readSTson("config.tson");
    }


    @Override
    public void reloadConfig() {
        loadConfig();
        super.reloadConfig();
    }


    @Override
    public TsonMap getMap() {
        return map;
    }


    @Override
    public void onDisable() {
        if(metrics != null) metrics.shutdown();
    }


    /**
     * Map method emulation, see {@link TsonMap#getBool(String)}
     * It is assumed that a value with this key will exist, and of type TsonBool
     * @return boolean by key.
     */
    public final boolean getBool(String key){
        return map.getBool(key);
    }


    /**
     * Map method emulation, see {@link TsonMap#getInt(String)}
     * It is assumed that a value with this key will exist, and of type TsonInt
     * @return int by key.
     */
    public final int getInt(String key){
        return map.getInt(key);
    }


    /**
     * Map method emulation, see {@link TsonMap#getStr(String)}
     * It is assumed that a non-custom value with this key will exist
     * @return string by key
     */
    public String getStr(String key){
        return map.getStr(key);
    }


    /**
     * Map method emulation, see {@link TsonMap#getField(String)}
     * It is assumed that value with this key will exist
     * @return Raw object by key
     */
    public Object getField(String key){
        return map.getField(key);
    }


    /**
     * Map method emulation, see {@link TsonMap#getCustom(String)}
     * It is assumed that value with this key will exist
     * @return Custom object by key and automatically cast
     */
    public <T> T getCustom(String key){
        return map.getCustom(key);
    }


    /**
     * Map method emulation
     * @return real TsonMap
     */
    @Override
    public TsonMap getField() {
        return map;
    }


    /**
     * Map method emulation, see {@link TsonMap#type()}
     */
    @Override
    public Type type() {
        return Type.MAP;
    }


    @Override
    public TsonPlugin clone() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void saveDefaultConfig() {
        //ignore default 'config.yaml', use only 'config.tson'
        saveResource("config.tson", false);
    }
}
