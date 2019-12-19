package com.github.noonmaru.piratesroulette.process;

import com.github.noonmaru.tap.entity.TapPlayer;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Nemo
 */
public class Gambler
{
    private final RouletteProcess process;

    private final UUID uniqueId;

    private String name;

    private Player player;

    private final TapItemStack item;

    private Gambler next;

    private TapPlayer tapPlayer;

    public Gambler(RouletteProcess process, Player player, TapItemStack item)
    {
        this.process = process;
        this.uniqueId = player.getUniqueId();
        this.item = item;

        setPlayer(player);
    }

    public UUID getUniqueId()
    {
        return uniqueId;
    }

    public void setNext(Gambler next)
    {
        this.next = next;
    }

    public Gambler getNext()
    {
        return next;
    }

    public String getName()
    {
        return name;
    }

    public void setPlayer(Player player)
    {
        if (player != null)
        {
            this.player = player;
            this.name = player.getName();
            this.tapPlayer = TapPlayer.wrapPlayer(player);
        }
        else
        {
            this.player = null;
            this.tapPlayer = null;
        }
    }

    public Player getPlayer()
    {
        return player;
    }

    public TapPlayer getTapPlayer()
    {
        return tapPlayer;
    }

    public boolean hasTurn()
    {
        return process.getCurrentGambler() == this;
    }

    public TapItemStack getSwordItem()
    {
        return item;
    }

    public void ready()
    {
        if (player != null)
        {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);

            Packet.TITLE.compound("", ChatColor.RESET + "당신의 차례입니다!", 0, 40, 5).sendTo(player);
        }
    }
}
