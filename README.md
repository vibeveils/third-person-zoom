# Third Person Zoom (Fabric, Minecraft 26.3)

Hold a key and scroll in third person to move the camera closer or much further out.

## Controls
- **Hold Left Alt + scroll** (in either third-person view) to zoom. Rebind under *Options → Controls → Key Binds → Third Person Zoom*.
- While the key is held, scrolling won't change your hotbar slot.

## Options
Open through **Mod Menu** (optional), or edit `config/thirdpersonzoom.json`.

| Setting | Default | Notes |
|---|---|---|
| Default distance | 4 blocks (vanilla) | Where the camera starts each time you go into third person |
| Max distance | 128 blocks | Can be raised up to 512 |
| Scroll step | 12% | Each notch changes distance by this % of the current distance, so zooming feels even at 3 or 300 blocks |
| Smooth zoom | On | Eases between distances |
| Stop camera at walls | On | Vanilla-style collision. Turn off to let the camera pass through blocks when zoomed far out |
| Reset when entering 3rd person | On | Off = remember your last zoom |
| Invert scroll | Off | |

## Build
Requires **JDK 25**.

```
./gradlew build
```

The mod jar is `build/libs/third-person-zoom-1.0.0.jar`. Put it in your `mods` folder with Fabric Loader 0.19.5+ and Fabric API for 26.3.

Versions come from the official `fabric-example-mod` (26.3): Loader 0.19.5, Loom 1.18-SNAPSHOT, Fabric API 0.161.0+26.3, Gradle 9.7.1. Mod Menu 21.0.0 is a compile-only dependency.

## How it works
- `CameraMixin` wraps the third-person pull-back (`Camera.alignWithEntity` → first `move(FFF)` call) and substitutes the zoom distance, scaled by entity size and the camera-distance attribute, with its own 8-ray block collision check.
- `MouseHandlerMixin` cancels `onScroll` while the key is held in third person and feeds the scroll into the zoom instead.

## GitHub Actions
`.github/workflows/build.yml` builds on every push and pull request; the jar is under the run's **Artifacts**.
To publish a release, bump `version` in `gradle.properties`, then push a tag:

```
git tag v1.0.0
git push origin v1.0.0
```

That creates a GitHub release with the jar attached.
