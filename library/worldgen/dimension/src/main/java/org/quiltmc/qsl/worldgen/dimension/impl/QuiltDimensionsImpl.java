/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022-2023 QuiltMC
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

package org.quiltmc.qsl.worldgen.dimension.impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.dimension.DimensionOptions;

import org.quiltmc.qsl.base.api.util.TriState;

@ApiStatus.Internal
public class QuiltDimensionsImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger("QuiltDimensions");
	private static final String IGNORE_FAIL_KEY = "quilt.dimension.ignore_failed_deserialization";
	public static final boolean IGNORE_FAIL = TriState.fromProperty(IGNORE_FAIL_KEY).toBooleanOrElse(false);
	private static DimensionDeserializationFailHandler failHandler = errors -> {
		if (!IGNORE_FAIL) {
			LOGGER.error("""
							Failed to deserialize dimensions from NBT due to missing elements.
							No confirmation interface could be displayed, so the loading of the world will be cancelled.
							If you wish to ignore this deserialization issue and force-load the world, please specify "-D{}=true" in JVM arguments.""",
					IGNORE_FAIL_KEY);
		}

		return IGNORE_FAIL;
	};
	private static FailSoftMapCodec<RegistryKey<DimensionOptions>, DimensionOptions> dimensionFailSoftMapCodec;

	// Static only-class, no instantiation necessary!
	private QuiltDimensionsImpl() {
		throw new UnsupportedOperationException("QuiltDimensionsImpl only contains static definitions.");
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E teleport(Entity entity, ServerWorld destinationWorld, TeleportTarget location) {
		Preconditions.checkArgument(
				Thread.currentThread() == entity.getServer().getThread(),
				"This method may only be called from the main server thread"
		);

		var access = (EntityAccess) entity;
		access.setTeleportTarget(location);

		try {
			// Fast path for teleporting within the same dimension.
			if (entity.getWorld() == destinationWorld) {
				if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
					serverPlayerEntity.networkHandler.requestTeleport(location.position.x, location.position.y, location.position.z, location.yaw, entity.getPitch());
				} else {
					entity.refreshPositionAndAngles(location.position.x, location.position.y, location.position.z, location.yaw, entity.getPitch());
				}

				entity.setVelocity(location.velocity);
				entity.setHeadYaw(location.yaw);

				return (E) entity;
			}
			return (E) entity.moveToWorld(destinationWorld);
		} finally {
			// Always clean up!
			access.setTeleportTarget(null);
		}
	}

	public static void setDimensionFailSoftMapCodec(FailSoftMapCodec<RegistryKey<DimensionOptions>, DimensionOptions> codec) {
		dimensionFailSoftMapCodec = codec;
	}

	public static void setDimensionFailHandler(DimensionDeserializationFailHandler failHandler) {
		QuiltDimensionsImpl.failHandler = failHandler;
	}

	public static boolean canContinueWorldLoad() {
		boolean result = true;

		if (!dimensionFailSoftMapCodec.getErrors().isEmpty()) {
			result = failHandler.shouldContinueLoad(dimensionFailSoftMapCodec.getErrors());
		}

		dimensionFailSoftMapCodec.getErrors().clear();

		return result;
	}
}
