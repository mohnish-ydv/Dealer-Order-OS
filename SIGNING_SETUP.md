# Apex DMS signing identity

Package: `com.apex.dms`

Expected release SHA1:
`56:26:3F:51:95:FE:56:67:8D:79:7D:F9:CA:C8:47:4F:1D:CC:46:C2`

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
