package plus.tsonspy.listener;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import plus.tson.TsonFunc;
import plus.tsonspy.TsonPlugin;


/**
 * Bukkit listener adapter for TsonFunc
 */
public final class TsonListener<T extends Event> implements Listener {
    private final TsonFunc func;

    TsonListener(TsonFunc func) {
        this.func = func;
    }


    /**
     * Compile Frame to TsonListener
     * @param plugin TsonPlugin instance
     * @param frame Tson Function`s frame
     */
    public static <C extends Event> TsonListener<C> compile(TsonPlugin plugin, TsonFunc.Frame frame){
        if(frame.getInst() == null){
            if(frame.hasInst()){
                frame.setInst(plugin);
            }
        }
        TsonFunc func0 = TsonFunc.COMPILER.compile(frame);
        return new TsonListener<>(func0);
    }


    /**
     * Handle Bukkit event, see {@link TsonEventExecutor#execute(Listener, Event)}
     */
    void onEvent(T event){
        func.call(event);
    }
}