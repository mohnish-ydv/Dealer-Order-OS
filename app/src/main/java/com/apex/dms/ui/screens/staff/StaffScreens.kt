package com.apex.dms.ui.screens.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apex.dms.data.ActorRole
import com.apex.dms.data.ApprovalStatus
import com.apex.dms.data.AppStore
import com.apex.dms.data.Order
import com.apex.dms.data.OrderStatus
import com.apex.dms.data.PaymentStatus
import com.apex.dms.data.PriceRequest
import com.apex.dms.data.Product
import com.apex.dms.data.QuoteStatus
import com.apex.dms.data.Quotation
import com.apex.dms.data.RequestStatus
import com.apex.dms.data.allowedRequestTransitions
import com.apex.dms.data.calculateQuoteTotals
import com.apex.dms.data.money
import com.apex.dms.ui.components.DividerLine
import com.apex.dms.ui.components.InfoRow
import com.apex.dms.ui.components.ModuleRow
import com.apex.dms.ui.components.PrimaryAction
import com.apex.dms.ui.components.ProductArtwork
import com.apex.dms.ui.components.SearchBox
import com.apex.dms.ui.components.SectionTitle
import com.apex.dms.ui.components.SecondaryAction
import com.apex.dms.ui.components.ShoppeCard
import com.apex.dms.ui.components.SimpleTopBar
import com.apex.dms.ui.components.StatusPill
import com.apex.dms.ui.components.StockPill
import com.apex.dms.ui.components.TinyPill
import com.apex.dms.ui.theme.ShoppeBlue
import com.apex.dms.ui.theme.ShoppeBlueSoft
import com.apex.dms.ui.theme.ShoppeDanger
import com.apex.dms.ui.theme.ShoppeInk
import com.apex.dms.ui.theme.ShoppeMint
import com.apex.dms.ui.theme.ShoppeMuted
import com.apex.dms.ui.theme.ShoppePeach
import com.apex.dms.ui.theme.ShoppeSky
import com.apex.dms.ui.theme.ShoppeStroke
import com.apex.dms.ui.theme.ShoppeSuccess
import com.apex.dms.ui.theme.ShoppeWarning

