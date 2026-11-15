# Pockets (UNOFFICIAL)

An unofficial Fabric port of [Pockets by Apace100](https://github.com/apace100/pockets) for Minecraft 1.21.1.

Pockets gives leather leggings bundle-like storage. Right-click the leggings in an inventory with an item on your cursor to put items into the pockets; right-click with an empty cursor to take out the most recently stored stack. Pocket contents are shown in the item tooltip, with a fullness line and inventory bar.

The storage limit is 64 bundle points. Items that cannot be nested and items in the `pockets:pocket_blacklist` tag cannot be stored. Pocket-bearing armor and bundles use their contained items when calculating nested capacity. Contents are dropped if pocket armor breaks or its item entity is destroyed.

## Datapack customization

The `pockets:has_pockets` item tag determines which armor pieces have pockets. By default it contains `minecraft:leather_leggings`. Add other armor identifiers to the tag in a datapack to give them the same behavior.

The `pockets:pocket_blacklist` item tag excludes items from storage. Add item identifiers to this tag in a datapack to prevent them from being pocketed.

## Compatibility

Requires Fabric Loader 0.16.10 or newer, Fabric API, and Java 21 for Minecraft 1.21.1. Legacy 1.20.1 item-data migration code is included, but migration from a real 1.20.1 world has not been verified.

## Verification

- Built from this public source with Java 21, Gradle 8.12.1, and Fabric Loom 1.10.5. The rebuilt JAR is byte-identical to the prepared release.
- The release JAR started on an isolated Minecraft 1.21.1 dedicated server with Fabric Loader 0.16.10 and Fabric API 0.116.7+1.21.1. A fresh test world was generated, saved in the Overworld, Nether, and End, and shut down cleanly.
- This server smoke test checks mod discovery, initialization, and startup only. It does not verify inventory right-click behavior, tooltip rendering, break/drop behavior, multiplayer synchronization, or migration of real 1.20.1 item data. The port remains an alpha release.

## Credits and license

The original Pockets project and assets are by Apace100. This port is independent and is not endorsed by the original author. The original MIT license and copyright notice are retained in `LICENSE`.

This port's compatibility code was developed with AI assistance.
