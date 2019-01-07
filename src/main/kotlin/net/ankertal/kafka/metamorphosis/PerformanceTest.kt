package net.ankertal.kafka.metamorphosis

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import java.util.*

/**
 * A basic Performance Test class sending messages to a given Kafka broker,
 * waiting for ACK for each message, returning the number of milliseconds it took
 * to send all messages
 * It's using coroutines Channels to build a data pipeline with multiple workers
 * https://kotlinlang.org/docs/reference/coroutines/channels.html
 * It's using still experimental features of the coroutines API
 **/
class PerformanceTest(val brokers: String = "localhost:9092") {

    private val logger = LogManager.getLogger(javaClass)

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    fun run(
        topic: String = "kafka-metamorphosis-test",
        nrOfRecords: Int = 1000,
        workers: Int = 1,
        payload: String = "test"
    ): Long = runBlocking {
        logger.info("Start with topic $topic, nrOfRecords $nrOfRecords, workers $workers")

        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        val kafkaProducer = KafkaProducer<String, String>(props)

        logger.info("Start message producer channel")
        val msgProdChannel = produce {
            logger.info("${Thread.currentThread().name} Start Record Producer")
            repeat(nrOfRecords) {
                send(payload)
            }
        }

        logger.info("Start message consumer channels - the Kafka producer")
        val start = System.currentTimeMillis()
        val msgConsChannel = Array(workers) {
            val thread = "kafka-m-worker-${it}"
            produce(newSingleThreadContext(thread)) {
                logger.info(" Start message consumer channel,thread: ${Thread.currentThread().name} ")

                for (msg in msgProdChannel) {
                    kafkaProducer.send(ProducerRecord(topic, msg)).get()
                }
                send("finished")
            }
        }
        msgConsChannel.forEach { it.receive() }

        val duration = System.currentTimeMillis() - start
        logger.info("Finished in $duration ms")
        duration
    }
}



