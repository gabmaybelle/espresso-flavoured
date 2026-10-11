package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.LootPool;

/**
 * Coffee out in the world: village houses, jungle temples and shipwreck supply chests sometimes
 * hold coffee cherries or roasted beans. The extra pools are in espresso/loot_inject.json
 * (written by tools/generate-data.js) and are added to the vanilla tables as they load.
 */
public final class CoffeeLoot {
	private CoffeeLoot() {
	}

	static void register() {
		Map<Identifier, JsonElement> extras = load();
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			JsonElement pools = extras.get(key.identifier());
			if (pools == null || !source.isBuiltin()) {
				return;
			}
			RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
			for (JsonElement json : pools.getAsJsonArray()) {
				LootPool.CODEC.parse(ops, json)
						.resultOrPartial(error -> Espresso.LOGGER.warn("Couldn't add coffee to {}: {}", key.identifier(), error))
						.ifPresent(table::pool);
			}
		});
	}

	private static Map<Identifier, JsonElement> load() {
		Map<Identifier, JsonElement> extras = new HashMap<>();
		try (InputStream in = CoffeeLoot.class.getResourceAsStream("/espresso/loot_inject.json")) {
			if (in == null) {
				Espresso.LOGGER.warn("espresso/loot_inject.json is missing; no coffee in chests");
				return extras;
			}
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				extras.put(Identifier.parse(entry.getKey()), entry.getValue());
			}
		} catch (Exception e) {
			Espresso.LOGGER.warn("Couldn't read espresso/loot_inject.json", e);
		}
		return extras;
	}
}
