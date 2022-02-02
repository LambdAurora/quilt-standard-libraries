package org.quiltmc.qsl.resource.loader.mixin.client;

import org.quiltmc.qsl.resource.loader.impl.QuiltBuiltinResourcePackProfile.BuiltinResourcePackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.PackListWidget.ResourcePackEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Shadow
    private PackListWidget availablePackList;

    @Shadow
    private PackListWidget selectedPackList;

    private PackScreenMixin(Text text) {
        super(text);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "render", at = @At("TAIL"))
    private void renderTooltips(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ResourcePackEntry availableEntry = ((EntryListWidgetAccessor<ResourcePackEntry>) this.availablePackList).invokeGetHoveredEntry();
        if (availableEntry != null) {
            if (((ResourcePackEntryAccessor)availableEntry).invokePack().getSource() instanceof BuiltinResourcePackSource source) {
                this.renderTooltip(matrices, source.getTooltip(), mouseX, mouseY);
            }
        }

        ResourcePackEntry selectedEntry = ((EntryListWidgetAccessor<ResourcePackEntry>) this.selectedPackList).invokeGetHoveredEntry();
        if (selectedEntry != null) {
            if (((ResourcePackEntryAccessor)selectedEntry).invokePack().getSource() instanceof BuiltinResourcePackSource source) {
                this.renderTooltip(matrices, source.getTooltip(), mouseX, mouseY);
            }
        }
    }
}
