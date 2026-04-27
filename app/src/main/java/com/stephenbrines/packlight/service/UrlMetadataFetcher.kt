package com.stephenbrines.packlight.service

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class GearMetadata(
    val name: String,
    val weightGrams: Double?,
    val rawWeightString: String?,
    val sourceUrl: String,
)

@Singleton
class UrlMetadataFetcher @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            chain.proceed(req)
        }
        .build()

    private val shopifyHosts = listOf(
        "zpacks.com", "gossamergear.com", "ula-equipment.com",
        "mountainlaureldesigns.com", "sixmoondesigns.com",
        "tarptent.com", "hyperlitemountaingear.com",
    )

    suspend fun fetch(urlString: String): Result<GearMetadata> = runCatching {
        val host = java.net.URL(urlString).host.lowercase()
        when {
            shopifyHosts.any { host.contains(it) } -> fetchShopify(urlString)
            host.contains("rei.com") -> fetchREI(urlString)
            host.contains("backcountry.com") -> fetchBackcountry(urlString)
            else -> fetchGeneric(urlString)
        }
    }

    // Shopify: use the public /products/{slug}.json endpoint
    private fun fetchShopify(urlString: String): GearMetadata {
        val jsonUrl = toShopifyJsonUrl(urlString)
        if (jsonUrl != null) {
            val body = get(jsonUrl)
            if (body != null) {
                try {
                    val product = JSONObject(body).getJSONObject("product")
                    val name = product.optString("title", "")
                    if (name.isNotBlank()) {
                        // Try variant weight field
                        val variants = product.optJSONArray("variants")
                        val firstVariant = variants?.optJSONObject(0)
                        val variantWeight = firstVariant?.optDouble("weight", 0.0) ?: 0.0
                        val variantUnit = firstVariant?.optString("weight_unit", "") ?: ""
                        if (variantWeight > 0 && variantUnit.isNotBlank()) {
                            return GearMetadata(name, convertToGrams(variantWeight, variantUnit),
                                "$variantWeight $variantUnit", urlString)
                        }
                        // Fall back to description text
                        val bodyHtml = product.optString("body_html", "")
                        val (raw, grams) = extractWeight(bodyHtml)
                        return GearMetadata(name, grams, raw, urlString)
                    }
                } catch (_: Exception) { /* fall through */ }
            }
        }
        return fetchGeneric(urlString)
    }

    private fun fetchREI(urlString: String): GearMetadata {
        val html = get(urlString) ?: return GearMetadata("", null, null, urlString)
        val doc = Jsoup.parse(html)
        var name: String? = null
        var weightGrams: Double? = null
        var rawWeight: String? = null

        // JSON-LD for name
        doc.select("script[type='application/ld+json']").forEach { script ->
            try {
                val json = JSONObject(script.html())
                if (json.optString("@type") == "Product") {
                    name = json.optString("name")
                }
            } catch (_: Exception) { }
        }

        // Specs table for weight
        val weightLabels = listOf("Trail Weight", "Pack Weight", "Weight", "Minimum Weight")
        outer@ for (el in doc.select("[data-ui='product-specs'] li, [class*='spec'] li, dl dt")) {
            val text = el.text()
            for (label in weightLabels) {
                if (text.contains(label, ignoreCase = true)) {
                    val valueEl = el.nextElementSibling()
                    val candidate = valueEl?.text() ?: text
                    val (raw, g) = extractWeight(candidate)
                    if (g != null) { rawWeight = raw; weightGrams = g; break@outer }
                }
            }
        }

        if (name == null) name = doc.select("meta[property='og:title']").attr("content").ifBlank { doc.title() }
        return GearMetadata(clean(name ?: ""), weightGrams, rawWeight, urlString)
    }

    private fun fetchBackcountry(urlString: String): GearMetadata {
        val html = get(urlString) ?: return GearMetadata("", null, null, urlString)
        val doc = Jsoup.parse(html)
        var name: String? = null
        var weightGrams: Double? = null
        var rawWeight: String? = null

        // __INITIAL_STATE__ JSON blob
        doc.select("script:not([src])").forEach { script ->
            val content = script.html()
            if (content.startsWith("window.__INITIAL_STATE__")) {
                val jsonStr = content.removePrefix("window.__INITIAL_STATE__ = ").trimEnd(';')
                try {
                    val state = JSONObject(jsonStr)
                    val catalog = state.optJSONObject("catalog")
                    val products = catalog?.optJSONObject("products")
                    val firstKey = products?.keys()?.asSequence()?.firstOrNull()
                    val product = firstKey?.let { products.optJSONObject(it) }
                    name = product?.optString("displayName") ?: product?.optString("name")
                    val specs = product?.optJSONArray("specs")
                    if (specs != null) {
                        for (i in 0 until specs.length()) {
                            val spec = specs.getJSONObject(i)
                            if (spec.optString("name").contains("weight", ignoreCase = true)) {
                                rawWeight = spec.optString("value")
                                weightGrams = rawWeight?.let { WeightParser.parseToGrams(it) }
                                break
                            }
                        }
                    }
                } catch (_: Exception) { }
            }
        }

        // Specs table fallback
        if (weightGrams == null) {
            for (dt in doc.select("[data-testid='product-specs'] dt, dl dt")) {
                if (dt.text().contains("weight", ignoreCase = true)) {
                    val dd = dt.nextElementSibling()
                    rawWeight = dd?.text()
                    weightGrams = rawWeight?.let { WeightParser.parseToGrams(it) }
                    break
                }
            }
        }

        if (name == null) name = doc.select("meta[property='og:title']").attr("content")
        return GearMetadata(clean(name ?: ""), weightGrams, rawWeight, urlString)
    }

    private fun fetchGeneric(urlString: String): GearMetadata {
        val html = get(urlString) ?: return GearMetadata("", null, null, urlString)
        val doc = Jsoup.parse(html)
        var name: String? = null
        var weightGrams: Double? = null
        var rawWeight: String? = null

        doc.select("script[type='application/ld+json']").forEach { script ->
            try {
                val json = JSONObject(script.html())
                val product = when {
                    json.optString("@type") == "Product" -> json
                    json.has("@graph") -> {
                        val graph = json.getJSONArray("@graph")
                        (0 until graph.length()).map { graph.getJSONObject(it) }
                            .firstOrNull { it.optString("@type") == "Product" }
                    }
                    else -> null
                } ?: return@forEach
                name = product.optString("name")
                val desc = product.optString("description")
                if (desc.isNotBlank()) {
                    val (raw, g) = extractWeight(desc)
                    rawWeight = raw; weightGrams = g
                }
            } catch (_: Exception) { }
        }

        if (name == null) {
            name = doc.select("meta[property='og:title']").attr("content").ifBlank { doc.title() }
        }
        if (weightGrams == null) {
            val sources = listOf(
                doc.select("meta[property='og:description']").attr("content"),
                doc.select("meta[name='description']").attr("content"),
            )
            for (src in sources) {
                val (raw, g) = extractWeight(src)
                if (g != null) { rawWeight = raw; weightGrams = g; break }
            }
        }

        return GearMetadata(name ?: "", weightGrams, rawWeight, urlString)
    }

    // Helpers

    private fun get(url: String): String? = try {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.string() else null
        }
    } catch (_: Exception) { null }

    private fun extractWeight(text: String): Pair<String?, Double?> {
        val cleaned = text.replace(Regex("<[^>]+>"), " ")
        val labeled = Regex(
            """(?:trail\s+weight|pack\s+weight|weight)[:\s]+([0-9][^\n<]{2,30})""",
            RegexOption.IGNORE_CASE
        )
        labeled.find(cleaned)?.groupValues?.get(1)?.trim()?.let { candidate ->
            WeightParser.parseToGrams(candidate)?.let { return candidate to it }
        }
        val unlabeled = Regex(
            """(\d+(?:\.\d+)?\s*(?:lbs?\.?|oz\.?|g|grams?|kg))""",
            RegexOption.IGNORE_CASE
        )
        unlabeled.find(cleaned)?.groupValues?.get(1)?.trim()?.let { candidate ->
            WeightParser.parseToGrams(candidate)?.let { return candidate to it }
        }
        return null to null
    }

    private fun toShopifyJsonUrl(urlString: String): String? {
        return try {
            val url = java.net.URL(urlString)
            var path = url.path
            val variantIdx = path.indexOf("/variants/")
            if (variantIdx != -1) path = path.substring(0, variantIdx)
            path = path.trimEnd('/')
            "${url.protocol}://${url.host}$path.json"
        } catch (_: Exception) { null }
    }

    private fun convertToGrams(value: Double, unit: String): Double = when (unit.lowercase()) {
        "g" -> value
        "kg" -> value * 1000
        "oz" -> value * 28.3495
        "lb" -> value * 453.592
        else -> value
    }

    private fun clean(name: String): String {
        val suffixes = listOf(" | REI Co-op", " - REI", " | Backcountry",
            " – Gossamer Gear", " - Zpacks", " | Free Shipping")
        var s = name
        suffixes.forEach { if (s.endsWith(it)) s = s.dropLast(it.length) }
        return s.trim()
    }
}
