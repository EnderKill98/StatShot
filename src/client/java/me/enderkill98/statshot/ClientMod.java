package me.enderkill98.statshot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientMod implements ClientModInitializer, ClientTickEvents.EndTick {

	public static ClientMod INSTANCE = null;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		ClientTickEvents.END_CLIENT_TICK.register(this);
	}

	public int requestStatsInTicks = 0;
	private int ticksSinceDayChangedChecked = 0;

	@Override
	public void onEndTick(MinecraftClient client) {
		if(client.player == null || client.world == null) {
			requestStatsInTicks = 0;
			ticksSinceDayChangedChecked = 0;
			return;
		}

		if(requestStatsInTicks > 0) {
			requestStatsInTicks--;
			if(requestStatsInTicks == 0) {
				// For auto-saving soon after joined / loaded
				client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
			}
		}

		ticksSinceDayChangedChecked++;
		if(ticksSinceDayChangedChecked >= 20*60) {
			if(!hasTodaysAutoSaveFile(getStatShotGameDirectory(client, false), new Date())) {
				// Midnight changed (for local timezone). Request new stats for auto-save
				client.player.networkHandler.sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
			}
			ticksSinceDayChangedChecked = 0;
		}
	}

	public File getStatShotMainDirectory(boolean createIfMissing) {
		File dir = new File(FabricLoader.getInstance().getGameDir().toFile(), "StatShotStatisticSnapshots");
		if(createIfMissing && !dir.exists())
			dir.mkdirs();
		return dir;
	}

	private String getCurrentGameName(MinecraftClient client) {
		if(client.getCurrentServerEntry() != null) {
			// Assume Multiplayer
			String address = client.getCurrentServerEntry().address;
			return "Multiplayer_" + (address == null ? "Unknown" : address.replace(':', '_'));
		}else if(client.isIntegratedServerRunning()) {
			// Assume Singleplayer
			return "Singleplayer_" + client.getServer().getSaveProperties().getLevelName().replace('/', '_').replace('\\', '_').replace(':', '_');
		}else {
			if(client.player != null && client.world != null) {
				MainMod.LOGGER.warn("Failed to determine current world/server name the player is on. Assuming \"Unknown\"!");
				return "Unknown";
			} else {
				// Player is not ingame
				return null;
			}
		}
	}

	public File getStatShotGameDirectory(MinecraftClient client, boolean createIfMissing) {
		String gameName = getCurrentGameName(client);
		if(gameName == null) return null;
		File gameDir = new File(getStatShotMainDirectory(false), gameName);
		if(createIfMissing && !gameDir.exists())
			gameDir.mkdirs();
		return gameDir;
	}

	public boolean hasTodaysAutoSaveFile(File dir, Date date) {
		if(dir == null || !dir.isDirectory()) return false;

		String nameOnlyDay = new SimpleDateFormat("yyyy-MM-dd_").format(date);
		for(String fileName : dir.list()) {
			if(fileName.startsWith(nameOnlyDay) && fileName.endsWith("_AutoSave.statpacket"))
				return true;
		}
		return false;
	}

	public String getAutoSaveFileName(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_z'_AutoSave.statpacket'").format(date);
	}

	public String getManualSaveFileName(Date date, String name) {
		if(name == null) name = "";
		name = name.replace('/', '_').replace('\\', '_').replace(':', '_');
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_z'_Manual" + (name.isBlank() ? "" : "_" + name) + ".statpacket'").format(date);
	}

	public void onStatisticsReceived(StatisticsS2CPacket packet) {
		MinecraftClient client = MinecraftClient.getInstance();
		File gameDir = getStatShotGameDirectory(client, true);
		if(gameDir == null || !gameDir.isDirectory()) return;

		Date now = new Date();
		if(!hasTodaysAutoSaveFile(getStatShotGameDirectory(client, false), now)) {
			File file = new File(getStatShotGameDirectory(client, true), getAutoSaveFileName(now));
			if(new StatisticsFile(packet, System.currentTimeMillis(), client.getSession().getUuidOrNull(), client.getSession().getUsername()).save(file)) {
				MainMod.LOGGER.info("AutoSaved statistics!");
			}else {
				MainMod.LOGGER.info("Failed to AutoSave statistics!");
			}
		}
	}
}