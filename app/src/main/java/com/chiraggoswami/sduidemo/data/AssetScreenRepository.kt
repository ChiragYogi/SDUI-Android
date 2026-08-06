package com.chiraggoswami.sduidemo.data

import android.content.Context
import android.os.Trace
import com.chiraggoswami.sduidemo.core.schema.ScreenSchema
import com.chiraggoswami.sduidemo.core.schema.parseScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Section names read by :macrobenchmark's TraceSectionMetric (SduiBreakdownBenchmark) — a
// rename here needs a matching rename there. android.os.Trace, not androidx.tracing: it's a
// platform API (minSdk 18+, this app's minSdk is 24), so no new Gradle dependency to add just
// for two beginSection/endSection pairs — androidx.tracing:tracing-ktx would only earn its
// keep if more call sites needed its try/finally-safe trace(name) { } wrapper than these two.
private const val SECTION_ASSET_READ = "sdui_asset_read"
private const val SECTION_JSON_PARSE = "sdui_json_parse"

/** Reads `{screenId}_design.json` from assets. Read is IO, parse is Default — never on main. */
class AssetScreenRepository(private val context: Context) : ScreenRepository {
    override suspend fun loadScreen(screenId: String): Result<ScreenSchema> {
        val raw = runCatching {
            withContext(Dispatchers.IO) {
                Trace.beginSection(SECTION_ASSET_READ)
                try {
                    context.assets.open("${screenId}_design.json").bufferedReader().use { it.readText() }
                } finally {
                    Trace.endSection()
                }
            }
        }.getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) {
            Trace.beginSection(SECTION_JSON_PARSE)
            try {
                parseScreen(raw)
            } finally {
                Trace.endSection()
            }
        }
    }
}
