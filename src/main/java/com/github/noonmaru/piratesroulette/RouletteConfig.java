package com.github.noonmaru.piratesroulette;

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.item.TapItem;
import com.github.noonmaru.tap.item.TapItemStack;
import com.google.common.collect.ImmutableList;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nemo
 */
public class RouletteConfig
{
    public static double spotGap;

    public static int spotLine;

    public static Vector pos;

    public static List<TapItemStack> swordItems;

    public static void load(ConfigurationSection config)
    {
        spotGap = config.getDouble("spot-gap");
        spotLine = config.getInt("spot-line");
        pos = getVector(config.getConfigurationSection("pos"));
        swordItems = getItems(config.getStringList("sword-items"));
    }

    private static Vector getVector(ConfigurationSection config)
    {
        double x = config.getDouble("x");
        double y = config.getDouble("y");
        double z = config.getDouble("z");

        return new Vector(x, y, z);
    }

    private static List<TapItemStack> getItems(List<String> list)
    {
        ArrayList<TapItemStack> items = new ArrayList<>(list.size());

        for (String name : list)
        {
            TapItem item = Tap.ITEM.getItem(name);

            if (item != null)
            {
                items.add(Tap.ITEM.newItemStack(item, 1, 0));
            }
        }

        return ImmutableList.copyOf(items);
    }
}
