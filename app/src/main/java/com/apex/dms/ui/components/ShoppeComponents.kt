package com.apex.dms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apex.dms.data.OrderStatus
import com.apex.dms.data.QuoteStatus
import com.apex.dms.data.RequestStatus
import com.apex.dms.data.StockState
import com.apex.dms.ui.theme.ShoppeBlue
import com.apex.dms.ui.theme.ShoppeBlueSoft
import com.apex.dms.ui.theme.ShoppeDanger
import com.apex.dms.ui.theme.ShoppeInk
import com.apex.dms.ui.theme.ShoppeMuted
import com.apex.dms.ui.theme.ShoppeStroke
import com.apex.dms.ui.theme.ShoppeSuccess
import com.apex.dms.ui.theme.ShoppeWarning

val ShoppeCardShape = RoundedCornerShape(22.dp)
val ShoppeButtonShape = RoundedCornerShape(16.dp)

@Composable
fun ShoppeCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = ShoppeCardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) { content() }
}

@Composable
fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = ShoppeButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = ShoppeBlue, disabledContainerColor = ShoppeBlue.copy(alpha = 0.35f)),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White) }
}

@Composable
fun SecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(50.dp).clickable(onClick = onClick),
        shape = ShoppeButtonShape,
        color = ShoppeBlueSoft,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = ShoppeBlue, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = ShoppeInk)
        if (action != null && onAction != null) {
            Text(action, modifier = Modifier.clickable(onClick = onAction), style = MaterialTheme.typography.labelLarge, color = ShoppeBlue)
        }
    }
}

@Composable
fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search products",
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = ShoppeMuted) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ShoppeMuted) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = ShoppeStroke,
            focusedBorderColor = ShoppeBlue,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
        ),
    )
}

@Composable
fun TinyPill(text: String, color: Color = ShoppeBlue, background: Color = ShoppeBlueSoft) {
    Surface(color = background, shape = RoundedCornerShape(999.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier, trailing: ImageVector? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ShoppeInk)
            if (trailing != null) {
                Spacer(Modifier.size(6.dp))
                Icon(trailing, contentDescription = null, tint = ShoppeMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ModuleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = ShoppeBlue, modifier = Modifier.size(22.dp)) }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ShoppeInk)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = ShoppeMuted)
    }
}

@Composable
fun StatusPill(status: RequestStatus) {
    val (text, fg, bg) = when (status) {
        RequestStatus.DRAFT -> Triple("Draft", ShoppeMuted, Color(0xFFF1F3F6))
        RequestStatus.SUBMITTED -> Triple("Submitted", ShoppeBlue, ShoppeBlueSoft)
        RequestStatus.UNDER_REVIEW -> Triple("In review", ShoppeWarning, Color(0xFFFFF4DA))
        RequestStatus.QUOTE_READY -> Triple("Quote ready", ShoppeBlue, ShoppeBlueSoft)
        RequestStatus.QUOTE_SENT -> Triple("Quote sent", ShoppeBlue, ShoppeBlueSoft)
        RequestStatus.AWAITING_CONFIRMATION -> Triple("Action needed", ShoppeWarning, Color(0xFFFFF4DA))
        RequestStatus.CONFIRMED -> Triple("Confirmed", ShoppeSuccess, Color(0xFFEAF8F2))
        RequestStatus.CLOSED -> Triple("Closed", ShoppeSuccess, Color(0xFFEAF8F2))
        RequestStatus.CANCELLED -> Triple("Cancelled", ShoppeDanger, Color(0xFFFFECEC))
    }
    TinyPill(text, fg, bg)
}

@Composable
fun StatusPill(status: QuoteStatus) {
    val (text, fg, bg) = when (status) {
        QuoteStatus.DRAFT -> Triple("Draft", ShoppeMuted, Color(0xFFF1F3F6))
        QuoteStatus.SENT -> Triple("Ready to review", ShoppeBlue, ShoppeBlueSoft)
        QuoteStatus.ACCEPTED -> Triple("Accepted", ShoppeSuccess, Color(0xFFEAF8F2))
        QuoteStatus.REVISION_REQUESTED -> Triple("Revision requested", ShoppeWarning, Color(0xFFFFF4DA))
        QuoteStatus.EXPIRED -> Triple("Expired", ShoppeDanger, Color(0xFFFFECEC))
    }
    TinyPill(text, fg, bg)
}

@Composable
fun StatusPill(status: OrderStatus) {
    val (text, fg, bg) = when (status) {
        OrderStatus.CONFIRMED -> Triple("Confirmed", ShoppeBlue, ShoppeBlueSoft)
        OrderStatus.PROCESSING -> Triple("Processing", ShoppeBlue, ShoppeBlueSoft)
        OrderStatus.READY_TO_DISPATCH -> Triple("Ready", ShoppeWarning, Color(0xFFFFF4DA))
        OrderStatus.DISPATCHED -> Triple("In transit", ShoppeBlue, ShoppeBlueSoft)
        OrderStatus.DELIVERED -> Triple("Delivered", ShoppeSuccess, Color(0xFFEAF8F2))
        OrderStatus.ON_HOLD -> Triple("On hold", ShoppeWarning, Color(0xFFFFF4DA))
        OrderStatus.CANCELLED -> Triple("Cancelled", ShoppeDanger, Color(0xFFFFECEC))
    }
    TinyPill(text, fg, bg)
}

@Composable
fun StockPill(state: StockState) {
    when (state) {
        StockState.IN_STOCK -> TinyPill("In stock", ShoppeSuccess, Color(0xFFEAF8F2))
        StockState.LIMITED -> TinyPill("Limited", ShoppeWarning, Color(0xFFFFF4DA))
        StockState.ON_REQUEST -> TinyPill("On request", ShoppeMuted, Color(0xFFF1F3F6))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(title: String, onBack: (() -> Unit)? = null, action: (@Composable () -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = ShoppeInk) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = ShoppeInk) }
            }
        },
        actions = { if (action != null) action() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
    )
}

@Composable
fun DividerLine() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(ShoppeStroke))
}

@Composable
fun SoftBadgeIcon(icon: ImageVector, tint: Color = ShoppeBlue) {
    Surface(shape = CircleShape, color = tint.copy(alpha = 0.1f), modifier = Modifier.size(42.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp)) }
    }
}
