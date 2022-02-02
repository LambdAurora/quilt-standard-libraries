package org.quiltmc.qsl.resource.loader.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.widget.EntryListWidget;

@Mixin(EntryListWidget.class)
public interface EntryListWidgetAccessor<E extends EntryListWidget.Entry<E>> {
    @Invoker
    E invokeGetHoveredEntry();
}
