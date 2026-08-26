package com.apex.dms.ui.screens.dealer

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.AlertDialog
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
import com.apex.dms.data.AppStore
import com.apex.dms.data.Order
import com.apex.dms.data.OrderStatus
import com.apex.dms.data.PaymentStatus
import com.apex.dms.data.PriceRequest
import com.apex.dms.data.Product
import com.apex.dms.data.QuoteStatus
import com.apex.dms.data.Quotation
import com.apex.dms.data.RequestStatus
import com.apex.dms.data.calculateQuoteTotals
import com.apex.dms.data.money
import com.apex.dms.data.orderProgress
import com.apex.dms.data.requestProgress
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
import com.apex.dms.ui.theme.ShoppeBackground
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
import coil.compose.AsyncImage

@Composable
fun DealerHomeScreen(
    store: AppStore,
    onShop: () -> Unit,
    onProduct: (String) -> Unit,
    onRequests: () -> Unit,
    onOrders: () -> Unit,
    onCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dealer = store.dealer(store.session.dealerId) ?: return
    val requests = store.dealerRequests()
    val quotes = store.dealerQuotes()
    val orders = store.dealerOrders()
    val activeOrders = orders.count { it.status !in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED) }
    val recentProducts = orders.firstOrNull()?.let { order ->
        store.request(order.requestId)?.lines?.mapNotNull { store.product(it.productId) }
    }.orEmpty().ifEmpty { store.visibleProducts().take(4) }
    var smartEnquiry by remember { mutableStateOf("") }
    var smartEnquiryResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hi, ${dealer.contactName.substringBefore(' ')}", style = MaterialTheme.typography.headlineLarge, color = ShoppeInk)
                    Text(dealer.businessName, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onCart),
                    shape = CircleShape,
                    color = Color.White,
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ShoppingBag, contentDescription = "Quote request", tint = ShoppeInk)
                        if (store.snapshot.draftItems.isNotEmpty()) {
                            Surface(color = ShoppeBlue, shape = CircleShape, modifier = Modifier.align(Alignment.TopEnd).size(18.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(store.snapshot.draftItems.size.toString(), color = Color.White, style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onShop),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = ShoppeMuted)
                    Text("Search SKU, bearing, belt, contactor...", modifier = Modifier.padding(start = 10.dp), color = ShoppeMuted, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            ShoppeCard(containerColor = ShoppeBlueSoft) {
                Column(Modifier.padding(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = ShoppeBlue, modifier = Modifier.size(20.dp))
                        Text("Smart enquiry · Demo", style = MaterialTheme.typography.titleMedium, color = ShoppeInk, modifier = Modifier.padding(start = 8.dp))
                    }
                    Text("Try: “6204 bearing 500 pcs and V-belt 30”. BAOS maps the message to catalogue items, then you review the RFQ before submitting.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 5.dp))
                    OutlinedTextField(
                        value = smartEnquiry,
                        onValueChange = { smartEnquiry = it; smartEnquiryResult = null },
                        label = { Text("Describe what you need") },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2,
                    )
                    PrimaryAction("Understand & review RFQ", {
                        val count = store.smartEnquiryToDraft(smartEnquiry)
                        smartEnquiryResult = if (count > 0) "$count item${if (count == 1) "" else "s"} matched. Review the RFQ cart before submitting." else "No confident match. Use catalogue search or try a SKU/product name."
                        if (count > 0) onCart()
                    }, Modifier.fillMaxWidth().padding(top = 10.dp), enabled = smartEnquiry.isNotBlank())
                    smartEnquiryResult?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue, modifier = Modifier.padding(top = 8.dp)) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HomeStat("Price requests", requests.count { it.status !in listOf(RequestStatus.CLOSED, RequestStatus.CANCELLED) }.toString(), ShoppeBlueSoft, ShoppeBlue, Modifier.weight(1f), onRequests)
                HomeStat("Quotes waiting", quotes.count { it.status == QuoteStatus.SENT }.toString(), ShoppePeach, Color(0xFFB36A00), Modifier.weight(1f), onRequests)
                HomeStat("Orders moving", activeOrders.toString(), ShoppeMint, ShoppeSuccess, Modifier.weight(1f), onOrders)
            }
        }
        item {
            SectionTitle("Shop by category", "View all", onShop)
            LazyRow(contentPadding = PaddingValues(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(store.snapshot.categories) { category ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp).clickable(onClick = onShop)) {
                        Surface(shape = CircleShape, color = categoryColor(category.id), modifier = Modifier.size(64.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(category.shortName.take(2).uppercase(), style = MaterialTheme.typography.titleMedium, color = ShoppeBlue)
                            }
                        }
                        Text(category.shortName, modifier = Modifier.padding(top = 7.dp), style = MaterialTheme.typography.labelMedium, color = ShoppeInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        item {
            SectionTitle("Buy again")
            LazyRow(contentPadding = PaddingValues(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recentProducts) { product -> CompactProduct(product, store, onProduct) }
            }
        }
        item {
            ShoppeCard(containerColor = ShoppeBlue) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Need a project quote?", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text("Add multiple products and send one structured price request.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = .82f), modifier = Modifier.padding(top = 4.dp))
                    }
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = .16f), modifier = Modifier.size(48.dp).clickable(onClick = onShop)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White) }
                    }
                }
            }
        }
        item {
            SectionTitle("Popular in your catalogue", "Shop", onShop)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
                store.visibleProducts().take(4).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { product -> ProductTile(product, store, onProduct, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStat(title: String, value: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), color = bg) {
        Column(Modifier.padding(13.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = fg)
            Text(title, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
fun DealerShopScreen(store: AppStore, onProduct: (String) -> Unit, onCart: () -> Unit, modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    val products = store.visibleProducts().filter { p ->
        (search.isBlank() || p.sku.contains(search, true) || p.name.contains(search, true) || p.primarySpec.contains(search, true)) &&
            (categoryId == null || p.categoryId == categoryId)
    }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Shop", style = MaterialTheme.typography.headlineLarge, color = ShoppeInk, modifier = Modifier.weight(1f))
            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(46.dp).clickable(onClick = onCart)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ShoppingBag, contentDescription = "Price request", tint = ShoppeInk)
                    if (store.snapshot.draftItems.isNotEmpty()) {
                        Surface(
                            color = ShoppeBlue,
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    store.snapshot.draftItems.size.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
        SearchBox(search, { search = it }, modifier = Modifier.padding(top = 14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip("All", categoryId == null) { categoryId = null }
            store.snapshot.categories.forEach { c -> FilterChip(c.shortName, categoryId == c.id) { categoryId = c.id } }
        }
        Text("${products.size} products", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(bottom = 10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(products, key = { it.id }) { product -> ProductTile(product, store, onProduct) }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) ShoppeBlue else Color.White,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, ShoppeStroke) else null,
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = if (selected) Color.White else ShoppeInk, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ProductTile(product: Product, store: AppStore, onProduct: (String) -> Unit, modifier: Modifier = Modifier) {
    val brand = store.brand(product.brandId)?.name ?: ""
    Column(modifier = modifier.clickable { onProduct(product.id) }) {
        ProductArtwork(product, Modifier.fillMaxWidth().height(150.dp))
        Text(product.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 9.dp))
        Text(product.name, style = MaterialTheme.typography.titleMedium, color = ShoppeInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("$brand · MOQ ${product.moq}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun CompactProduct(product: Product, store: AppStore, onProduct: (String) -> Unit) {
    Surface(modifier = Modifier.width(190.dp).clickable { onProduct(product.id) }, shape = RoundedCornerShape(20.dp), color = Color.White) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductArtwork(product, Modifier.size(64.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text(product.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                Text(product.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ShoppeInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun DealerProductDetailScreen(store: AppStore, productId: String, onBack: () -> Unit, onCart: () -> Unit) {
    val product = store.product(productId) ?: return
    var quantity by remember { mutableIntStateOf(product.moq) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Product details", onBack)
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
            item { ProductArtwork(product, Modifier.fillMaxWidth().height(290.dp)) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.sku, style = MaterialTheme.typography.labelLarge, color = ShoppeBlue, modifier = Modifier.weight(1f))
                    StockPill(product.stockState)
                }
                Text(product.name, style = MaterialTheme.typography.headlineMedium, color = ShoppeInk, modifier = Modifier.padding(top = 7.dp))
                Text(product.description, style = MaterialTheme.typography.bodyLarge, color = ShoppeMuted, modifier = Modifier.padding(top = 7.dp))
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Specification", product.primarySpec)
                        DividerLine()
                        InfoRow("Commercial pack", product.packSize)
                        DividerLine()
                        InfoRow("Minimum quantity", "${product.moq} ${product.unit}")
                        DividerLine()
                        InfoRow("Warehouse", product.warehouse)
                    }
                }
            }
            item {
                Text("Quantity", style = MaterialTheme.typography.titleMedium, color = ShoppeInk)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                    QtyButton(Icons.Rounded.Remove) { quantity = (quantity - product.moq).coerceAtLeast(product.moq) }
                    Text(quantity.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 22.dp))
                    QtyButton(Icons.Rounded.Add) { quantity += product.moq }
                    Text("increments of ${product.moq}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(start = 12.dp))
                }
            }
            item {
                ShoppeCard(containerColor = ShoppeSky) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = ShoppeBlue)
                        Text("No payment now. Send a price request and your assigned sales team will respond with a quotation.", style = MaterialTheme.typography.bodyMedium, color = ShoppeInk, modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
        }
        Surface(color = Color.White) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryAction("View request", onCart, Modifier.weight(.42f))
                PrimaryAction("Request your price", {
                    store.addToDraft(product.id, quantity)
                    onCart()
                }, Modifier.weight(.58f))
            }
        }
    }
}

@Composable
private fun QtyButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(42.dp).clickable(onClick = onClick)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = ShoppeBlue) }
    }
}

@Composable
fun DealerRequestCartScreen(store: AppStore, onBack: () -> Unit, onSubmitted: (String) -> Unit) {
    val items = store.snapshot.draftItems
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Price request", onBack)
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(76.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ShoppingBag, null, tint = ShoppeBlue) } }
                    Text("Your request is empty", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
                    Text("Open Shop and add products you want pricing for.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 6.dp))
                }
            }
            return
        }
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StepPill("1", "Products", true, Modifier.weight(1f))
                    StepPill("2", "Delivery", true, Modifier.weight(1f))
                    StepPill("3", "Send", false, Modifier.weight(1f))
                }
            }
            items(items, key = { it.productId }) { draft ->
                val product = store.product(draft.productId) ?: return@items
                ShoppeCard {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProductArtwork(product, Modifier.size(82.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(product.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                            Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                QtyButton(Icons.Rounded.Remove) { store.incrementDraft(product.id, -product.moq) }
                                Text(draft.quantity.toString(), modifier = Modifier.padding(horizontal = 14.dp), style = MaterialTheme.typography.titleMedium)
                                QtyButton(Icons.Rounded.Add) { store.incrementDraft(product.id, product.moq) }
                            }
                        }
                        IconButton(onClick = { store.removeDraft(product.id) }) { Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = ShoppeMuted) }
                    }
                }
            }
            item {
                Text("Delivery details", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 4.dp))
                SimpleField("Delivery city", store.snapshot.draftDeliveryCity) { store.updateDraftMeta(city = it) }
                SimpleField("Required by", store.snapshot.draftRequiredBy, "e.g. 05 Sep 2026") { store.updateDraftMeta(requiredBy = it) }
                SimpleField("Buyer reference", store.snapshot.draftBuyerReference, "Optional PO / internal ref") { store.updateDraftMeta(buyerReference = it) }
                SimpleField("Note", store.snapshot.draftNote, "Commercial or delivery note") { store.updateDraftMeta(note = it) }
            }
            item {
                ShoppeCard(containerColor = ShoppeBlueSoft) {
                    Column(Modifier.padding(16.dp)) {
                        Text("What happens next?", style = MaterialTheme.typography.titleMedium, color = ShoppeInk)
                        Text("Your salesperson reviews quantities and terms, then sends a formal quotation. You can approve or request changes before an order is created.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
        Surface(color = Color.White) {
            PrimaryAction("Send price request", {
                store.submitDraft()?.let(onSubmitted)
            }, Modifier.fillMaxWidth().padding(16.dp))
        }
    }
}

@Composable
private fun StepPill(number: String, label: String, active: Boolean, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = if (active) ShoppeBlueSoft else Color(0xFFF2F4F7)) {
        Column(Modifier.padding(10.dp)) {
            Text(number, style = MaterialTheme.typography.labelMedium, color = if (active) ShoppeBlue else ShoppeMuted)
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) ShoppeInk else ShoppeMuted)
        }
    }
}

