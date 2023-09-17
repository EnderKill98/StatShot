package me.enderkill98.statshot.mixin.client;

import me.enderkill98.statshot.StatsScreenManager;
import me.enderkill98.statshot.StatisticsFile;
import me.enderkill98.statshot.gui.CreateSnapshotScreen;
import me.enderkill98.statshot.gui.SelectSnapshotScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatsScreen.class)
public abstract class StatisticsScreenMixin extends Screen {

    private StatisticsFile statisticsFile = null;
    boolean isViewingStatShotFile = false;
    private @Nullable Text displayOverTitle;
    private Text originalTitle = null;
    private Text overriddenTitle = null;

    private ButtonWidget snapshotNowButton = null;
    private ButtonWidget viewSnapshotsButton = null;

    protected StatisticsScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    public void sendStatRequestPacketFromInit(ClientPlayNetworkHandler instance, Packet<?> statsRequestPacket) {
        if(isViewingStatShotFile) return;
        instance.sendPacket(statsRequestPacket);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void constructor(CallbackInfo info) {
        originalTitle = title;
        if(StatsScreenManager.statShotFileName != null) {
            isViewingStatShotFile = true;
            MutableText displayOverTitleText = originalTitle.copy();
            displayOverTitle = displayOverTitleText.append(" - StatShot");
            MinecraftClient client = MinecraftClient.getInstance(); // Field is not yet initialized
            if(client.getSession() != null && StatsScreenManager.statShotUserName != null
                    && !client.getSession().getUsername().equals(StatsScreenManager.statShotUserName))
                displayOverTitleText.append(Text.literal(" (for " + StatsScreenManager.statShotUserName + ")"));
            displayOverTitleText.append(Text.literal(":"));

            overriddenTitle = Text.literal(StatsScreenManager.statShotFileName);
        }else {
            isViewingStatShotFile = false;
            overriddenTitle = null;
            displayOverTitle = null;
        }
    }

    @Inject(at = @At("TAIL"), method = "onStatsReady")
    public void onStatsReady(CallbackInfo info) {
        if(StatsScreenManager.onStatsReadySourcePacket != null)
            statisticsFile = new StatisticsFile(StatsScreenManager.onStatsReadySourcePacket, System.currentTimeMillis(), client.getSession().getUuidOrNull(), client.getSession().getUsername());

        if(!isViewingStatShotFile) {
            snapshotNowButton = ButtonWidget.builder(Text.literal("Create Snapshot"), this::onSnapshotNowPressed)
                    .position(0, 0) // Set later
                    .size(100, 20)
                    .build();
            addDrawableChild(snapshotNowButton);
        }
        viewSnapshotsButton = ButtonWidget.builder(Text.literal("View Snapshots"), this::onViewSnapshotsPressed)
                .position(0, 0) // Set later
                .size(100, 20)
                .build();
        addDrawableChild(viewSnapshotsButton);

    }
    @Inject(at = @At("HEAD"), method = "render")
    public void renderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if(overriddenTitle != null && title != overriddenTitle)
            ((ScreenTitleAccessor) this).setTitle(overriddenTitle);
        else if(overriddenTitle == null && title != originalTitle)
            ((ScreenTitleAccessor) this).setTitle(originalTitle);

        int btnGap = 3;
        int xFromRight = 0;
        if(viewSnapshotsButton != null) {
            xFromRight += viewSnapshotsButton.getWidth() + btnGap;
            viewSnapshotsButton.setPosition(width - xFromRight, 10);
        }
        if(snapshotNowButton != null) {
            xFromRight += snapshotNowButton.getWidth() + btnGap;
            snapshotNowButton.setPosition(width - xFromRight, 10);
        }

    }

    @Inject(at = @At("TAIL"), method = "render")
    public void renderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if(displayOverTitle != null)
            context.drawCenteredTextWithShadow(textRenderer, displayOverTitle, this.width / 2, 10, 0xFFFFFF);
    }

    public void onSnapshotNowPressed(ButtonWidget widget) {
        if(statisticsFile != null)
            client.setScreen(new CreateSnapshotScreen(statisticsFile));
    }


    private void onViewSnapshotsPressed(ButtonWidget buttonWidget) {
        client.setScreen(new SelectSnapshotScreen());
    }

}
