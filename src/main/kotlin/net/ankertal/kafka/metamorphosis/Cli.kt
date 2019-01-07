package net.ankertal.kafka.metamorphosis

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.KafkaFuture
import java.lang.Exception

class Cli {
    @ExperimentalCoroutinesApi
    private val topic =
        Topic()
            .subcommands(
                List(),
                Create(),
                Delete(),
                Describe(),
                Publish(),
                Subscribe(),
                Test()
            )

    @ExperimentalCoroutinesApi
    fun main(args: Array<String>) {
        topic.main(args)
    }

    @ExperimentalCoroutinesApi
    fun test(args: Array<String>) {
        topic.parse(args)
    }
}

class Topic : CliktCommand() {
    override fun run() {
    }
}

class List : CliktCommand(help = "Display a list of all topics") {

    private val host: String by option(help = "Host of kafka brokers").default("127.0.0.1:9092")

    override fun run() = try {
        Topics(host)
            .list()
            .stream()
            .forEach() {
                TermUi.echo(it.name())
            }
    } catch (e: Exception) {
        TermUi.echo(
            "Command list failed: ${e.message}",
            true,
            true
        )
    }
}

class Create : CliktCommand(help = "Create topic(s)") {
    private val host: String by option(help = "Host of kafka brokers").default("127.0.0.1:9092")
    private val partitions: Int by option(help = "Number of partitions to create").int().default(1)
    private val replication: Int by option(help = "Replication factor the new topic").int().default(1)
    private val topicList by argument().multiple(true)

    private fun echoSafe(key: String, value: KafkaFuture<Void>) {
        try {
            value.get()
            TermUi.echo("$key: created")
        } catch (e: Exception) {
            TermUi.echo("$key: failure: ${e.message}")
        }
    }

    override fun run() {
        try {
            Topics(host)
                .create(topicList, partitions, replication.toShort())
                .forEach { it -> echoSafe(it.key, it.value) }
        } catch (e: Exception) {
            TermUi.echo(
                "Command create ($topicList, $partitions, $replication) failed: ${e.message}",
                true,
                true
            )
        }
    }
}

class Delete : CliktCommand(help = "Delete topics") {
    private val host: String by option(help = "Host of Kafka brokers").default("127.0.0.1:9092")
    private val topicList by argument().multiple(true)

    private fun echoSafe(key: String, value: KafkaFuture<Void>) {
        try {
            value.get()
            TermUi.echo("$key: deleted")
        } catch (e: Exception) {
            TermUi.echo("$key: failure: ${e.message}")
        }
    }

    override fun run() {
        try {
            Topics(host)
                .delete(topicList)
                .forEach { it -> echoSafe(it.key, it.value) }
        } catch (e: Exception) {
            TermUi.echo(
                "Command delete ($topicList) failed: ${e.message}",
                true,
                true
            )
        }
    }
}

class Describe : CliktCommand(help = "Describe topics") {
    private val host: String by option(help = "Host of Kafka brokers").default("127.0.0.1:9092")
    private val topicList by argument().multiple(true)

    private fun echoSafe(key: String, value: KafkaFuture<TopicDescription>) {
        try {
            TermUi.echo("$key: ${value.get()}")
        } catch (e: Exception) {
            TermUi.echo("$key: ${e.message}")
        }
    }

    override fun run() {
        try {
            Topics(host)
                .describe(topicList)
                .forEach { it -> echoSafe(it.key, it.value) }
        } catch (e: Exception) {
            TermUi.echo(
                "Command describe ($topicList) failed: ${e.message}",
                true,
                true
            )
        }
    }
}

class Publish : CliktCommand(help = "Publish a new record to a Kafka topic ") {
    private val host: String by option(help = "Host of Kafka brokers").default("127.0.0.1:9092")
    private val topic: String by argument(help = "Name of Kafka topic to publish the new record ")
    private val key: String by option(help = "Key of new record").default("")
    private val value: String by option(help = "Value of new record").required()

    override fun run() {
        try {
            Topics(host).publish(topic, key, value)
            TermUi.echo("Publish successful")

        } catch (e: Exception) {
            TermUi.echo(
                "Command publish ($topic) failed: ${e.message}",
                true,
                true
            )
        }
    }
}

class Subscribe : CliktCommand("Subscribe to a Kafka Topic ") {
    private val host: String by option(help = "Host of Kafka brokers").default("127.0.0.1:9092")
    private val groupId: String by option(help = "GroupId of the subscriber ").default("default")
    private val topicList by argument().multiple(true)

    override fun run() {
        try {
            val cb: (ConsumerRecord<String, String>) -> Unit = {
                TermUi.echo(it)
            }
            Topics(host).subscribe(topicList, cb, groupId)
        } catch (e: Exception) {
            TermUi.echo(
                "Command subsribe ($topicList) failed: ${e.message}",
                true,
                true
            )
        }
    }
}

@ExperimentalCoroutinesApi
class Test : CliktCommand("Run a simple performance test by creating + publishing to a topic ") {
    private val host: String by option(help = "Host of Kafka brokers").default("127.0.0.1:9092")
    private val nr: Int by option(help = "Number of records to generate").int().default(1000)
    private val workers: Int by option(help = "Number of workers generating records").int().default(1)
    private val partitions: Int by option(help = "Number of partitions to create for the test topic").int().default(1)
    private val payload: String by option(help = "Payload to use for generating records").default("test")

    @ObsoleteCoroutinesApi
    override fun run() {
        try {
            val topic = "kafka-metamorphosis-test-${java.util.UUID.randomUUID()}"
            Topics(host).create(listOf(topic),numPartitions = partitions)
            val duration = PerformanceTest(host).run(topic, nr, workers, payload)
            val rate = nr * 1000 / duration
            TermUi.echo("Test submitted $nr records in $duration ms, rate: $rate rec/s ")
            Topics(host).delete(listOf(topic))

        } catch (e: Exception) {
            TermUi.echo(
                "Command test (topic) failed: ${e.message}", true, true
            )
        }
    }
}