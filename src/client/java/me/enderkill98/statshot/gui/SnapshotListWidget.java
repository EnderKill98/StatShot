package me.enderkill98.statshot.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.jetbrains.annotations.Nullable;

public class SnapshotListWidget extends EntryListWidget<SnapshotListWidget.SnapshotFileEntry> {

    public interface SnapshotDoubleClickHandler {
        void onSelectedSnapshotDoubleClicked();
    }

    private SnapshotDoubleClickHandler doubleClickHandler;

    public SnapshotListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, SnapshotDoubleClickHandler doubleClickHandler) {
        super(client, width, height, top, bottom, itemHeight);
        this.doubleClickHandler = doubleClickHandler;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}

    @Override
    public int getRowWidth() {
        return 440;
    }

    @Override
    public int getScrollbarPositionX() {
        return this.width / 2 + (getRowWidth() / 2) + 10;
    }

    @Override
    public void setSelected(@Nullable SnapshotListWidget.SnapshotFileEntry entry) {
        SnapshotListWidget.SnapshotFileEntry selectedEntry = getSelectedOrNull();
        if (selectedEntry != null) selectedEntry.selected = false;
        if (entry != null) entry.selected = true;
        super.setSelected(entry);
    }

    // TODO: 6/2/2022 Add a delete icon
    public static class SnapshotFileEntry extends Entry<SnapshotFileEntry> {

        public final SnapshotListWidget parent;
        public final String fileName;
        public boolean selected;

        public long lastClickedAt = -1L;

        public SnapshotFileEntry(SnapshotListWidget parent, String fileName) {
            this.parent = parent;
            this.fileName = fileName;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (selected) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0xFFFFFF);
                context.fill(x + 1, y + 1, x + entryWidth - 1, y + entryHeight - 1, 0x000000);
            }

            context.drawText(parent.client.textRenderer, fileName, x + 5, y + 5, 0xFFFFFF, true);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            parent.setSelected(this);
            long now = System.currentTimeMillis();
            if(lastClickedAt != -1L && now - lastClickedAt < 300) {
                if(parent.doubleClickHandler != null)
                    parent.doubleClickHandler.onSelectedSnapshotDoubleClicked();
            }
            lastClickedAt = now;
            return true;
        }
    }
}
