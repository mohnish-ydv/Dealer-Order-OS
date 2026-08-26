const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
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

async function sessionUser(req: Request) {
  const raw = req.headers.get("authorization")?.replace(/^Bearer\s+/i, "") || "";
  if (!raw || raw.startsWith("sb_publishable_") || raw.split(".").length === 3) return null;
  const hash = await sha256Hex(raw);
  const sessions = await rest(`app_sessions?token_hash=eq.${encodeURIComponent(hash)}&expires_at=gt.${encodeURIComponent(new Date().toISOString())}&select=user_id,expires_at&limit=1`);
  if (!sessions?.[0]?.user_id) return null;
  await rest(`app_sessions?token_hash=eq.${encodeURIComponent(hash)}`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ last_seen_at: new Date().toISOString() }) });
  const users = await rest(`app_users?id=eq.${sessions[0].user_id}&select=*&limit=1`);
  return users?.[0] || null;
}

function automationFor(type: string) {
  if (type === "rfq.created") return ["rfq.intake", "RFQ normalized and routed"];
  if (type === "quote.approval_requested") return ["approval.quote", "High-value quote paused for manager approval"];
  if (type === "quote.sent") return ["quote.notify", "Quotation notification and follow-up scheduled"];
  if (type === "quote.accepted") return ["order.create", "Accepted quotation converted into order"];
  if (type === "payment.paid") return ["payment.verify", "Payment event recorded; downstream sync queued"];
  if (type === "inventory.low") return ["inventory.low", "Low-stock exception surfaced"];
  if (type === "profile.updated") return ["profile.sync", "Dealer profile synchronized"];
  return ["event.router", `Event ${type} received`];
}

Deno.serve(async (req) => {
  if (req.method !== "POST") return new Response(JSON.stringify({ error: "POST required" }), { status: 405, headers: jsonHeaders });
  try {
    const user = await sessionUser(req);
    if (!user) return new Response(JSON.stringify({ error: "Invalid or expired app session" }), { status: 401, headers: jsonHeaders });
    const body = await req.json();
    const action = String(body.action || "");

    if (action === "products") {
      const products = await rest("products?active=eq.true&select=*&order=name.asc");
      return new Response(JSON.stringify({ products }), { headers: jsonHeaders });
    }

    if (action === "profile_update") {
      const p = body.profile || {};
      const update = {
        business_name: String(p.businessName || "").trim(),
        contact_name: String(p.contactName || "").trim(),
        phone: String(p.phone || "").trim(),
        email: String(p.email || "").trim(),
        gstin: String(p.gstin || "").trim().toUpperCase(),
        city: String(p.city || "").trim(),
        state: String(p.state || "").trim(),
        updated_at: new Date().toISOString(),
      };
      const profiles = await rest(`dealer_profiles?user_id=eq.${user.id}`, {
        method: "PATCH",
        headers: { Prefer: "return=representation" },
        body: JSON.stringify(update),
      });
      const dealer = profiles?.[0] || update;
      await rest(`app_users?id=eq.${user.id}`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ phone: update.phone || user.phone, email: update.email, updated_at: new Date().toISOString() }) });
      return new Response(JSON.stringify({
        sessionToken: req.headers.get("authorization")?.replace(/^Bearer\s+/i, "") || "",
        user: {
          userId: user.id, role: user.role, phone: update.phone || user.phone || "", givenName: user.given_name || "", familyName: user.family_name || "", email: update.email || user.email || "", pictureUrl: user.picture_url || "",
          businessName: dealer.business_name || "", gstin: dealer.gstin || "", city: dealer.city || "", state: dealer.state || "",
        },
      }), { headers: jsonHeaders });
    }

    if (action === "entity_sync") {
      const entityType = String(body.entityType || "");
      const e = body.entity || {};
      if (!e.id) return new Response(JSON.stringify({ error: "Entity id required" }), { status: 400, headers: jsonHeaders });
      if (entityType === "rfq") {
        await rest("rfqs?on_conflict=id", {
          method: "POST", headers: { Prefer: "resolution=merge-duplicates,return=minimal" },
          body: JSON.stringify({
            id: String(e.id), user_id: user.id, dealer_id: String(e.dealerId || ""), reference: String(e.reference || e.id),
            salesperson_id: e.salespersonId || null, status: String(e.status || "SUBMITTED"), required_by: String(e.requiredBy || ""),
            delivery_city: String(e.deliveryCity || ""), buyer_reference: String(e.buyerReference || ""), note: String(e.note || ""),
            payload: e, updated_at: new Date().toISOString(),
          }),
        });
      } else if (entityType === "quote") {
        await rest("quotes?on_conflict=id", {
          method: "POST", headers: { Prefer: "resolution=merge-duplicates,return=minimal" },
          body: JSON.stringify({
            id: String(e.id), user_id: user.id, dealer_id: String(e.dealerId || ""), request_id: String(e.requestId || ""),
            quote_number: String(e.quoteNumber || e.id), status: String(e.status || "DRAFT"), approval_status: String(e.approvalStatus || "NOT_REQUIRED"),
            grand_total: Number(e.grandTotal || 0), payload: e, updated_at: new Date().toISOString(),
          }),
        });
      } else if (entityType === "order") {
        await rest("orders?on_conflict=id", {
          method: "POST", headers: { Prefer: "resolution=merge-duplicates,return=minimal" },
          body: JSON.stringify({
            id: String(e.id), user_id: user.id, dealer_id: String(e.dealerId || ""), request_id: String(e.requestId || ""), quote_id: String(e.quoteId || ""),
            order_number: String(e.orderNumber || e.id), status: String(e.status || "CONFIRMED"), payment_status: String(e.paymentStatus || "PENDING"),
            total: Number(e.total || 0), dispatch_reference: String(e.dispatchReference || ""), payload: e, updated_at: new Date().toISOString(),
          }),
        });
      } else {
        return new Response(JSON.stringify({ error: "Unsupported entity type" }), { status: 400, headers: jsonHeaders });
      }
      return new Response(JSON.stringify({ ok: true }), { headers: jsonHeaders });
    }

    if (action === "event") {
      const type = String(body.type || "");
      const entityType = String(body.entityType || "");
      const entityId = String(body.entityId || "");
      const clientEventId = String(body.clientEventId || "");
      if (!type || !entityType || !entityId || !clientEventId) return new Response(JSON.stringify({ error: "Invalid event" }), { status: 400, headers: jsonHeaders });
      const events = await rest("business_events?on_conflict=client_event_id", {
        method: "POST",
        headers: { Prefer: "resolution=ignore-duplicates,return=representation" },
        body: JSON.stringify({ client_event_id: clientEventId, user_id: user.id, type, entity_type: entityType, entity_id: entityId, payload: body.payload || {}, sync_status: "received" }),
      });
      const event = events?.[0];
      if (event?.id) {
        const [workflow, summary] = automationFor(type);
        await rest("automation_runs", { method: "POST", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ event_id: event.id, workflow_key: workflow, status: "SUCCESS", summary, details: { source: "android-m2" } }) });
      }
      return new Response(JSON.stringify({ ok: true, duplicate: !event }), { headers: jsonHeaders });
    }

    return new Response(JSON.stringify({ error: "Unknown action" }), { status: 400, headers: jsonHeaders });
  } catch (error) {
    console.error(error);
    return new Response(JSON.stringify({ error: error instanceof Error ? error.message : "BAOS API failed" }), { status: 500, headers: jsonHeaders });
  }
});
