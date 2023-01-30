/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.dimension.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

/**
 * Has the same functionality as {@link UnboundedMapCodec} but it will fail-soft when an entry cannot be deserialized.
 */
@ApiStatus.Internal
public class FailSoftMapCodec<K, V> implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
	public static final Logger LOGGER = LoggerFactory.getLogger("FailSoftMapCodec");

	private final Codec<K> keyCodec;
	private final Codec<V> elementCodec;
	private final List<DecodeError> errors = new ArrayList<>();

	public FailSoftMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
		this.keyCodec = keyCodec;
		this.elementCodec = elementCodec;
	}

	@Override
	public Codec<K> keyCodec() {
		return this.keyCodec;
	}

	@Override
	public Codec<V> elementCodec() {
		return this.elementCodec;
	}

	public List<DecodeError> getErrors() {
		return this.errors;
	}

	@Override
	public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
	}

	@Override
	public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
		return this.encode(input, ops, ops.mapBuilder()).build(prefix);
	}

	/**
	 * In {@link BaseMapCodec#decode(DynamicOps, MapLike)}, the whole deserialization will fail if one element fails.
	 * {@code apply2stable} will return fail when any of the two elements is failed.
	 * In this implementation, if one deserialization fails, it will log and ignore.
	 */
	@Override
	public <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
		final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		this.errors.clear();

		input.entries().forEach(pair -> {
			try {
				final DataResult<K> k = this.keyCodec().parse(ops, pair.getFirst());
				final DataResult<V> v = this.elementCodec().parse(ops, pair.getSecond());

				k.get().ifRight(partial -> {
					LOGGER.error("Failed to decode key {} from {}  {}", k, pair, partial);

					this.errors.add(new DecodeError(Kind.KEY, partial.message()));
				});
				v.get().ifRight(partial -> {
					LOGGER.error("Failed to decode value {} from {}  {}", v, pair, partial);

					this.errors.add(new DecodeError(Kind.VALUE, partial.message()));
				});

				if (k.get().left().isPresent() && v.get().left().isPresent()) {
					builder.put(k.get().left().get(), v.get().left().get());
				} // ignore failure
			} catch (Throwable e) {
				LOGGER.error("Decoding {}", pair, e);
			}
		});

		final Map<K, V> elements = builder.build();

		return DataResult.success(elements);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final var that = (FailSoftMapCodec<?, ?>) o;
		return Objects.equals(this.keyCodec, that.keyCodec) && Objects.equals(this.elementCodec, that.elementCodec);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.keyCodec, this.elementCodec);
	}

	@Override
	public String toString() {
		return "FailSoftMapCodec[" + this.keyCodec + " -> " + this.elementCodec + ']';
	}

	public enum Kind {
		KEY {
			private static final String NOT_A_VALID_ID = "Not a valid resource location: ";

			@Override
			Text getFancyMessage(String originalMessage) {
				if (originalMessage.startsWith(NOT_A_VALID_ID)) {
					var substr = originalMessage.substring(NOT_A_VALID_ID.length());

					return Text.translatable("quilt.error.id.malformed",
									substr.substring(0, substr.indexOf(" "))
							)
							.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(originalMessage))));
				}

				return Text.literal(originalMessage);
			}
		},
		VALUE {
			private static final String FAILED_TO_GET_ELEMENT = "Failed to get element ";

			@Override
			Text getFancyMessage(String originalMessage) {
				if (originalMessage.startsWith(FAILED_TO_GET_ELEMENT)) {
					return Text.translatable("quilt.error.registry.get_element_failure",
									originalMessage.substring(FAILED_TO_GET_ELEMENT.length())
							)
							.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(originalMessage))));
				}

				return Text.literal(originalMessage);
			}
		};

		abstract Text getFancyMessage(String originalMessage);
	}

	public record DecodeError(Kind kind, String message) {
		public Text getFancyMessage() {
			return this.kind.getFancyMessage(this.message);
		}
	}
}
