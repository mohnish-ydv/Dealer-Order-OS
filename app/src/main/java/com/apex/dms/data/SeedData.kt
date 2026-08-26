package com.apex.dms.data

object SeedData {
    private val categories = listOf(
        Category("cat-bearings", "Deep Groove Ball Bearings", "Bearings", "Radial bearings for motors, pumps and machinery."),
        Category("cat-vbelts", "V-Belts", "V-Belts", "Classical and wedge belts for power transmission."),
        Category("cat-timing", "Timing Belts", "Timing", "Synchronous belts for controlled transmission."),
        Category("cat-fasteners", "Industrial Fasteners", "Fasteners", "Bolts, nuts and workshop fastening components."),
        Category("cat-contactors", "Electrical Contactors", "Contactors", "Motor switching and industrial control contactors."),
        Category("cat-relays", "Relays & Protection", "Relays", "Overload, control and protection devices."),
        Category("cat-consumables", "Workshop Consumables", "Workshop", "Cutting, grinding and maintenance consumables."),
        Category("cat-power", "Power Transmission", "Power", "Pulleys, couplings and transmission accessories."),
    )

    private val brands = listOf(
        Brand("brand-skf", "SKF"), Brand("brand-fag", "FAG"), Brand("brand-fenner", "Fenner"),
        Brand("brand-schneider", "Schneider Electric"), Brand("brand-lt", "L&T"), Brand("brand-apex", "Apex Select"),
    )

    private val salespeople = listOf(
        Salesperson("sales-ravi", "Ravi Mehta", "+91 98220 44108", "ravi@apex-demo.in", "West"),
        Salesperson("sales-neha", "Neha Kulkarni", "+91 97654 11290", "neha@apex-demo.in", "Maharashtra"),
        Salesperson("sales-arjun", "Arjun Shah", "+91 98980 31670", "arjun@apex-demo.in", "Gujarat"),
        Salesperson("sales-priya", "Priya Nair", "+91 98102 60714", "priya@apex-demo.in", "North & Central"),
    )

    private data class RawProduct(
        val sku: String,
        val name: String,
        val spec: String,
        val pack: String,
        val available: Int,
        val category: String,
        val brand: String,
    )

