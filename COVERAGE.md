# COVERAGE.md

Status: **in progress**. This is the "while building" pass — registry rows and
the patterns the schema expresses, kept current as components get added. The
honest `X%` coverage claim and the second-screen dry-run results land here
after that exercise happens (see `notes.md` for the running log).

## Component registry

Every row is one file in `catalog/` + one line in `AppRegistry.kt`. Type
dispatch is a map lookup (`ComponentRegistry.rendererFor`); nothing here is a
`when (type)` branch, and nothing in `core/` names a single one of these
strings.

| `type` | File | Renders | Key JSON-only levers |
|---|---|---|---|
| `column` | `ColumnNode.kt` | Vertical stack. Also the sheet-container type and the schema's root node. | `style.bg`, `style.padding`; children literal or `template`+`items` |
| `header` | `Header.kt` | Location + avatar row, search bar. | `location`, `avatarInitials`, `searchHint`; `onLocationClick`/`onSearchClick`/`onAvatarClick` |
| `chip_row` | `ChipRow.kt` | Horizontal-scroll selectable pills. Drives filters and the EMI tenure selector with the same component. | `stateKey` (which state key selection writes), `chips[]`, `textColor`; `onSelect` |
| `hero_card` | `HeroCard.kt` | Promo card. `variant: "split"` (image beside content) or `"stacked"` (image on top) — same props, different arrangement, zero code. | `variant`, `eyebrow`/`title`/`subtitle`/`tags[]`/`priceBadge`, `cta.label`; `onCtaClick` |
| `section` | `Section.kt` | Title + optional badge + optional trailing link, wrapping any children. | `title`, `titleStyle` (size/bold/color), `badge`, `trailing.label`; `onTrailingClick` |
| `lazy_row` | `LazyRowNode.kt` | Horizontal carousel/rail. | `style.itemSpacing`; children literal or `template`+`items` |
| `grid` | `GridNode.kt` | Fixed-column wrap grid — `columns: 1` reads as a vertical list. | `columns`; same children resolution as `lazy_row`/`column` — swapping the `type` between these three is a pure JSON edit (see `notes.md`, 11:00) |
| `image_tile` | `ImageTile.kt` | Icon- or card-sized image + title, overlay or below. | `title`, `imageUrl`, `imageFill`, `titleStyle.position` (`top_start`/`bottom_start`/`below`), `width`/`height` |
| `car_card` | `CarCard.kt` | Used-car / new-car listing card: image, wishlist heart, price, EMI, badges, or a compact rank-badge variant for "trending". Same node shape covers both. | `width` (fixed dp or `"fill"`), `stateKey` (wishlist), `specs[]`/`badges[]`; `onClick`/`onEmiClick`/`onWishlistSelect`/`onWishlistUnselect` |
| `icon_card` | `IconCard.kt` | Small icon + label leaf, sized to content — the grid's service-tile template. | `title`, `imageUrl`; `onClick` |
| `image_banner` | `ImageBanner.kt` | Full-width tappable promo image, schema-driven aspect ratio/corner radius. | `imageUrl`, `aspectRatio`, `cornerRadius`; `onClick` |
| `button` | `ButtonNode.kt` | Standalone CTA. `variant: "primary"`/`"secondary"`. | `label`, `variant`; `onClick` |
| `text` | `TextNode.kt` | Generic text primitive, `variant` selects a typography scale. | `text`, `variant` (`title`/`heading`/`caption`/body), `color` |
| `footer` | `Footer.kt` | Centered sign-off block, terminal node (no actions). | `headline`, `subtitle`, `textColor` |

14 types. `showroom_rail` appears in `home_design.json` but is **deliberately
not registered** — it's the unknown-component fallback demo (see below), not
a gap.

## Patterns the schema expresses today

- **Lists & carousels** — `lazy_row` (horizontal), `grid` (wrapping,
  N columns), `column` (plain vertical). All three resolve children the same
  way, so which one a rail uses is a `type` string, not a structural choice
  baked into the data.
- **Homogeneous repeats** — `template` + `items[]`. One node shape describes
  N children; each `items` entry supplies its data and its own `id` (used as
  the rendered node's id, never the index). `@{item.path}` substitutes the
  raw JSON value when it's the *entire* prop value (arrays/numbers/bools
  survive), or does plain string interpolation when embedded in a longer
  string.
