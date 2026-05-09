package me.pantrypins.plugin.utils.commands;

import me.pantrypins.plugin.PantryPins;

public abstract class BaseCommand {

    public BaseCommand() {
        PantryPins.getInstance().getCommandFramework().registerCommands(this, null);
    }

    public abstract void executeAs(CommandArguments command);
}
