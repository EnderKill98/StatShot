package me.enderkill98.statshot.mixin.client;

import me.enderkill98.statshot.ClientMod;
import me.enderkill98.statshot.StatsScreenManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class StatisticReceiveListener {

    @Inject(at = @At("HEAD"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        // Reset states just in case
        StatsScreenManager.onStatsReadySourcePacket = null;
        StatsScreenManager.statShotFileName = null;

        // Auto-Request stats 30 seconds after joining for auto-saving purposes
        ClientMod.INSTANCE.requestStatsInTicks = 20 * 30;
    }

    @Inject(at = @At("HEAD"), method = "onStatistics")
    public void onStatisticsHead(StatisticsS2CPacket packet, CallbackInfo info) {
        StatsScreenManager.onStatsReadySourcePacket = packet;
    }

    @Inject(at = @At("TAIL"), method = "onStatistics")
    public void onStatisticsTail(StatisticsS2CPacket packet, CallbackInfo info) {
        ClientMod.INSTANCE.onStatisticsReceived(packet);
        StatsScreenManager.onStatsReadySourcePacket = null;
    }


}
