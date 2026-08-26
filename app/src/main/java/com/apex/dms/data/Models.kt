package com.apex.dms.data

enum class ActorRole { SUPER_ADMIN, ADMIN, SALESPERSON, DEALER }
enum class StockState { IN_STOCK, LIMITED, ON_REQUEST }
enum class RequestStatus { DRAFT, SUBMITTED, UNDER_REVIEW, QUOTE_READY, QUOTE_SENT, AWAITING_CONFIRMATION, CONFIRMED, CLOSED, CANCELLED }
enum class QuoteStatus { DRAFT, SENT, ACCEPTED, REVISION_REQUESTED, EXPIRED }
enum class OrderStatus { CONFIRMED, PROCESSING, READY_TO_DISPATCH, DISPATCHED, DELIVERED, ON_HOLD, CANCELLED }
enum class PaymentStatus { PENDING, PARTIAL, PAID, CREDIT }
enum class ApprovalStatus { NOT_REQUIRED, PENDING, APPROVED, REJECTED }
enum class AutomationStatus { SUCCESS, WAITING, FAILED, RETRYING }

data class Category(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String,
)

data class Brand(val id: String, val name: String)

data class Salesperson(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val region: String,
    val active: Boolean = true,
    val imageUrl: String = "",
)

data class Product(
    val id: String,
    val sku: String,
    val name: String,
    val brandId: String,
    val categoryId: String,
    val description: String,
    val primarySpec: String,
    val packSize: String,
    val unit: String,
    val moq: Int,
    val stockState: StockState,
    val availableQty: Int,
    val warehouse: String,
    val active: Boolean = true,
    val imageUrl: String? = null,
)

data class Dealer(
    val id: String,
    val businessName: String,
    val contactName: String,
    val phone: String,
    val email: String,
    val gstin: String,
    val city: String,
    val state: String,
    val assignedSalespersonId: String,
    val paymentTerms: String,
    val tier: String,
    val creditLimit: Double,
    val outstanding: Double,
    val active: Boolean = true,
    val photoUrl: String? = null,
)

data class RequestLine(
    val productId: String,
    val sku: String,
    val productName: String,
    val quantity: Int,
    val unit: String,
    val note: String = "",
)

data class PriceRequest(
    val id: String,
    val reference: String,
    val dealerId: String,
    val salespersonId: String?,
    val status: RequestStatus,
    val requiredBy: String,
    val deliveryCity: String,
    val buyerReference: String,
    val note: String,
    val lines: List<RequestLine>,
    val createdAt: String,
    val updatedAt: String,
)

data class QuoteLine(
    val productId: String,
    val description: String,
    val quantity: Int,
    val unit: String,
    val unitRate: Double,
    val discountPct: Double,
    val gstPct: Double,
)

data class Quotation(
    val id: String,
    val quoteNumber: String,
    val requestId: String,
    val dealerId: String,
    val issueDate: String,
    val validUntil: String,
    val freight: Double,
    val paymentTerms: String,
    val deliveryTerms: String,
    val notes: String,
    val lines: List<QuoteLine>,
    val status: QuoteStatus,
    val approvalStatus: ApprovalStatus = ApprovalStatus.NOT_REQUIRED,
    val approvalRequestedAt: String = "",
    val approvedBy: String = "",
)

data class Order(
    val id: String,
    val orderNumber: String,
    val quoteId: String,
    val requestId: String,
    val dealerId: String,
    val status: OrderStatus,
    val paymentStatus: PaymentStatus,
    val total: Double,
    val dispatchReference: String,
    val createdAt: String,
)

data class ActivityEvent(
    val id: String,
    val title: String,
    val message: String,
    val actor: String,
    val entityType: String,
    val entityId: String,
    val timestamp: String,
)

data class DraftItem(
    val productId: String,
    val quantity: Int,
    val note: String = "",
)


data class AuthUserProfile(
    val userId: String,
    val sessionToken: String,
    val role: ActorRole,
    val phone: String,
    val givenName: String,
    val familyName: String,
    val email: String,
    val pictureUrl: String,
    val businessName: String,
    val gstin: String,
    val city: String,
    val state: String,
) {
    val displayName: String get() = listOf(givenName, familyName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Dealer" }
}

data class AutomationRun(
    val id: String,
    val title: String,
    val message: String,
    val status: AutomationStatus,
    val timestamp: String,
    val workflowKey: String,
    val entityId: String = "",
)

data class ApprovalItem(
    val id: String,
    val quoteId: String,
    val quoteNumber: String,
    val dealerName: String,
    val amount: Double,
    val reason: String,
    val status: ApprovalStatus,
    val requestedAt: String,
)

data class IntegrationState(
    val key: String,
    val label: String,
    val mode: String,
    val status: String,
    val lastSync: String = "",
)

data class OutboxEvent(
    val id: String,
    val type: String,
    val entityType: String,
    val entityId: String,
    val payload: Map<String, Any?>,
    val createdAt: String,
    val synced: Boolean = false,
)

data class AppSnapshot(
    val categories: List<Category>,
    val brands: List<Brand>,
    val salespeople: List<Salesperson>,
    val products: List<Product>,
    val dealers: List<Dealer>,
    val requests: List<PriceRequest>,
    val quotations: List<Quotation>,
    val orders: List<Order>,
    val activities: List<ActivityEvent>,
    val draftItems: List<DraftItem>,
    val draftDeliveryCity: String,
    val draftRequiredBy: String,
    val draftBuyerReference: String,
    val draftNote: String,
)

data class SessionState(
    val role: ActorRole? = null,
    val dealerId: String = "dealer-shree-tools",
    val salespersonId: String = "sales-ravi",
    val previousRole: ActorRole? = null,
) {
    val isImpersonating: Boolean get() = previousRole != null
}

data class QuoteTotals(
    val subtotal: Double,
    val discount: Double,
    val taxable: Double,
    val gst: Double,
    val freight: Double,
    val grandTotal: Double,
)

fun calculateQuoteTotals(quote: Quotation): QuoteTotals {
    var subtotal = 0.0
    var discount = 0.0
    var gst = 0.0
    quote.lines.forEach { line ->
        val lineSubtotal = line.quantity * line.unitRate
        val lineDiscount = lineSubtotal * (line.discountPct / 100.0)
        val taxable = lineSubtotal - lineDiscount
        val lineGst = taxable * (line.gstPct / 100.0)
        subtotal += lineSubtotal
        discount += lineDiscount
        gst += lineGst
    }
    val taxable = subtotal - discount
    return QuoteTotals(
        subtotal = subtotal,
        discount = discount,
        taxable = taxable,
        gst = gst,
        freight = quote.freight,
        grandTotal = taxable + gst + quote.freight,
    )
}
