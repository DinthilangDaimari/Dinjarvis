package com.example.smarthome.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

// --- Home Assistant Models ---

@JsonClass(generateAdapter = true)
data class HaServicePayload(
    @field:Json(name = "entity_id") val entityId: String,
    @field:Json(name = "brightness_pct") val brightnessPct: Int? = null,
    @field:Json(name = "temperature") val temperature: Float? = null,
    @field:Json(name = "hvac_mode") val hvacMode: String? = null
)

@JsonClass(generateAdapter = true)
data class HaEntityAttributes(
    @field:Json(name = "friendly_name") val friendlyName: String? = null,
    @field:Json(name = "brightness") val brightness: Int? = null,
    @field:Json(name = "temperature") val temperature: Float? = null,
    @field:Json(name = "current_temperature") val currentTemperature: Float? = null,
    @field:Json(name = "hvac_mode") val hvacMode: String? = null
)

@JsonClass(generateAdapter = true)
data class HaEntityState(
    @field:Json(name = "entity_id") val entityId: String,
    @field:Json(name = "state") val state: String,
    @field:Json(name = "attributes") val attributes: HaEntityAttributes? = null,
    @field:Json(name = "last_updated") val lastUpdated: String? = null
)

@JsonClass(generateAdapter = true)
data class HaApiStatus(
    @field:Json(name = "message") val message: String = ""
)

interface HomeAssistantApi {
    @GET("api/")
    suspend fun checkApiStatus(
        @Header("Authorization") token: String
    ): Response<HaApiStatus>

    @GET("api/states")
    suspend fun getStates(
        @Header("Authorization") token: String
    ): Response<List<HaEntityState>>

    @POST("api/services/{domain}/{service}")
    suspend fun callService(
        @Header("Authorization") token: String,
        @Path("domain") domain: String,
        @Path("service") service: String,
        @Body payload: HaServicePayload
    ): Response<List<HaEntityState>>
}

// --- Google Home / Nest REST & Webhook Models ---

@JsonClass(generateAdapter = true)
data class GoogleHomeCommandPayload(
    @field:Json(name = "command") val command: String,
    @field:Json(name = "params") val params: Map<String, Any?> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class GoogleHomeDeviceTraits(
    @field:Json(name = "sdm.devices.traits.Info") val info: Map<String, Any?>? = null,
    @field:Json(name = "sdm.devices.traits.ThermostatTemperatureSetpoint") val temperatureSetpoint: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class GoogleHomeDeviceItem(
    @field:Json(name = "name") val name: String,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "traits") val traits: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class GoogleHomeDevicesResponse(
    @field:Json(name = "devices") val devices: List<GoogleHomeDeviceItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GoogleHomeWebhookPayload(
    @field:Json(name = "action") val action: String,
    @field:Json(name = "device") val device: String,
    @field:Json(name = "value") val value: Float? = null,
    @field:Json(name = "state") val state: Boolean? = null
)

interface GoogleHomeApi {
    @GET("v1/enterprises/{enterpriseId}/devices")
    suspend fun getDevices(
        @Header("Authorization") token: String,
        @Path("enterpriseId") enterpriseId: String
    ): Response<GoogleHomeDevicesResponse>

    @POST("v1/enterprises/{enterpriseId}/devices/{deviceId}:executeCommand")
    suspend fun executeDeviceCommand(
        @Header("Authorization") token: String,
        @Path("enterpriseId") enterpriseId: String,
        @Path("deviceId") deviceId: String,
        @Body payload: GoogleHomeCommandPayload
    ): Response<Map<String, Any?>>

    @POST
    suspend fun sendWebhookCommand(
        @Url url: String,
        @Header("Authorization") token: String?,
        @Body payload: GoogleHomeWebhookPayload
    ): Response<Map<String, Any?>>
}
