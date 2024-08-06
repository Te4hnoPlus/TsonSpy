import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerJoinEvent
import plus.tsonspy.TsonPlugin

/**
 * Templates for scripts
 */

def onEnable(TsonPlugin inst){
    println "Hello, Tson Spy :3"
}

def onDisable(TsonPlugin inst){
    println inst.name + " Disabled"
}

def exampleEvent(PlayerJoinEvent event){

}

def onCommand(TsonPlugin inst, CommandSender sender, Command command, String label, String[] args){
    println "Try call command " + String.join(" ", args)
}