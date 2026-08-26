# Apex DMS signing identity

Package: `com.apex.dms`

Expected release SHA1:
`D4:1D:9F:BA:97:B0:74:C1:D4:74:B4:0B:4E:CD:EB:A0:4C:C2:76:1B`

Expected SHA256:
`18:4F:6C:8F:56:77:17:78:22:86:D8:0F:50:50:D8:3E:4D:46:D5:6F:30:54:83:5A:E5:A8:0D:5B:37:17:33:D4`

Update the Truecaller Android credential to package `com.apex.dms` and the SHA1 above before testing Truecaller login.

## GitHub Actions secrets

Add these repository secrets:

- `APEX_KEYSTORE_BASE64` — base64 of `apex-dms-release.jks`
- `APEX_KEYSTORE_PASSWORD`
- `APEX_KEY_ALIAS` — `apex_dms_release`
- `APEX_KEY_PASSWORD`

On Termux/Linux, generate the base64 value with:

```bash
base64 -w 0 apex-dms-release.jks
```

The workflow hard-fails if the keystore fingerprint or final APK signature is not the expected SHA1.

## Local signed build

Copy `keystore.properties.example` to `keystore.properties`, fill it with the supplied signing values, and run:

```bash
gradle :app:assembleRelease
```

Do not commit `keystore.properties` or the JKS to a public repository.
