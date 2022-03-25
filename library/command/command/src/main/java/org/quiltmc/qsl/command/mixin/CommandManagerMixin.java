/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.command.mixin;

import com.mojang.brigadier.CommandDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.QuiltCommandRegistrationEnvironment;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
	@Shadow
	@Final
	private CommandDispatcher<ServerCommandSource> dispatcher;

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V",
					remap = false
			)
	)
	void addQuiltCommands(CommandManager.RegistrationEnvironment environment, CommandBuildContext context, CallbackInfo ci) {
		CommandRegistrationCallback.EVENT.invoker().registerCommands(this.dispatcher, context, environment);
	}

	@Mixin(CommandManager.RegistrationEnvironment.class)
	public static abstract class RegistrationEnvironmentMixin implements QuiltCommandRegistrationEnvironment {
		@Shadow
		@Final
		boolean dedicated;

		@Shadow
		@Final
		boolean integrated;

		@Override
		public boolean isDedicated() {
			return this.dedicated;
		}

		@Override
		public boolean isIntegrated() {
			return this.integrated;
		}
	}
}
