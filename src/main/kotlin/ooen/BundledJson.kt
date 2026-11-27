package ooen

import com.google.gson.Gson
import com.google.gson.JsonArray

internal object BundledJson {
    fun readArray(path: String): JsonArray {
        val stream = checkNotNull(javaClass.getResourceAsStream(path)) { "Missing resource: $path" }
        return stream.bufferedReader(Charsets.UTF_8).use { reader ->
            checkNotNull(Gson().fromJson(reader, JsonArray::class.java)) { "Empty JSON: $path" }
        }
    }
}
