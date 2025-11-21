package com.habit.app.helper

import androidx.collection.ArraySet
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import kotlin.apply
import kotlin.collections.forEach
import kotlin.jvm.java

object GsonUtil {
    val gson = GsonBuilder()
        // localDate
        .registerTypeAdapter(LocalDate::class.java, object : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
            override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.toString())
            }

            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): LocalDate {
                return LocalDate.parse(json?.asString ?: "")
            }
        })
        // LocalTime
        .registerTypeAdapter(LocalTime::class.java, object : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
            override fun serialize(src: LocalTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.toString())
            }

            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): LocalTime {
                return LocalTime.parse(json?.asString ?: "")
            }
        })
        // ArraySet<Int>
        .registerTypeAdapter(ArraySet::class.java, object : JsonSerializer<ArraySet<Int>>,
            JsonDeserializer<ArraySet<Int>> {
            override fun serialize(src: ArraySet<Int>?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                return JsonArray().apply { src?.forEach { add(it) } }
            }
            override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): ArraySet<Int> {
                val set = ArraySet<Int>()
                json?.asJsonArray?.forEach { set.add(it.asInt) }
                return set
            }
        })
        .setPrettyPrinting()
        .create()
}