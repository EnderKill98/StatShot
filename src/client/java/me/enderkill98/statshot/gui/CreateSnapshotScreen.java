package me.enderkill98.statshot.gui;

import me.enderkill98.statshot.ClientMod;
import me.enderkill98.statshot.MainMod;
import me.enderkill98.statshot.StatisticsFile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.File;
import java.util.Date;

public class CreateSnapshotScreen extends Screen {

    final StatisticsFile statisticsFile;

    public CreateSnapshotScreen(StatisticsFile file) {
        super(Text.literal("Create new Snapshot"));
        statisticsFile = file;
    }

    public TextFieldWidget nameText;
    public ButtonWidget submitButton;

    @Override
    protected void init() {
        nameText = new TextFieldWidget(textRenderer, 0, 0, 200, 20, Text.of(""));
        submitButton = ButtonWidget.builder(Text.literal("Save"), this::onSubmitPressed)
                .position(0, 0) // Set later
                .size(nameText.getWidth(), 20)
                .build();
        addDrawableChild(nameText);
        addDrawableChild(submitButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int gap = 5;
        int childrenHeight = nameText.getHeight() + gap + submitButton.getHeight() + gap + 10*2;
        int y = height / 2 - (childrenHeight / 2);
        nameText.setPosition(width / 2 - (nameText.getWidth() / 2), y);
        y += nameText.getHeight() + gap;
        submitButton.setPosition(width / 2 - (submitButton.getWidth() / 2), y);
        y += submitButton.getHeight() + gap;

        String line1 = "Filename will be:";
        String line2 = ClientMod.INSTANCE.getManualSaveFileName(new Date(statisticsFile.timestamp()), nameText.getText());
        context.drawCenteredTextWithShadow(textRenderer, line1, this.width / 2, y,  0xFFFFFF);
        y += 10;
        context.drawCenteredTextWithShadow(textRenderer, line2, this.width / 2, y,  0xFFFFFF);
        y += 10;

        super.render(context, mouseX, mouseY, delta);
    }

    private void onSubmitPressed(ButtonWidget buttonWidget) {
        File file = new File(
                ClientMod.INSTANCE.getStatShotGameDirectory(MinecraftClient.getInstance(), true),
                ClientMod.INSTANCE.getManualSaveFileName(new Date(statisticsFile.timestamp()), nameText.getText()));
        if(statisticsFile.save(file)) {
            close();
        }else {
            MainMod.LOGGER.error("Failed to save file!");
        }
    }
}
