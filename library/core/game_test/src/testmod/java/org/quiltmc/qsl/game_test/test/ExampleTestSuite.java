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

package org.quiltmc.qsl.game_test.test;

import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.qsl.game_test.api.QuiltGameTest;
import org.quiltmc.qsl.game_test.api.QuiltTestContext;
import org.quiltmc.qsl.game_test.api.TestStructureNamePrefix;

@TestStructureNamePrefix("quilt:")
public class ExampleTestSuite implements QuiltGameTest {
	@GameTest(structureName = "empty")
	public void noStructure(QuiltTestContext context) {
		context.setBlockState(0, 2, 0, Blocks.DIAMOND_BLOCK);

		context.addInstantFinalTask(() ->
				context.checkBlock(
						new BlockPos(0, 2, 0),
						(block) -> block == Blocks.DIAMOND_BLOCK,
						"Expected block to be diamond"
				)
		);
	}
}