    private val rawProducts = listOf(
        RawProduct("6201-2RS", "SKF 6201-2RS Deep Groove Ball Bearing", "12×32×10 mm", "10 pcs", 128, "cat-bearings", "brand-skf"),
        RawProduct("6202-2RS", "SKF 6202-2RS Deep Groove Ball Bearing", "15×35×11 mm", "10 pcs", 114, "cat-bearings", "brand-skf"),
        RawProduct("6203-2RS", "SKF 6203-2RS Deep Groove Ball Bearing", "17×40×12 mm", "10 pcs", 96, "cat-bearings", "brand-skf"),
        RawProduct("6204-2RS", "SKF 6204-2RS Deep Groove Ball Bearing", "20×47×14 mm", "10 pcs", 84, "cat-bearings", "brand-skf"),
        RawProduct("6205-2RS", "SKF 6205-2RS Deep Groove Ball Bearing", "25×52×15 mm", "10 pcs", 63, "cat-bearings", "brand-skf"),
        RawProduct("6206-2RS", "SKF 6206-2RS Deep Groove Ball Bearing", "30×62×16 mm", "5 pcs", 48, "cat-bearings", "brand-skf"),
        RawProduct("A-42", "Fenner V-Belt A42", "13×8 mm section", "5 pcs", 75, "cat-vbelts", "brand-fenner"),
        RawProduct("A-48", "Fenner V-Belt A48", "13×8 mm section", "5 pcs", 68, "cat-vbelts", "brand-fenner"),
        RawProduct("B-45", "Fenner V-Belt B45", "17×11 mm section", "5 pcs", 37, "cat-vbelts", "brand-fenner"),
        RawProduct("B-60", "Fenner V-Belt B60", "17×11 mm section", "5 pcs", 18, "cat-vbelts", "brand-fenner"),
        RawProduct("T5-500-10", "Fenner Timing Belt T5 500", "10 mm width · 5 mm pitch", "1 pc", 24, "cat-timing", "brand-fenner"),
        RawProduct("T10-750-16", "Fenner Timing Belt T10 750", "16 mm width · 10 mm pitch", "1 pc", 12, "cat-timing", "brand-fenner"),
        RawProduct("HTD5M-800-15", "HTD 5M Timing Belt 800", "15 mm width · 5M profile", "1 pc", 11, "cat-timing", "brand-fenner"),
        RawProduct("HB-M8X40-88", "Hex Bolt M8 × 40 Grade 8.8", "Zinc plated", "100 pcs", 560, "cat-fasteners", "brand-apex"),
        RawProduct("HB-M10X50-88", "Hex Bolt M10 × 50 Grade 8.8", "Zinc plated", "100 pcs", 420, "cat-fasteners", "brand-apex"),
        RawProduct("HN-M8-8", "Hex Nut M8 Grade 8", "Metric coarse", "100 pcs", 780, "cat-fasteners", "brand-apex"),
        RawProduct("FW-M10", "Flat Washer M10", "Bright zinc plated", "200 pcs", 860, "cat-fasteners", "brand-apex"),
        RawProduct("LC1D09", "Schneider TeSys D 9A Contactor", "3P · AC3", "1 pc", 18, "cat-contactors", "brand-schneider"),
        RawProduct("LC1D18", "Schneider TeSys D 18A Contactor", "3P · AC3", "1 pc", 14, "cat-contactors", "brand-schneider"),
        RawProduct("LC1D32", "Schneider TeSys D 32A Contactor", "3P · AC3", "1 pc", 9, "cat-contactors", "brand-schneider"),
        RawProduct("MN2-6A", "L&T Thermal Overload Relay 4–6A", "Class 10", "1 pc", 24, "cat-relays", "brand-lt"),
        RawProduct("MN2-16A", "L&T Thermal Overload Relay 10–16A", "Class 10", "1 pc", 19, "cat-relays", "brand-lt"),
        RawProduct("EMPR-3P", "L&T Electronic Motor Protection Relay", "3 phase sensing", "1 pc", 6, "cat-relays", "brand-lt"),
        RawProduct("CUT-4-SS", "Cutting Disc 4 inch Stainless Steel", "105×1.0×16 mm", "25 pcs", 245, "cat-consumables", "brand-apex"),
        RawProduct("GRIND-4", "Grinding Wheel 4 inch", "100×6×16 mm", "25 pcs", 118, "cat-consumables", "brand-apex"),
        RawProduct("PTFE-12", "PTFE Thread Seal Tape 12 mm", "12 mm × 12 m", "50 pcs", 310, "cat-consumables", "brand-apex"),
        RawProduct("PUL-A-2-4", "A Section 2-Groove Pulley 4 inch", "Pilot bore", "1 pc", 16, "cat-power", "brand-fag"),
        RawProduct("HRC-110", "HRC Coupling 110", "Pilot bore", "1 set", 17, "cat-power", "brand-fag"),
        RawProduct("JAW-095", "Jaw Coupling L095", "NBR spider", "1 set", 23, "cat-power", "brand-fag"),
    )


    private fun imageForCategory(category: String, index: Int): String {
        val images = when (category) {
            "cat-bearings" -> listOf(
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=900&q=82",
                "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=900&q=82",
            )
            "cat-vbelts", "cat-timing", "cat-power" -> listOf(
                "https://images.unsplash.com/photo-1504917595217-d4dc5ebe6122?auto=format&fit=crop&w=900&q=82",
                "https://images.unsplash.com/photo-1565043666747-69f6646db940?auto=format&fit=crop&w=900&q=82",
            )
            "cat-fasteners" -> listOf(
                "https://images.unsplash.com/photo-1530124566582-a618bc2615dc?auto=format&fit=crop&w=900&q=82",
                "https://images.unsplash.com/photo-1586864387967-d02ef85d93e8?auto=format&fit=crop&w=900&q=82",
            )
            "cat-contactors", "cat-relays" -> listOf(
                "https://images.unsplash.com/photo-1513828583688-c52646db42da?auto=format&fit=crop&w=900&q=82",
                "https://images.unsplash.com/photo-1473341304170-971dccb5ac1e?auto=format&fit=crop&w=900&q=82",
            )
            else -> listOf(
                "https://images.unsplash.com/photo-1565043666747-69f6646db940?auto=format&fit=crop&w=900&q=82",
                "https://images.unsplash.com/photo-1504917595217-d4dc5ebe6122?auto=format&fit=crop&w=900&q=82",
            )
        }
        return images[index % images.size]
    }

    private val products: List<Product> = rawProducts.mapIndexed { index, raw ->
        Product(
            id = "prd-${raw.sku.lowercase().replace(Regex("[^a-z0-9]+"), "-")}",
            sku = raw.sku,
            name = raw.name,
            brandId = raw.brand,
            categoryId = raw.category,
            description = "${raw.name} for industrial maintenance, OEM and workshop applications.",
            primarySpec = raw.spec,
            packSize = raw.pack,
            unit = if (raw.pack.contains("set")) "set" else "pcs",
            moq = raw.pack.substringBefore(" ").toIntOrNull() ?: 1,
            stockState = when {
                raw.available <= 8 -> StockState.ON_REQUEST
                raw.available <= 24 -> StockState.LIMITED
                else -> StockState.IN_STOCK
            },
            availableQty = raw.available,
            warehouse = listOf("Pune Main", "Bhosari", "Chakan")[index % 3],
            imageUrl = imageForCategory(raw.category, index),
        )
    }

