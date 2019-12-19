package com.github.noonmaru.piratesroulette.process;

import com.github.noonmaru.piratesroulette.PiratesRoulettePlugin;
import com.github.noonmaru.piratesroulette.RouletteConfig;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Nemo
 */
public class RouletteProcess
{
    private final PiratesRoulettePlugin plugin;

    private final RouletteListener listener;

    private final BukkitTask task;

    private final Roulette roulette;

    private final Map<UUID, Gambler> gamblers = new HashMap<>();

    private final Map<Player, Gambler> onlineGamblers = new IdentityHashMap<>();

    private Gambler currentGambler;

    public RouletteProcess(PiratesRoulettePlugin plugin, TapItemStack blockItem)
    {
        this.plugin = plugin;
        this.roulette = new Roulette(blockItem);

        List<TapItemStack> swords = RouletteConfig.swordItems;
        int swordIndex = 0;

        for (Player player : Bukkit.getOnlinePlayers())
        {
            GameMode mode = player.getGameMode();

            if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR)
                continue;

            Gambler gambler = new Gambler(this, player, swords.get(swordIndex++ % swords.size()));

            gamblers.put(gambler.getUniqueId(), gambler);
            onlineGamblers.put(player, gambler);
        }

        ArrayList<Gambler> order = new ArrayList<>(gamblers.values());
        Collections.shuffle(order);

        Logger logger = plugin.getLogger();
        int size = order.size();

        for (int i = 0; i < size; i++)
        {
            Gambler gambler = order.get(i);
            Gambler next = order.get((i + 1) % size);
            gambler.setNext(next);

            logger.info((i + 1) + ". " + gambler.getName());
        }

        this.listener = new RouletteListener(this);
        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, new RouletteScheduler(this), 0, 1);

        this.roulette.spawnTo(Bukkit.getOnlinePlayers());
        this.roulette.updateCustomEntity(Bukkit.getOnlinePlayers());

        for (Gambler value : gamblers.values())
        {
            value.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    void onJoin(Player player)
    {
        Gambler gambler = this.gamblers.get(player.getUniqueId());

        if (gambler != null)
        {
            gambler.setPlayer(player);
            this.onlineGamblers.put(player, gambler);

            if (currentGambler == gambler)
                gambler.ready();
            else
                player.setGameMode(GameMode.SPECTATOR);
        }
    }

    void onQuit(Player player)
    {
        Gambler gambler = this.onlineGamblers.remove(player);

        if (gambler != null)
        {
            gambler.setPlayer(null);
        }
    }

    public Gambler getGambler(Player player)
    {
        return this.onlineGamblers.get(player);
    }

    public Roulette getRoulette()
    {
        return roulette;
    }

    public void unregister()
    {
        HandlerList.unregisterAll(this.listener);
        task.cancel();

        roulette.destroy();

        for (Gambler gambler : onlineGamblers.values())
        {
            Player player = gambler.getPlayer();

            if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)
            {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
            else
            {
                player.setGameMode(GameMode.ADVENTURE);
            }
        }
    }

    public Gambler getCurrentGambler()
    {
        return this.currentGambler;
    }

    public void stop()
    {
        plugin.stopProcess();
    }

    public void ready()
    {
        Random random = new Random();
        ArrayList<Gambler> gamblers = new ArrayList<>(this.gamblers.values());

        this.currentGambler = gamblers.get(random.nextInt(gamblers.size()));
        currentGambler.ready();
        broadcastGambler();
    }

    public void nextGambler()
    {
        Player player = currentGambler.getPlayer();

        if (player != null)
            player.setGameMode(GameMode.SPECTATOR);

        this.currentGambler = currentGambler.getNext();
        broadcastGambler();
        currentGambler.ready();
    }

    public void broadcastGambler()
    {
        Packet.TITLE.compound("", currentGambler.getName(), 0, 40, 5).sendTo(Bukkit.getOnlinePlayers().stream().filter(o -> o != currentGambler).collect(Collectors.toList()));
    }
}
