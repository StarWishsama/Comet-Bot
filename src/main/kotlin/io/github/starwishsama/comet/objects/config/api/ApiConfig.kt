package io.github.starwishsama.comet.objects.config.api

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.starwishsama.comet.objects.wrapper.XmlElement
import java.util.concurrent.TimeUnit

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(value = BiliBiliConfig::class),
    JsonSubTypes.Type(value = R6StatsConfig::class),
    JsonSubTypes.Type(value = TwitterConfig::class),
    JsonSubTypes.Type(value = XmlElement::class)
)
interface ApiConfig {
    val apiName: String

    val interval: Int

    val timeUnit: TimeUnit
}
