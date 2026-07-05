# Baritone — Fork

A personal fork of [Baritone](https://github.com/cabaletta/baritone) built on the [MeteorDevelopment](https://github.com/MeteorDevelopment/baritone) maintenance branch.

---

## Changes in This Fork

### New Features

| Feature | Description |
|---|---|
| **Safe Stop** | Controlled deceleration when cancelling pathing mid-stride. Configurable ticks and optional block-center alignment. Settings: `safeStop`, `safeStopDecelerationTicks`, `safeStopCenterOnBlock`. |
| **Farm List** (`#farmlist`) | Farm specific crops by name, similar to `#mine`. Validates block types, deduplicates entries, lists crop names with no arguments. |
| **Giant Tree Mining** (`#gianttree`) | Top-down tree mining: climbs the trunk then mines downward, avoiding the falling-log pile. Settings: `giantTreeMiningEnabled`, `giantTreeMinHeight`, `giantTreeSearchRadius`. |
| **Swim In Water** | Baritone holds JUMP while in water so the player swims on the surface instead of walking the lake floor. Setting: `swimInWater` (default: true). |
| **Boat Navigation** (`#boat`) | Autonomous boat driving across large water bodies. Places a boat from hotbar, steers toward goal, detects landfall, dismounts. Elytra conflict guards. Settings: `boatNavigationEnabled`, `boatMinWaterWidth`, `boatLookaheadBlocks`, `boatAutoPlace`. |
| **Portal Avoidance** | When disabled, portals are treated as obstacles and Baritone paths around them. Setting: `allowPortal` (default: true). Affects END_PORTAL, NETHER_PORTAL, END_GATEWAY. |
| **Hazardous Block Customization** | User-configurable list of blocks to avoid walking into. Default: cactus, magma, sweet berry bush, wither rose, pointed dripstone. Setting: `hazardousBlocksToAvoid`. |
| **Player Proximity Pause** | Auto-pauses pathing when another non-spectator player is nearby. Resumes when the player leaves. Settings: `pauseOnPlayersNearby`, `pausePlayersNearbyRadius`. |
| **Route Store** (`#saveroute` / `#loadroute`) | Save named multi-waypoint routes as JSON files. Load them as composite pathing goals. |

---

## License

LGPL-3.0 with the Baritone Anime Exception.
