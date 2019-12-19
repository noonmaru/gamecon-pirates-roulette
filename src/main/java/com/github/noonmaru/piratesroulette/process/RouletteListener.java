package com.github.noonmaru.piratesroulette.process;

import com.github.noonmaru.customentity.CustomEntityPacket;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.piratesroulette.RouletteConfig;
import com.github.noonmaru.tap.firework.FireworkEffect;
import com.github.noonmaru.tap.math.BoundingBox;
import com.github.noonmaru.tap.math.RayTraceResult;
import com.github.noonmaru.tap.packet.Packet;
import com.github.noonmaru.tap.sound.SoundCategory;
import com.github.noonmaru.tap.sound.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.Collections;

/**
 * @author Nemo
 */
public class RouletteListener implements Listener
{
    private final RouletteProcess process;

    public RouletteListener(RouletteProcess process)
    {
        this.process = process;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        process.onJoin(player);
        process.getRoulette().spawnTo(Collections.singleton(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        process.onQuit(player);
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event)
    {
        if (event.getChannel().equalsIgnoreCase(CustomEntityPacket.CHANNEL))
        {
            process.getRoulette().updateCustomEntity(Collections.singleton(event.getPlayer()));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
            event.setCancelled(true);
    }

    @EventHandler
    public void onAnimation(PlayerAnimationEvent event)
    {
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
        {
            Player player = event.getPlayer();
            Gambler gambler = process.getGambler(player);

            if (gambler != null && gambler.hasTurn())
            {
                Location loc = player.getEyeLocation();
                org.bukkit.util.Vector v = loc.getDirection().multiply(8);
                Vector from = new Vector(loc.getX(), loc.getY(), loc.getZ());
                Vector to = from.copy().add(v.getX(), v.getY(), v.getZ());

                Roulette roulette = process.getRoulette();
                BoundingBox box = roulette.getBox();
                RayTraceResult result = box.calculateRayTrace(from, to);

                if (result != null)
                {
                    Spot spot = roulette.getNearestSpot(new Vector(result.getX(), result.getY(), result.getZ()));

                    if (spot != null && !spot.isStabbed())
                    {
                        if (roulette.getWeakness() == spot) //적중
                        {
                            Packet.TITLE.compound(ChatColor.GOLD + player.getName(), "게임종료!", 0, 90, 10).sendAll();
                            Bukkit.broadcastMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + player.getName() + ChatColor.RESET + "님이 터뜨렸습니다!");
                            FireworkEffect.Builder builder = FireworkEffect.builder();
                            builder.type(FireworkEffect.Type.STAR).color(0xDDFF11).color(0xFF1111).color(0x00FFFF).trail(true).flicker(true);

                            Vector pos = RouletteConfig.pos;

                            Packet.EFFECT.firework(builder.build(), pos.x, pos.y, pos.z).sendAll();
                            process.stop();
                        }
                        else
                        {
                            Packet.EFFECT.namedSound(Sounds.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.MASTER, result.getX(), result.getY(), result.getZ(),100.0F, 1.0F).sendAll();
                            spot.stab(gambler, gambler.getSwordItem());
                            process.nextGambler();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        event.setCancelled(true);
    }
}
