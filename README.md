# Baritone — Fork by Rahul Shyam

<p align="center">
  <a href="https://github.com/rahulcvwebsitehosting/baritone/releases/"><img src="https://img.shields.io/github/downloads/rahulcvwebsitehosting/baritone/total?color=22c55e&label=downloads" alt="Downloads"/></a>
  <a href="https://github.com/rahulcvwebsitehosting/baritone/releases/"><img src="https://img.shields.io/github/release/rahulcvwebsitehosting/baritone?color=22c55e&label=version" alt="Release"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-LGPL--3.0-green.svg" alt="License"/></a>
  <a href="https://github.com/rahulcvwebsitehosting/baritone/issues/"><img src="https://img.shields.io/github/issues/rahulcvwebsitehosting/baritone?color=22c55e" alt="Issues"/></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/MC-26.2-brightgreen.svg" alt="Minecraft 26.2"/>
  <img src="https://img.shields.io/badge/java-25-orange.svg" alt="Java 25"/>
  <img src="https://img.shields.io/badge/fabric-0.18.6-blue.svg" alt="Fabric"/>
  <img src="https://img.shields.io/badge/neoforge-19--beta-blue.svg" alt="NeoForge"/>
  <a href="https://github.com/rahulcvwebsitehosting/baritone/commit/"><img src="https://img.shields.io/github/last-commit/rahulcvwebsitehosting/baritone?color=22c55e" alt="Last commit"/></a>
  <img src="https://img.shields.io/github/languages/code-size/rahulcvwebsitehosting/baritone?color=22c55e" alt="Code size"/>
</p>

<p align="center">
  <i>A Minecraft pathfinder bot — rebuilt, extended, and maintained.</i>
</p>

---

## About This Fork

This is a **personal fork** of the legendary [Baritone](https://github.com/cabaletta/baritone) pathfinding bot by leijurv & Brady, built on top of the [MeteorDevelopment](https://github.com/MeteorDevelopment/baritone) maintenance branch.

The goal is simple: **keep Baritone alive on the latest Minecraft versions while adding practical features that make automation more flexible.**

Built with ❤️ by **Rahul Shyam** — a civil engineer who codes.

---

## Maintainer

<p>
  <strong>Rahul Shyam</strong><br/>
  <em>Civil Engineer · Full-Stack Developer · AI Builder</em><br/>
  Chennai, India 🇮🇳<br/>
  B.E. Civil Engineering @ ESEC
</p>

| | |
|---|---|
| **Portfolio** | [rahulshyam-portfolio.vercel.app](https://rahulshyam-portfolio.vercel.app/) |
| **GitHub** | [@rahulcvwebsitehosting](https://github.com/rahulcvwebsitehosting) |
| **LinkedIn** | [in/rahulshyamcivil](https://linkedin.com/in/rahulshyamcivil) |
| **X** | [@RahulShyamCV](https://x.com/RahulShyamCV) |

**Experience:** Site Engineering Intern @ Tata Projects (Chennai Metro), BIM Intern @ Pinnacle Future Build.

When I'm not building Minecraft bots, I'm working on AI-powered construction tools, browser agents, and full-stack apps — 40+ projects shipped on Vercel.

---

## New Features

| Feature | Description | Status |
|---|---|---|
| **`#farmlist` command** | Farm only specific crops — like `#mine` for farming. `#farmlist minecraft:wheat minecraft:carrots` | ✅ |
| **All MC 26.2 crops** | torchflower_crop, pitcher_crop, sweet_berry_bush added to Harvest enum | ✅ |
| **NeoForge support** | Full NeoForge mod loader compatibility | ✅ |
| **MC 26.2 support** | Targets latest Minecraft Java Edition | ✅ |
| **Filtered item pickup** | Only picks up drops from selected crops when using `#farmlist` | ✅ |
| **Filtered replanting** | Only replants crops specified in the filter | ✅ |

---

## Improvements

| Improvement | Details |
|---|---|
| **Crop coverage** | Extended from 10 to 13 crop types — all farmable plants in MC 26.2 are now supported |
| **Render pipeline** | Updated to MC 26.2 rendering API (RenderPipeline/RenderType system) |
| **Mixin architecture** | New `FabricMixinPlugin` for conditional mixin loading; modernized mixin config |
| **Java 25** | Upgraded toolchain to Java 25 |
| **Build system** | Modernized Gradle build; simplified platform selection via `available_loaders` property |

---

## Changes from Upstream

| What | Why |
|---|---|
| **Removed** `BuildLimitPathFinder` / `IElytraPathFinder` | Elytra above-build-limit flight stripped (upstream decision) |
| **Removed** `elytraAllowAboveRoof`, `elytraAllowTightSpaces` | Corresponding settings removed |
| **Removed** `MixinLootContext` | Replaced with `MixinLootContextBuilder` for newer MC |
| **Added** `neoforge/` module | NeoForge mod loader support |
| **Added** `mixins.baritone-meteor.json` | Meteor-specific mixin config with plugin hook |
| **Default** `chatControl: false` | Chat commands opt-in by default |

---

## Original Baritone Features

All the pathfinding power of upstream Baritone is preserved:

### Pathing
- Long-distance pathing & splicing
- Chunk caching (2-bit block representation: AIR, SOLID, WATER, AVOID)
- Block breaking & placing (tool-aware, sneak-back-placing, pillaring)
- Falling (up to 3 blocks, or 23 with water bucket)
- Vines, ladders, fence gates, doors, slabs, stairs
- Parkour (1–3 block gaps) & parkour place
- Falling block safety, dangerous block avoidance

### Goals
`GoalBlock`, `GoalXZ`, `GoalYLevel`, `GoalTwoBlocks`, `GoalGetToBlock`, `GoalNear`, `GoalAxis`, `GoalComposite`

### Commands
`#goto`, `#mine`, `#farm`, `#build`, `#explore`, `#follow`, `#come`, `#tunnel`, `#elytra`, `#sel`, `#set`, and 30+ more

---

## Quick Start

```
#goto 1000 500       — travel to x=1000, z=500
#mine diamond_ore    — mine all diamond ore
#farm                — farm all nearby mature crops
#farmlist wheat carrots potato — farm only those three crops
#stop                — stop everything
```

For full documentation, see [USAGE.md](USAGE.md) and [SETUP.md](SETUP.md).

---

## API

```java
BaritoneAPI.getSettings().allowSprint.value = true;
BaritoneAPI.getSettings().primaryTimeoutMS.value = 2000L;
BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(10000, 20000));
```

Full Javadocs: [baritone.leijurv.com](https://baritone.leijurv.com/)

---

## Credits & Acknowledgments

### Original Baritone
- **[leijurv](https://github.com/leijurv/)** — original creator of MineBot & Baritone
- **[Brady](https://github.com/leijurv/)** — co-creator and maintainer
- [cabaletta/baritone](https://github.com/cabaletta/baritone) — the upstream project

### MeteorDevelopment Fork
- [MeteorDevelopment/baritone](https://github.com/MeteorDevelopment/baritone) — maintenance fork providing the base for this branch

### Special Thanks
- **YourKit** — for an OSS license of the [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)
- The entire Baritone community on [Discord](http://discord.gg/s6fRBAUpmr)

This project is licensed under LGPL-3.0 with the Baritone Anime Exception.

---

<p align="center">
  <sub>If you find this useful, <a href="https://github.com/rahulcvwebsitehosting/baritone">star the repo</a> ⭐</sub>
</p>
