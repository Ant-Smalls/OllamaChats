import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

import kotlinx.coroutines.runBlocking

//Data class for the request to the model
@Serializable
data class LlamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean
)

//Data class for the response from the model
@Serializable
data class LlamaResponse (
    val model: String,
    val response: String,
)

class LLamaClient {
    //Create the client to send the post request with and increase the timeout limit
    private val client = HttpClient(CIO){
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                requestTimeout = 60000 // Set request timeout to 60 seconds
            }
    }

    suspend fun callModel(model:String, prompt:String):String {
        val requestData = LlamaRequest(
            model = model,
            prompt = prompt,
            stream = false
        )

        // Send POST request and parse response
        val response: LlamaResponse = client.post("http://localhost:11434/api/generate") {
            contentType(ContentType.Application.Json)
            setBody(requestData)
        }.body()

        return response.response
    }
    // Close the client
    fun close() {
        client.close()
    }
}


fun main() = runBlocking {
    val llamaClient = LLamaClient() // Instantiate the LlamaClient

    try {
        // Define the model and prompt
        val model = "llama3.2"
        val prompt = "Why do I sneeze everytime I stare at the sun?"

        // Call the model and get the response
        val response = llamaClient.callModel(model, prompt)

        // Print the response from the model
        println("Model response: $response")
    }

    catch (e: Exception) {
        // Handle any errors
        println("Error: ${e.message}")
    }
    finally {
        // Close the client
        llamaClient.close()
    }
}