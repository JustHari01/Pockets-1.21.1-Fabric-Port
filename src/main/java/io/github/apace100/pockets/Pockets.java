package io.github.apace100.pockets;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class Pockets implements ModInitializer {

	public static final TagKey<Item> HAS_POCKET_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("pockets", "has_pockets"));
	public static final TagKey<Item> BLACKLIST_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("pockets", "pocket_blacklist"));
	public static ComponentType<BundleContentsComponent> POCKET_CONTENTS;

	@Override
	public void onInitialize() {
		POCKET_CONTENTS = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				Identifier.of("pockets", "contents"),
				ComponentType.<BundleContentsComponent>builder()
						.codec(BundleContentsComponent.CODEC)
						.packetCodec(BundleContentsComponent.PACKET_CODEC)
						.build()
		);
	}
}
