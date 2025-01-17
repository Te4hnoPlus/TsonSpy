package plus.tsonspy.listener;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import plus.tson.TsonFunc;
import plus.tsonspy.TsonPlugin;
import java.util.function.Function;


/**
 * Bukkit listener adapter for TsonFunc
 */
public final class TsonListener<T extends Event> implements Listener {
    private final TsonFunc func;

    public TsonListener(TsonFunc func) {
        int count = func.countArgs();
        if(count != -1 && count != 1){
            throw new IllegalArgumentException("Listener func must have 1 argument");
        }
        this.func = func;
    }


    /**
     * Adapt Function to TsonListener
     */
    public static <C extends Event> TsonListener<C> wrap(Function<C,Boolean> func){
        if(func instanceof TsonFunc){
            return new TsonListener<>((TsonFunc) func);
        }
        return new TsonListener<>(args -> func.apply((C) args[0]));
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