@Composable
private fun SimpleField(label: String, value: String, placeholder: String = "", keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        singleLine = label != "Note",
        minLines = if (label == "Note") 3 else 1,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
fun DealerRequestsScreen(store: AppStore, onRequest: (String) -> Unit, onQuote: (String) -> Unit, modifier: Modifier = Modifier) {
    var tab by remember { mutableIntStateOf(0) }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text("Requests", style = MaterialTheme.typography.headlineLarge)
        SegmentRow(listOf("Price requests", "Quotations"), tab) { tab = it }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tab == 0) {
                items(store.dealerRequests(), key = { it.id }) { req -> RequestCard(req, store, { onRequest(req.id) }) }
            } else {
                items(store.dealerQuotes(), key = { it.id }) { q -> QuoteCard(q, store, { onQuote(q.id) }) }
            }
        }
    }
}

@Composable
private fun SegmentRow(labels: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFEEF2F8), modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
        Row(Modifier.padding(4.dp)) {
            labels.forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier.weight(1f).clickable { onSelected(index) },
                    shape = RoundedCornerShape(15.dp),
                    color = if (selected == index) Color.White else Color.Transparent,
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.height(42.dp)) {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected == index) ShoppeBlue else ShoppeMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(req: PriceRequest, store: AppStore, onClick: () -> Unit) {
    ShoppeCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(req.reference, style = MaterialTheme.typography.titleMedium)
                    Text("${req.lines.size} items · ${req.deliveryCity}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 2.dp))
                }
                StatusPill(req.status)
            }
            Text(req.lines.joinToString(" · ") { it.sku }, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 12.dp))
            Text(req.updatedAt, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 9.dp))
        }
    }
}