    private val dealers = listOf(
        Dealer("dealer-shree-tools", "Shree Tools & Bearings", "Amit Shah", "+91 98220 11023", "amit@shreetools.in", "27ABCDE1234F1Z5", "Pune", "Maharashtra", "sales-ravi", "30 days", "Gold", 350000.0, 82000.0),
        Dealer("dealer-02", "Metro Industrial Traders", "Nilesh Patil", "+91 98220 11812", "nilesh@metroindustrial.in", "27ABCDF2345G1Z3", "Nashik", "Maharashtra", "sales-neha", "21 days", "Silver", 225000.0, 41000.0),
        Dealer("dealer-03", "Vardhman Engineering Stores", "Rohit Jain", "+91 98900 44152", "rohit@vardhmaneng.in", "27ABCDG3456H1Z9", "Aurangabad", "Maharashtra", "sales-neha", "30 days", "Gold", 300000.0, 126000.0),
        Dealer("dealer-04", "Om Sai Machinery", "Ganesh Pawar", "+91 97677 66821", "ganesh@omsaimachinery.in", "27ABCDH4567J1Z1", "Kolhapur", "Maharashtra", "sales-ravi", "15 days", "Silver", 180000.0, 39000.0),
        Dealer("dealer-05", "Rajdeep Electricals", "Kunal Desai", "+91 98980 11987", "kunal@rajdeep.in", "24ABCDJ5678K1Z4", "Surat", "Gujarat", "sales-arjun", "30 days", "Gold", 400000.0, 76000.0),
        Dealer("dealer-06", "Kaveri Tools Centre", "Sanjay Rao", "+91 98270 44771", "sanjay@kaveritools.in", "23ABCDK6789L1Z2", "Indore", "Madhya Pradesh", "sales-priya", "21 days", "Silver", 200000.0, 28000.0),
        Dealer("dealer-07", "Delhi Motor & Tools", "Aman Khanna", "+91 98100 99218", "aman@delhimotor.in", "07ABCDL7890M1Z8", "Delhi", "Delhi", "sales-priya", "30 days", "Gold", 450000.0, 161000.0),
        Dealer("dealer-08", "Patna Mill Stores", "Ritesh Kumar", "+91 98350 66712", "ritesh@patnamill.in", "10ABCDM8901N1Z6", "Patna", "Bihar", "sales-priya", "15 days", "Silver", 150000.0, 22000.0),
    )

    private fun line(productIndex: Int, quantity: Int): RequestLine {
        val p = products[productIndex]
        return RequestLine(p.id, p.sku, p.name, quantity, p.unit)
    }

    private val requests = listOf(
        PriceRequest("rfq-001", "RFQ-2608-1001", "dealer-shree-tools", "sales-ravi", RequestStatus.QUOTE_SENT, "29 Aug 2026", "Pune", "PO-AUG-118", "Need dispatch before month end.", listOf(line(0, 50), line(8, 20), line(17, 4)), "25 Aug · 10:14", "25 Aug · 15:30"),
        PriceRequest("rfq-002", "RFQ-2608-1002", "dealer-02", "sales-neha", RequestStatus.UNDER_REVIEW, "30 Aug 2026", "Nashik", "NIL-293", "Please quote best project rate.", listOf(line(3, 30), line(14, 200)), "25 Aug · 11:02", "25 Aug · 14:11"),
        PriceRequest("rfq-003", "RFQ-2608-1003", "dealer-03", "sales-neha", RequestStatus.SUBMITTED, "02 Sep 2026", "Aurangabad", "VE-771", "Routine restock.", listOf(line(6, 25), line(21, 6)), "25 Aug · 12:20", "25 Aug · 12:20"),
        PriceRequest("rfq-004", "RFQ-2608-1004", "dealer-04", "sales-ravi", RequestStatus.CONFIRMED, "27 Aug 2026", "Kolhapur", "OSM-918", "Urgent production line requirement.", listOf(line(2, 40), line(24, 50)), "24 Aug · 09:18", "25 Aug · 09:55"),
        PriceRequest("rfq-005", "RFQ-2608-1005", "dealer-05", "sales-arjun", RequestStatus.AWAITING_CONFIRMATION, "31 Aug 2026", "Surat", "RDE-504", "Need freight included.", listOf(line(18, 12), line(19, 6)), "24 Aug · 13:44", "25 Aug · 10:41"),
        PriceRequest("rfq-006", "RFQ-2608-1006", "dealer-06", "sales-priya", RequestStatus.CLOSED, "23 Aug 2026", "Indore", "KTC-090", "Completed repeat requirement.", listOf(line(26, 4), line(27, 2)), "18 Aug · 15:12", "23 Aug · 18:04"),
    )

