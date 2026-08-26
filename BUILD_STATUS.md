# Build Status

## Completed
- Native Android source upgraded to BAOS M2
- `com.apex.dms` retained as release application ID
- Truecaller OAuth SDK 3.2.1 integration added
- onboarding + login/register journey added
- editable dealer/business profile added
- Supabase backend connected and deployed
- remote product feed with real photo URLs added
- RFQ -> quote -> high-value approval -> order state machine added
- simulated payment/Tally/WhatsApp automation path added
- Automation Center + activity + owner digest added
- offline event outbox added
- n8n modular workflow export pack added
- release signing fingerprint gate added
- reproducible Supabase migration added

## Backend verification
Supabase `Dealer-Order-OS` is active with BAOS tables, seeded products and active Edge Functions.

## Local validation
- Java/Kotlin available
- domain Kotlin compilation: PASS
- release keystore SHA1: PASS
- full Android APK compilation cannot run inside this container because Android SDK/Gradle dependency resolution is unavailable.

## Authoritative release gate
`.github/workflows/android.yml`

A release build hard-fails if signing secrets are missing or the final APK certificate does not equal:
`D4:1D:9F:BA:97:B0:74:C1:D4:74:B4:0B:4E:CD:EB:A0:4C:C2:76:1B`
