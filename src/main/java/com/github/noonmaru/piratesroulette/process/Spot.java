package com.github.noonmaru.piratesroulette.process;

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.entity.TapArmorStand;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;

/**
 * @author Nemo
 */
public class Spot
{
    private final Vector pos;

    private final TapArmorStand sword;

    private Gambler stabber;

    public Spot(Vector pos, float yaw, float pitch)
    {
        this.pos = pos;
        sword = Tap.ENTITY.createEntity(ArmorStand.class);
        sword.setInvisible(true);
        sword.setPositionAndRotation(pos.x, pos.y - 0.5, pos.z, yaw + 90, 0F);
        sword.setHeadPose(0F, 0F, pitch + 45);
    }

    public void updatePos()
    {
        sword.setPosition(pos.x, pos.y - 0.5, pos.z);
    }

    public Vector getPos()
    {
        return pos;
    }

    public Gambler getStabber()
    {
        return stabber;
    }

    public boolean isStabbed()
    {
        return stabber != null;
    }

    public void spawnTo(Collection<? extends Player> players)
    {
        Packet.ENTITY.spawnMob(sword.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.metadata(sword.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.equipment(sword.getId(), EquipmentSlot.HEAD, sword.getEquipment(EquipmentSlot.HEAD)).sendTo(players);
    }

    public void destroy()
    {
        Packet.ENTITY.destroy(sword.getId()).sendAll();
    }

    public void stab(Gambler gambler, TapItemStack swordItem)
    {
        this.stabber = gambler;
        sword.setEquipment(EquipmentSlot.HEAD, swordItem);
        Packet.ENTITY.equipment(sword.getId(), EquipmentSlot.HEAD, sword.getEquipment(EquipmentSlot.HEAD)).sendAll();

    }
}
