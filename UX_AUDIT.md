# UX rebuild audit

## What was removed from the old web direction

- desktop-first dashboard density on dealer screens
- card-on-card-on-card layouts
- six-item bottom navigation
- RFQ jargon as the primary dealer label
- duplicated admin/dealer visual language
- oversized filters and dense status blocks
- generic responsive-web spacing that felt like a prototype in a phone browser

## New interaction model

### Dealer
The dealer sees only five persistent destinations: Home, Shop, Requests, Orders and Profile. Quotations live inside Requests instead of adding another permanent navigation destination. “RFQ” is translated to “Price request” in the dealer experience.

The main commerce journey has one obvious action per screen:

1. Find product
2. Request your price
3. Review request
4. Review quotation
5. Accept / request changes
6. Track order
7. Repeat order

### Staff
Staff gets five mobile destinations: Home, Requests, Orders, Data and More. Secondary modules are intentionally moved under More instead of competing in the primary navigation.

## Shoppe-derived design decisions

- bright saturated blue as the single primary accent
- white surfaces on an almost-white background
- large calm headings
- pill filters
- circular category discovery
- generous image/artwork area before product metadata
- two-column product browsing
- low-elevation rounded cards
- compact fixed bottom navigation
- progressive disclosure: advanced operational controls appear only on detail screens
- full-width primary CTA for the next business action

## DMS-specific adaptations

This is not a consumer cart/checkout clone. Price visibility is intentionally replaced by MOQ, stock signal and structured quotation flow. No fake public checkout or immediate card payment is shown.

## Responsive/native conclusion

The app is native Android and no longer relies on browser breakpoints. Layout uses Compose constraints and scrolling containers designed around handset screens.