@Composable
private fun QuoteCard(q: Quotation, store: AppStore, onClick: () -> Unit) {
    val totals = calculateQuoteTotals(q)
    ShoppeCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(q.quoteNumber, style = MaterialTheme.typography.titleMedium)
                    Text("Valid until ${q.validUntil}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                }
                StatusPill(q.status)
            }
            Text(money(totals.grandTotal), style = MaterialTheme.typography.headlineSmall, color = ShoppeInk, modifier = Modifier.padding(top = 12.dp))
            Text("${q.lines.size} items · ${q.paymentTerms}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
fun DealerRequestDetailScreen(store: AppStore, requestId: String, onBack: () -> Unit, onQuote: (String) -> Unit) {
    val req = store.request(requestId) ?: return
    val quote = store.quoteForRequest(req.id)
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Price request", onBack)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(req.reference, style = MaterialTheme.typography.headlineMedium)
                        Text(req.createdAt, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                    StatusPill(req.status)
                }
            }
            if (quote != null && quote.status != QuoteStatus.DRAFT) {
                item {
                    ShoppeCard(containerColor = ShoppeBlue) {
                        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Quotation is ready", style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Text("Review commercial terms before an order is created.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=.82f))
                            }
                            Surface(shape = CircleShape, color = Color.White.copy(alpha=.15f), modifier = Modifier.size(46.dp).clickable { onQuote(quote.id) }) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ChevronRight, null, tint = Color.White) }
                            }
                        }
                    }
                }
            }
            item { JourneyCard("Request progress", requestProgress(req.status), listOf("Sent", "Review", "Quote", "Approval", "Order")) }
            item {
                Text("Products", style = MaterialTheme.typography.headlineSmall)
                Column(Modifier.padding(top = 8.dp)) {
                    req.lines.forEach { line ->
                        val p = store.product(line.productId)
                        Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (p != null) ProductArtwork(p, Modifier.size(58.dp))
                            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(line.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                                Text(line.productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                            }
                            Text("${line.quantity} ${line.unit}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Delivery city", req.deliveryCity)
                        DividerLine()
                        InfoRow("Required by", req.requiredBy)
                        DividerLine()
                        InfoRow("Buyer reference", req.buyerReference.ifBlank { "—" })
                        DividerLine()
                        InfoRow("Salesperson", store.salesperson(req.salespersonId)?.name ?: "Unassigned")
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyCard(title: String, progress: Int, steps: List<String>) {
    ShoppeCard {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                steps.forEachIndexed { index, label ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Surface(shape = CircleShape, color = if (index + 1 <= progress) ShoppeBlue else Color(0xFFE8ECF4), modifier = Modifier.size(26.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                if (index + 1 <= progress) Icon(Icons.Rounded.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp)) else Text((index+1).toString(), style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
                            }
                        }
                        Text(label, style = MaterialTheme.typography.labelMedium, color = if (index + 1 <= progress) ShoppeInk else ShoppeMuted, modifier = Modifier.padding(top = 5.dp), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun DealerQuoteDetailScreen(store: AppStore, quoteId: String, onBack: () -> Unit, onOrder: (String) -> Unit) {
    val q = store.quote(quoteId) ?: return
    val totals = calculateQuoteTotals(q)
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Quotation", onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(q.quoteNumber, style = MaterialTheme.typography.headlineMedium)
                        Text("Issued ${q.issueDate} · valid ${q.validUntil}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                    StatusPill(q.status)
                }
            }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        q.lines.forEachIndexed { index, line ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(line.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                                    Text("${line.quantity} ${line.unit} × ${money(line.unitRate)} · ${line.discountPct.toInt()}% off", style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
                                }
                                val lineTotal = line.quantity * line.unitRate * (1 - line.discountPct/100) * (1 + line.gstPct/100)
                                Text(money(lineTotal), style = MaterialTheme.typography.labelLarge)
                            }
                            if (index != q.lines.lastIndex) DividerLine()
                        }
                    }
                }
            }
            item {
                ShoppeCard(containerColor = ShoppeBlueSoft) {
                    Column(Modifier.padding(16.dp)) {
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
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Payment", q.paymentTerms)
                        DividerLine()
                        InfoRow("Delivery", q.deliveryTerms)
                        if (q.notes.isNotBlank()) {
                            DividerLine()
                            Text(q.notes, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        if (q.status == QuoteStatus.SENT || q.status == QuoteStatus.REVISION_REQUESTED) {
            Surface(color = Color.White) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryAction("Ask for changes", { store.requestQuoteRevision(q.id) }, Modifier.weight(.42f))
                    PrimaryAction("Accept quotation", {
                        store.acceptQuote(q.id)?.let(onOrder)
                    }, Modifier.weight(.58f))
                }
            }
        } else if (q.status == QuoteStatus.ACCEPTED) {
            val order = store.orderForQuote(q.id)
            if (order != null) Surface(color = Color.White) { PrimaryAction("View order", { onOrder(order.id) }, Modifier.fillMaxWidth().padding(16.dp)) }
        }
    }
}

@Composable
fun DealerOrdersScreen(store: AppStore, onOrder: (String) -> Unit, modifier: Modifier = Modifier) {
    var tab by remember { mutableIntStateOf(0) }
    val all = store.dealerOrders()
    val active = all.filter { it.status !in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED) }
    val past = all.filter { it.status in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED) }
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text("Orders", style = MaterialTheme.typography.headlineLarge)
        SegmentRow(listOf("Active", "Past"), tab) { tab = it }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(if (tab == 0) active else past, key = { it.id }) { order -> OrderCard(order, store, { onOrder(order.id) }) }
        }
    }
}

@Composable
private fun OrderCard(order: Order, store: AppStore, onClick: () -> Unit) {
    ShoppeCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(order.orderNumber, style = MaterialTheme.typography.titleMedium)
                    Text(order.createdAt, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                }
                StatusPill(order.status)
            }
            Text(money(order.total), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
            val req = store.request(order.requestId)
            Text("${req?.lines?.size ?: 0} items · ${order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
        }
    }
}

@Composable
fun DealerOrderDetailScreen(store: AppStore, orderId: String, onBack: () -> Unit, onRepeat: () -> Unit) {
    val order = store.order(orderId) ?: return
    val req = store.request(order.requestId)
    var paymentMessage by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar("Order", onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(order.orderNumber, style = MaterialTheme.typography.headlineMedium)
                        Text(order.createdAt, style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                    StatusPill(order.status)
                }
            }
            item { JourneyCard("Order progress", orderProgress(order.status), listOf("Confirmed", "Processing", "Ready", "Shipped", "Delivered")) }
            item {
                ShoppeCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Order total", money(order.total))
                        DividerLine()
                        InfoRow("Payment", order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() })
                        DividerLine()
                        InfoRow("Dispatch reference", order.dispatchReference.ifBlank { "Not dispatched yet" })
                    }
                }
            }
            if (order.paymentStatus == PaymentStatus.PENDING || order.paymentStatus == PaymentStatus.PARTIAL) {
                item {
                    ShoppeCard(containerColor = ShoppeBlueSoft) {
                        Column(Modifier.padding(16.dp)) {
                            Text("BAOS Demo Payment", style = MaterialTheme.typography.titleLarge, color = ShoppeInk)
                            Text("No real money is charged. Use these controls to demonstrate payment webhooks and downstream automations.", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted, modifier = Modifier.padding(top = 5.dp))
                            PrimaryAction("Simulate successful payment", {
                                store.simulatePayment(order.id, true)
                                paymentMessage = "Payment verified · inventory and Tally simulation triggered"
                            }, Modifier.fillMaxWidth().padding(top = 14.dp))
                            SecondaryAction("Simulate failed payment", {
                                store.simulatePayment(order.id, false)
                                paymentMessage = "Failure captured · order remains unpaid"
                            }, Modifier.fillMaxWidth().padding(top = 8.dp))
                            paymentMessage?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue, modifier = Modifier.padding(top = 10.dp)) }
                        }
                    }
                }
            }
            if (req != null) item {
                Text("Items", style = MaterialTheme.typography.headlineSmall)
                req.lines.forEach { line ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        val p = store.product(line.productId)
                        if (p != null) ProductArtwork(p, Modifier.size(56.dp))
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(line.sku, style = MaterialTheme.typography.labelMedium, color = ShoppeBlue)
                            Text(line.productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                        }
                        Text("${line.quantity} ${line.unit}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        Surface(color = Color.White) {
            PrimaryAction("Repeat this order", {
                store.repeatOrder(order.id)
                onRepeat()
            }, Modifier.fillMaxWidth().padding(16.dp))
        }
    }
}

