package net.ankertal.kafka.metamorphosis.test

import org.junit.Test;
import org.junit.ClassRule

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource
import net.ankertal.kafka.metamorphosis.Topics
import java.lang.Exception
import kotlin.test.asserter


class TopicsTest {

    /**
     * We have a single embedded Kafka server that gets started when this test class is initialized.
     *
     * It's automatically started before any methods are run via the @ClassRule annotation.
     * It's automatically stopped after all of the tests are completed via the @ClassRule annotation.
     */
    companion object {

        @ClassRule
        @JvmField
        val sharedKafkaTestResource: SharedKafkaTestResource = object : SharedKafkaTestResource() {
        }
    }

    fun getBrokerConnectString(): String {
        val broker = sharedKafkaTestResource
            .kafkaBrokers
            .getBrokerById(1)
        return broker.connectString
    }

    @Test
    fun `Create topics`() {

        val expected = listOf("1-test1", "1-test2", "1-test3")
        val actual = Topics(getBrokerConnectString()).create(expected)
            .map { (key, value) ->
                try {
                    value.get()
                    key
                } catch (e: Exception) {
                    "error"
                }
            }

        asserter.assertEquals(
            "It should create 3 topics",
            expected,
            actual.sorted()
        )
        Topics(getBrokerConnectString()).delete(expected)
    }

    @Test
    fun `List topics`() {

        val expected = listOf("2-test1", "2-test2", "2-test3")
        Topics(getBrokerConnectString())
            .create(expected)
            .map { (key, value) ->
                try {
                    value.get()
                    key
                } catch (e: Exception) {
                    "error"
                }
            }

        val actual = Topics(getBrokerConnectString())
            .list()
            .map { it.name() }

        asserter.assertEquals(
            "it should return 3 topics",
            expected,
            actual.sorted()
        );

    }
}