package net.ankertal.kafka.metamorphosis.test
import org.junit.Test;
import org.junit.ClassRule

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource
import net.ankertal.kafka.metamorphosis.Cli
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import com.karumi.kotlinsnapshot.KotlinSnapshot

val kotlinSnapshot = KotlinSnapshot(snapshotsFolder = "src/test/resources/CliTest")

class CliTest {

    /**
     * We have a single embedded Kafka server that gets started when this test class is initialized.
     * It's automatically started before any methods are run via the @ClassRule annotation.
     * It's automatically stopped after all of the tests are completed via the @ClassRule annotation.
     */
    companion object {
        @ClassRule
        @JvmField
        val sharedKafkaTestResource: SharedKafkaTestResource = object : SharedKafkaTestResource() {
        }
    }

    private val originalOut = System.out
    private val originalErr = System.err

    private fun getBrokerConnectString(): String {
        val broker = sharedKafkaTestResource
            .kafkaBrokers
            .getBrokerById(1)
        return broker.connectString
    }

    private fun execCommand(command: String): HashMap<String, String> {
        val outContent = ByteArrayOutputStream()
        val errContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))

        val args = command.split(" ").toTypedArray()
        Cli().test(args)

        System.setOut(originalOut);
        System.setErr(originalErr);

        return hashMapOf("out" to outContent.toString(), "err" to errContent.toString())

    }

    @Test
    fun `CLI topic create`() {

        listOf<HashMap<String,String>>(
            hashMapOf(
                "command" to "create topic1 topic2 --host=${getBrokerConnectString()}",
                "name" to "CLI topic create: it should create 2 topics"
            ),
            hashMapOf(
                "command" to "create topic1 --host=${getBrokerConnectString()}",
                "name" to "CLI topic create: topic won't be created since it already exists"
            ),
            hashMapOf(
                "command" to "create topic1 topic2 --host=123",
                "name" to "CLI topic create: it should fail due to an invalid host"
            ),
            hashMapOf(
                "command" to "create --host=${getBrokerConnectString()}",
                "name" to "CLI topic create: it should do nothing since lack of topic arguments"
            ),
            hashMapOf(
                //TODO this needs to be verified with a seperate test
                "command" to "create topic-partition-2 --partitions=2 --host=${getBrokerConnectString()}",
                "name" to "CLI topic create: it should create a topic with 2 partitions"
            )
        ).forEach {
            val command = it.getOrDefault("command","")
            val name = it.getOrDefault("name","")
            kotlinSnapshot.matchWithSnapshot(this.execCommand(command),name)
        }


    }

}