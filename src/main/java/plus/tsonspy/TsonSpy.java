package plus.tsonspy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import plus.tson.*;
import plus.tsonspy.listener.TsonListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import static plus.tson.ext.TsonNumUtils.*;


/**
 * Bootstrap TsonSpy
 */
public final class TsonSpy extends TsonPlugin {
    @Override
    public void onEnable() {
        super.onEnable();
        runIfExists("onEnable");
        getMap().ifContainsList("listeners", this::initListeners);
        addMetric(0xB30C >> 1);
    }


    /**
     * Load list listeners, see {@link TsonSpy#initListener(TsonMap)}
     * @param list TsonList of TsonMaps
     */
    private void initListeners(TsonList list){
        for (TsonObj obj:list) try {
            initListener(obj.getMap());
        } catch (Throwable e){
            getLogger().log(Level.SEVERE, "Error in listener: ["+obj+"]", e);
        }
    }


    /**
     * Load and register TsonListener, use TsonEventExecutor for acceleration
     */
    private <T extends Event> void initListener(TsonMap map){
        Class<T> type = map.getCustom("type");
        Object raw = map.getCustom("func");

        TsonListener<T> listener;
        if(raw instanceof TsonFunc.Frame){
            listener = TsonListener.compile(this, (TsonFunc.Frame) raw);
        } else if(raw instanceof TsonFunc){
            TsonFunc func = (TsonFunc) raw;
            listener = new TsonListener<>(func);
        } else if(raw instanceof String){
            String[] args0;
            TsonObj args = map.get("args");
            if(args != null) {
                if (args.isString()) {
                    args0 = new String[]{args.getStr()};
                } else if (args.isList()) {
                    TsonList list = args.getList();
                    args0 = new String[list.size()];
                    for (int i = 0; i < args0.length; i++) {
                        args0[i] = list.getStr(i);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported args: " + args);
                }
            } else {
                args0 = new String[]{"event"};
            }
            byte[] bytes = TsonFile.read(new File(getDataFolder(), (String) raw), "")
                    .getBytes(StandardCharsets.UTF_8);
            if(bytes.length == 0){
                throw new IllegalArgumentException("Listener file is empty: " + raw);
            }

            TsonFunc.Frame frame = new TsonFunc.Frame(this, bytes, 0, bytes.length, args0);

            listener = TsonListener.compile(this, frame);
        } else{
            throw new IllegalArgumentException("Listener func must be TsonFunc");
        }

        registerTsonListener(this, type, listener, priorityOf(calc(map, "priority", 2)));
    }


    @Override
    public void reloadConfig() {
        //run scripts if exists
        TsonObj tsonObj = getMap().get("onReload");
        if(tsonObj.isFunc()){
            runIfExists(getMap(), "onReload");
        } else if(tsonObj.isMap()){
            tsonObj.getMap().ifContainsFunc("pre", func -> func.call(TsonSpy.this));
        }

        super.getConfig();

        if(tsonObj.isMap()){
            tsonObj.getMap().ifContainsFunc("post", func -> func.call(TsonSpy.this));
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //process command scripts if exists
        TsonFunc func = getFunc(getMap(), "onCommand");
        if(func != null){
            Object result = func.call(sender, command, label, args);
            if(result == null)return true;
            if(result instanceof Boolean)return (boolean) result;
            return true;
        }
        return false;
    }


    @Override
    public void onDisable() {
        super.onDisable();
        //run scripts if exists
        runIfExists("onDisable");
    }


    /**
     * Run TsonFunc if exists in root config map by key, see {@link TsonSpy#runIfExists(TsonMap, String)}
     * @param key Target key
     */
    private void runIfExists(String key){
        runIfExists(getMap(), key);
    }


    /**
     * Run TsonFunc if exists in target map by key
     * @param map Target map
     * @param key Target key
     */
    private void runIfExists(TsonMap map, String key){
        TsonFunc func = getFunc(map, key);
        if(func != null)func.call();
    }


    /**
     * Get TsonFunc from TsonMap by key
     * <br>
     * If TsonFunc exists as Frame, compile it and replace
     * @param map Target map
     * @param key Target key
     * @return TsonFunc if exists, else null
     */
    private TsonFunc getFunc(TsonMap map, String key){
        if(map == null)return null;
        TsonObj func = map.get(key);
        if(func != null && func.isCustom()){
            Object obj = func.getField();
            if(obj instanceof TsonFunc) return (TsonFunc) obj;

            if(obj instanceof TsonFunc.Frame){
                TsonFunc.Frame frame = (TsonFunc.Frame) obj;
                if(frame.getInst() == null){
                    if(frame.hasInst()) frame.setInst(this);
                }
                TsonFunc func1 = TsonFunc.COMPILER.compile(frame);
                map.put(key, func1);
                return func1;
            }
        }
        return null;
    }
}
