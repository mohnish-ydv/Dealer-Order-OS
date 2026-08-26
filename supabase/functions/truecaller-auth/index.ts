const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const TRUECALLER_CLIENT_ID = "ones8dwiwzvi6m-xjmsuyqjzpufhlolnvban9etrray";

const jsonHeaders = { "content-type": "application/json" };

async function sha256Hex(value: string) {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(value));
  return [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function rest(path: string, init: RequestInit = {}) {
  const res = await fetch(`${SUPABASE_URL}/rest/v1/${path}`, {
    ...init,
    headers: {
      apikey: SERVICE_ROLE,
      authorization: `Bearer ${SERVICE_ROLE}`,
      "content-type": "application/json",
      ...(init.headers || {}),
    },
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`Database ${res.status}: ${text.slice(0, 500)}`);
  return text ? JSON.parse(text) : null;
}

Deno.serve(async (req) => {
  if (req.method !== "POST") return new Response(JSON.stringify({ error: "POST required" }), { status: 405, headers: jsonHeaders });
  try {
    const body = await req.json();
    const authorizationCode = String(body.authorizationCode || "");
    const codeVerifier = String(body.codeVerifier || "");
    const state = String(body.state || "");
    if (!authorizationCode || !codeVerifier || !state) {
      return new Response(JSON.stringify({ error: "Missing OAuth parameters" }), { status: 400, headers: jsonHeaders });
    }

    const form = new URLSearchParams({
      grant_type: "authorization_code",
      client_id: TRUECALLER_CLIENT_ID,
      code: authorizationCode,
      code_verifier: codeVerifier,
    });
    const tokenRes = await fetch("https://oauth-account-noneu.truecaller.com/v1/token", {
      method: "POST",
      headers: { "content-type": "application/x-www-form-urlencoded" },
      body: form,
    });
    const tokenText = await tokenRes.text();
    if (!tokenRes.ok) {
      return new Response(JSON.stringify({ error: "Truecaller token exchange failed", detail: tokenText.slice(0, 400) }), { status: 401, headers: jsonHeaders });
    }
    const token = JSON.parse(tokenText);
    const profileRes = await fetch("https://oauth-account-noneu.truecaller.com/v1/userinfo", {
      headers: { authorization: `Bearer ${token.access_token}` },
    });
    const profileText = await profileRes.text();
    if (!profileRes.ok) {
      return new Response(JSON.stringify({ error: "Truecaller profile fetch failed", detail: profileText.slice(0, 400) }), { status: 401, headers: jsonHeaders });
    }
    const tc = JSON.parse(profileText);
    if (!tc.sub || !tc.phone_number) {
      return new Response(JSON.stringify({ error: "Verified Truecaller profile missing required identity fields" }), { status: 422, headers: jsonHeaders });
    }

    const appUsers = await rest("app_users?on_conflict=truecaller_sub", {
      method: "POST",
      headers: { Prefer: "resolution=merge-duplicates,return=representation" },
      body: JSON.stringify({
        truecaller_sub: String(tc.sub),
        phone: String(tc.phone_number || ""),
        given_name: String(tc.given_name || ""),
        family_name: String(tc.family_name || ""),
        email: String(tc.email || ""),
        picture_url: String(tc.picture || ""),
        phone_verified: Boolean(tc.phone_number_verified),
        updated_at: new Date().toISOString(),
      }),
    });
    const user = appUsers?.[0];
    if (!user?.id) throw new Error("Unable to create app user");

    const locality = String(tc.address?.locality || "");
    const profiles = await rest("dealer_profiles?on_conflict=user_id", {
      method: "POST",
      headers: { Prefer: "resolution=merge-duplicates,return=representation" },
      body: JSON.stringify({
        user_id: user.id,
        business_name: "",
        contact_name: [tc.given_name, tc.family_name].filter(Boolean).join(" "),
        phone: String(tc.phone_number || ""),
        email: String(tc.email || ""),
        city: locality,
        photo_url: String(tc.picture || ""),
        updated_at: new Date().toISOString(),
      }),
    });
    const dealer = profiles?.[0] || {};

    const sessionToken = `${crypto.randomUUID()}-${crypto.randomUUID()}`;
    const tokenHash = await sha256Hex(sessionToken);
    const expires = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
    await rest("app_sessions", {
      method: "POST",
      headers: { Prefer: "return=minimal" },
      body: JSON.stringify({ token_hash: tokenHash, user_id: user.id, expires_at: expires }),
    });

    const response = {
      sessionToken,
      user: {
        userId: user.id,
        role: user.role || "DEALER",
        phone: user.phone || tc.phone_number || "",
        givenName: user.given_name || tc.given_name || "",
        familyName: user.family_name || tc.family_name || "",
        email: user.email || tc.email || "",
        pictureUrl: user.picture_url || tc.picture || "",
        businessName: dealer.business_name || "",
        gstin: dealer.gstin || "",
        city: dealer.city || locality,
        state: dealer.state || "",
      },
    };
    return new Response(JSON.stringify(response), { status: 200, headers: jsonHeaders });
  } catch (error) {
    console.error(error);
    return new Response(JSON.stringify({ error: error instanceof Error ? error.message : "Authentication failed" }), { status: 500, headers: jsonHeaders });
  }
});
