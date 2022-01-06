package net.kigawa.template.spigotplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;

public final class SpigotPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register("aaa",new Command("aaa"));
    }
}

class Command extends BukkitCommand {

    protected Command(String name) {
        super(name,"bbb","ccc",new LinkedList<>());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return false;
    }
}