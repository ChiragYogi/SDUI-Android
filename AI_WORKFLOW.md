# AI_WORKFLOW.md

## Tool stack

- **Claude Code** (CLI/desktop) — primary pairing tool for the whole build:
  schema design, `core/`/`catalog/` implementation, Macrobenchmark setup,
  and this document.
- **ChatGPT** — used once, deliberately, for an independent second opinion
  on the schema, rather than as a second primary tool. The point was
  triangulation against a different model, not redundancy.
- **Android Studio + a physical device** (OPPO CPH2371) and an API 36.1
  emulator — every feature verified on-device, not just compiled; every
  perf number from the physical device specifically (`PERF.md`'s own
  methodology section explains why).

## Context/rules files

- **`CLAUDE.md`** — the prescriptive rules file. First cut came from
  Claude Code's own `/init`, which was accurate but purely descriptive
  (gradle commands, stack facts) and would not have stopped a single bad
  pattern. Rewrote it as rules with a stated failure mode instead —
  "never sealed-class polymorphism" reads differently from "sealed-class
  deserialization fails exactly the case SDUI exists to handle." Hit 181
  lines at one point and cut to 145: every rule that arrived with a
  paragraph of justification got skimmed less carefully the longer the file
  got. Moved the justification to `README.md`, kept two exceptions inline
  (the sealed-class rule, the `runCatching` rule) where the failure mode
  *is* the rule and stripping it invites the model to refactor the
  protection back out.

## Three prompt → outcome stories

### 1. Two independent AI reviews of the same schema, and the value was the disagreement

Ran the schema past both Claude Code and ChatGPT independently — same
brief, same old schema, same reference screenshot — and asked each for
gaps.

**Converged and acted on:** `template` + `items[]` for `lazy_row`/`grid`.
Both named this the single highest-leverage gap — every homogeneous rail
was N hand-written sibling nodes at the time, and a 40-car listing page
(the likeliest surprise screen) doesn't survive that shape. Converted all
seven rails. Also converged on a real `button` primitive (the EMI sheet's
"Got it" was `text` + `variant:"cta"`, a workaround wearing a component's
clothes) and on capping `visibleWhen` at `equals`/`notEquals`/`in`/`notIn`
— both reviews suggested `and`/`or` support; rejected deliberately, since a
boolean-expression evaluator is out of scope and the brief explicitly
rewards naming a ceiling over building one reactively.

**Converged, but rejected the framing:** ChatGPT flagged "no actions-as-array
for multi-CTA cards" as a schema gap. It wasn't — `car_card` already proves
one node carries multiple independent named triggers
(`onClick`/`onEmiClick`/`onWishlistClick`); a hypothetical `showroom_card`
would reuse the same pattern. The real gap was the component not being
authored yet, not the schema being unable to express it — correcting that
distinction mattered because conflating the two understates what the
schema already does. Also rejected: renaming `header`→`app_bar`,
`hero_card`→`promo_card` into generic slotted containers. Both already
take optional fields; genericizing the names further means guessing the
next screen's shape blind, which is the exact speculative-generality
`CLAUDE.md` says to flag rather than build.

**Why this is the story worth keeping**: two tools reviewing the same
artifact disagreed with each other almost as much as they agreed with me.
Neither model's confidence was a reliable signal for whether a suggestion
fit *this* architecture — the useful information was in where they
diverged, not in averaging their output.

### 2. "the tenure selector + bottom sheet" was a specific ask, not the example restated

Assumed the brief's EMI-tenure-selector line was illustrative — chip-driven
state (`topTab`, `carsFilter`) already proved SDUI actions worked, so the
plan was to leave the EMI sheet as a static "Got it" and reuse that proof.

Claude caught the distinction on a direct re-read: the brief's "Actions"
bullet under *What your system must handle* is a loose example, but the
Submission checklist names **"the tenure selector + bottom sheet working"**
as one of exactly four things the recording must show — a specific,
separate requirement, not the same one restated.

Rejected the "one action demo covers all action demos" assumption once
that distinction was pointed out. The fix was cheap because the underlying
pattern already existed: added an `emiTenure` chip_row and five
`visibleWhen`-gated EMI groups, same shape as `topTab`. No client-side
interest-rate math — precomputed strings, same as everywhere else in the
schema. Would have shipped believing one interactive proof was
interchangeable with another, specifically-named one, without the re-read.

### 3. "fit the width in column state, wrap in row state" — rejected my own first framing

Wanted `car_card` to fill its container when a rail switched from
`lazy_row` to `grid`, for the planned live-edit demo. First instinct: have
the component detect its parent container type and adapt.

Rejected that instinct myself before writing it — this architecture's
component signature is exactly `(node: SduiNode, ctx: RenderContext)`, no
extra params, no parent context passed down (`CLAUDE.md`). "Detect the
parent" would have meant breaking that contract for one component's
convenience.

The actual answer was simpler and consistent with every other sizing
decision already in the schema: let the JSON say what it wants. Added
`car_card.props.width` (`"200"` fixed dp default, `"fill"` to
`fillMaxWidth()`), the same way `resolveColor` already interprets a color
token. The live-edit demo became two JSON field changes
(`type: grid` + `width: fill`), not one — and `width` stayed as a real,
committed schema capability afterward, not scaffolding built only for the
demo.

