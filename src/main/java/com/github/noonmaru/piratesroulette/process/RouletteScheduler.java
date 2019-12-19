package com.github.noonmaru.piratesroulette.process;

import com.github.noonmaru.tap.ChatType;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.ChatColor;

/**
 * @author Nemo
 */
public class RouletteScheduler implements Runnable
{
    private final RouletteProcess process;

    private Task task;

    public RouletteScheduler(RouletteProcess process)
    {
        this.process = process;
        task = new TitleTask();
    }

    @Override
    public void run()
    {
        task = task.execute();
    }

    private interface Task
    {
        Task execute();
    }

    private class TitleTask implements Task
    {
        int ticks;

        @Override
        public Task execute()
        {
            if (ticks == 0)
            {
                Packet.TITLE.compound(ChatColor.RED + "해적 룰렛", "", 5, 40, 15).sendAll();
            }

            if (++ticks >= 60)
            {
                process.ready();
                return new GameTask();
            }

            return this;
        }
    }

    private class GameTask implements Task
    {
        @Override
        public Task execute()
        {
            Packet.INFO.chat(ChatColor.WHITE + process.getCurrentGambler().getName(), ChatType.GAME_INFO).sendAll();
            process.getRoulette().playParticles();

            return this;
        }
    }
}
