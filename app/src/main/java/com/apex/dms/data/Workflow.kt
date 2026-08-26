package com.apex.dms.data

fun allowedRequestTransitions(status: RequestStatus): List<RequestStatus> = when (status) {
    RequestStatus.DRAFT -> listOf(RequestStatus.SUBMITTED, RequestStatus.CANCELLED)
    RequestStatus.SUBMITTED -> listOf(RequestStatus.UNDER_REVIEW, RequestStatus.CANCELLED)
    RequestStatus.UNDER_REVIEW -> listOf(RequestStatus.QUOTE_READY, RequestStatus.CANCELLED)
    RequestStatus.QUOTE_READY -> listOf(RequestStatus.QUOTE_SENT, RequestStatus.CANCELLED)
    RequestStatus.QUOTE_SENT -> listOf(RequestStatus.AWAITING_CONFIRMATION, RequestStatus.CANCELLED)
    RequestStatus.AWAITING_CONFIRMATION -> listOf(RequestStatus.CONFIRMED, RequestStatus.CANCELLED)
    RequestStatus.CONFIRMED -> listOf(RequestStatus.CLOSED, RequestStatus.CANCELLED)
    RequestStatus.CLOSED, RequestStatus.CANCELLED -> emptyList()
}

fun nextOrderStatus(status: OrderStatus): OrderStatus? = when (status) {
    OrderStatus.CONFIRMED -> OrderStatus.PROCESSING
    OrderStatus.PROCESSING -> OrderStatus.READY_TO_DISPATCH
    OrderStatus.READY_TO_DISPATCH -> OrderStatus.DISPATCHED
    OrderStatus.DISPATCHED -> OrderStatus.DELIVERED
    else -> null
}

fun requestProgress(status: RequestStatus): Int = when (status) {
    RequestStatus.DRAFT -> 0
    RequestStatus.SUBMITTED -> 1
    RequestStatus.UNDER_REVIEW -> 2
    RequestStatus.QUOTE_READY, RequestStatus.QUOTE_SENT -> 3
    RequestStatus.AWAITING_CONFIRMATION -> 4
    RequestStatus.CONFIRMED, RequestStatus.CLOSED -> 5
    RequestStatus.CANCELLED -> 0
}

fun orderProgress(status: OrderStatus): Int = when (status) {
    OrderStatus.CONFIRMED -> 1
    OrderStatus.PROCESSING -> 2
    OrderStatus.READY_TO_DISPATCH -> 3
    OrderStatus.DISPATCHED -> 4
    OrderStatus.DELIVERED -> 5
    OrderStatus.ON_HOLD, OrderStatus.CANCELLED -> 0
}
