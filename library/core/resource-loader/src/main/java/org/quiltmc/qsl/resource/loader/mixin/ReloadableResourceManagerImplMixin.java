/*
 * Copyright 2021 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.resource.loader.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Unit;

import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {
	@Final
	@Shadow
	private ResourceType type;

	@Shadow
	@Final
	private List<ResourceReloader> reloaders;

	@Inject(
			method = "reload",
			at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z", remap = false)
	)
	private void reload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage,
						List<ResourcePack> packs,
						CallbackInfoReturnable<ResourceReload> info) {
		ResourceLoaderImpl.sort(this.type, this.reloaders);
	}
}