/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.worldgen.dimension.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.world.dimension.DimensionOptions;

import org.quiltmc.qsl.worldgen.dimension.impl.FailSoftMapCodec;
import org.quiltmc.qsl.worldgen.dimension.impl.QuiltDimensionsImpl;

@Mixin(RegistryCodecs.class)
public class RegistryCodecsMixin {
	@SuppressWarnings({"UnstableApiUsage", "unchecked"})
	@Inject(method = "fullCodec", at = @At("HEAD"), cancellable = true)
	private static <E> void quilt$onCreateFullCodec(
			RegistryKey<? extends Registry<E>> registryKey, Lifecycle lifecycle, Codec<E> codec,
			CallbackInfoReturnable<Codec<Registry<E>>> cir
	) {
		if (registryKey.equals(RegistryKeys.DIMENSION)) {
			var mapCodec = new FailSoftMapCodec<>(RegistryKey.codec(registryKey), codec);
			QuiltDimensionsImpl.setDimensionFailSoftMapCodec((FailSoftMapCodec<RegistryKey<DimensionOptions>, DimensionOptions>) (Object) mapCodec);

			Codec<Registry<E>> result = mapCodec.xmap(entries -> {
				var mutableRegistry = new SimpleRegistry<>(registryKey, lifecycle);
				entries.forEach((key, value) -> mutableRegistry.register(key, value, lifecycle));
				return mutableRegistry.freeze();
			}, registry -> ImmutableMap.copyOf(registry.getEntries()));
			cir.setReturnValue(result);
		}
	}
}
