# SDUI Demo

A Server-Driven UI system for Android: the server sends a JSON node tree, the
client renders it natively in Compose. `home_design.json` is the entire home
screen — layout, content, and interaction wiring — with zero of it
hand-coded per-screen on the client.

Status: schema/renderer/catalog built and exercised; `PERF.md` and
`COVERAGE.md`'s final numbers/claims are pending the measure → optimize
pass and the second-screen dry run (see those files, and `notes.md` for the
running log). This README's own Trade-offs section is provisional for the
same reason.

**Note on commit history**: if you're viewing this from an extracted zip
rather than a clone, the `.git` folder (and with it the full commit history —
"we read how you worked" per the brief) may not have survived the zip step.
Clone the actual repo to see it: **`https://github.com/ChiragYogi/SDUI-Android`**.

## Video

3-5 min screen recording: JSON rendering into the live screen, the EMI tenure
selector + bottom sheet, the `showroom_rail` unknown-component fallback, and
one live JSON edit (change `home_design.json`, re-run, page changes with zero
client code touched). Link: **`<add recording link here>`**.

## Which screen, and why

The Cars24 home/landing page. It clears every complexity bar the brief sets
and then some: 14 distinct component types across header, hero promo,
category tiles, service grid, two filterable car rails, promo banners, a
new-car rail, and a footer; both a horizontal carousel (`lazy_row`) and a
wrapping grid (`grid`); multiple SDUI-driven interactive elements (tab/chip
filters that change what's visible, a wishlist toggle with server-defined
feedback, an EMI tenure selector that drives a bottom sheet); and a
deliberate unknown-component case (`showroom_rail`) with a live fallback,
not a synthetic one added just to check a box. Real-feeling data throughout
(actual car specs, EMI figures, badge copy), hardcoded in the JSON per the
brief.

## Screenshots: reference vs. build

Side by side for a quick visual sanity check — the actual Cars24 home screen
next to this SDUI-rendered build, same sections, same order.

| Cars24 app (reference) | This build (SDUI-rendered) |
|---|---|
| [cars24_reference.jpg](screenshots/cars24_reference.jpg) | [sdui_build.jpg](screenshots/sdui_build.jpg) |

Both are full-page scrolling captures — click either link to view.

## Setup

Requires Android Studio (or the `gradlew` wrapper directly) with an Android
SDK, a connected device or emulator for install/instrumented-test commands.

```
./gradlew build                 # full build
./gradlew installDebug          # install on a connected device/emulator
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # instrumented tests (needs a device)
```

The app has one launcher icon. `MainActivity` renders the SDUI screen by
default; launching it with a `screen_variant=static` string extra renders
the hardcoded twin instead (see Architecture below for why both exist):

```
adb shell am start -n com.chiraggoswami.sduidemo/.MainActivity --es screen_variant static
```

Cold-start benchmark (needs a connected device/emulator):

```
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
```

See `CLAUDE.md` for the full command reference and repo conventions.

## Architecture overview

```
core/      schema, registry, render      — the engine
catalog/   UI components + registry      — the design system
data/      ScreenRepository              — payload source
static/    hardcoded screen              — benchmark twin
ui/theme/  colors, typography
```

`core/` never imports from `catalog/`, `data/`, `static/`, or `ui/theme` — it's
liftable into its own module unchanged. Only `catalog/AppRegistry.kt` names
component type strings; nothing in `core/` does.

**Data flow, cold to rendered:**

1. `AssetScreenRepository.loadScreen` reads `{screenId}_design.json` from
   assets (`Dispatchers.IO`), then `parseScreen` decodes it into a
   `ScreenSchema` (`Dispatchers.Default`) — read and parse are separate
   dispatcher hops, never on main, never inside `remember {}`.
2. `SduiScreenViewModel` holds the resulting `ScreenUiState` (`Loading` /
   `Ready` / `Error`) as a `StateFlow`. It talks to `ScreenRepository`
   directly — no use-case/domain layer.
3. `SduiScreen` collects that state. Once `Ready`, it builds one
   `StateHolder` (named state keys + which sheet is open), one
   `ActionDispatcher`, and one `RenderContext` bundling both plus the
   `ComponentRegistry` — all `remember`ed once per screen instance.
4. `RenderNode` walks the tree: check `visibleWhen` against current state,
   look up the node's `type` in the registry (map lookup, not a `when`),
   render if found, else try the node's own `fallback` subtree, else
   skip-and-log (with a visible debug placeholder in debug builds).
5. Components are plain `(node: SduiNode, ctx: RenderContext)` composables.
   They decode their own props locally (`decodeProps<T>()`, `runCatching`,
   null-safe), read state through `ctx.state`, and fire triggers through
   `ctx.dispatch` — a component never knows what a trigger does.

