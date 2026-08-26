package com.apex.dms.backend

import com.apex.dms.BuildConfig
import com.apex.dms.data.ActorRole
import com.apex.dms.data.AuthUserProfile
import com.apex.dms.data.Product
import com.apex.dms.data.StockState
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class BackendClient {
    private val gson = Gson()
    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val apiKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    suspend fun exchangeTruecallerAuth(
        authorizationCode: String,
        codeVerifier: String,
        state: String,
    ): Result<AuthUserProfile> = runCatching {
        val body = mapOf(
            "authorizationCode" to authorizationCode,
            "codeVerifier" to codeVerifier,
            "state" to state,
        )
        val json = request("truecaller-auth", body, null)
        parseAuthProfile(json)
    }

    suspend fun fetchProducts(sessionToken: String): Result<List<Product>> = runCatching {
        val json = request("baos-api", mapOf("action" to "products"), sessionToken)
        val array = json.getAsJsonArray("products") ?: return@runCatching emptyList()
        array.mapNotNull { element ->
            runCatching {
                val p = element.asJsonObject
                Product(
                    id = p.string("id"),
                    sku = p.string("sku"),
                    name = p.string("name"),
                    brandId = p.string("brand_id"),
                    categoryId = p.string("category_id"),
                    description = p.string("description"),
                    primarySpec = p.string("primary_spec"),
                    packSize = p.string("pack_size"),
                    unit = p.string("unit", "pcs"),
                    moq = p.int("moq", 1),
                    stockState = runCatching { StockState.valueOf(p.string("stock_state", "IN_STOCK")) }.getOrDefault(StockState.IN_STOCK),
                    availableQty = p.int("available_qty", 0),
                    warehouse = p.string("warehouse", "Pune Main"),
                    active = p.boolean("active", true),
                    imageUrl = p.string("image_url"),
                )
            }.getOrNull()
        }
    }

    suspend fun updateProfile(
        sessionToken: String,
        fields: Map<String, String>,
    ): Result<AuthUserProfile> = runCatching {
        val json = request(
            "baos-api",
            mapOf("action" to "profile_update", "profile" to fields),
            sessionToken,
        )
        parseAuthProfile(json)
    }

    suspend fun syncEntity(
        sessionToken: String,
        entityType: String,
        entity: Map<String, Any?>,
    ): Result<Unit> = runCatching {
        request(
            "baos-api",
            mapOf("action" to "entity_sync", "entityType" to entityType, "entity" to entity),
            sessionToken,
        )
        Unit
    }

    suspend fun emitEvent(
        sessionToken: String,
        clientEventId: String,
        type: String,
        entityType: String,
        entityId: String,
        payload: Map<String, Any?>,
    ): Result<Unit> = runCatching {
        request(
            "baos-api",
            mapOf(
                "action" to "event",
                "clientEventId" to clientEventId,
                "type" to type,
                "entityType" to entityType,
                "entityId" to entityId,
                "payload" to payload,
            ),
            sessionToken,
        )
        Unit
    }

    private suspend fun request(
        function: String,
        body: Any,
        sessionToken: String?,
    ): JsonObject = withContext(Dispatchers.IO) {
        val connection = (URL("$baseUrl/functions/v1/$function").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer ${sessionToken ?: apiKey}")
        }
        connection.outputStream.bufferedWriter().use { it.write(gson.toJson(body)) }
        val code = connection.responseCode
        val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()
        if (code !in 200..299) error("Backend $code: ${raw.take(300)}")
        JsonParser.parseString(raw.ifBlank { "{}" }).asJsonObject
    }

    private fun parseAuthProfile(json: JsonObject): AuthUserProfile {
        val user = json.getAsJsonObject("user") ?: json
        return AuthUserProfile(
            userId = user.string("userId", user.string("id")),
            sessionToken = json.string("sessionToken", user.string("sessionToken")),
            role = runCatching { ActorRole.valueOf(user.string("role", "DEALER")) }.getOrDefault(ActorRole.DEALER),
            phone = user.string("phone"),
            givenName = user.string("givenName", user.string("given_name")),
            familyName = user.string("familyName", user.string("family_name")),
            email = user.string("email"),
            pictureUrl = user.string("pictureUrl", user.string("picture_url")),
            businessName = user.string("businessName", user.string("business_name")),
            gstin = user.string("gstin"),
            city = user.string("city"),
            state = user.string("state"),
        )
    }
}

private fun JsonObject.string(key: String, fallback: String = ""): String =
    get(key)?.takeUnless { it.isJsonNull }?.asString ?: fallback

private fun JsonObject.int(key: String, fallback: Int = 0): Int =
    get(key)?.takeUnless { it.isJsonNull }?.asInt ?: fallback

private fun JsonObject.boolean(key: String, fallback: Boolean = false): Boolean =
    get(key)?.takeUnless { it.isJsonNull }?.asBoolean ?: fallback
