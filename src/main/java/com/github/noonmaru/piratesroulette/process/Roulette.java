package com.github.noonmaru.piratesroulette.process;

import com.github.noonmaru.customentity.CustomEntityPacket;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.math.VectorSpace;
import com.github.noonmaru.piratesroulette.RouletteConfig;
import com.github.noonmaru.tap.Particle;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.entity.TapArmorStand;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.math.BoundingBox;
import com.github.noonmaru.tap.packet.Packet;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author Nemo
 */
public class Roulette
{
    private final List<Spot> spots;

    private final Spot weakness;

    private final TapArmorStand block;

    private final double size;

    private final BoundingBox box;

    public Roulette(TapItemStack blockItem)
    {
        double spotGap = RouletteConfig.spotGap;
        int spotLine = RouletteConfig.spotLine;

        VectorSpace origin = new VectorSpace();
        for (int x = 0; x < spotLine; x++)
        {
            for (int y = 0; y < spotLine; y++)
            {
                origin.addVector(new Vector(x, y, 0));
            }
        }

        //중심으로 이동, 동시에 중심 기준으로 밀기
        double center = (spotLine - 1) / 2.0D;
        origin.subtract(center, center, center + 0.6);

        ArrayList<Spot> spots = new ArrayList<>(origin.size() * 6);
        List<Vector> vectors = new ArrayList<>(origin.size() * 6);

        // 회전시켜 6면체 적용
        for (int i = 0; i < 4; i++)
        {
            VectorSpace space = origin.copy().rotateAxisY(i * 90);
            vectors.addAll(space.getVectors());

            for (Vector v : space)
            {
                spots.add(new Spot(v, i * 90, 0));
            }
        }
        for (int i = 0 ; i < 2; i++)
        {
            VectorSpace space = origin.copy().rotateAxisX(-90 + i * 180);
            vectors.addAll(space.getVectors());

            for (Vector v : space)
            {
                spots.add(new Spot(v, 0, -90 + i * 180));
            }
        }

        VectorSpace space = new VectorSpace(vectors);

        //스팟 간격 확장
        space.multiply(spotGap);

        //스팟 위치 변경
        space.add(RouletteConfig.pos);

        for (Spot spot : spots)
        {
            spot.updatePos();
        }
        this.spots = ImmutableList.copyOf(spots);

        //약점 뽑기
        Random random = new Random();
        weakness = spots.get(random.nextInt(spots.size()));

        //아머스탠드 설정
        this.block = Tap.ENTITY.createEntity(ArmorStand.class);
        block.setEquipment(EquipmentSlot.HEAD, blockItem);
        block.setInvisible(true);

        size = (float) (RouletteConfig.spotGap * RouletteConfig.spotLine);
        Vector pos = RouletteConfig.pos;
        block.setPositionAndRotation(pos.x, pos.y - size / 2, pos.z, 0, 0);
        block.setHeadPose(0, 0, 0);

        double r = size / 2.0D;

        this.box = Tap.MATH.newBoundingBox(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r);
    }

    public BoundingBox getBox()
    {
        return box;
    }

    public void spawnTo(Collection<? extends Player> players)
    {
        Packet.ENTITY.spawnMob(block.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.metadata(block.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.equipment(block.getId(), EquipmentSlot.HEAD, block.getEquipment(EquipmentSlot.HEAD)).sendTo(players);

        for (Spot spot : spots)
        {
            spot.spawnTo(players);
        }
    }

    public void updateCustomEntity(Collection<? extends Player> players)
    {
        CustomEntityPacket.register(block.getId()).sendTo(players);
        float size = (float) this.size;
        CustomEntityPacket.scale(block.getId(), size, size, size, 60).sendTo(players);
    }

    public Spot getNearestSpot(Vector v)
    {
        double distance = 0.0D;
        Spot nearest = null;

        for (Spot spot : spots)
        {
            double curDistance = spot.getPos().distance(v);

            if (distance == 0.0D || curDistance < distance)
            {
                distance = curDistance;
                nearest = spot;
            }
        }

        return nearest;
    }

    public List<Spot> getSpots()
    {
        return spots;
    }

    public Spot getWeakness()
    {
        return weakness;
    }

    public void playParticles()
    {
        for (Spot spot : spots)
        {
            if (spot.isStabbed())
                continue;

            Vector pos = spot.getPos();

            Packet.EFFECT.particle(Particle.END_ROD, (float) pos.x, (float) pos.y, (float) pos.z, 0F, 0F, 0F, 0, 0).sendAll();
        }
    }

    public void destroy()
    {
        Packet.ENTITY.destroy(block.getId()).sendAll();
        CustomEntityPacket.unregister(block.getId()).sendAll();

        for (Spot spot : spots)
        {
            spot.destroy();
        }
    }
}
