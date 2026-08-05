# CLAUDE.md

Rules for AI assistants working in this repo. Read before writing code.

## Project overview

A Server-Driven UI system for Android. The server sends a JSON node tree; the client renders native Compose. The repo also contains a hardcoded twin of the same screen for performance comparison.

The schema is the product. The renderer must work for screens that do not exist yet.

## Commands

- Build: `./gradlew build`
- Assemble debug APK: `./gradlew assembleDebug`
- Install on connected device/emulator: `./gradlew installDebug`
- Unit tests: `./gradlew test`
- Single unit test: `./gradlew testDebugUnitTest --tests "com.chiraggoswami.sduidemo.ExampleUnitTest"`
- Instrumented tests (needs a connected device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`

## Stack facts

- `namespace` / `applicationId` `com.chiraggoswami.sduidemo`, minSdk 24,
  target/compileSdk 37, Java 11 source/target compatibility, Gradle JVM toolchain 25.
- 100% Jetpack Compose. No XML layouts, no Fragments. Entry point `MainActivity.kt`.
- Material3. Theming in `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`).
- Dependency versions centralized in `gradle/libs.versions.toml`. The
  `kotlin-serialization` plugin is applied at root and per-module.
- Coil 3 (`coil-compose` + `coil-network-okhttp`) for remote images.
- Screen configs are JSON in `app/src/main/assets`, parsed with
  `kotlinx.serialization`. There is no network layer.
- **No DI framework.** No Hilt, no Koin. Plain constructors and factories.
- No new dependencies without asking. No Accompanist. No Gson/Moshi.

## Non-negotiables

- `SduiNode.type` stays a `String`. `SduiNode.props` stays a raw `JsonElement`.
- **Never** use sealed-class polymorphic deserialization for nodes. An unknown
  `type` from the server must not throw and kill the page parse. This is the
  single most important behaviour in the codebase.
- Json config is `ignoreUnknownKeys = true`, `isLenient = true`,
  `explicitNulls = false`.
- Prop decoding is guarded with `runCatching` and returns null on failure, so a
  malformed node is skipped while the rest of the page renders. A backend change
  must never be able to white-screen the app. This guard belongs in exactly two
  places — the per-component props decode and the top-level page parse. Do not
  scatter `runCatching` elsewhere; real bugs should throw.
- A skipped node is **never silent**: log node id and type, and in debug builds
  render a visible placeholder naming the failure.
- Type dispatch is a **map lookup** in `ComponentRegistry`, never a branch. No
  `when (node.type)` anywhere in the codebase. `catalog/AppRegistry.kt` is the
  only file that names component types. Adding a component must not require
  editing anything in `core/`.
- Every node rendered in a lazy list uses `node.id` as the key. Never the index.

## Package boundaries

```
core/      schema, registry, render      — the engine
catalog/   UI components + registry  — the design system
data/      ScreenRepository              — payload source
static/    hardcoded screen              — benchmark twin
ui/theme/  colors, typography
```

- **`core/` must not import from `catalog/`, `data/`, `static/`, or `ui/theme/`.**
  Dependencies point one way. `core/` must be liftable into its own module unchanged.
- No files at package root.
- No `dto/` or `mapper/` package. The payload schema *is* the presentation model;
  a mapper here is an identity function that also defeats lazy prop decoding.
- No `utils/` package. Helpers live in the package that uses them.

## Code budget

- No file over 150 lines. No composable or function over 40.
- No interface with exactly one implementation. Exception: `ScreenRepository`.
- No use-case or domain layer. ViewModel talks to the repository directly.
- One ViewModel for the SDUI screen. One UI state class.
- If you are about to add a layer, a wrapper, or a second model that mirrors an
  existing one — stop and say so instead of writing it.

## Conventions

- Asset reading **and** parsing happen in the repository, off the main thread
  (`Dispatchers.IO` for the read, `Dispatchers.Default` for the parse). The
  repository holds the `Context`; the ViewModel does not. No `AndroidViewModel`.
- Parsing happens **once** per screen load. Never in composition, never inside
  `remember {}`. The ViewModel holds the parsed tree in `StateFlow` so it
  survives configuration change.
- `Context` reaches the repository via the ViewModel factory at the call site,
  using `applicationContext`. Never store an Activity `Context`.
- Component composables take exactly `(node: SduiNode, ctx: RenderContext)`.
  Nothing else. No extra params, no callbacks passed down.
- Adding a component = one file in `catalog/` + one line in `AppRegistry.kt`.
  If it needs more than that, the design is wrong — tell me, don't work around it.
- Component type names are `snake_case`, role-based, platform-neutral.
  `car_card`, `section_rail`, `icon_grid`. Never `CarCardView`, `LazyRowSection`,
  `LinearLayout`.
- Layout primitives keep generic names: `column`, `row`, `lazy_row`, `text`,
  `image`, `spacer`.
- Per-component props are typed locally with a `@Serializable data class` inside
  that component's file. Props classes are never shared or centralised.
- Component type strings live as constants in `object NodeType` inside
  `catalog/AppRegistry.kt` — same file as the registry map. Never as inline
  string literals, never in a shared `Constants.kt`, never in `core/`.
  Action type constants may live in `core/`; the action set is engine-owned
  and closed.
- Server sends display strings (`"₹5.21 lakh"`, `"72,971 km"`), not raw numbers.
  The client does no currency, unit, or locale formatting.
- **No domain models.** No `Price`, `Car`, `Emi` value types. The client holds no
  business logic, so there is nothing for a domain type to model — it would only
  re-introduce client-side formatting, which is what SDUI exists to remove.
  Where a client-side behaviour genuinely needs a number (sorting, filtering),
  the payload carries a parallel raw field (`"price"` + `"priceValue"`), and only
  when something actually consumes it.

## Commit style

Short, imperative, capitalized subjects, no body — matching existing history
(`Add viewModel dependency`, `Add dependencies`, `Setup project`).

---

## When responding to me

- Show the diff or the file, not a narrated plan.
- If a rule above blocks the cleanest solution, say which rule and why, and let
  me decide. Do not silently work around it.
- Do not add comments explaining what the code does. Comment only non-obvious
  *why* — mainly the parser's failure-tolerance decisions.
- Do not generate `README.md` / `PERF.md` / `COVERAGE.md` / `AI_WORKFLOW.md`
  content unless I ask. I write those.

## Verification expectations

Any change to `core/schema/` or `core/render/` must keep these passing:

- unknown `type` → node skipped, rest of page renders
- unknown `type` with a `fallback` node → fallback renders
- malformed `props` on one node → that node skipped, page renders
- unknown action `type` → ignored, no crash
- empty `children`, missing `props`, null `style` → no crash

If you change parsing behaviour and don't add or update a test, say so explicitly.

