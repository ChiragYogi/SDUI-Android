package com.chiraggoswami.sduidemo.data

import android.content.Context
import com.chiraggoswami.sduidemo.core.schema.ScreenSchema
import com.chiraggoswami.sduidemo.core.schema.parseScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads `{screenId}_design.json` from assets. Read is IO, parse is Default — never on main. */
class AssetScreenRepository(private val context: Context) : ScreenRepository {
    override suspend fun loadScreen(screenId: String): Result<ScreenSchema> {
        val raw = runCatching {
            withContext(Dispatchers.IO) {
                context.assets.open("${screenId}_design.json").bufferedReader().use { it.readText() }
            }
        }.getOrElse { return Result.failure(it) }
        return withContext(Dispatchers.Default) { parseScreen(raw) }
    }
}
