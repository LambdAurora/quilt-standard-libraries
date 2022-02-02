package org.quiltmc.qsl.resource.loader.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.screen.pack.PackListWidget.ResourcePackEntry;

@Mixin(ResourcePackEntry.class)
public interface ResourcePackEntryAccessor {
    @Accessor
    ResourcePackOrganizer.Pack invokePack();
}