**Two builds of one screen, one app.** `static/StaticHomeScreen` is an
independent, hand-written Compose implementation of the same screen — no
`core`/`catalog` imports, so it doesn't pay SDUI's registry-lookup/
prop-decode cost, which is the point of the comparison (`PERF.md`). It's not
a second app or Gradle flavor: `MainActivity` picks between the two off a
launch-time intent extra, which still gives `:macrobenchmark` a genuine cold
start for both (`StartupMode.COLD` force-stops the whole process before every
iteration regardless of how many entry points live in one app). See
`notes.md` (10:48) for why an earlier two-product-flavor version of this was
reverted — it solved a problem the benchmark didn't actually have.

### Directory structure

```
app/src/main/java/com/chiraggoswami/sduidemo/
├── MainActivity.kt
├── core/
│   ├── schema/    SduiNode.kt, ActionSpec.kt, VisibleWhen.kt, ScreenSchema.kt,
│   │              ScreenParser.kt, PropsDecoding.kt
│   ├── registry/  ComponentRegistry.kt
│   └── render/    SduiRenderer.kt, RenderContext.kt, StateHolder.kt,
│                  ActionDispatcher.kt, TemplateExpander.kt, Interpolation.kt
├── catalog/       AppRegistry.kt, ColumnNode.kt, LazyRowNode.kt, GridNode.kt,
│                  Header.kt, HeroCard.kt, Section.kt, ChipRow.kt, CarCard.kt,
│                  IconCard.kt, ImageTile.kt, ImageBanner.kt, ButtonNode.kt,
│                  TextNode.kt, Footer.kt, NodeStyle.kt
├── data/          ScreenRepository.kt, AssetScreenRepository.kt
├── screen/        SduiScreen.kt, SduiScreenViewModel.kt, ScreenUiState.kt
├── static/        StaticHomeScreen.kt + 13 files, zero core/catalog imports
└── ui/theme/

app/src/main/assets/
├── home_design.json   the entire home screen: layout, content, actions
└── images/            placeholder car/banner/icon images referenced by imageUrl

macrobenchmark/src/main/java/.../macrobenchmark/
├── StartupBenchmark.kt         TTID/TTFD, static vs. SDUI cold start
├── SduiBreakdownBenchmark.kt   asset-read / json-parse / view-build trace sections
├── ScrollBenchmark.kt          FrameTimingMetric — currently blocked, see PERF.md
└── Targets.kt
```

### Example: a JSON node and its component, side by side

`home_design.json`'s EMI sheet dismiss button:

```json
{
  "id": "emi_cta",
  "type": "button",
  "props": { "label": "Got it", "variant": "primary" },
  "actions": {
    "onClick": {
      "type": "sequence",
      "actions": [
        { "type": "track", "payload": { "event": "emi_sheet_dismiss" } },
        { "type": "dismiss", "payload": {} }
      ]
    }
  }
}
```

Its entire renderer, `catalog/ButtonNode.kt` — every component in the catalog
follows this exact shape (decode props locally, read `node.actions` for
triggers, never touch anything the registry or `core/` owns):

```kotlin
@Serializable
private data class ButtonProps(val label: String = "", val variant: String = "primary")

@Composable
fun ButtonNode(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<ButtonProps>() ?: return PropsDecodeFailurePlaceholder(node)
    val onClick: () -> Unit = { node.actions?.get("onClick")?.let(ctx::dispatch) }
    when (props.variant) {
        "secondary" -> OutlinedButton(onClick = onClick) { Text(props.label) }
        else -> Button(onClick = onClick) { Text(props.label) }
    }
}
```

`ButtonNode` never knows what `onClick` *does* — `ctx.dispatch` hands the
`ActionSpec` to `ActionDispatcher`, which is the only place that interprets
action types. Adding a component is one file here plus one line in
`AppRegistry.kt`; nothing in `core/` changes shape.

## Schema design rationale

- **`SduiNode.props` stays a raw `JsonElement`; `type` stays a `String`.**
  The payload *is* the presentation model. A `dto`/`mapper` layer here would
  be an identity function that also defeats decoding props lazily, per
  component, only when that component actually renders.
- **Type dispatch is a map lookup, never sealed-class polymorphism or a
  `when (type)` branch.** This is the single most load-bearing decision in
  the codebase: an unknown `type` from the server must not throw and kill
  the whole page parse. Sealed-class deserialization fails exactly the case
  SDUI exists to handle — a client that's behind the server's schema.
- **Two, and only two, `runCatching` guards**: top-level `parseScreen`, and
  `decodeProps` per component. A malformed node is isolated and skipped;
  everything else on the page still renders. Guards aren't scattered further
  than that — real bugs elsewhere should throw, not be swallowed.
- **`template` + `items[]`** for homogeneous repeats, instead of N
  hand-authored sibling nodes. Keeps the payload declarative (data describes
  what varies per item) rather than imperative (N copies of near-identical
  structure), and `@{item.path}` substitution preserves raw JSON types
  (arrays/numbers/bools) when a prop's value is *exactly* the token, so
  numeric fields like `priceValue` don't round-trip through a string.
- **Every container (`column`/`lazy_row`/`grid`) resolves children through
  the same `resolveChildren()`.** The payload's repeat structure is
  decoupled from its visual arrangement — a rail becoming a grid is a
  `type` string edit, not a data reshape. Demonstrated live: swapping
  `lazy_row`→`grid` on "Used cars you'll love" (see `notes.md`, 11:00).
