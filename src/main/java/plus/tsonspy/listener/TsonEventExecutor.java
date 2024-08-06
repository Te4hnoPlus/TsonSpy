package plus.tsonspy.listener;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;


/**
 * Tson Event executor. Faster than {@link org.bukkit.plugin.java.JavaPluginLoader#createRegisteredListeners(Listener, Plugin)}
 */
public class TsonEventExecutor implements EventExecutor {
    public static final TsonEventExecutor INSTANCE = new TsonEventExecutor();
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            ((TsonListener)listener).onEvent(event);
        } catch (Exception e){
            throw new EventException(e);
        }
    }
}