## One AI failure

**Reached for two Gradle product flavors (`static`/`sdui`) to get two
independently cold-startable app variants for the benchmark — more
machinery than the actual constraint needed, and it produced a real bug.**

The reasoning at the time seemed sound: `StartupMode.COLD` needs two
genuinely separate cold-start entry points, and two Gradle product flavors
(two `applicationId`s, two launcher icons) is the idiomatic Android pattern
for "two variants of one app" — the same shape as a free/paid split. Built
it, and it worked.

It produced a real bug on the way: both flavors shared one `app_name`
string resource, so installed side by side they showed up as two
identically-labeled, identically-iconed launcher entries — impossible to
tell apart without opening each one. Fixed with per-flavor resource
overrides, which worked, but was a symptom of the actual problem, not the
problem itself.

**Caught by a direct question, not by re-reading my own work**: asked
point-blank why `app/src/benchmark/` existed at all, and whether the
two-flavor split was overkill versus simpler alternatives. Re-examining it
under that question surfaced the actual constraint: `StartupMode.COLD`
force-stops the whole process before every iteration regardless of how
many launch entry points live inside *one* installable app. Two Gradle
product flavors were never required to get two genuine cold starts — only
two independently-launchable entry points, which a single app with a
`screen_variant` launch-intent extra provides just as validly. (A proposed
alternative — an in-app navigation button between the two screens — is
*also* wrong, for a different, worth-naming reason: navigating from an
already-warm process isn't a cold start, and TTR/TTI are explicitly
cold-open metrics. That would have silently produced numbers that don't
answer what Part 2 asks.)

Collapsed the flavor split entirely: removed `flavorDimensions`/
`productFlavors` from both Gradle files, deleted the now-unnecessary
per-flavor `strings.xml` overrides, switched `MainActivity` to branch on a
`screen_variant=static` launch extra instead of `BuildConfig.FLAVOR`. Same
cold-start guarantee, one installable app, no duplicate launcher icons to
mislabel in the first place.

**What made this the failure worth keeping**: the AI-suggested pattern
wasn't *wrong* in isolation — it worked, and it's genuinely the idiomatic
choice for a different problem (shipping two separate app variants to
users). It was more surface area than *this* constraint required, and that
extra surface area is exactly what produced the launcher-icon bug. I
didn't catch the overreach myself by re-reading the decision after the
fact — it took being asked "why does this exist" from outside. The
verification habit that came out of it: after any structural choice (new
build dimension, new module, new abstraction), ask "what's the smallest
thing that satisfies the actual constraint" *before* writing it, not after
someone else questions its cost.

## Verification strategy

- **"It compiles" and "it works" are different claims for SDUI
  specifically** — a skipped node doesn't throw, so nothing forces you to
  notice a silent failure. Every component was verified live on-device
  after building it: tapped the actual interaction, watched logcat for
  zero `PropsDecoding`/`SduiRenderer` warnings, not just a green build.
- **Checked platform APIs against actual decompiled library classes before
  trusting a suggestion**, rather than trusting memory or a model's
  confidence — `TraceSectionMetric`/`ExperimentalMetricApi`/
  `testTagsAsResourceId` (first import guess for the last one was wrong)
  and `androidx.activity.compose.ReportDrawnWhen`/`FullyDrawnReporter` were
  both confirmed against real `.class` files before being wired in, not
  assumed correct because they sounded right.
- **Every rule written into `CLAUDE.md` had to survive "why?" once before
  it went in** — the rule that generated an early `AndroidViewModel`
  mistake came from trusting a generated file as authoritative without
  asking why it was true first.
- **For graded docs specifically** (`README.md`/`COVERAGE.md`/`PERF.md`,
  hard to unwrite once submitted): the check before writing a claim was
  "did I read this file this session, or am I recalling it" — re-read
  anything that check failed on, rather than trusting an earlier summary
  of it.
- **Measurement honesty over a convenient number**: caught the startup
  benchmark's own headline result being backwards (SDUI reading "faster"
  than static) *before* writing it down, by asking "why would this be
  true" instead of transcribing what `StartupTimingMetric` printed —
  traced it to TTID vs. TTFD being different signals, not a real
  performance win, and wrote the finding into `PERF.md` ahead of the raw
  numbers instead of after.
- **Default to the project's own stated constraints over the fastest thing
  to type**: corrected twice for testing on the debug build/emulator
  instead of the release build/physical device `PERF.md`'s own methodology
  section already specified — the fast loop and the written constraint
  pointed in different directions, and defaulting to "fast" was the
  mistake, even for a throwaway diagnostic step that never touched a
  committed number.
- **Compile + test after every change that touches `core/schema/` or
  `core/render/`**, per `CLAUDE.md`'s own verification bar — every code
  change in this build ends with `./gradlew :app:compileDebugKotlin` and
  `testDebugUnitTest`, not just a visual read of the diff.
