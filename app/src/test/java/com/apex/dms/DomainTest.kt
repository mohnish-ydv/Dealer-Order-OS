package com.apex.dms

import com.apex.dms.data.OrderStatus
import com.apex.dms.data.QuoteStatus
import com.apex.dms.data.RequestStatus
import com.apex.dms.data.SeedData
import com.apex.dms.data.allowedRequestTransitions
import com.apex.dms.data.calculateQuoteTotals
import com.apex.dms.data.nextOrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainTest {
    @Test
    fun seededQuoteMathIsDeterministic() {
        val snapshot = SeedData.snapshot()
        val quote = snapshot.quotations.first()
        val totals = calculateQuoteTotals(quote)
        assertTrue(totals.subtotal > 0)
        assertTrue(totals.grandTotal > totals.taxable)
        assertEquals(quote.freight, totals.freight, 0.001)
    }

    @Test
    fun requestWorkflowRejectsJumps() {
        assertEquals(listOf(RequestStatus.UNDER_REVIEW, RequestStatus.CANCELLED), allowedRequestTransitions(RequestStatus.SUBMITTED))
        assertTrue(RequestStatus.CONFIRMED !in allowedRequestTransitions(RequestStatus.SUBMITTED))
        assertTrue(allowedRequestTransitions(RequestStatus.CLOSED).isEmpty())
    }

    @Test
    fun orderWorkflowIsSequential() {
        assertEquals(OrderStatus.PROCESSING, nextOrderStatus(OrderStatus.CONFIRMED))
        assertEquals(OrderStatus.READY_TO_DISPATCH, nextOrderStatus(OrderStatus.PROCESSING))
        assertEquals(OrderStatus.DISPATCHED, nextOrderStatus(OrderStatus.READY_TO_DISPATCH))
        assertEquals(OrderStatus.DELIVERED, nextOrderStatus(OrderStatus.DISPATCHED))
        assertEquals(null, nextOrderStatus(OrderStatus.DELIVERED))
    }

    @Test
    fun seededEntitiesAreConnected() {
        val snapshot = SeedData.snapshot()
        snapshot.requests.forEach { request ->
            assertTrue(snapshot.dealers.any { it.id == request.dealerId })
            request.lines.forEach { line -> assertTrue(snapshot.products.any { it.id == line.productId }) }
        }
        snapshot.quotations.forEach { quote ->
            assertTrue(snapshot.requests.any { it.id == quote.requestId })
            assertTrue(snapshot.dealers.any { it.id == quote.dealerId })
        }
        snapshot.orders.forEach { order ->
            assertTrue(snapshot.dealers.any { it.id == order.dealerId })
        }
    }
}
