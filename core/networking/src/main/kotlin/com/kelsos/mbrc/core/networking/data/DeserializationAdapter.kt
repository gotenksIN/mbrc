package com.kelsos.mbrc.core.networking.data

import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

interface DeserializationAdapter {
  fun <T> objectify(line: String, kClass: KClass<T>): T where T : Any

  fun <T> objectify(line: String, type: ParameterizedType): T where T : Any

  fun <T> convertValue(data: Any?, kClass: KClass<T>): T where T : Any
}

class DeserializationAdapterImpl(private val moshi: Moshi) : DeserializationAdapter {
  override fun <T : Any> objectify(line: String, type: ParameterizedType): T {
    val adapter = moshi.adapter<T>(type)
    return checkNotNull(adapter.fromJson(line)) { "Failed to deserialize JSON line to type $type: $line" }
  }

  override fun <T : Any> convertValue(data: Any?, kClass: KClass<T>): T {
    val adapter = moshi.adapter(kClass.java)
    return checkNotNull(adapter.fromJsonValue(data)) { "Failed to convert generic value $data to class ${kClass.simpleName}" }
  }

  override fun <T : Any> objectify(line: String, kClass: KClass<T>): T {
    val adapter = moshi.adapter(kClass.java)
    return checkNotNull(adapter.fromJson(line)) { "Failed to deserialize JSON line to class ${kClass.simpleName}: $line" }
  }
}
