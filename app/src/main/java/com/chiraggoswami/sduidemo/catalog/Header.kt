package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class HeaderProps(
    val location: String = "",
    val avatarInitials: String = "",
    val searchHint: String = "",
)

@Composable
fun Header(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<HeaderProps>() ?: return
    val style = node.style.styleObj()
    Column(
        Modifier.fillMaxWidth().background(style.bg() ?: Color(0xFF5B4FE9)).padding(style.paddingValues()),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                props.location,
                color = Color.White,
                modifier = Modifier.clickable { node.actions?.get("onLocationClick")?.let(ctx::dispatch) },
            )
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable { node.actions?.get("onAvatarClick")?.let(ctx::dispatch) },
                contentAlignment = Alignment.Center,
            ) { Text(props.avatarInitials, color = Color.White) }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable { node.actions?.get("onSearchClick")?.let(ctx::dispatch) }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { Text(props.searchHint, color = Color.White.copy(alpha = 0.8f)) }
    }
}
