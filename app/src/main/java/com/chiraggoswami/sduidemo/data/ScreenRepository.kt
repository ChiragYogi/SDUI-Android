package com.chiraggoswami.sduidemo.data

import com.chiraggoswami.sduidemo.core.schema.ScreenSchema

/** Payload source. Only interface in the codebase with more than one intended implementation. */
interface ScreenRepository {
    suspend fun loadScreen(screenId: String): Result<ScreenSchema>
}