    private fun quoteLine(productIndex: Int, quantity: Int, rate: Double, discount: Double, gst: Double): QuoteLine {
        val p = products[productIndex]
        return QuoteLine(p.id, p.name, quantity, p.unit, rate, discount, gst)
    }

    private val quotations = listOf(
        Quotation("q-001", "QTN-2608-2401", "rfq-001", "dealer-shree-tools", "25 Aug 2026", "01 Sep 2026", 950.0, "30 days from invoice", "2–3 working days ex Pune", "Rates valid for quoted quantity.", listOf(quoteLine(0, 50, 248.0, 5.0, 18.0), quoteLine(8, 20, 610.0, 4.0, 18.0), quoteLine(17, 4, 1780.0, 6.0, 18.0)), QuoteStatus.SENT),
        Quotation("q-004", "QTN-2608-2394", "rfq-004", "dealer-04", "24 Aug 2026", "31 Aug 2026", 650.0, "15 days from invoice", "Ready stock; dispatch next business day", "Confirmed by dealer.", listOf(quoteLine(2, 40, 295.0, 3.0, 18.0), quoteLine(24, 50, 82.0, 0.0, 18.0)), QuoteStatus.ACCEPTED),
        Quotation("q-005", "QTN-2608-2398", "rfq-005", "dealer-05", "25 Aug 2026", "30 Aug 2026", 1250.0, "30 days from invoice", "3–4 working days", "Freight included as shown.", listOf(quoteLine(18, 12, 2890.0, 7.0, 18.0), quoteLine(19, 6, 4120.0, 8.0, 18.0)), QuoteStatus.SENT),
        Quotation("q-006", "QTN-2608-2352", "rfq-006", "dealer-06", "19 Aug 2026", "26 Aug 2026", 500.0, "21 days from invoice", "2 working days", "Delivered.", listOf(quoteLine(26, 4, 1375.0, 5.0, 18.0), quoteLine(27, 2, 2140.0, 5.0, 18.0)), QuoteStatus.ACCEPTED),
    )

    private val orders = listOf(
        Order("ord-004", "ORD-2608-3401", "q-004", "rfq-004", "dealer-04", OrderStatus.PROCESSING, PaymentStatus.CREDIT, 18482.0, "", "25 Aug · 10:02"),
        Order("ord-006", "ORD-2608-3368", "q-006", "rfq-006", "dealer-06", OrderStatus.DELIVERED, PaymentStatus.PAID, 11064.0, "VRL/IND/8239001", "20 Aug · 09:40"),
        Order("ord-old-1", "ORD-2607-3188", "q-001", "rfq-001", "dealer-shree-tools", OrderStatus.DELIVERED, PaymentStatus.PAID, 48780.0, "TCI/PNQ/770121", "29 Jul · 12:14"),
    )

    private val activities = listOf(
        ActivityEvent("act-1", "Quotation sent", "QTN-2608-2401 sent to Shree Tools & Bearings", "Ravi Mehta", "quote", "q-001", "25 Aug · 15:30"),
        ActivityEvent("act-2", "Request assigned", "RFQ-2608-1002 assigned to Neha Kulkarni", "Operations Admin", "request", "rfq-002", "25 Aug · 14:11"),
        ActivityEvent("act-3", "Order processing", "ORD-2608-3401 moved to processing", "Ravi Mehta", "order", "ord-004", "25 Aug · 10:18"),
        ActivityEvent("act-4", "Dealer quote accepted", "Om Sai Machinery accepted QTN-2608-2394", "Ganesh Pawar", "quote", "q-004", "25 Aug · 09:55"),
    )

    fun snapshot() = AppSnapshot(
        categories = categories,
        brands = brands,
        salespeople = salespeople,
        products = products,
        dealers = dealers,
        requests = requests,
        quotations = quotations,
        orders = orders,
        activities = activities,
        draftItems = emptyList(),
        draftDeliveryCity = "Pune",
        draftRequiredBy = "",
        draftBuyerReference = "",
        draftNote = "",
    )
}
