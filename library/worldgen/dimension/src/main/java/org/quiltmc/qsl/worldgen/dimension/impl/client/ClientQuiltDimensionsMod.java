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

package org.quiltmc.qsl.worldgen.dimension.impl.client;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.worldgen.dimension.impl.FailSoftMapCodec;
import org.quiltmc.qsl.worldgen.dimension.impl.QuiltDimensionsImpl;
import org.quiltmc.qsl.worldgen.dimension.mixin.client.MinecraftClientAccessor;

@ApiStatus.Internal
public class ClientQuiltDimensionsMod implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		QuiltDimensionsImpl.setDimensionFailHandler(errors -> {
			if (QuiltDimensionsImpl.IGNORE_FAIL) return true;

			MinecraftClient client = MinecraftClient.getInstance();
			var oldScreen = client.currentScreen;
			client.currentScreen = null;
			final var result = new boolean[2];
			result[1] = true;

			var errorMessage = errors.stream()
					.map(FailSoftMapCodec.DecodeError::getFancyMessage)
					.reduce(Text.literal("Errors:\n"), (mutableText, text) -> mutableText.copy().append("\n - ").append(text));

			var confirmScreen = new ConfirmScreen(res -> {
				result[0] = res;
				result[1] = false;
			}, Text.translatable("quilt.error.dimension.deserialization"), errorMessage);

			client.setScreen(confirmScreen);

			// This is quite bad but there's not much choice since this will run on the render thread.
			while (result[1]) {
				((MinecraftClientAccessor) client).invokeRender(true);
			}

			client.setScreen(oldScreen);

			return result[0];
		});
	}
}