- **`visibleWhen` is one state key, one operator**
  (`equals`/`notEquals`/`in`/`notIn`) — deliberately not a boolean
  expression evaluator. A screen needing compound conditions is a documented
  coverage gap (`COVERAGE.md`), not a feature built reactively under time
  pressure.
- **The action set is closed at 8** (`navigate`, `set_state`, `open_sheet`,
  `dismiss`, `open_url`, `track`, `sequence`, `show_snackbar`). A component
  fires a named trigger and never knows what it does — the JSON, not the
  component, owns behavior. Each addition was deliberate and demand-driven:
  `dismiss` when the EMI sheet had no way to close from JSON, `show_snackbar`
  only once wishlist toggling needed transient feedback none of the other 7
  provided.
- **`navigate` payload is `{ route, params, deeplink? }`**, not a URI. A
  named route resolves against the client's own route table; stringly-typed
  deep links fail silently on typos and bake platform URI conventions into a
  schema meant to be platform-neutral. `deeplink` stays reserved for genuine
  external/web targets.
- **Selection/pressed/disabled state is owned by the component, never
  described in the payload.** The payload sends a seed value + `stateKey`
  (`chip_row`'s selection, `car_card`'s wishlist heart); a `StateHolder`
  tracks the live value; the JSON action writes it on interaction. The
  server never has to resend a node tree because the user tapped something.
- **The server sends display strings** (`"₹5.21 lakh"`), not raw numbers
  needing client-side formatting. The client does no currency/unit/locale
  formatting. A parallel raw field (`priceValue`) exists only where a real
  client behavior consumes it — not speculatively.
- **No domain models.** No `Price`/`Car`/`Emi` types. A domain model here
  would just mirror the payload shape with no behavior of its own — dead
  weight in a system where the payload already is the model.
- **Colors and sizes are tokens/keywords, not raw platform values**, with
  graceful degradation: unknown color token → `null` → component default;
  fixed dp, `"fill"`, or `"content"` for sizing, nothing else. An older
  client handed a token it doesn't recognize renders *something* reasonable
  instead of crashing or rendering nothing.

## Versioning story

- **Parser tolerance is the first line of defense.** `ignoreUnknownKeys =
  true`, `isLenient = true`, `explicitNulls = false` — an old client handed
  a newer payload with fields it doesn't understand yet ignores them and
  keeps parsing, instead of failing the whole screen.
- **Unknown component `type` → two-tier fallback**, live in this build, not
  theoretical: registry miss checks the node's own `fallback` subtree first
  (`showroom_rail`'s fallback renders a plain `image_banner`), and only
  skips-and-logs (with a visible debug placeholder) if there's no fallback
  either. A client several versions behind a new section still renders every
  other section on the page.
- **`SduiNode.minSchemaVersion`** exists in the schema today (parsed,
  present on `showrooms_teaser` in `home_design.json` as `2`) as the
  intended lever for "this node needs client schema version ≥ N" —
  documented honestly in `COVERAGE.md` as **not yet consulted by the
  renderer**. `showroom_rail`'s fallback currently fires because the type is
  unregistered, not because of a version check. Wiring it in is small and
  contained given the fallback tiering already exists: `RenderNode` would
  compare `node.minSchemaVersion` against a client-side
  `CURRENT_SCHEMA_VERSION` constant and treat "too new" identically to
  "unregistered type."
- **`ScreenSchema.schemaVersion`** (top-level, currently always `1`) is the
  payload's own version stamp — the anchor `minSchemaVersion` compares
  against once that's wired in. Nothing reads it yet either; it's present
  because the versioning story needs an anchor to exist before it needs
  logic.
- **The component registry and action set are closed by deliberate PR, not
  implicitly extensible.** Adding either is a stated, deliberate schema
  change (`CLAUDE.md`) — server and client teams share an explicit contract
  surface that doesn't silently drift, rather than an implicit one that does.

## Trade-offs

Provisional — completed once `PERF.md` has real numbers. Known candidates
already visible without measuring:

- Per-component prop decoding goes through reflection-based
  `kotlinx.serialization` on every render, not a stricter/faster
  hand-rolled decode. Chosen deliberately — it's what makes "unknown/
  malformed props skip the node instead of crashing" cheap to guarantee
  uniformly — but worth checking whether it shows up as a meaningful slice
  of view-build time once `PERF.md`'s SDUI breakdown exists.
- `resolveChildren()` re-expands `template`+`items` inside the composable
  body of whichever container calls it; whether that re-expansion is
  actually re-running on recompositions that don't need it (vs. being
  skipped by Compose's structural equality checks) is unverified — a
  candidate profiling target, not a claimed problem.
- Full-tree recomposition scoping (does tapping one `car_card`'s wishlist
  heart recompose just that card, or more of the tree around it) is likewise
  unmeasured. `StateHolder` uses `mutableStateMapOf`, which scopes state
  reads per key, but whether every read site is narrow enough to benefit
  hasn't been checked with the Layout Inspector's recomposition counts.
