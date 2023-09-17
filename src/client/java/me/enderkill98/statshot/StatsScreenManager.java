package me.enderkill98.statshot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;

import java.util.Map;

public class StatsScreenManager {

    public static StatisticsS2CPacket onStatsReadySourcePacket = null;
    public static String statShotFileName = null;
    public static String statShotUserName = null;

    public static boolean openStats(MinecraftClient client, StatisticsFile file, String fileName) {
        if(client.player == null || client.world == null) return false;
        statShotFileName = fileName;
        statShotUserName = file.userName();

        StatHandler handler = new StatHandler();
        StatsScreen screen = new StatsScreen(client.currentScreen, handler);
        client.setScreen(screen);

        for (Map.Entry<Stat<?>, Integer> entry : file.packet().getStatMap().entrySet())
            handler.setStat(client.player, entry.getKey(), entry.getValue());
        screen.onStatsReady();

        statShotFileName = null;
        statShotUserName = null;
        return true;
    }


}
