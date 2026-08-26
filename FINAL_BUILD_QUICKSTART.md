# Apex DMS / BAOS M2 — Final Build Quickstart

## Already live
- Supabase project: `Dealer-Order-OS`
- Project ref: `tzofttljxfkokunckqfy`
- Backend URL compiled into the app
- `truecaller-auth` Edge Function deployed
- `baos-api` Edge Function deployed
- BAOS schema, product feed and integration status deployed

## Truecaller credential
Use this release credential in the Truecaller Android project:

- Package: `com.apex.dms`
- SHA1: `56:26:3F:51:95:FE:56:67:8D:79:7D:F9:CA:C8:47:4F:1D:CC:46:C2`
- Client ID is already in `app/src/main/res/values/strings.xml`

Truecaller will not authenticate a build whose package/signing fingerprint does not match the portal credential.

## Release signing
The source ZIP intentionally contains no private JKS/passwords. Keep the separate PRIVATE signing package safe.

GitHub Actions expects these secrets:
- `APEX_KEYSTORE_BASE64`
- `APEX_KEYSTORE_PASSWORD`
- `APEX_KEY_ALIAS`
- `APEX_KEY_PASSWORD`

The workflow verifies the JKS fingerprint before building and verifies the final APK certificate after building. A wrong key hard-fails the workflow.

## CI output
Push the source to a GitHub repository and configure the four secrets above. On a push to `main` or manual workflow run, `.github/workflows/android.yml` performs:

1. unit tests
2. Android lint
3. debug compile validation
4. signed release APK build
5. APK certificate SHA1 verification
6. artifact upload

Expected release artifact name:
`Apex-DMS-BAOS-M2-Signed-Release`

## Demo journey
Introduction -> Truecaller Login/Register -> Dealer Home -> Catalogue -> Smart Enquiry/RFQ -> Staff RFQ -> Quote -> manager approval for >= ₹1L -> Dealer accepts -> Order -> BAOS Demo Payment -> inventory/Tally/notification automation -> Automation Center / Owner Digest.
