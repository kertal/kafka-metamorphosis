package net.ankertal.kafka.metamorphosis

import org.apache.kafka.clients.admin.AdminClient
import java.util.*
import org.apache.log4j.LogManager
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.clients.admin.TopicListing
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import kotlin.collections.List


class Topics(val brokers: String = "localhost:9092") {

    private val logger = LogManager.getLogger(javaClass)

    private val adminClient : AdminClient by lazy {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        AdminClient.create(props)
    }

    private fun createConsumerClient(groupId: String = "default", offsetReset: String = "earliest"): Consumer<String, String> {

        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = groupId
        props["auto.offset.reset"] = offsetReset
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java

        return KafkaConsumer<String, String>(props)
    }

    private fun createProducerClient(): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    fun list():Collection<TopicListing> {
        adminClient.use {
            return it.listTopics().listings().get()
        }
    }

    fun create(topics: List<String>, numPartitions: Int = 1, replicationFactor: Short =  1):Map<String,KafkaFuture<Void>> {
        logger.info("create new topics $topics (partitions: $numPartitions, replicationFactor: $replicationFactor)")

        adminClient.use {
            val topicList = topics.map{
                NewTopic(it, numPartitions, replicationFactor)
            }
            return it.createTopics(topicList).values()
        }
    }

    fun delete(topics: List<String>):Map<String,KafkaFuture<Void>> {
        logger.info("Delete topics $topics ")

        adminClient.use {
            return it.deleteTopics(topics).values()
        }
    }

    fun describe(topics: List<String>):Map<String,KafkaFuture<TopicDescription>> {
        logger.info("Describe topics $topics")

        adminClient.use {
            return it.describeTopics(topics).values()
        }
    }

    fun subscribe(topics: List<String>, callback: (ConsumerRecord<String,String>) -> Unit, groupId: String = "default" ) {
        logger.info("Subscribe topics $topics ")
        val timeoutInSec: Long = 1

        createConsumerClient(groupId = groupId).use {
            it.subscribe(topics)
            while (true) {
                val records = it.poll(Duration.ofSeconds(timeoutInSec))
                records.iterator().forEach {
                    callback(it)
                }
            }
        }
    }

    fun publish(topicName: String, key: String, value: String) {
        logger.info("Publish to topic")

        createProducerClient().use {
             if (key != "") {
                 it.send(ProducerRecord(topicName, key, value)).get()
             } else {
                 it.send(ProducerRecord(topicName, value)).get()
             }
        }
    }



}