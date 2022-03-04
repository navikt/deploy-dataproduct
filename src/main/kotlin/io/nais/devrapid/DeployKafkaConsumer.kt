package io.nais.devrapid

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration

class DeployKafkaConsumer(val configuration: Configuration, private val bq: BigQuery) {

    private val consumer = KafkaConsumer<String, ByteArray>(configuration.props)
    private val LOGGER = LoggerFactory.getLogger("deploy-dataproduct")


    fun run() {
        LOGGER.info("Started consumer thread")
        consumer.subscribe(listOf(configuration.topic))
        val collector = EventCollector()
        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            records.iterator()
                .forEach { collector.extractDeployData(it.value())?.let { row -> bq.write(row) } }
        }
    }
}


