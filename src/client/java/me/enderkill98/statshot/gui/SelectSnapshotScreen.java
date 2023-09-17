package me.enderkill98.statshot.gui;

import me.enderkill98.statshot.ClientMod;
import me.enderkill98.statshot.MainMod;
import me.enderkill98.statshot.StatisticsFile;
import me.enderkill98.statshot.StatsScreenManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.File;
import java.util.*;

public class SelectSnapshotScreen extends Screen implements SnapshotListWidget.SnapshotDoubleClickHandler {

    public SelectSnapshotScreen() {
        super(Text.literal("Select Snapshot to View"));
    }

    public ButtonWidget openButton;
    public ButtonWidget deleteButton;
    public SnapshotListWidget snapshotList;

    private final int FOOTER_HEIGHT = 32;

    @Override
    protected void init() {
        snapshotList = new SnapshotListWidget(client, width, height - 64, 32, height - FOOTER_HEIGHT, 20, this);
        File gameDir = ClientMod.INSTANCE.getStatShotGameDirectory(client, false);
        if(gameDir.isDirectory()) {
            List<String> fileNames = new ArrayList<>(Arrays.stream(gameDir.list()).filter((name) -> name.endsWith(".statpacket")).toList());
            Collections.sort(fileNames);
            Collections.reverse(fileNames);
            for(String fileName : fileNames)
                snapshotList.children().add(new SnapshotListWidget.SnapshotFileEntry(snapshotList, fileName));
        }

        addDrawableChild(snapshotList);
        openButton = ButtonWidget.builder(Text.literal("Open"), this::onOpenPressed)
                .position(0, 0) // Set later
                .size(80, 20)
                .build();
        addDrawableChild(openButton);
        deleteButton = ButtonWidget.builder(Text.literal("Delete"), this::onDeletePressed)
                .position(0, 0) // Set later
                .size(80, 20)
                .build();
        addDrawableChild(deleteButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int bodyWidth = snapshotList.getRowWidth();
        int gap = 5;
        int x = 0;
        openButton.setPosition(width / 2 - bodyWidth / 2 + x, height - FOOTER_HEIGHT + (FOOTER_HEIGHT / 2 - openButton.getHeight() / 2));
        x += openButton.getWidth() + gap;
        deleteButton.setPosition(width / 2 - bodyWidth / 2 + x, height - FOOTER_HEIGHT + (FOOTER_HEIGHT / 2 - openButton.getHeight() / 2));
        x += deleteButton.getWidth() + gap;

        super.render(context, mouseX, mouseY, delta);
    }

    private void onOpenPressed(ButtonWidget buttonWidget) {
        SnapshotListWidget.SnapshotFileEntry entry = snapshotList.getSelectedOrNull();
        if(entry == null) {
            MainMod.LOGGER.info("Can't open file. None selected.");
            return;
        }
        File file = new File(ClientMod.INSTANCE.getStatShotGameDirectory(client, false), entry.fileName);
        if(!file.isFile()) {
            MainMod.LOGGER.warn("Couldn't find selected file!");
            return;
        }
        StatisticsFile statisticsFile = StatisticsFile.read(file);
        if(statisticsFile == null) {
            MainMod.LOGGER.warn("Couldn't read selected file!");
            return;
        }
        StatsScreenManager.openStats(client, statisticsFile, file.getName());
    }

    private void onDeletePressed(ButtonWidget buttonWidget) {
        SnapshotListWidget.SnapshotFileEntry entry = snapshotList.getSelectedOrNull();
        if(entry == null) {
            MainMod.LOGGER.info("Can't open file. None selected.");
            return;
        }
        File file = new File(ClientMod.INSTANCE.getStatShotGameDirectory(client, false), entry.fileName);
        if(!file.isFile()) {
            MainMod.LOGGER.warn("Couldn't find selected file!");
            return;
        }
        if(file.delete()) {
            snapshotList.children().remove(entry);
            MainMod.LOGGER.info("Deleted file " + file.getPath());
        }else {
            MainMod.LOGGER.warn("Failed to delete file " + file.getPath());

        }
    }

    @Override
    public void onSelectedSnapshotDoubleClicked() {
        onOpenPressed(openButton);
    }
}
