package plus.tsonspy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import plus.tson.*;
import plus.tsonspy.listener.TsonEventExecutor;
import plus.tsonspy.listener.TsonListener;
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
        addMetric(0x5986);
    }


    /**
     * Load list listeners, see {@link TsonSpy#initListener(TsonMap)}
     * @param list TsonList of TsonMaps
     */
    private void initListeners(TsonList list){
        for (TsonObj obj:list) initListener(obj.getMap());
    }


    /**
     * Load and register TsonListener, use TsonEventExecutor for acceleration
     */
    private void initListener(TsonMap map){
        Class<? extends Event> type = map.getCustom("type");
        TsonFunc.Frame frame = map.getCustom("func");
        TsonListener<?> listener = TsonListener.compile(this, frame);

        int priority = calc(map, "priority", 2);

        getServer().getPluginManager().registerEvent(
                type,
                listener,
                priorityOf(priority),
                TsonEventExecutor.INSTANCE, this
        );
    }


    /**
     * Safe calculate event priority
     */
    private static EventPriority priorityOf(int i){
        EventPriority[] values = EventPriority.values();
        if(i >= values.length)i = values.length-1;
        if(i < 0)i = 0;
        return values[i];
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
            if(obj instanceof TsonFunc) {
                return (TsonFunc) obj;
            }
            if(obj instanceof TsonFunc.Frame){
                TsonFunc.Frame frame = (TsonFunc.Frame) obj;
                if(frame.getInst() == null){
                    if(frame.hasInst()){
                        frame.setInst(this);
                    }
                }
                TsonFunc func1 = TsonFunc.COMPILER.compile(frame);
                map.put(key, func1);
                return func1;
            }
        }
        return null;
    }
}
