package com.pawbyte.pawassistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

data class AiContextMessage(
    val role: String,
    val text: String
)

object AiService {
    const val WORKER_URL = "https://paw-assistant-worker.devy-build-backup.workers.dev/"

    private const val CONNECT_TIMEOUT_MS = 20_000
    private const val READ_TIMEOUT_MS = 35_000

    suspend fun sendConversation(messages: List<AiContextMessage>): Result<String> =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val messagesArray = JSONArray().apply {
                    messages.forEach { message ->
                        put(
                            JSONObject().apply {
                                put("role", message.role)
                                put("text", message.text)
                            }
                        )
                    }
                }

                val requestBody = JSONObject().apply {
                    put("messages", messagesArray)
                }.toString()
                val bodyBytes = requestBody.toByteArray(Charsets.UTF_8)

                val url = URL(WORKER_URL)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    doInput = true
                    doOutput = true
                    setFixedLengthStreamingMode(bodyBytes.size)
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "PawAssistant-Android/1.0.1")
                }

                connection.outputStream.use { output ->
                    output.write(bodyBytes)
                    output.flush()
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseText = stream?.let { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                }.orEmpty()

                val json = if (responseText.isNotBlank()) {
                    runCatching { JSONObject(responseText) }.getOrNull()
                } else {
                    null
                }

                if (responseCode in 200..299) {
                    val reply = json?.optString("reply", "").orEmpty().trim()
                    if (reply.isNotEmpty()) {
                        Result.success(reply)
                    } else {
                        Result.failure(
                            IllegalStateException("Asistan şu anda yanıt üretemedi. Lütfen tekrar deneyin.")
                        )
                    }
                } else {
                    val serverError = json?.optString("error", "").orEmpty().trim()
                    val fallbackError = when (responseCode) {
                        403 -> "Sunucu isteği reddetti (403). Lütfen daha sonra tekrar deneyin."
                        404 -> "Asistan servis adresi bulunamadı (404)."
                        429 -> "Şu anda çok fazla istek var. Lütfen biraz bekleyip tekrar deneyin."
                        in 500..599 -> "Asistan servisine şu anda ulaşılamıyor ($responseCode). Lütfen daha sonra tekrar deneyin."
                        else -> "Yanıt alınırken bir sorun oluştu (Kod: $responseCode)."
                    }
                    Result.failure(
                        IllegalStateException(serverError.ifEmpty { fallbackError })
                    )
                }
            } catch (_: SecurityException) {
                Result.failure(
                    IllegalStateException("Uygulamanın internet erişim izni bulunmuyor.")
                )
            } catch (_: UnknownHostException) {
                Result.failure(
                    IllegalStateException("İnternet bağlantınız yok veya sunucu adresi bulunamadı.")
                )
            } catch (_: SocketTimeoutException) {
                Result.failure(
                    IllegalStateException("Yanıt beklenirken zaman aşımı oluştu. Lütfen tekrar deneyin.")
                )
            } catch (_: SSLException) {
                Result.failure(
                    IllegalStateException("Güvenli bağlantı (SSL) kurulamadı. Cihazınızın tarih/saat ayarını ve internet bağlantısını kontrol edin.")
                )
            } catch (_: ConnectException) {
                Result.failure(
                    IllegalStateException("Sunucuya bağlanılamadı. İnternet bağlantınızı kontrol edin.")
                )
            } catch (e: IOException) {
                Result.failure(
                    IllegalStateException("Ağ iletişimi sırasında hata oluştu (${e.javaClass.simpleName}).")
                )
            } catch (e: Exception) {
                Result.failure(
                    IllegalStateException("Bağlantı hatası (${e.javaClass.simpleName}): ${e.localizedMessage ?: "Bilinmeyen hata"}")
                )
            } finally {
                connection?.disconnect()
            }
        }
}
