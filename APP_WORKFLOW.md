# Apex DMS Native — App Workflow

## Product direction
Apex DMS is now a native Android dealer-management and B2B ordering app. The old web dashboard mental model is intentionally removed.

The UI follows a Shoppe-inspired mobile grammar: one primary task per screen, white/blue palette, rounded surfaces, image-led product discovery, compact bottom navigation, plain-language labels and progressive disclosure.

## Dealer journey
1. **Home** — current commercial snapshot, shortcuts and recent activity.
2. **Shop** — search, categories and a two-column industrial product catalogue.
3. **Product detail** — specifications, availability/MOQ and one primary CTA: **Request your price**.
4. **Price request** — review selected products, quantities, delivery city, date/reference and note.
5. **Submit** — creates a structured price request and makes it visible to staff.
6. **Requests** — track submitted/in-review/quoted requests without exposing internal RFQ terminology.
7. **Quotation** — inspect rates, discount, GST, freight, validity and commercial terms.
8. **Decision** — accept the quotation or request a revision.
9. **Order** — accepted quote creates one order; duplicate quote-to-order conversion is protected in the store logic.
10. **Tracking** — confirmed → processing → ready to dispatch → dispatched → delivered.
11. **Repeat order** — past-order lines can be returned to a new editable price-request basket.
12. **Profile** — credit/outstanding context, salesperson, account shortcuts and role/impersonation exit.

## Staff journey
1. **Home** — requests needing attention, quote/order metrics and actionable recent work.
2. **Requests** — tabs for price requests and quotations; salesperson role is scoped to assigned requests.
3. **Request detail** — dealer context, line items, assignment and valid workflow progress.
4. **Quote builder** — deterministic commercial math with editable rates/discounts/freight/terms.
5. **Send quote** — quote becomes dealer-visible and request state is synchronized.
6. **Orders** — fulfilment queue, payment state and dispatch controls.
7. **Data** — Dealers / Products / Inventory in one compact mobile workspace.
8. **Dealer detail** — commercial history and Super Admin dealer-portal impersonation.
9. **Product detail** — catalogue visibility and inventory controls.
10. **More** — Reports, Sales Team, Activity, Integrations and Settings.

## Roles
- **Super Admin** — full data and workflow access; can view the app as a dealer.
- **Admin** — operational workspace.
- **Salesperson** — assigned commercial requests.
- **Dealer** — catalogue, price requests, quotations, orders and account journey.

## State machines
### Price request
Draft → Submitted → In review → Quote ready → Quote sent → Awaiting confirmation → Confirmed → Closed

Cancellation is available only through the allowed transition map.

### Order
Confirmed → Processing → Ready to dispatch → Dispatched → Delivered

A dispatch reference is required before the app advances to Dispatched.
