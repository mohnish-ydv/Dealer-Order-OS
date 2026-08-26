# BAOS n8n workflow pack

Import each JSON into the client's own n8n instance. These are modular M2 starter workflows, not the source of truth for orders, inventory, pricing, payments or permissions.

Recommended event mapping:
- `rfq.created` -> 01 RFQ Intake
- `quote.approval_requested` -> 02 Quote Approval
- `quote.sent` -> 03 Quote Notification + 04 Quote Follow-up
- `quote.accepted` -> 05 Order and Payment
- `payment.paid` -> 05 Order and Payment + 07 Tally Sync Simulator
- `inventory.low` -> 06 Inventory Alerts
- `profile.updated` -> 09 Profile Sync
- scheduled -> 08 Owner Daily Digest
- workflow errors -> 99 Global Error Handler

The Android app already simulates these automations locally for sales demos. Production provider adapters can later be wired to Razorpay/WhatsApp/Tally without changing the app's core state machine.
