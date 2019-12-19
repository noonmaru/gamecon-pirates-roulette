package com.github.noonmaru.piratesroulette;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author Nemo
 */
public class ConfigReloader implements Runnable
{
    private final File file;

    private long lastModified;

    private final Consumer<ConfigurationSection> applier;

    public ConfigReloader(File file, Consumer<ConfigurationSection> applier)
    {
        this.file = file;
        this.lastModified = file.lastModified();
        this.applier = applier;
    }

    @Override
    public void run()
    {
        long last = file.lastModified();

        if (last != this.lastModified)
        {
            this.lastModified = last;
            applier.accept(YamlConfiguration.loadConfiguration(file));
        }
    }
}
