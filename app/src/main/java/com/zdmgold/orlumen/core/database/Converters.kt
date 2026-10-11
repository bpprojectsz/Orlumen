package com.zdmgold.orlumen.core.database

import androidx.room.TypeConverter
import com.zdmgold.orlumen.core.model.DesignKind
import java.net.URLDecoder
import java.net.URLEncoder

internal object KeyValueCodec {
    private const val PAIR_SEPARATOR = "&"
    private const val KEY_VALUE_SEPARATOR = "="
    private const val CHARSET = "UTF-8"

    fun encodeStrings(map: Map<String, String>): String = map.entries.joinToString(PAIR_SEPARATOR) { (key, value) ->
        URLEncoder.encode(key, CHARSET) + KEY_VALUE_SEPARATOR + URLEncoder.encode(value, CHARSET)
    }

    fun decodeStrings(text: String): Map<String, String> {
        if (text.isEmpty()) return emptyMap()
        return text.split(PAIR_SEPARATOR).mapNotNull { pair ->
            val index = pair.indexOf(KEY_VALUE_SEPARATOR)
            if (index < 0) {
                null
            } else {
                URLDecoder.decode(pair.substring(0, index), CHARSET) to
                    URLDecoder.decode(pair.substring(index + 1), CHARSET)
            }
        }.toMap()
    }

    fun encodeFloats(map: Map<String, Float>): String = encodeStrings(map.mapValues { it.value.toString() })

    fun decodeFloats(text: String): Map<String, Float> = decodeStrings(text).mapNotNull { (key, value) ->
        value.toFloatOrNull()?.let { key to it }
    }.toMap()
}

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        if (value.isEmpty()) emptyList() else value.split(",").mapNotNull { it.toIntOrNull() }

    @TypeConverter
    fun fromFloatMap(value: Map<String, Float>): String = KeyValueCodec.encodeFloats(value)

    @TypeConverter
    fun toFloatMap(value: String): Map<String, Float> = KeyValueCodec.decodeFloats(value)

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = KeyValueCodec.encodeStrings(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> = KeyValueCodec.decodeStrings(value)

    @TypeConverter
    fun fromDesignKind(value: DesignKind): String = value.name

    @TypeConverter
    fun toDesignKind(value: String): DesignKind = DesignKind.valueOf(value)
}
