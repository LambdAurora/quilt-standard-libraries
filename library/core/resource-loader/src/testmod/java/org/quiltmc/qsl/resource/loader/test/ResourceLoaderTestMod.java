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

package org.quiltmc.qsl.resource.loader.test;

import net.minecraft.util.Identifier;

public final class ResourceLoaderTestMod {
	public static final String NAMESPACE = "quilt-resource-loader-testmod";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	private ResourceLoaderTestMod() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}
}