@Composable
fun StaffHomeScreen(store: AppStore, onRequests: () -> Unit, onOrders: () -> Unit, onData: () -> Unit, onRequest: (String) -> Unit, modifier: Modifier = Modifier) {
    val metrics = store.dashboardMetrics()
    val roleLabel = when (store.session.role) {
        ActorRole.SUPER_ADMIN -> "Super Admin"
        ActorRole.ADMIN -> "Operations Admin"
        ActorRole.SALESPERSON -> store.salesperson(store.session.salespersonId)?.name ?: "Salesperson"
        else -> "Staff"
    }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("Good morning", style = MaterialTheme.typography.bodyLarge, color = ShoppeMuted)
            Text(roleLabel, style = MaterialTheme.typography.headlineLarge, color = ShoppeInk)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StaffMetric("Open requests", metrics["openRequests"].toString(), "Need review", ShoppeBlueSoft, ShoppeBlue, Modifier.weight(1f), onRequests)
                    StaffMetric("Quotes waiting", metrics["quotesWaiting"].toString(), "Dealer action", ShoppePeach, ShoppeWarning, Modifier.weight(1f), onRequests)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StaffMetric("Active orders", metrics["activeOrders"].toString(), "In fulfilment", ShoppeMint, ShoppeSuccess, Modifier.weight(1f), onOrders)
                    StaffMetric("Low stock", metrics["lowStock"].toString(), "Needs attention", ShoppeSky, ShoppeBlue, Modifier.weight(1f), onData)
                }
            }
        }
        item {
            ShoppeCard(containerColor = ShoppeBlue) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${store.conversionPercent()}% quote conversion", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("Accepted quotations / issued quotations", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = .8f), modifier = Modifier.padding(top = 3.dp))
                    }
                    TinyPill("Pipeline", ShoppeBlue, Color.White)
                }
            }
        }
        item {
            SectionTitle("Needs attention", "View inbox", onRequests)
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                store.staffRequests().filter { it.status in listOf(RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW, RequestStatus.QUOTE_READY) }.take(4).forEach { req ->
                    AttentionRow(req, store) { onRequest(req.id) }
                }
            }
        }
        item {
            SectionTitle("Quick actions")
            ShoppeCard(modifier = Modifier.padding(top = 10.dp)) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ModuleRow(Icons.Rounded.ListAlt, "Review price requests", "Assign, review and prepare quotations", onRequests)
                    DividerLine()
                    ModuleRow(Icons.Rounded.ShoppingBag, "Manage orders", "Fulfilment, dispatch and payment states", onOrders)
                    DividerLine()
                    ModuleRow(Icons.Rounded.Storefront, "Dealer & product data", "Network, catalogue and stock", onData)
                }
            }
        }
        item {
            SectionTitle("Recent activity")
            ShoppeCard(modifier = Modifier.padding(top = 10.dp)) {
                Column(Modifier.padding(16.dp)) {
                    store.snapshot.activities.take(5).forEachIndexed { index, activity ->
                        Text(activity.title, style = MaterialTheme.typography.titleMedium)
                        Text(activity.message, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${activity.actor} · ${activity.timestamp}", style = MaterialTheme.typography.labelMedium, color = ShoppeBlue, modifier = Modifier.padding(top = 3.dp))
                        if (index != store.snapshot.activities.take(5).lastIndex) DividerLine()
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffMetric(title: String, value: String, note: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), color = bg) {
        Column(Modifier.padding(15.dp)) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = fg)
            Text(title, style = MaterialTheme.typography.labelLarge, color = ShoppeInk, modifier = Modifier.padding(top = 2.dp))
            Text(note, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun AttentionRow(req: PriceRequest, store: AppStore, onClick: () -> Unit) {
    ShoppeCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Assignment, null, tint = ShoppeBlue) }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(req.reference, style = MaterialTheme.typography.titleMedium)
                Text(store.dealer(req.dealerId)?.businessName ?: req.dealerId, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
            }
            StatusPill(req.status)
        }
    }
}

@Composable
fun StaffRequestsScreen(store: AppStore, onRequest: (String) -> Unit, onQuote: (String) -> Unit, modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<RequestStatus?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    val requests = store.staffRequests().filter { req ->
        val dealerName = store.dealer(req.dealerId)?.businessName.orEmpty()
        (search.isBlank() || req.reference.contains(search, true) || dealerName.contains(search, true) || req.deliveryCity.contains(search, true)) &&
            (statusFilter == null || req.status == statusFilter)
    }
    val quotes = store.snapshot.quotations.filter { quote ->
        val dealerName = store.dealer(quote.dealerId)?.businessName.orEmpty()
        (search.isBlank() || quote.quoteNumber.contains(search, true) || dealerName.contains(search, true)) &&
            (store.session.role != ActorRole.SALESPERSON || store.request(quote.requestId)?.salespersonId == store.session.salespersonId)
    }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text("Commercial inbox", style = MaterialTheme.typography.headlineLarge)
        DataSegments(listOf("Requests", "Quotations"), tab) { tab = it }
        SearchBox(search, { search = it }, if (tab == 0) "Search request or dealer" else "Search quotation or dealer")
        if (tab == 0) {
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StaffFilter("All", statusFilter == null) { statusFilter = null }
                listOf(RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW, RequestStatus.QUOTE_READY, RequestStatus.QUOTE_SENT, RequestStatus.CONFIRMED).forEach { status ->
                    StaffFilter(status.name.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() }, statusFilter == status) { statusFilter = status }
                }
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tab == 0) {
                items(requests, key = { it.id }) { req ->
                    ShoppeCard(modifier = Modifier.fillMaxWidth().clickable { onRequest(req.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(req.reference, style = MaterialTheme.typography.titleMedium)
                                    Text(store.dealer(req.dealerId)?.businessName ?: req.dealerId, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                                }
                                StatusPill(req.status)
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${req.lines.size} items · ${req.deliveryCity}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                                Text(store.salesperson(req.salespersonId)?.name ?: "Unassigned", style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                            }
                        }
                    }
                }
            } else {
                items(quotes, key = { it.id }) { quote ->
                    val totals = calculateQuoteTotals(quote)
                    ShoppeCard(modifier = Modifier.fillMaxWidth().clickable { onQuote(quote.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(quote.quoteNumber, style = MaterialTheme.typography.titleMedium)
                                    Text(store.dealer(quote.dealerId)?.businessName ?: quote.dealerId, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                                }
                                StatusPill(quote.status)
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(money(totals.grandTotal), style = MaterialTheme.typography.titleLarge)
                                Text(quote.validUntil, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffFilter(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(999.dp), color = if (selected) ShoppeBlue else Color.White) {
        Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = if (selected) Color.White else ShoppeInk, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun StaffRequestDetailScreen(store: AppStore, requestId: String, onBack: () -> Unit, onQuote: (String) -> Unit) {
    val req = store.request(requestId) ?: return
    var salesMenu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Request review", onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(req.reference, style = MaterialTheme.typography.headlineMedium)
                        Text(store.dealer(req.dealerId)?.businessName ?: req.dealerId, style = MaterialTheme.typography.bodyLarge, color = ShoppeMuted)
                    }
                    StatusPill(req.status)
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Owner", style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
                        Box {
                            Surface(modifier = Modifier.fillMaxWidth().padding(top = 7.dp).clickable { salesMenu = true }, shape = RoundedCornerShape(14.dp), color = ShoppeBlueSoft) {
                                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(store.salesperson(req.salespersonId)?.name ?: "Unassigned", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = ShoppeBlue)
                                    Icon(Icons.Rounded.ChevronRight, null, tint = ShoppeBlue)
                                }
                            }
                            DropdownMenu(expanded = salesMenu, onDismissRequest = { salesMenu = false }) {
                                store.snapshot.salespeople.filter { it.active }.forEach { sp ->
                                    DropdownMenuItem(text = { Text(sp.name) }, onClick = { store.assignRequest(req.id, sp.id); salesMenu = false })
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Requested products", style = MaterialTheme.typography.headlineSmall)
                ShoppeCard(modifier = Modifier.padding(top = 8.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        req.lines.forEachIndexed { index, line ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                store.product(line.productId)?.let { ProductArtwork(it, Modifier.size(58.dp)) }
                                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                    Text(line.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                                    Text(line.productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                                }
                                Text("${line.quantity} ${line.unit}", style = MaterialTheme.typography.labelLarge)
                            }
                            if (index != req.lines.lastIndex) DividerLine()
                        }
                    }
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Delivery", req.deliveryCity)
                        DividerLine()
                        InfoRow("Required by", req.requiredBy)
                        DividerLine()
                        InfoRow("Buyer reference", req.buyerReference.ifBlank { "—" })
                        if (req.note.isNotBlank()) {
                            DividerLine()
                            Text(req.note, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
            item {
                val existingQuote = store.quoteForRequest(req.id)
                if (existingQuote != null) {
                    PrimaryAction(if (existingQuote.status == QuoteStatus.DRAFT) "Continue quotation" else "Open quotation", { onQuote(existingQuote.id) }, Modifier.fillMaxWidth())
                } else if (req.status !in listOf(RequestStatus.CLOSED, RequestStatus.CANCELLED, RequestStatus.CONFIRMED)) {
                    PrimaryAction("Create quotation", { store.createQuoteFromRequest(req.id)?.let(onQuote) }, Modifier.fillMaxWidth())
                }
            }
            item {
                val transitions = allowedRequestTransitions(req.status)
                if (transitions.isNotEmpty() && store.quoteForRequest(req.id) == null) {
                    Text("Workflow", style = MaterialTheme.typography.headlineSmall)
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        transitions.take(2).forEach { target ->
                            SecondaryAction(target.name.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() }, { store.moveRequest(req.id, target) }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffQuoteScreen(store: AppStore, quoteId: String, onBack: () -> Unit) {
    val quote = store.quote(quoteId) ?: return
    val totals = calculateQuoteTotals(quote)
    var freightText by remember(quote.freight) { mutableStateOf(if (quote.freight == 0.0) "0" else quote.freight.toInt().toString()) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(if (quote.status == QuoteStatus.DRAFT) "Build quotation" else "Quotation", onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(quote.quoteNumber, style = MaterialTheme.typography.headlineMedium)
                        Text(store.dealer(quote.dealerId)?.businessName ?: quote.dealerId, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                    StatusPill(quote.status)
                }
            }
            items(quote.lines, key = { it.productId }) { line ->
                var rate by remember(line.unitRate) { mutableStateOf(line.unitRate.toInt().toString()) }
                var discount by remember(line.discountPct) { mutableStateOf(line.discountPct.toInt().toString()) }
                ShoppeCard {
                    Column(Modifier.padding(15.dp)) {
                        Text(line.description, style = MaterialTheme.typography.titleMedium)
                        Text("${line.quantity} ${line.unit} · GST ${line.gstPct.toInt()}%", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
                        if (quote.status == QuoteStatus.DRAFT) {
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = rate,
                                    onValueChange = { value -> rate = value.filter { it.isDigit() }; rate.toDoubleOrNull()?.let { store.updateQuoteLine(quote.id, line.productId, rate = it) } },
                                    label = { Text("Unit rate") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                OutlinedTextField(
                                    value = discount,
                                    onValueChange = { value -> discount = value.filter { it.isDigit() }.take(2); discount.toDoubleOrNull()?.let { store.updateQuoteLine(quote.id, line.productId, discount = it) } },
                                    label = { Text("Discount %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                )
                            }
                        } else {
                            InfoRow("Unit rate", money(line.unitRate))
                            InfoRow("Discount", "${line.discountPct.toInt()}%")
                        }
                    }
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        if (quote.status == QuoteStatus.DRAFT) {
                            OutlinedTextField(
                                value = freightText,
                                onValueChange = { value -> freightText = value.filter { it.isDigit() }; freightText.toDoubleOrNull()?.let { store.updateQuoteTerms(quote.id, freight = it) } },
                                label = { Text("Freight") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                            )
                        }
                        InfoRow("Subtotal", money(totals.subtotal))
                        InfoRow("Discount", "− ${money(totals.discount)}")
                        InfoRow("GST", money(totals.gst))
                        InfoRow("Freight", money(totals.freight))
                        DividerLine()
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand total", style = MaterialTheme.typography.titleMedium)
                            Text(money(totals.grandTotal), style = MaterialTheme.typography.headlineSmall, color = ShoppeBlue)
                        }
                    }
                }
            }
            item {
                var paymentTerms by remember(quote.paymentTerms) { mutableStateOf(quote.paymentTerms) }
                var deliveryTerms by remember(quote.deliveryTerms) { mutableStateOf(quote.deliveryTerms) }
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        if (quote.status == QuoteStatus.DRAFT) {
                            OutlinedTextField(
                                value = paymentTerms,
                                onValueChange = { paymentTerms = it; store.updateQuoteTerms(quote.id, paymentTerms = it) },
                                label = { Text("Payment terms") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                            )
                            OutlinedTextField(
                                value = deliveryTerms,
                                onValueChange = { deliveryTerms = it; store.updateQuoteTerms(quote.id, deliveryTerms = it) },
                                label = { Text("Delivery terms") },
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                shape = RoundedCornerShape(14.dp),
                            )
                            Text("Validity · ${quote.validUntil}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 10.dp))
                        } else {
                            InfoRow("Payment", quote.paymentTerms)
                            DividerLine()
                            InfoRow("Delivery", quote.deliveryTerms)
                            DividerLine()
                            InfoRow("Validity", quote.validUntil)
                        }
                    }
                }
            }
        }
        if (quote.status == QuoteStatus.DRAFT) {
            val needsApproval = totals.grandTotal >= 100000.0
            Surface(color = Color.White) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    if (needsApproval) {
                        val approvalText = when (quote.approvalStatus) {
                            ApprovalStatus.PENDING -> "Manager approval pending · this quote cannot be sent yet"
                            ApprovalStatus.APPROVED -> "Approved by ${quote.approvedBy.ifBlank { "manager" }} · ready to send"
                            ApprovalStatus.REJECTED -> "Approval rejected · revise and request approval again"
                            ApprovalStatus.NOT_REQUIRED -> "High-value quote · manager approval is required before sending"
                        }
                        Text(approvalText, style = MaterialTheme.typography.bodyMedium, color = if (quote.approvalStatus == ApprovalStatus.APPROVED) ShoppeSuccess else ShoppeMuted, modifier = Modifier.padding(bottom = 10.dp))
                    }
                    val buttonText = when {
                        !needsApproval -> "Send quotation"
                        quote.approvalStatus == ApprovalStatus.APPROVED -> "Send approved quotation"
                        quote.approvalStatus == ApprovalStatus.PENDING -> "Awaiting manager approval"
                        quote.approvalStatus == ApprovalStatus.REJECTED -> "Request approval again"
                        else -> "Request manager approval"
                    }
                    PrimaryAction(
                        buttonText,
                        { if (needsApproval && quote.approvalStatus != ApprovalStatus.APPROVED) store.requestQuoteApproval(quote.id) else store.sendQuote(quote.id) },
                        Modifier.fillMaxWidth(),
                        enabled = quote.approvalStatus != ApprovalStatus.PENDING,
                    )
                }
            }
        }
    }
}

@Composable
fun StaffOrdersScreen(store: AppStore, onOrder: (String) -> Unit, modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }
    val orders = store.snapshot.orders.filter { order ->
        val dealerName = store.dealer(order.dealerId)?.businessName.orEmpty()
        search.isBlank() || order.orderNumber.contains(search, true) || dealerName.contains(search, true)
    }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text("Orders", style = MaterialTheme.typography.headlineLarge)
        SearchBox(search, { search = it }, "Search order or dealer", Modifier.padding(top = 14.dp))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(orders, key = { it.id }) { order ->
                ShoppeCard(modifier = Modifier.fillMaxWidth().clickable { onOrder(order.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(order.orderNumber, style = MaterialTheme.typography.titleMedium)
                                Text(store.dealer(order.dealerId)?.businessName ?: order.dealerId, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                            }
                            StatusPill(order.status)
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(money(order.total), style = MaterialTheme.typography.titleLarge)
                            TinyPill(order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffOrderDetailScreen(store: AppStore, orderId: String, onBack: () -> Unit) {
    val order = store.order(orderId) ?: return
    var dispatchRef by remember(order.dispatchReference) { mutableStateOf(order.dispatchReference) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Order fulfilment", onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(order.orderNumber, style = MaterialTheme.typography.headlineMedium)
                        Text(store.dealer(order.dealerId)?.businessName ?: order.dealerId, style = MaterialTheme.typography.bodyLarge, color = ShoppeMuted)
                    }
                    StatusPill(order.status)
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Order total", money(order.total))
                        DividerLine()
                        InfoRow("Payment", order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() })
                        DividerLine()
                        InfoRow("Created", order.createdAt)
                    }
                }
            }
            if (order.status == OrderStatus.READY_TO_DISPATCH) {
                item {
                    OutlinedTextField(
                        value = dispatchRef,
                        onValueChange = { dispatchRef = it },
                        label = { Text("Dispatch / LR reference") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(PaymentStatus.PENDING, PaymentStatus.PARTIAL, PaymentStatus.PAID, PaymentStatus.CREDIT).forEach { status ->
                        StaffFilter(status.name.lowercase().replaceFirstChar { it.uppercase() }, order.paymentStatus == status) { store.updateOrderPayment(order.id, status) }
                    }
                }
            }
            item {
                val nextText = when (order.status) {
                    OrderStatus.CONFIRMED -> "Start processing"
                    OrderStatus.PROCESSING -> "Mark ready to dispatch"
                    OrderStatus.READY_TO_DISPATCH -> "Mark dispatched"
                    OrderStatus.DISPATCHED -> "Mark delivered"
                    else -> null
                }
                if (nextText != null) {
                    PrimaryAction(nextText, { store.advanceOrder(order.id, dispatchRef) }, Modifier.fillMaxWidth(), enabled = order.status != OrderStatus.READY_TO_DISPATCH || dispatchRef.isNotBlank())
                }
            }
            if (order.dispatchReference.isNotBlank()) item {
                ShoppeCard(containerColor = ShoppeBlueSoft) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Dispatch details", style = MaterialTheme.typography.titleMedium)
                        Text(order.dispatchReference, style = MaterialTheme.typography.bodyLarge, color = ShoppeBlue, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StaffDataScreen(store: AppStore, onDealer: (String) -> Unit, onProduct: (String) -> Unit, modifier: Modifier = Modifier) {
    var tab by remember { mutableIntStateOf(0) }
    var showAddDealer by remember { mutableStateOf(false) }
    var showAddProduct by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Business data", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = { if (tab == 0) showAddDealer = true else if (tab == 1) showAddProduct = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", tint = ShoppeBlue)
            }
        }
        DataSegments(listOf("Dealers", "Products", "Inventory"), tab) { tab = it }
        when (tab) {
            0 -> DealerList(store, onDealer, Modifier.weight(1f))
            1 -> ProductList(store, onProduct, Modifier.weight(1f))
            else -> InventoryList(store, Modifier.weight(1f))
        }
    }
    if (showAddDealer) AddDealerDialog(store) { showAddDealer = false }
    if (showAddProduct) AddProductDialog(store) { showAddProduct = false }
}

@Composable
private fun DataSegments(labels: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label -> StaffFilter(label, selected == index) { onSelected(index) } }
    }
}

@Composable
private fun DealerList(store: AppStore, onDealer: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(store.snapshot.dealers, key = { it.id }) { dealer ->
            ShoppeCard(modifier = Modifier.fillMaxWidth().clickable { onDealer(dealer.id) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Text(dealer.businessName.take(2).uppercase(), color = ShoppeBlue, style = MaterialTheme.typography.titleMedium) } }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(dealer.businessName, style = MaterialTheme.typography.titleMedium)
                        Text("${dealer.contactName} · ${dealer.city}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                    TinyPill(dealer.tier)
                }
            }
        }
    }
}

@Composable
private fun ProductList(store: AppStore, onProduct: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(store.snapshot.products, key = { it.id }) { product ->
            ShoppeCard(modifier = Modifier.fillMaxWidth().clickable { onProduct(product.id) }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductArtwork(product, Modifier.size(64.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(product.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                        Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${product.availableQty} available · ${product.warehouse}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                    StockPill(product.stockState)
                }
            }
        }
    }
}

@Composable
private fun InventoryList(store: AppStore, modifier: Modifier = Modifier) {
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(store.snapshot.products.sortedBy { it.availableQty }, key = { it.id }) { product ->
            var qtyText by remember(product.availableQty) { mutableStateOf(product.availableQty.toString()) }
            ShoppeCard {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductArtwork(product, Modifier.size(56.dp))
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(product.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                        Text(product.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(product.warehouse, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
                    }
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { value -> qtyText = value.filter { it.isDigit() }; qtyText.toIntOrNull()?.let { store.updateInventory(product.id, it) } },
                        modifier = Modifier.width(86.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddDealerDialog(store: AppStore, dismiss: () -> Unit) {
    var business by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Add dealer") },
        text = {
            Column {
                OutlinedTextField(business, { business = it }, label = { Text("Business name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(contact, { contact = it }, label = { Text("Contact person") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        },
        confirmButton = { Text("Add", modifier = Modifier.clickable { store.addDealer(business, contact, city); dismiss() }.padding(12.dp), color = ShoppeBlue) },
        dismissButton = { Text("Cancel", modifier = Modifier.clickable(onClick = dismiss).padding(12.dp), color = ShoppeMuted) },
    )
}

@Composable
private fun AddProductDialog(store: AppStore, dismiss: () -> Unit) {
    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(store.snapshot.categories.first().id) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Add product") },
        text = {
            Column {
                OutlinedTextField(sku, { sku = it }, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(name, { name = it }, label = { Text("Product name") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Row(Modifier.fillMaxWidth().padding(top = 10.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    store.snapshot.categories.forEach { c -> StaffFilter(c.shortName, category == c.id) { category = c.id } }
                }
            }
        },
        confirmButton = { Text("Add", modifier = Modifier.clickable { store.addProduct(sku, name, category); dismiss() }.padding(12.dp), color = ShoppeBlue) },
        dismissButton = { Text("Cancel", modifier = Modifier.clickable(onClick = dismiss).padding(12.dp), color = ShoppeMuted) },
    )
}

@Composable
fun StaffDealerDetailScreen(store: AppStore, dealerId: String, onBack: () -> Unit, onImpersonate: () -> Unit) {
    val dealer = store.dealer(dealerId) ?: return
    val requests = store.snapshot.requests.filter { it.dealerId == dealer.id }
    val orders = store.snapshot.orders.filter { it.dealerId == dealer.id }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Dealer", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                ShoppeCard(containerColor = ShoppeBlue) {
                    Column(Modifier.padding(18.dp)) {
                        Text(dealer.businessName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Text("${dealer.contactName} · ${dealer.city}, ${dealer.state}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=.82f), modifier = Modifier.padding(top = 4.dp))
                        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TinyPill(dealer.tier, ShoppeBlue, Color.White)
                            TinyPill(dealer.paymentTerms, ShoppeBlue, Color.White)
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniMetric("Requests", requests.size.toString(), Modifier.weight(1f))
                    MiniMetric("Orders", orders.size.toString(), Modifier.weight(1f))
                    MiniMetric("Value", money(orders.sumOf { it.total }), Modifier.weight(1f))
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Phone", dealer.phone)
                        DividerLine()
                        InfoRow("Email", dealer.email)
                        DividerLine()
                        InfoRow("GSTIN", dealer.gstin)
                        DividerLine()
                        InfoRow("Salesperson", store.salesperson(dealer.assignedSalespersonId)?.name ?: "Unassigned")
                        DividerLine()
                        InfoRow("Credit limit", money(dealer.creditLimit))
                        DividerLine()
                        InfoRow("Outstanding", money(dealer.outstanding))
                    }
                }
            }
            if (store.session.role == ActorRole.SUPER_ADMIN) item {
                PrimaryAction("View dealer portal", { store.impersonateDealer(dealer.id); onImpersonate() }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
            Text(value, style = MaterialTheme.typography.titleMedium, color = ShoppeInk, modifier = Modifier.padding(top = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun StaffProductDetailScreen(store: AppStore, productId: String, onBack: () -> Unit) {
    val product = store.product(productId) ?: return
    var qtyText by remember(product.availableQty) { mutableStateOf(product.availableQty.toString()) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Product", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ProductArtwork(product, Modifier.fillMaxWidth().height(250.dp)) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(product.sku, style = MaterialTheme.typography.labelLarge, color = ShoppeBlue)
                        Text(product.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                    StockPill(product.stockState)
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Specification", product.primarySpec)
                        DividerLine()
                        InfoRow("Pack", product.packSize)
                        DividerLine()
                        InfoRow("MOQ", product.moq.toString())
                        DividerLine()
                        InfoRow("Warehouse", product.warehouse)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { value -> qtyText = value.filter { it.isDigit() }; qtyText.toIntOrNull()?.let { store.updateInventory(product.id, it) } },
                    label = { Text("Available quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
            }
            item {
                SecondaryAction(if (product.active) "Hide from dealer catalogue" else "Show in dealer catalogue", { store.toggleProduct(product.id) }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun StaffMoreScreen(
    store: AppStore,
    onReports: () -> Unit,
    onSales: () -> Unit,
    onActivity: () -> Unit,
    onIntegrations: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val role = store.session.role?.name?.replace('_',' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Staff"
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("More", style = MaterialTheme.typography.headlineLarge)
            ShoppeCard(modifier = Modifier.padding(top = 14.dp), containerColor = ShoppeBlue) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha=.17f), modifier = Modifier.size(54.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AccountCircle, null, tint = Color.White, modifier = Modifier.size(30.dp)) } }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(role, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text("Apex Industrial Supply Co.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=.82f))
                    }
                }
            }
        }
        item {
            ShoppeCard {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ModuleRow(Icons.Rounded.Analytics, "Reports", "Conversion, pipeline and commercial metrics", onReports)
                    DividerLine()
                    ModuleRow(Icons.Rounded.People, "Sales team", "Regions, ownership and workload", onSales)
                    DividerLine()
                    ModuleRow(Icons.Rounded.History, "Activity", "Operational audit trail", onActivity)
                    DividerLine()
                    ModuleRow(Icons.Rounded.CloudSync, "Automation Center", "Approvals, workflows, integrations and BAOS health", onIntegrations)
                    DividerLine()
                    ModuleRow(Icons.Rounded.Settings, "Settings", "Company, demo data and access", onSettings)
                }
            }
        }
        item { SecondaryAction("Switch demo role", onExit, Modifier.fillMaxWidth()) }
    }
}

@Composable
fun StaffReportsScreen(store: AppStore, onBack: () -> Unit) {
    val metrics = store.dashboardMetrics()
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Reports", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportMetric("Quote conversion", "${store.conversionPercent()}%", ShoppeBlueSoft, Modifier.weight(1f))
                    ReportMetric("Order value", money(store.snapshot.orders.sumOf { it.total }), ShoppeMint, Modifier.weight(1f))
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pipeline", style = MaterialTheme.typography.titleLarge)
                        PipelineRow("Open requests", metrics["openRequests"] ?: 0, ShoppeBlue)
                        PipelineRow("Quotes waiting", metrics["quotesWaiting"] ?: 0, ShoppeWarning)
                        PipelineRow("Active orders", metrics["activeOrders"] ?: 0, ShoppeSuccess)
                    }
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Top dealers by order value", style = MaterialTheme.typography.titleLarge)
                        store.snapshot.dealers.map { dealer -> dealer to store.snapshot.orders.filter { it.dealerId == dealer.id }.sumOf { it.total } }
                            .sortedByDescending { it.second }.take(5).forEachIndexed { index, pair ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
                                    Text("${index+1}", modifier = Modifier.width(28.dp), color = ShoppeMuted)
                                    Text(pair.first.businessName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontWeight=FontWeight.SemiBold))
                                    Text(money(pair.second), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetric(label: String, value: String, bg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = bg) {
        Column(Modifier.padding(15.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = ShoppeInk, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun PipelineRow(label: String, value: Int, color: Color) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(10.dp)) {}
        Text(label, modifier = Modifier.weight(1f).padding(start = 9.dp), style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun StaffSalesScreen(store: AppStore, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Sales team", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(store.snapshot.salespeople, key = { it.id }) { sp ->
                val owned = store.snapshot.requests.count { it.salespersonId == sp.id && it.status !in listOf(RequestStatus.CLOSED, RequestStatus.CANCELLED) }
                ShoppeCard {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Text(sp.name.take(1), color = ShoppeBlue, style = MaterialTheme.typography.titleLarge) } }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(sp.name, style = MaterialTheme.typography.titleMedium)
                            Text("${sp.region} · ${sp.phone}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                        }
                        TinyPill("$owned open")
                    }
                }
            }
        }
    }
}

@Composable
fun StaffActivityScreen(store: AppStore, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Activity", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(store.snapshot.activities, key = { it.id }) { activity ->
                ShoppeCard {
                    Column(Modifier.padding(15.dp)) {
                        Text(activity.title, style = MaterialTheme.typography.titleMedium)
                        Text(activity.message, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
                        Text("${activity.actor} · ${activity.timestamp}", style = MaterialTheme.typography.labelMedium, color = ShoppeBlue, modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StaffIntegrationsScreen(store: AppStore, onBack: () -> Unit) {
    val pending = store.approvals.filter { it.status == ApprovalStatus.PENDING }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Automation Center", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                ShoppeCard(containerColor = ShoppeBlue) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Owner digest", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = .82f))
                        Text(store.ownerDigestText(), style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.padding(top = 7.dp))
                        Text("Generated from current order, payment, approval and inventory state.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = .76f), modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }

            item {
                SectionTitle("Human approvals")
                Text(if (pending.isEmpty()) "No automation is waiting for a manager decision." else "${pending.size} workflow${if (pending.size == 1) "" else "s"} paused for a decision.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 5.dp))
            }
            if (pending.isEmpty()) {
                item {
                    ShoppeCard {
                        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = ShoppeMint, modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CloudSync, null, tint = ShoppeSuccess) } }
                            Column(Modifier.padding(start = 12.dp)) {
                                Text("All clear", style = MaterialTheme.typography.titleMedium)
                                Text("Normal cases can continue automatically.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                            }
                        }
                    }
                }
            } else {
                items(pending, key = { it.id }) { approval ->
                    ShoppeCard {
                        Column(Modifier.padding(15.dp)) {
                            Text(approval.quoteNumber, style = MaterialTheme.typography.titleMedium)
                            Text("${approval.dealerName} · ${money(approval.amount)}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
                            Text(approval.reason, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 7.dp))
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SecondaryAction("Reject", { store.rejectQuote(approval.quoteId) }, Modifier.weight(1f))
                                PrimaryAction("Approve", { store.approveQuote(approval.quoteId) }, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item { SectionTitle("Automation modules") }
            item { AutomationModuleCard("RFQ intake", "Capture → normalize → assign salesperson", "ACTIVE") }
            item { AutomationModuleCard("Quote approval", "₹1L+ quotations pause for human approval", "ACTIVE") }
            item { AutomationModuleCard("Quote follow-up", "Reminder and salesperson exception workflow", "ACTIVE") }
            item { AutomationModuleCard("Payment events", "Demo gateway verifies success/failure events", "SIMULATED") }
            item { AutomationModuleCard("Inventory exceptions", "Low-stock events surface for action", "ACTIVE") }
            item { AutomationModuleCard("Owner digest", "Orders, payments, enquiries and risks summarized", "ACTIVE") }

            item { SectionTitle("Recent automation activity") }
            items(store.automationRuns.take(12), key = { it.id }) { run ->
                ShoppeCard {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) {
                        val bg = when (run.status.name) {
                            "FAILED" -> ShoppePeach
                            "WAITING", "RETRYING" -> ShoppeSky
                            else -> ShoppeMint
                        }
                        Surface(shape = CircleShape, color = bg, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CloudSync, null, tint = ShoppeBlue, modifier = Modifier.size(20.dp)) } }
                        Column(Modifier.weight(1f).padding(start = 11.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(run.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                TinyPill(run.status.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                            Text(run.message, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 4.dp))
                            Text("${run.workflowKey} · ${run.timestamp}", style = MaterialTheme.typography.labelMedium, color = ShoppeBlue, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }

            item { SectionTitle("Integrations") }
            items(store.integrations, key = { it.key }) { integration ->
                IntegrationCard(integration.label, integration.mode, integration.status, if (integration.mode == "LIVE") ShoppeMint else ShoppeBlueSoft)
            }
            item {
                SecondaryAction("Run Tally demo sync", { store.runTallyDemoSync() }, Modifier.fillMaxWidth())
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Simulation policy", style = MaterialTheme.typography.titleMedium)
                        Text("Payments, Tally, WhatsApp and n8n remain clearly marked simulated until a client connects its own production credentials. Supabase and Truecaller are the live M2 integration path.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AutomationModuleCard(title: String, subtitle: String, status: String) {
    ShoppeCard {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CloudSync, null, tint = ShoppeBlue) } }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
            }
            TinyPill(status.lowercase().replaceFirstChar { it.uppercase() })
        }
    }
}

@Composable
private fun IntegrationCard(title: String, subtitle: String, status: String, bg: Color) {
    ShoppeCard {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = bg, modifier = Modifier.size(50.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CloudSync, null, tint = ShoppeBlue) } }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
            }
            TinyPill(status)
        }
    }
}

@Composable
fun StaffSettingsScreen(store: AppStore, onBack: () -> Unit, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Settings", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Apex Industrial Supply Co.", style = MaterialTheme.typography.titleLarge)
                        Text("Pune, Maharashtra · Native DMS showcase", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Company profile", "Apex Industrial Supply Co.")
                        DividerLine()
                        InfoRow("Primary warehouse", "Pune Main")
                        DividerLine()
                        InfoRow("Default GST", "18%")
                        DividerLine()
                        InfoRow("Default quote validity", "7 days")
                    }
                }
            }
            item { SecondaryAction("Reset local demo data", { store.resetDemo() }, Modifier.fillMaxWidth()) }
            item { SecondaryAction("Switch demo role", onExit, Modifier.fillMaxWidth()) }
        }
    }
}
