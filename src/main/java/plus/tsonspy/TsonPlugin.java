package plus.tsonspy;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import plus.tson.*;
import plus.tson.security.ClassManager;
import java.io.File;
import java.nio.charset.StandardCharsets;


/**
 * Base class for all Tson plugins
 */
public class TsonPlugin extends JavaPlugin implements TsonObj {
    private TsonMap map;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        //use groovy script engine defaults
        TsonFunc.COMPILER.setEngine(new GroovyScriptEngineFactory().getScriptEngine());
        try {
            loadConfig();
        } catch (Exception e){
            map = new TsonMap();
            getLogger().warning("Error in config: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * BStats metrics
     * @param id Plugin id
     */
    protected void addMetric(int id){
        Metrics metrics = new Metrics(this, id);
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
            if(b == '\r' || b == '\t'){
                src[i] = ' ';
            }
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
