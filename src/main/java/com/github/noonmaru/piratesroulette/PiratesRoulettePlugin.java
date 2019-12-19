package com.github.noonmaru.piratesroulette;

import com.github.noonmaru.piratesroulette.process.RouletteProcess;
import com.github.noonmaru.tap.entity.TapPlayer;
import com.github.noonmaru.tap.item.TapItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PiratesRoulettePlugin extends JavaPlugin
{
    private RouletteProcess process;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        RouletteConfig.load(getConfig());
        getServer().getScheduler().runTaskTimer(this, new ConfigReloader(new File(getDataFolder(), "config.yml"), RouletteConfig::load), 0, 1);
    }

    @Override
    public void onDisable()
    {
        stopProcess();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
            return false;

        String sub = args[0];

        if ("start".equalsIgnoreCase(sub))
        {
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                TapPlayer tapPlayer = TapPlayer.wrapPlayer(player);
                TapItemStack item = tapPlayer.getHeldItemMainHand();

                if (item != null && !item.isEmpty())
                {
                    startProcess(item);
                }
                else
                {
                    sender.sendMessage("블록 혹은 머리를 들고 실행해주세요.");
                }
            }
            else
            {
                sender.sendMessage("콘솔에서 사용 할 수 없는 명령입니다.");
            }
        }
        else if ("stop".equalsIgnoreCase(sub))
        {
            stopProcess();
            sender.sendMessage("게임을 종료했습니다.");
        }

        return true;
    }

    public void startProcess(TapItemStack blockItem)
    {
        if (process != null)
            return;

        process = new RouletteProcess(this, blockItem);
    }

    public void stopProcess()
    {
        if (process != null)
        {
            process.unregister();
            process = null;
        }
    }
}
