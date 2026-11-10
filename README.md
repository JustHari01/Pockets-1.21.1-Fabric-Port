# Pockets (UNOFFICIAL)

An unofficial Fabric port of [Pockets by Apace100](https://github.com/apace100/pockets) for Minecraft 1.21.1.

Pockets gives leather leggings bundle-like storage. Right-click the leggings in an inventory with an item on your cursor to put items into the pockets; right-click with an empty cursor to take out the most recently stored stack. Pocket contents are shown in the item tooltip, with a fullness line and inventory bar.

The storage limit is 64 bundle points. Items that cannot be nested and items in the `pockets:pocket_blacklist` tag cannot be stored. Pocket-bearing armor and bundles use their contained items when calculating nested capacity. Contents are dropped if pocket armor breaks or its item entity is destroyed.

## Datapack customization

The `pockets:has_pockets` item tag determines which armor pieces have pockets. By default it contains `minecraft:leather_leggings`. Add other armor identifiers to the tag in a datapack to give them the same behavior.

The `pockets:pocket_blacklist` item tag excludes items from storage. Add item identifiers to this tag in a datapack to prevent them from being pocketed.

## Compatibility and credits

Requires Fabric Loader and Fabric API for Minecraft 1.21.1. Existing 1.20.1 Pockets item data is migrated when the server can decode its stored items.

The original Pockets project and assets are by Apace100. This port is independent and is not endorsed by the original author. The original MIT license and copyright notice are retained in `LICENSE`.

This port's compatibility code was developed with AI assistance.