@Composable
fun DealerProfileScreen(store: AppStore, onRequests: () -> Unit, onOrders: () -> Unit, onShop: () -> Unit, onExit: () -> Unit, modifier: Modifier = Modifier) {
    val dealer = store.dealer(store.session.dealerId) ?: return
    val salesperson = store.salesperson(dealer.assignedSalespersonId)
    var editing by remember { mutableStateOf(false) }

    if (editing) {
        var businessName by remember(dealer.id, dealer.businessName) { mutableStateOf(dealer.businessName) }
        var contactName by remember(dealer.id, dealer.contactName) { mutableStateOf(dealer.contactName) }
        var phone by remember(dealer.id, dealer.phone) { mutableStateOf(dealer.phone) }
        var email by remember(dealer.id, dealer.email) { mutableStateOf(dealer.email) }
        var gstin by remember(dealer.id, dealer.gstin) { mutableStateOf(dealer.gstin) }
        var city by remember(dealer.id, dealer.city) { mutableStateOf(dealer.city) }
        var state by remember(dealer.id, dealer.state) { mutableStateOf(dealer.state) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit business profile") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { OutlinedTextField(businessName, { businessName = it }, label = { Text("Business name") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(contactName, { contactName = it }, label = { Text("Contact name") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
                    item { OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)) }
                    item { OutlinedTextField(gstin, { gstin = it }, label = { Text("GSTIN") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(state, { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                }
            },
            confirmButton = {
                Text("Save", color = ShoppeBlue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable {
                    store.updateDealerProfile(businessName, contactName, phone, email, gstin, city, state)
                    editing = false
                }.padding(10.dp))
            },
            dismissButton = { Text("Cancel", color = ShoppeMuted, modifier = Modifier.clickable { editing = false }.padding(10.dp)) },
        )
    }

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("My business", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.clickable { editing = true }) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Edit, null, tint = ShoppeBlue, modifier = Modifier.size(17.dp))
                        Text("Edit", style = MaterialTheme.typography.labelLarge, color = ShoppeBlue, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            ShoppeCard(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), containerColor = ShoppeBlue) {
                Column(Modifier.padding(18.dp)) {
                    if (dealer.photoUrl.orEmpty().isNotBlank()) {
                        AsyncImage(model = dealer.photoUrl, contentDescription = dealer.contactName, modifier = Modifier.size(58.dp).clip(CircleShape))
                    } else {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = .18f), modifier = Modifier.size(58.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(dealer.businessName.take(2).uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge) }
                        }
                    }
                    Text(dealer.businessName, style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier = Modifier.padding(top = 12.dp))
                    Text("${dealer.city}, ${dealer.state} · ${dealer.tier} dealer", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=.82f), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ProfileMetric("Credit limit", money(dealer.creditLimit), ShoppeBlueSoft, Modifier.weight(1f))
                ProfileMetric("Outstanding", money(dealer.outstanding), ShoppePeach, Modifier.weight(1f))
            }
        }
        item {
            ShoppeCard {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ModuleRow(Icons.Rounded.Storefront, "Shop catalogue", "Browse products and create price requests", onShop)
                    DividerLine()
                    ModuleRow(Icons.Rounded.ShoppingBag, "Requests & quotations", "Review pricing conversations", onRequests)
                    DividerLine()
                    ModuleRow(Icons.Rounded.LocalShipping, "Orders", "Track fulfilment and repeat orders", onOrders)
                }
            }
        }
        item {
            SectionTitle("Account")
            ShoppeCard(modifier = Modifier.padding(top = 10.dp)) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Contact", dealer.contactName)
                    DividerLine()
                    InfoRow("Phone", dealer.phone)
                    DividerLine()
                    InfoRow("Email", dealer.email.ifBlank { "Not added" })
                    DividerLine()
                    InfoRow("GSTIN", dealer.gstin.ifBlank { "Not added" })
                    DividerLine()
                    InfoRow("Payment terms", dealer.paymentTerms)
                }
            }
        }
        item {
            SectionTitle("Your salesperson")
            ShoppeCard(modifier = Modifier.padding(top = 10.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = ShoppeBlueSoft, modifier = Modifier.size(50.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.SupportAgent, null, tint = ShoppeBlue) } }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(salesperson?.name ?: "Unassigned", style = MaterialTheme.typography.titleMedium)
                        Text(salesperson?.phone ?: "", style = MaterialTheme.typography.bodyMedium, color = ShoppeMuted)
                    }
                }
            }
        }
        item {
            SecondaryAction(if (store.session.isImpersonating) "Exit dealer view" else "Sign out", onExit, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ProfileMetric(label: String, value: String, bg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = bg) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ShoppeMuted)
            Text(value, style = MaterialTheme.typography.titleLarge, color = ShoppeInk, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun categoryColor(id: String): Color = when (id) {
    "cat-bearings" -> ShoppeBlueSoft
    "cat-vbelts" -> Color(0xFFF2EDFF)
    "cat-timing" -> Color(0xFFFFF0F4)
    "cat-fasteners" -> ShoppeSky
    "cat-contactors" -> ShoppeMint
    "cat-relays" -> Color(0xFFFFF5E6)
    "cat-consumables" -> ShoppePeach
    else -> Color(0xFFF1F4F9)
}
