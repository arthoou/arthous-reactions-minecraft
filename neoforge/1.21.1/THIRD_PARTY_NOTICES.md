# Bundled animation libraries

- **Player Animator 2.0.4+1.21.1**, Copyright (c) 2022 KosmX.
  [Source](https://github.com/KosmX/minecraftPlayerAnimator/tree/1.21),
  [MIT license](https://github.com/KosmX/minecraftPlayerAnimator/blob/1.21/LICENSE).
  The license text is included at `META-INF/licenses/playeranimator-MIT.txt`.
- **Bendy lib 5.1**, by KosmX.
  [Source](https://github.com/KosmX/bendy-lib),
  [Creative Commons Attribution 4.0 International](https://creativecommons.org/licenses/by/4.0/).

These libraries are distributed unmodified inside this mod through NeoForge
Jar-in-Jar. Bendy lib supplies the arm and leg bends used by the Scuba dance.
The supplied animation, icon and music are separate assets provided for this mod.

## Supplied angel wings

The user supplied `asa.cpmproject`, whose embedded description identifies the
model as **Little Angel — Made by eosuki**. The wing hierarchy and original
`skin.png` were converted for this mod. The texture bytes, dimensions, per-face
UVs and original mesh proportions are retained; mounting and animation are
added separately. Original project SHA-256:
`0677318f9cf07522f86cd89928fec6b12c2ad597d61ae2922e30d7a8734b95de`.

The conversion reads the [CPM project format](https://github.com/tom5454/CustomPlayerModels)
and produces data for this mod's renderer. CPM itself is not bundled or required.

## Optional 3D Skin Layers integration

This mod can use the public MeshTransformer API from
[3D Skin Layers by tr7zw](https://github.com/tr7zw/3d-Skin-Layers).
The official NeoForge 1.11.4 binaries are compile-only/test dependencies, not
bundled or redistributed in this JAR. Install the matching Minecraft version
separately to enable its 3D skin geometry. The mod remains optional.
