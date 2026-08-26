package com.apex.dms.data

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apex.dms.backend.BackendClient
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

class AppStore(application: Application) : AndroidViewModel(application) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val stateFile = File(application.filesDir, "apex_dms_state.json")
    private val prefs = application.getSharedPreferences("apex_dms_prefs", Context.MODE_PRIVATE)
    private val backend = BackendClient()

    var snapshot by mutableStateOf(loadSnapshot())
        private set

    var session by mutableStateOf(loadSession())
        private set

    var onboardingCompleted by mutableStateOf(prefs.getBoolean("onboarding_complete", false))
        private set

    var authLoading by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var currentUser by mutableStateOf(loadUser())
        private set
    var backendSessionToken by mutableStateOf(prefs.getString("backend_session", "").orEmpty())
        private set

    var automationRuns by mutableStateOf(seedAutomationRuns())
        private set
    var approvals by mutableStateOf<List<ApprovalItem>>(emptyList())
        private set
    var integrations by mutableStateOf(seedIntegrations())
        private set

    var outbox by mutableStateOf(loadOutbox())
        private set
    private var outboxSyncing = false

    init {
        if (backendSessionToken.isNotBlank()) {
            refreshRemoteProducts()
            flushOutbox()
        }
    }

    private fun loadSnapshot(): AppSnapshot {
        return runCatching {
            if (!stateFile.exists()) return@runCatching SeedData.snapshot()
            gson.fromJson(stateFile.readText(), AppSnapshot::class.java)
        }.getOrElse { SeedData.snapshot() }
    }

    private fun loadSession(): SessionState {
        val role = prefs.getString("session_role", null)?.let { runCatching { ActorRole.valueOf(it) }.getOrNull() }
        return SessionState(
            role = role,
            dealerId = prefs.getString("session_dealer", "dealer-shree-tools") ?: "dealer-shree-tools",
            salespersonId = prefs.getString("session_salesperson", "sales-ravi") ?: "sales-ravi",
        )
    }

    private fun loadUser(): AuthUserProfile? = runCatching {
        prefs.getString("auth_user", null)?.let { gson.fromJson(it, AuthUserProfile::class.java) }
    }.getOrNull()

    private fun loadOutbox(): List<OutboxEvent> = runCatching {
        val raw = prefs.getString("event_outbox", "[]").orEmpty()
        val type = object : TypeToken<List<OutboxEvent>>() {}.type
        gson.fromJson<List<OutboxEvent>>(raw, type).orEmpty()
    }.getOrDefault(emptyList())

    private fun persistOutbox() {
        prefs.edit().putString("event_outbox", gson.toJson(outbox)).apply()
    }

    private fun persistSession() {
        prefs.edit()
            .putString("session_role", session.role?.name)
            .putString("session_dealer", session.dealerId)
            .putString("session_salesperson", session.salespersonId)
            .apply()
    }

    private fun commit(next: AppSnapshot) {
        snapshot = next
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { stateFile.writeText(gson.toJson(next)) }
        }
    }

    fun completeOnboarding() {
        onboardingCompleted = true
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }

    fun resetDemo() {
        commit(SeedData.snapshot())
        automationRuns = seedAutomationRuns()
        approvals = emptyList()
        outbox = emptyList()
        persistOutbox()
    }

    fun signIn(role: ActorRole) {
        session = SessionState(role = role)
        persistSession()
    }

    fun signInWithTruecaller(
        authorizationCode: String,
        codeVerifier: String,
        state: String,
        onFinished: (Boolean, ActorRole) -> Unit,
    ) {
        if (authLoading) return
        authLoading = true
        authError = null
        viewModelScope.launch {
            val result = backend.exchangeTruecallerAuth(authorizationCode, codeVerifier, state)
            result.onSuccess { profile ->
                applyAuthenticatedProfile(profile)
                refreshRemoteProducts()
                flushOutbox()
                authLoading = false
                onFinished(true, profile.role)
            }.onFailure { error ->
                authLoading = false
                authError = error.message ?: "Truecaller sign-in failed"
                onFinished(false, ActorRole.DEALER)
            }
        }
    }

    private fun applyAuthenticatedProfile(profile: AuthUserProfile) {
        currentUser = profile
        backendSessionToken = profile.sessionToken
        prefs.edit()
            .putString("backend_session", profile.sessionToken)
            .putString("auth_user", gson.toJson(profile))
            .apply()

        val dealerId = "dealer-user-${profile.userId.take(8)}"
        val existing = snapshot.dealers.firstOrNull { it.id == dealerId }
        val dealer = Dealer(
            id = dealerId,
            businessName = profile.businessName.ifBlank { "${profile.displayName}'s Business" },
            contactName = profile.displayName,
            phone = profile.phone,
            email = profile.email,
            gstin = profile.gstin.ifBlank { "GSTIN not added" },
            city = profile.city.ifBlank { "Pune" },
            state = profile.state.ifBlank { "Maharashtra" },
            assignedSalespersonId = existing?.assignedSalespersonId ?: "sales-ravi",
            paymentTerms = existing?.paymentTerms ?: "15 days",
            tier = existing?.tier ?: "New",
            creditLimit = existing?.creditLimit ?: 100000.0,
            outstanding = existing?.outstanding ?: 0.0,
            active = true,
            photoUrl = profile.pictureUrl,
        )
        val dealers = if (existing == null) listOf(dealer) + snapshot.dealers else snapshot.dealers.map { if (it.id == dealerId) dealer else it }
        commit(snapshot.copy(dealers = dealers))
        session = SessionState(role = profile.role, dealerId = dealerId)
        persistSession()
        recordAutomation("Truecaller verified", "${profile.displayName} signed in with a verified Truecaller profile", AutomationStatus.SUCCESS, "auth.truecaller", dealerId)
    }

    fun exitToAccess() {
        session = SessionState()
        currentUser = null
        backendSessionToken = ""
        prefs.edit().remove("session_role").remove("session_dealer").remove("auth_user").remove("backend_session").apply()
    }

    fun clearAuthError() { authError = null }

    fun impersonateDealer(dealerId: String) {
        val current = session.role ?: ActorRole.SUPER_ADMIN
        session = SessionState(
            role = ActorRole.DEALER,
            dealerId = dealerId,
            salespersonId = session.salespersonId,
            previousRole = current,
        )
        addActivity("Dealer view started", "Viewing portal as ${dealer(dealerId)?.businessName ?: dealerId}", "Super Admin", "system", dealerId)
    }

    fun stopImpersonation() {
        val previous = session.previousRole ?: return
        session = SessionState(role = previous, dealerId = session.dealerId, salespersonId = session.salespersonId)
        addActivity("Dealer view ended", "Returned to staff workspace", "Super Admin", "system", "session")
    }

    fun category(id: String) = snapshot.categories.firstOrNull { it.id == id }
    fun brand(id: String) = snapshot.brands.firstOrNull { it.id == id }
    fun product(id: String) = snapshot.products.firstOrNull { it.id == id }
    fun dealer(id: String) = snapshot.dealers.firstOrNull { it.id == id }
    fun salesperson(id: String?) = snapshot.salespeople.firstOrNull { it.id == id }
    fun request(id: String) = snapshot.requests.firstOrNull { it.id == id }
    fun quote(id: String) = snapshot.quotations.firstOrNull { it.id == id }
    fun order(id: String) = snapshot.orders.firstOrNull { it.id == id }
    fun quoteForRequest(requestId: String) = snapshot.quotations.firstOrNull { it.requestId == requestId }
    fun orderForQuote(quoteId: String) = snapshot.orders.firstOrNull { it.quoteId == quoteId }

    fun visibleProducts(): List<Product> = snapshot.products.filter { it.active }
    fun dealerRequests(): List<PriceRequest> = snapshot.requests.filter { it.dealerId == session.dealerId }
    fun dealerQuotes(): List<Quotation> = snapshot.quotations.filter { it.dealerId == session.dealerId && it.status != QuoteStatus.DRAFT }
    fun dealerOrders(): List<Order> = snapshot.orders.filter { it.dealerId == session.dealerId }

    fun staffRequests(): List<PriceRequest> = if (session.role == ActorRole.SALESPERSON) {
        snapshot.requests.filter { it.salespersonId == session.salespersonId }
    } else snapshot.requests

    fun refreshRemoteProducts() {
        val token = backendSessionToken
        if (token.isBlank()) return
        viewModelScope.launch {
            backend.fetchProducts(token).onSuccess { remote ->
                if (remote.isNotEmpty()) {
                    val remoteIds = remote.map { it.id }.toSet()
                    commit(snapshot.copy(products = remote + snapshot.products.filterNot { it.id in remoteIds }))
                    integrations = integrations.map { if (it.key == "supabase") it.copy(status = "LIVE", lastSync = "Just now") else it }
                    recordAutomation("Product feed synced", "${remote.size} products refreshed from Supabase", AutomationStatus.SUCCESS, "products.sync")
                }
            }.onFailure {
                recordAutomation("Product sync delayed", it.message ?: "Backend unavailable", AutomationStatus.RETRYING, "products.sync")
            }
        }
    }

    fun updateDealerProfile(
        businessName: String,
        contactName: String,
        phone: String,
        email: String,
        gstin: String,
        city: String,
        state: String,
    ) {
        val dealerId = session.dealerId
        val old = dealer(dealerId) ?: return
        val updated = old.copy(
            businessName = businessName.trim().ifBlank { old.businessName },
            contactName = contactName.trim().ifBlank { old.contactName },
            phone = phone.trim().ifBlank { old.phone },
            email = email.trim(),
            gstin = gstin.trim().uppercase(),
            city = city.trim().ifBlank { old.city },
            state = state.trim().ifBlank { old.state },
        )
        commit(snapshot.copy(dealers = snapshot.dealers.map { if (it.id == dealerId) updated else it }))
        addActivity("Profile updated", "Business profile details updated", updated.contactName, "dealer", dealerId)
        emitBusinessEvent("profile.updated", "dealer", dealerId, mapOf("businessName" to updated.businessName, "city" to updated.city))
        val token = backendSessionToken
        if (token.isNotBlank()) {
            viewModelScope.launch {
                backend.updateProfile(token, mapOf(
                    "businessName" to updated.businessName,
                    "contactName" to updated.contactName,
                    "phone" to updated.phone,
                    "email" to updated.email,
                    "gstin" to updated.gstin,
                    "city" to updated.city,
                    "state" to updated.state,
                )).onSuccess { profile ->
                    currentUser = profile
                    prefs.edit().putString("auth_user", gson.toJson(profile)).apply()
                    recordAutomation("Profile synced", "Updated profile saved to Supabase", AutomationStatus.SUCCESS, "profile.sync", dealerId)
                }.onFailure {
                    recordAutomation("Profile queued", "Local profile saved; backend sync will retry", AutomationStatus.RETRYING, "profile.sync", dealerId)
                }
            }
        }
    }

    fun smartEnquiryToDraft(text: String): Int {
        val query = text.trim().lowercase()
        if (query.isBlank()) return 0
        val numberTokens = Regex("\\b(\\d{1,6})\\b").findAll(query).mapNotNull { it.value.toIntOrNull() }.toList()
        var added = 0
        visibleProducts().forEach { product ->
            val skuToken = product.sku.lowercase().replace("-", " ")
            val nameTokens = product.name.lowercase().split(Regex("\\W+")).filter { it.length >= 4 }
            val matched = query.contains(product.sku.lowercase()) || query.contains(skuToken) || nameTokens.any { query.contains(it) }
            if (matched && added < 4) {
                val quantity = numberTokens.firstOrNull { it >= product.moq } ?: product.moq
                addToDraft(product.id, quantity)
                added++
            }
        }
        if (added > 0) {
            recordAutomation("Smart enquiry parsed", "$added catalogue item${if (added == 1) "" else "s"} added to RFQ draft", AutomationStatus.SUCCESS, "enquiry.parse")
        } else {
            recordAutomation("Smart enquiry needs review", "No confident catalogue match; dealer kept in manual search flow", AutomationStatus.WAITING, "enquiry.parse")
        }
        return added
    }

    fun addToDraft(productId: String, quantity: Int) {
        val p = product(productId) ?: return
        val safeQty = quantity.coerceAtLeast(p.moq)
        val existing = snapshot.draftItems.firstOrNull { it.productId == productId }
        val items = if (existing == null) snapshot.draftItems + DraftItem(productId, safeQty)
        else snapshot.draftItems.map { if (it.productId == productId) it.copy(quantity = safeQty) else it }
        commit(snapshot.copy(draftItems = items))
    }

    fun incrementDraft(productId: String, delta: Int) {
        val p = product(productId) ?: return
        val item = snapshot.draftItems.firstOrNull { it.productId == productId } ?: return
        val next = (item.quantity + delta).coerceAtLeast(p.moq)
        commit(snapshot.copy(draftItems = snapshot.draftItems.map { if (it.productId == productId) it.copy(quantity = next) else it }))
    }

    fun removeDraft(productId: String) {
        commit(snapshot.copy(draftItems = snapshot.draftItems.filterNot { it.productId == productId }))
    }

    fun updateDraftMeta(city: String? = null, requiredBy: String? = null, buyerReference: String? = null, note: String? = null) {
        commit(snapshot.copy(
            draftDeliveryCity = city ?: snapshot.draftDeliveryCity,
            draftRequiredBy = requiredBy ?: snapshot.draftRequiredBy,
            draftBuyerReference = buyerReference ?: snapshot.draftBuyerReference,
            draftNote = note ?: snapshot.draftNote,
        ))
    }

    fun submitDraft(): String? {
        if (snapshot.draftItems.isEmpty()) return null
        val index = snapshot.requests.size + 1007
        val id = "rfq-native-$index"
        val reference = "RFQ-2608-$index"
        val d = dealer(session.dealerId) ?: return null
        val lines = snapshot.draftItems.mapNotNull { draft ->
            product(draft.productId)?.let { p -> RequestLine(p.id, p.sku, p.name, draft.quantity, p.unit, draft.note) }
        }
        val next = PriceRequest(id, reference, d.id, d.assignedSalespersonId, RequestStatus.SUBMITTED,
            snapshot.draftRequiredBy.ifBlank { "Not specified" }, snapshot.draftDeliveryCity.ifBlank { d.city },
            snapshot.draftBuyerReference, snapshot.draftNote, lines, "26 Aug · now", "26 Aug · now")
        commit(snapshot.copy(requests = listOf(next) + snapshot.requests, draftItems = emptyList(), draftRequiredBy = "", draftBuyerReference = "", draftNote = ""))
        addActivity("Price request submitted", "$reference · ${lines.size} items · ${d.businessName}", d.contactName, "request", id)
        recordAutomation("RFQ captured", "$reference normalized and assigned to ${salesperson(d.assignedSalespersonId)?.name ?: "sales"}", AutomationStatus.SUCCESS, "rfq.intake", id)
        emitBusinessEvent("rfq.created", "request", id, mapOf("reference" to reference, "items" to lines.size, "dealerId" to d.id))
        return id
    }

    fun repeatOrder(orderId: String) {
        val order = order(orderId) ?: return
        val req = request(order.requestId) ?: return
        commit(snapshot.copy(draftItems = req.lines.map { DraftItem(it.productId, it.quantity, it.note) }, draftDeliveryCity = dealer(order.dealerId)?.city ?: "", draftNote = "Repeat order from ${order.orderNumber}"))
    }

    fun moveRequest(requestId: String, target: RequestStatus) {
        val current = request(requestId) ?: return
        if (target !in allowedRequestTransitions(current.status)) return
        commit(snapshot.copy(requests = snapshot.requests.map { if (it.id == requestId) it.copy(status = target, updatedAt = "26 Aug · now") else it }))
        addActivity("Request updated", "${current.reference} moved to ${target.name.replace('_', ' ').lowercase()}", current.salespersonId?.let { salesperson(it)?.name } ?: "Operations", "request", requestId)
    }

    fun assignRequest(requestId: String, salespersonId: String) {
        val req = request(requestId) ?: return
        commit(snapshot.copy(requests = snapshot.requests.map { if (it.id == requestId) it.copy(salespersonId = salespersonId, updatedAt = "26 Aug · now") else it }))
        addActivity("Request assigned", "${req.reference} assigned to ${salesperson(salespersonId)?.name ?: salespersonId}", "Operations Admin", "request", requestId)
    }

    fun createQuoteFromRequest(requestId: String): String? {
        quoteForRequest(requestId)?.let { return it.id }
        val req = request(requestId) ?: return null
        val id = "q-native-${snapshot.quotations.size + 2501}"
        val quoteNumber = "QTN-2608-${snapshot.quotations.size + 2501}"
        val lines = req.lines.mapIndexed { index, line ->
            val p = product(line.productId)
            val baseRate = 180.0 + (index * 240) + ((p?.availableQty ?: 10) % 17) * 9
            QuoteLine(line.productId, line.productName, line.quantity, line.unit, baseRate, if (index == 0) 5.0 else 3.0, 18.0)
        }
        val q = Quotation(id, quoteNumber, req.id, req.dealerId, "26 Aug 2026", "02 Sep 2026", 750.0,
            dealer(req.dealerId)?.paymentTerms?.let { "$it from invoice" } ?: "30 days from invoice",
            "2–4 working days ex Pune", "Rates valid for quoted quantity and validity period.", lines, QuoteStatus.DRAFT)
        var requests = snapshot.requests
        if (req.status == RequestStatus.SUBMITTED) requests = requests.map { if (it.id == req.id) it.copy(status = RequestStatus.UNDER_REVIEW) else it }
        if (requests.first { it.id == req.id }.status == RequestStatus.UNDER_REVIEW) requests = requests.map { if (it.id == req.id) it.copy(status = RequestStatus.QUOTE_READY) else it }
        commit(snapshot.copy(quotations = listOf(q) + snapshot.quotations, requests = requests))
        addActivity("Quotation created", "$quoteNumber created from ${req.reference}", sessionActor(), "quote", id)
        recordAutomation("Quote drafted", "$quoteNumber generated from structured RFQ data", AutomationStatus.SUCCESS, "quote.create", id)
        emitBusinessEvent("quote.created", "quote", id, mapOf("quoteNumber" to quoteNumber, "requestId" to requestId))
        syncQuote(q)
        return id
    }

    fun updateQuoteLine(quoteId: String, productId: String, rate: Double? = null, discount: Double? = null) {
        val safeRate = rate?.coerceAtLeast(0.0)
        val safeDiscount = discount?.coerceIn(0.0, 100.0)
        commit(snapshot.copy(quotations = snapshot.quotations.map { q ->
            if (q.id != quoteId) q else q.copy(lines = q.lines.map { line ->
                if (line.productId != productId) line else line.copy(unitRate = safeRate ?: line.unitRate, discountPct = safeDiscount ?: line.discountPct)
            })
        }))
        quote(quoteId)?.let(::syncQuote)
    }

    fun updateQuoteTerms(quoteId: String, freight: Double? = null, paymentTerms: String? = null, deliveryTerms: String? = null) {
        commit(snapshot.copy(quotations = snapshot.quotations.map { q ->
            if (q.id != quoteId) q else q.copy(freight = freight?.coerceAtLeast(0.0) ?: q.freight, paymentTerms = paymentTerms ?: q.paymentTerms, deliveryTerms = deliveryTerms ?: q.deliveryTerms)
        }))
        quote(quoteId)?.let(::syncQuote)
    }

    fun sendQuote(quoteId: String): Boolean {
        val q = quote(quoteId) ?: return false
        val total = calculateQuoteTotals(q).grandTotal
        if (total >= 100000.0 && q.approvalStatus != ApprovalStatus.APPROVED) {
            requestQuoteApproval(quoteId)
            return false
        }
        commit(snapshot.copy(
            quotations = snapshot.quotations.map { if (it.id == quoteId) it.copy(status = QuoteStatus.SENT) else it },
            requests = snapshot.requests.map { if (it.id == q.requestId) it.copy(status = RequestStatus.QUOTE_SENT, updatedAt = "26 Aug · now") else it },
        ))
        addActivity("Quotation sent", "${q.quoteNumber} sent to ${dealer(q.dealerId)?.businessName}", sessionActor(), "quote", quoteId)
        recordAutomation("Quote notification sent", "${q.quoteNumber} queued for dealer notification and follow-up", AutomationStatus.SUCCESS, "quote.notify", quoteId)
        emitBusinessEvent("quote.sent", "quote", quoteId, mapOf("amount" to total, "dealerId" to q.dealerId))
        quote(quoteId)?.let(::syncQuote)
        return true
    }

    fun requestQuoteApproval(quoteId: String) {
        val q = quote(quoteId) ?: return
        if (q.approvalStatus == ApprovalStatus.PENDING) return
        val amount = calculateQuoteTotals(q).grandTotal
        val approval = ApprovalItem("approval-$quoteId", quoteId, q.quoteNumber, dealer(q.dealerId)?.businessName ?: q.dealerId, amount,
            "High-value quotation requires manager approval", ApprovalStatus.PENDING, "26 Aug · now")
        approvals = listOf(approval) + approvals.filterNot { it.quoteId == quoteId }
        commit(snapshot.copy(quotations = snapshot.quotations.map { if (it.id == quoteId) it.copy(approvalStatus = ApprovalStatus.PENDING, approvalRequestedAt = "26 Aug · now") else it }))
        recordAutomation("Manager approval required", "${q.quoteNumber} · ${money(amount)} paused before dealer delivery", AutomationStatus.WAITING, "approval.quote", quoteId)
        emitBusinessEvent("quote.approval_requested", "quote", quoteId, mapOf("amount" to amount, "quoteNumber" to q.quoteNumber))
        quote(quoteId)?.let(::syncQuote)
    }

    fun approveQuote(quoteId: String) {
        val q = quote(quoteId) ?: return
        commit(snapshot.copy(quotations = snapshot.quotations.map { if (it.id == quoteId) it.copy(approvalStatus = ApprovalStatus.APPROVED, approvedBy = sessionActor()) else it }))
        approvals = approvals.map { if (it.quoteId == quoteId) it.copy(status = ApprovalStatus.APPROVED) else it }
        addActivity("Quotation approved", "${q.quoteNumber} approved for dealer delivery", sessionActor(), "quote", quoteId)
        recordAutomation("Approval completed", "${q.quoteNumber} can now be sent to the dealer", AutomationStatus.SUCCESS, "approval.quote", quoteId)
        emitBusinessEvent("quote.approved", "quote", quoteId, mapOf("quoteNumber" to q.quoteNumber))
        quote(quoteId)?.let(::syncQuote)
    }

    fun rejectQuote(quoteId: String) {
        val q = quote(quoteId) ?: return
        commit(snapshot.copy(quotations = snapshot.quotations.map { if (it.id == quoteId) it.copy(approvalStatus = ApprovalStatus.REJECTED) else it }))
        approvals = approvals.map { if (it.quoteId == quoteId) it.copy(status = ApprovalStatus.REJECTED) else it }
        recordAutomation("Approval rejected", "${q.quoteNumber} returned to sales for revision", AutomationStatus.FAILED, "approval.quote", quoteId)
        quote(quoteId)?.let(::syncQuote)
    }

    fun requestQuoteRevision(quoteId: String) {
        val q = quote(quoteId) ?: return
        commit(snapshot.copy(quotations = snapshot.quotations.map { if (it.id == quoteId) it.copy(status = QuoteStatus.REVISION_REQUESTED) else it }))
        addActivity("Revision requested", "Dealer requested changes to ${q.quoteNumber}", dealer(q.dealerId)?.contactName ?: "Dealer", "quote", quoteId)
        emitBusinessEvent("quote.revision_requested", "quote", quoteId, emptyMap())
    }

    fun acceptQuote(quoteId: String): String? {
        val q = quote(quoteId) ?: return null
        orderForQuote(quoteId)?.let { return it.id }
        val total = calculateQuoteTotals(q).grandTotal
        val orderId = "ord-native-${snapshot.orders.size + 4101}"
        val orderNumber = "ORD-2608-${snapshot.orders.size + 4101}"
        val order = Order(orderId, orderNumber, q.id, q.requestId, q.dealerId, OrderStatus.CONFIRMED, PaymentStatus.PENDING, total, "", "26 Aug · now")
        commit(snapshot.copy(
            quotations = snapshot.quotations.map { if (it.id == q.id) it.copy(status = QuoteStatus.ACCEPTED) else it },
            requests = snapshot.requests.map { if (it.id == q.requestId) it.copy(status = RequestStatus.CONFIRMED, updatedAt = "26 Aug · now") else it },
            orders = listOf(order) + snapshot.orders,
        ))
        addActivity("Quotation accepted", "${q.quoteNumber} accepted; $orderNumber created", dealer(q.dealerId)?.contactName ?: "Dealer", "order", orderId)
        recordAutomation("Order created", "$orderNumber generated from accepted quotation", AutomationStatus.SUCCESS, "order.create", orderId)
        emitBusinessEvent("quote.accepted", "quote", quoteId, mapOf("orderId" to orderId, "amount" to total))
        quote(quoteId)?.let(::syncQuote)
        syncOrder(order)
        return orderId
    }

    fun simulatePayment(orderId: String, success: Boolean) {
        val current = order(orderId) ?: return
        if (!success) {
            recordAutomation("Demo payment failed", "${current.orderNumber} payment failure simulated; no business state changed", AutomationStatus.FAILED, "payment.demo", orderId)
            emitBusinessEvent("payment.failed", "order", orderId, mapOf("amount" to current.total, "simulated" to true))
            return
        }
        commit(snapshot.copy(orders = snapshot.orders.map { if (it.id == orderId) it.copy(paymentStatus = PaymentStatus.PAID) else it }))
        val req = request(current.requestId)
        if (req != null) {
            req.lines.forEach { line ->
                val p = product(line.productId) ?: return@forEach
                updateInventory(p.id, (p.availableQty - line.quantity).coerceAtLeast(0), emit = false)
            }
        }
        addActivity("Payment received", "${current.orderNumber} marked paid in BAOS demo gateway", "BAOS Payment Simulator", "order", orderId)
        recordAutomation("Payment verified", "Demo payment event validated and order marked paid", AutomationStatus.SUCCESS, "payment.verify", orderId)
        recordAutomation("Tally sync simulated", "Sales order and dealer ledger queued to Tally adapter", AutomationStatus.SUCCESS, "tally.order", orderId)
        recordAutomation("Dealer confirmation simulated", "Payment confirmation queued for WhatsApp/email", AutomationStatus.SUCCESS, "whatsapp.notify", orderId)
        emitBusinessEvent("payment.paid", "order", orderId, mapOf("amount" to current.total, "simulated" to true))
        order(orderId)?.let(::syncOrder)
    }

    fun advanceOrder(orderId: String, dispatchReference: String = "") {
        val current = order(orderId) ?: return
        val next = nextOrderStatus(current.status) ?: return
        if (next == OrderStatus.DISPATCHED && dispatchReference.isBlank() && current.dispatchReference.isBlank()) return
        val dispatch = if (dispatchReference.isNotBlank()) dispatchReference else current.dispatchReference
        commit(snapshot.copy(orders = snapshot.orders.map { if (it.id == orderId) it.copy(status = next, dispatchReference = dispatch) else it }))
        addActivity("Order updated", "${current.orderNumber} moved to ${next.name.replace('_', ' ').lowercase()}", sessionActor(), "order", orderId)
        recordAutomation("Order status propagated", "${current.orderNumber}: ${next.name.replace('_', ' ').lowercase()}", AutomationStatus.SUCCESS, "order.status", orderId)
        emitBusinessEvent("order.status_changed", "order", orderId, mapOf("status" to next.name))
        order(orderId)?.let(::syncOrder)
    }

    fun updateOrderPayment(orderId: String, status: PaymentStatus) {
        commit(snapshot.copy(orders = snapshot.orders.map { if (it.id == orderId) it.copy(paymentStatus = status) else it }))
        emitBusinessEvent("payment.updated", "order", orderId, mapOf("status" to status.name))
        order(orderId)?.let(::syncOrder)
    }

    fun addDealer(name: String, contact: String, city: String) {
        if (name.isBlank() || contact.isBlank()) return
        val id = "dealer-native-${snapshot.dealers.size + 1}"
        val d = Dealer(id, name.trim(), contact.trim(), "+91 90000 00000", "contact@${name.filter { it.isLetter() }.lowercase().take(12)}.in", "GSTIN pending", city.ifBlank { "Pune" }, "Maharashtra", "sales-ravi", "15 days", "New", 100000.0, 0.0)
        commit(snapshot.copy(dealers = listOf(d) + snapshot.dealers))
        addActivity("Dealer added", "${d.businessName} added to dealer network", sessionActor(), "dealer", id)
    }

    fun addProduct(sku: String, name: String, categoryId: String) {
        if (sku.isBlank() || name.isBlank()) return
        val p = Product("prd-native-${snapshot.products.size + 1}", sku.trim().uppercase(), name.trim(), "brand-apex", categoryId,
            "$name for industrial applications.", "Specification pending", "1 pc", "pcs", 1, StockState.ON_REQUEST, 0, "Pune Main", true)
        commit(snapshot.copy(products = listOf(p) + snapshot.products))
        addActivity("Product added", "${p.sku} · ${p.name}", sessionActor(), "product", p.id)
    }

    fun toggleProduct(productId: String) {
        val p = product(productId) ?: return
        commit(snapshot.copy(products = snapshot.products.map { if (it.id == productId) it.copy(active = !it.active) else it }))
    }

    fun updateInventory(productId: String, qty: Int, emit: Boolean = true) {
        val safe = qty.coerceAtLeast(0)
        val nextState = when {
            safe <= 8 -> StockState.ON_REQUEST
            safe <= 24 -> StockState.LIMITED
            else -> StockState.IN_STOCK
        }
        commit(snapshot.copy(products = snapshot.products.map { p -> if (p.id != productId) p else p.copy(availableQty = safe, stockState = nextState) }))
        if (emit) emitBusinessEvent("inventory.updated", "product", productId, mapOf("qty" to safe, "state" to nextState.name))
        if (safe <= 8) {
            recordAutomation("Low-stock exception", "${product(productId)?.sku ?: productId} is at $safe units", AutomationStatus.WAITING, "inventory.low", productId)
            emitBusinessEvent("inventory.low", "product", productId, mapOf("qty" to safe))
        }
    }

    fun runTallyDemoSync() {
        integrations = integrations.map { if (it.key == "tally") it.copy(status = "HEALTHY", lastSync = "Just now") else it }
        recordAutomation("Tally demo sync complete", "Customers, orders and inventory simulator synchronized", AutomationStatus.SUCCESS, "tally.sync")
    }

    fun ownerDigestText(): String {
        val paid = snapshot.orders.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.total }
        val booked = snapshot.orders.sumOf { it.total }
        val pendingApprovals = approvals.count { it.status == ApprovalStatus.PENDING }
        val low = snapshot.products.count { it.stockState != StockState.IN_STOCK }
        return "${money(booked)} orders booked · ${money(paid)} paid · ${snapshot.requests.size} enquiries · $pendingApprovals approvals · $low low-stock SKUs"
    }

    private fun emitBusinessEvent(type: String, entityType: String, entityId: String, payload: Map<String, Any?>) {
        val event = OutboxEvent(
            id = "evt-${UUID.randomUUID()}",
            type = type,
            entityType = entityType,
            entityId = entityId,
            payload = payload,
            createdAt = "26 Aug · now",
            synced = false,
        )
        outbox = outbox + event
        persistOutbox()
        flushOutbox()
    }

    fun flushOutbox() {
        val token = backendSessionToken
        if (token.isBlank() || outboxSyncing || outbox.isEmpty()) return
        outboxSyncing = true
        viewModelScope.launch {
            try {
                val pending = outbox.toList()
                pending.forEach { event ->
                    backend.emitEvent(token, event.id, event.type, event.entityType, event.entityId, event.payload)
                        .onSuccess {
                            outbox = outbox.filterNot { it.id == event.id }
                            persistOutbox()
                        }
                        .onFailure {
                            recordAutomation("Event queued for retry", "${event.type} is safely stored on-device until backend sync succeeds", AutomationStatus.RETRYING, "event.outbox", event.entityId)
                        }
                }
            } finally {
                outboxSyncing = false
            }
        }
    }

    private fun syncRequest(req: PriceRequest) {
        val token = backendSessionToken
        if (token.isBlank()) return
        val entity = mapOf<String, Any?>(
            "id" to req.id, "reference" to req.reference, "dealerId" to req.dealerId, "salespersonId" to req.salespersonId,
            "status" to req.status.name, "requiredBy" to req.requiredBy, "deliveryCity" to req.deliveryCity, "buyerReference" to req.buyerReference,
            "note" to req.note, "createdAt" to req.createdAt, "updatedAt" to req.updatedAt, "lines" to req.lines,
        )
        viewModelScope.launch { backend.syncEntity(token, "rfq", entity).onFailure { recordAutomation("RFQ backend sync queued", req.reference, AutomationStatus.RETRYING, "rfq.sync", req.id) } }
    }

    private fun syncQuote(q: Quotation) {
        val token = backendSessionToken
        if (token.isBlank()) return
        val entity = mapOf<String, Any?>(
            "id" to q.id, "quoteNumber" to q.quoteNumber, "requestId" to q.requestId, "dealerId" to q.dealerId, "status" to q.status.name,
            "approvalStatus" to q.approvalStatus.name, "grandTotal" to calculateQuoteTotals(q).grandTotal, "issueDate" to q.issueDate, "validUntil" to q.validUntil,
            "freight" to q.freight, "paymentTerms" to q.paymentTerms, "deliveryTerms" to q.deliveryTerms, "notes" to q.notes, "lines" to q.lines,
        )
        viewModelScope.launch { backend.syncEntity(token, "quote", entity).onFailure { recordAutomation("Quote backend sync queued", q.quoteNumber, AutomationStatus.RETRYING, "quote.sync", q.id) } }
    }

    private fun syncOrder(o: Order) {
        val token = backendSessionToken
        if (token.isBlank()) return
        val entity = mapOf<String, Any?>(
            "id" to o.id, "orderNumber" to o.orderNumber, "quoteId" to o.quoteId, "requestId" to o.requestId, "dealerId" to o.dealerId,
            "status" to o.status.name, "paymentStatus" to o.paymentStatus.name, "total" to o.total, "dispatchReference" to o.dispatchReference, "createdAt" to o.createdAt,
        )
        viewModelScope.launch { backend.syncEntity(token, "order", entity).onFailure { recordAutomation("Order backend sync queued", o.orderNumber, AutomationStatus.RETRYING, "order.sync", o.id) } }
    }

    private fun recordAutomation(title: String, message: String, status: AutomationStatus, workflowKey: String, entityId: String = "") {
        automationRuns = listOf(AutomationRun("run-${System.currentTimeMillis()}-${automationRuns.size}", title, message, status, "26 Aug · now", workflowKey, entityId)) + automationRuns
        if (automationRuns.size > 120) automationRuns = automationRuns.take(120)
    }

    private fun addActivity(title: String, message: String, actor: String, entityType: String, entityId: String) {
        val event = ActivityEvent("act-${System.currentTimeMillis()}-${snapshot.activities.size}", title, message, actor, entityType, entityId, "26 Aug · now")
        commit(snapshot.copy(activities = listOf(event) + snapshot.activities))
    }

    private fun sessionActor(): String = when (session.role) {
        ActorRole.SUPER_ADMIN -> "Super Admin"
        ActorRole.ADMIN -> "Operations Admin"
        ActorRole.SALESPERSON -> salesperson(session.salespersonId)?.name ?: "Salesperson"
        ActorRole.DEALER -> dealer(session.dealerId)?.contactName ?: "Dealer"
        null -> "System"
    }

    fun dashboardMetrics(): Map<String, Int> = mapOf(
        "openRequests" to staffRequests().count { it.status !in listOf(RequestStatus.CLOSED, RequestStatus.CANCELLED, RequestStatus.CONFIRMED) },
        "quotesWaiting" to snapshot.quotations.count { it.status == QuoteStatus.SENT },
        "activeOrders" to snapshot.orders.count { it.status !in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED) },
        "lowStock" to snapshot.products.count { it.stockState != StockState.IN_STOCK },
    )

    fun conversionPercent(): Int {
        val sent = snapshot.quotations.count { it.status != QuoteStatus.DRAFT }
        if (sent == 0) return 0
        val accepted = snapshot.quotations.count { it.status == QuoteStatus.ACCEPTED }
        return ((accepted.toDouble() / sent) * 100).roundToInt()
    }

    private fun seedAutomationRuns() = listOf(
        AutomationRun("seed-run-1", "Automation OS ready", "Event router, approvals, payments, inventory and digest modules are available", AutomationStatus.SUCCESS, "Demo", "system"),
        AutomationRun("seed-run-2", "Supabase backend configured", "Dealer-Order-OS project is the remote data source", AutomationStatus.SUCCESS, "Demo", "supabase.sync"),
    )

    private fun seedIntegrations() = listOf(
        IntegrationState("supabase", "Supabase Backend", "LIVE", "READY"),
        IntegrationState("truecaller", "Truecaller OAuth", "LIVE", "SHA1 UPDATE NEEDED"),
        IntegrationState("payments", "BAOS Demo Payment", "SIMULATED", "READY"),
        IntegrationState("tally", "TallyPrime", "SIMULATED", "READY"),
        IntegrationState("whatsapp", "WhatsApp Business", "SIMULATED", "READY"),
        IntegrationState("n8n", "n8n Orchestrator", "SIMULATED", "READY"),
    )
}

fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 0
}.format(value)
