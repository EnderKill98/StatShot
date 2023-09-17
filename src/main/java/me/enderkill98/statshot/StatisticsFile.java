package me.enderkill98.statshot;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public record StatisticsFile(StatisticsS2CPacket packet, long timestamp, UUID userId, String userName) {

    public boolean save(File file) {
        int packetId = NetworkState.PLAY.getPacketId(NetworkSide.CLIENTBOUND, packet);
        PacketByteBuf buf = new PacketByteBuf(ByteBufAllocator.DEFAULT.buffer());
        buf.writeString("StatShot StatisticsS2CPacket"); // Helper of people opening the file
        buf.writeInt(1); // Version
        buf.writeLong(timestamp);
        buf.writeUuid(userId);
        buf.writeString(userName);
        buf.writeVarInt(packetId);
        packet.write(buf);

        try {
            Files.write(file.toPath(), buf.getWrittenBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        }catch (IOException ex) {
            MainMod.LOGGER.error("Could not write Statistics to File " + file.getPath(), ex);
            return false;
        }
    }

    public static @Nullable StatisticsFile read(File file) {
        if(!file.isFile()) {
            MainMod.LOGGER.error("The file " + file.getPath() + " is not a valid file or doesn't exist!");
            return null;
        }
        if(file.length() > 1024*1024*16) { // 16 MiB - Size sanity check
            MainMod.LOGGER.error("The file " + file.getPath() + " is oddly huge! Won't potentially fill up ram by reading it!");
            return null;
        }

        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(file.toPath());
        }catch (IOException ex) {
            MainMod.LOGGER.error("Could not read File " + file.getPath(), ex);
            return null;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.copiedBuffer(fileContent));
        buf.readString(); // We don't need it
        int version = buf.readInt();
        if(version != 1) {
            MainMod.LOGGER.error("Could not parse File " + file.getPath() + " due to version not being 1!");
            return null; // No other version expected, yet!
        }
        long timestamp = buf.readLong();
        UUID userId = buf.readUuid();
        String userName = buf.readString();
        int packetId = buf.readVarInt();
        Packet<?> packet = NetworkState.PLAY.getPacketHandler(NetworkSide.CLIENTBOUND, packetId, buf);
        if(!(packet instanceof StatisticsS2CPacket statPacket)) {
            MainMod.LOGGER.error("Parsed Packet in " + file.getPath() + " is not StatisticsS2CPacket!");
            return null; // Read wrong packet. Maybe version change?
        }

        return new StatisticsFile(statPacket, timestamp, userId, userName);
    }

}
