package example.micronaut

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.Optional
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit.SECONDS
import jakarta.inject.Inject

@Testcontainers // <1>
@MicronautTest
@TestInstance(PER_CLASS) // <2>
class BookControllerTest : TestPropertyProvider { // <3>

    companion object {
        val received: MutableCollection<Book> = ConcurrentLinkedDeque()
    }

    @Container
    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest")) // <4>

    @Inject
    lateinit var analyticsListener: AnalyticsListener // <5>

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient // <6>

    @Test
    fun testMessageIsPublishedToKafkaWhenBookFound() {
        val isbn = "1491950358"

        val result : Optional<Book> = retrieveGet("/books/" + isbn) as Optional<Book> // <7>
        assertNotNull(result)
        assertTrue(result.isPresent)
        assertEquals(isbn, result.get().isbn)

        await().atMost(5, SECONDS).until { !received.isEmpty() } // <8>
        assertEquals(1, received.size) // <9>

        val bookFromKafka = received.iterator().next()
        assertNotNull(bookFromKafka)
        assertEquals(isbn, bookFromKafka.isbn)
    }

    @Test
    fun testMessageIsNotPublishedToKafkaWhenBookNotFound() {
        assertThrows(HttpClientResponseException::class.java) { retrieveGet("/books/INVALID") }

        Thread.sleep(5_000); // <10>
        assertEquals(0, received.size);
    }

    override fun getProperties(): Map<String, String> {
        kafka.start()
        return mapOf("kafka.bootstrap.servers" to kafka.bootstrapServers) // <11>
    }

    @AfterEach
    fun cleanup() {
        received.clear()
    }

    private fun retrieveGet(url: String) = client
            .toBlocking()
            .retrieve(HttpRequest.GET<Any>(url),
                    Argument.of(Optional::class.java, Book::class.java))

    @KafkaListener(offsetReset = EARLIEST)
    class AnalyticsListener {

        @Topic("analytics")
        fun updateAnalytics(book: Book) {
            received.add(book)
        }
    }
}