- **Conditionals** — `visibleWhen` against a single state key:
  `equals`/`notEquals`/`in`/`notIn`. Used for tab-driven section visibility
  (`topTab`) and filter-driven rail visibility (`carsFilter`). No `and`/`or`,
  no nesting — a documented ceiling, not an oversight (see Known gaps).
- **Actions** — a closed set of 8: `navigate`, `set_state`, `open_sheet`,
  `dismiss`, `open_url`, `track`, `sequence` (a list of the others, run in
  order), `show_snackbar`. A node's `actions` map names trigger keys
  (`onClick`, `onSelect`, `onWishlistSelect`, ...); the component fires a
  trigger and never knows what it does. Adding one (`show_snackbar` was the
  8th, added when wishlist toggling needed transient feedback and none of
  the existing 7 did that job) is a deliberate schema change, not a reflex.
- **State-driven UI** — a single string-keyed `StateHolder` per screen.
  Selection/pressed state is never described in the payload (`chip_row`,
  `car_card`'s wishlist heart) — payload sends a seed value + `stateKey`,
  the component reads current state to render, the JSON action writes it.
  Same split every stateful component uses.
- **Styling overrides** — `style.bg`/`style.padding`/`style.itemSpacing` at
  the node level (color tokens or `#hex`, unknown token → null → component
  default), plus per-component props (`titleStyle`, `textColor`, `width`).
  `car_card.width` (`"200"` fixed dp or `"fill"`) is the newest example: a
  container swap (`lazy_row`→`grid`) plus one prop makes the same card fill
  its new layout, no code involved.
- **Bottom sheets** — `sheets: { id: <node tree> }` at the schema root,
  opened via `open_sheet`/`sheetId`, dismissed via `dismiss` or a swipe. The
  EMI breakup sheet is a `chip_row` (tenure) + five `visibleWhen`-gated
  `column`s (one per tenure) + `text` + `button` — no sheet-specific
  component, just composition of existing ones.
- **Unknown-component fallback** — `showroom_rail` is used in
  `home_design.json` but not in `AppRegistry`. `SduiRenderer` tries a
  registry lookup, then the node's own `fallback` subtree (an `image_banner`
  here), then skips-and-logs with a visible debug placeholder if there's no
  fallback either. All three tiers are real, not theoretical — this is the
  live example, not a synthetic one built to check a box.
- **Theming** — `schema.theme: "light"` pins the whole screen to the light
  Material scheme regardless of device dark mode / dynamic color, so a
  payload can't be silently handed a color combination nobody reviewed.

## Known gaps — need new client code, not just JSON

- **New component types.** Obviously. The registry is a map, so adding one
  is one file + one line — but it's still client code, not a JSON edit.
- **Compound conditionals.** `visibleWhen` is one key, one operator. A
  screen needing `state A equals X AND state B in [Y, Z]` needs either a
  derived state key (client writes a combined key on every relevant change)
  or a real boolean-expression evaluator — out of scope per `CLAUDE.md`,
  documented here as a coverage ceiling rather than solved reactively.
- **Layout beyond padding/spacing/columns.** No weights, no gravity, no
  alignment specs in the schema (deliberate — see `CLAUDE.md` "Sizing").
  A screen needing e.g. proportional flex layout needs a new primitive.
- **New action types.** Same shape as new components — the action set is
  closed on purpose (`CLAUDE.md`), so a genuinely new interaction (not
  expressible as `sequence` + the existing 8) is a client change.
- **`minSchemaVersion`** exists on every node (parsed, present on
  `showrooms_teaser` in `home_design.json` as `2`) but **isn't consulted by
  the renderer yet** — the `showroom_rail` fallback currently fires because
  the type is unregistered, not because of a version gate. Real version
  gating (skip a node whose `minSchemaVersion` exceeds the client's known
  schema version, even if the type *is* registered) is documented intent,
  not shipped behavior. Flagged here rather than left silently implied.

## Second-screen dry run

Pending. Brief's Part 3: given a Cars24 screen not built for this
submission, measure what renders JSON-only vs. what needs a new component,
live. Result goes here once run.
