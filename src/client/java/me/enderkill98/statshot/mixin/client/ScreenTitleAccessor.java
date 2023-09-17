package me.enderkill98.statshot.mixin.client;


import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface ScreenTitleAccessor {

    @Accessor("title") @Mutable
    void setTitle(Text title);
}
