# Kafka Metamorphosis (kafka-m)

A demo toolkit to have hands on Kotlin and Kafka by building a basic Kafka CLI.
There are plenty of good CLI tools for Kafka available (e.g. [kafkacat](https://github.com/edenhill/kafkacat)),
so the primary motivation to create this project was learning by doing. However, there 
might be a use case, that current kafka CLI clients don't cover. Dear valuable reader (or parser) 
of this Readme, please open an issue, if you've got an idea for such a feature. 

- **[Kotlin](https://kotlinlang.org)**: An interesting statically typed programming language by JetBrains. 
While running on the JVM it's multiplatform approach also allows e.g. compilation to javascript.

- **[Kafka](https://kafka.apache.org)**: 
A unified, high-throughput, low-latency platform for handling real-time messages and events.

The code of this demo is a result of reading and trying out the samples of the blog posts at 
[aseigneurin.github.io](https://aseigneurin.github.io). It's as ready for production as 
a sideproject done during winter holiday season instead of reading Franz Kafkas novels can be.

In a nutshell, the following commands are available. Currently you can just publish plaintext
content. 

```
Usage: topic [OPTIONS] COMMAND [ARGS]...

Options:
  -h, --help  Show this message and exit

Commands:
  list       Display a list of all topics
  create     Create topics
  delete     Delete topics
  describe   Describe topics
  publish    Publish a new record to a Kafka Topic
  subscribe  Subscribe to a Kafka Topic
  test       Run a simple performance test by creating + publishing to a topic
``` 


## Prerequisites 

You need [Gradle](https://gradle.org/install/) and [JVM](https://java.com/de/download/) to work with 
the project. Furthermore, given that you've got no running kafka instance, you need  
[Docker](https://www.docker.com/get-started) 

By ```docker-compose up``` in the root directory of this project, you start your own 
Kafka/Zookeeper docker instance

## Run

You can start the CLI in development mode (it triggers a build every time) by 

```./run.sh```

Then you should get an overview of the available tasks.
 
E.g you can start a simple performance test generating 10000 messages with the given payload

```./run.sh topic test --nr=10000 --workers=2 --payload="just some text" ```

## Test

Execute the following command to start the automatic tests

```./gradlew test ```

You don't need a working Kafka instance, the test provisions it's own instance just for testing purpose.
There are currently just a few junit tests available. More to come. One nice thing about 
it, there's a [snapshot test library](https://blog.karumi.com/kotlin-snapshot-testing/) used similar to jest.

More documentation follows, i've to leave the train for now :)  



