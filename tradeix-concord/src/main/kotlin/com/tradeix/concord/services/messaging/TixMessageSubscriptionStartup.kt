package com.tradeix.concord.services.messaging

import com.google.gson.Gson
import com.rabbitmq.client.ConnectionFactory
import com.tradeix.concord.interfaces.IQueueConsumer
import com.tradeix.concord.interfaces.IQueueProducer
import com.tradeix.concord.messages.rabbit.RabbitRequestMessage
import com.tradeix.concord.services.messaging.publishers.CordaTiXPOPublisher
import com.tradeix.concord.services.messaging.publishers.CordaTiXInvoicePublisher
import com.tradeix.concord.services.messaging.subscribers.*
import com.typesafe.config.ConfigFactory
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.loggerFor
import net.corda.nodeapi.internal.config.parseAs
import org.slf4j.Logger
import java.io.File
import kotlin.reflect.KFunction

//TODO Rename this file to TixMessagingStartup
class TixMessageSubscriptionStartup(val services: CordaRPCOps) {

    init {
        val legalEntity = services.nodeInfo().legalIdentities.first()
        log.info("Reached TixMessageSub in ${legalEntity.name.organisation}")

        if (legalEntity.name.organisation == "C6acdb3a7-2fde-44d0-b400-ea69d79ae27b") {
            TixMessageSubscriptionStartup.initializeQueues(services)
        }
    }

    companion object {
        private val log: Logger = loggerFor<TixMessageSubscriptionStartup>()
        private val currentConsumers: MutableMap<String, IQueueConsumer> = mutableMapOf()
        val currentPublishers: MutableMap<String, IQueueProducer<RabbitRequestMessage>> = mutableMapOf()

        private fun initializeQueues(cordaRpcService: CordaRPCOps) {
            try {
                val currentPath = System.getProperty("user.dir")
                log.debug("The current path is $currentPath")
                val defaultConfig = ConfigFactory.parseFile(File("$currentPath/tix.integration.conf"))
                val serializer = Gson()
                val connectionConfig = defaultConfig!!
                        .resolve()
                        .getConfig("tix-integration.rabbitMqConnectionConfiguration")
                        .parseAs<RabbitMqConnectionConfiguration>()
                val connectionFactory = ConnectionFactory()

                log.info("The connectionConfig is " + connectionConfig)
                connectionFactory.username = connectionConfig.userName
                connectionFactory.password = connectionConfig.password
                connectionFactory.host = connectionConfig.hostName
                connectionFactory.virtualHost = connectionConfig.virtualHost
                connectionFactory.port = connectionConfig.portNumber

                log.info("The connectionFactory username is " + connectionFactory.username)
                log.info("The connectionConfig.password is " + connectionConfig.password)
                log.info("The connectionFactory host is " + connectionFactory.host)
                log.info("The connectionFactory virtualHost is " + connectionFactory.virtualHost)
                log.info("The connectionFactory port is " + connectionFactory.port)

                val connectionProvider = RabbitMqConnectionProvider(connectionFactory)

                log.info("Starting to connect now....")

                listOf(
                        ::InvoiceIssuanceFlowQueuesSubscriber,
                        ::PurchaseOrderIssuanceFlowQueuesSubscriber,

                        ::InvoiceAmendmentFlowQueuesSubscriber,
                        ::PurchaseOrderAmendmentFlowQueuesSubscriber,

                        ::InvoiceCancellationFlowQueuesSubscriber,
                        ::PurchaseOrderCancellationFlowQueuesSubscriber,

                        ::InvoiceChangeOwnerFlowQueuesSubscriber,
                        ::PurchaseOrderChangeOwnerFlowQueuesSubscriber
                ).forEach {
                    log.info("Starting ${it.javaClass.name}")
                    it.invoke(cordaRpcService, defaultConfig, serializer)
                            .initialize(connectionProvider, currentConsumers)
                }

                log.info("Subscription connection successful")

                log.info("Starting CordaTiXInvoicePublisher")
                CordaTiXInvoicePublisher(defaultConfig, serializer)
                        .initialize(connectionProvider, currentPublishers)

                log.info("Starting CordaTiXPOPublisher")
                CordaTiXPOPublisher(defaultConfig, serializer)
                        .initialize(connectionProvider, currentPublishers)

            } catch (ex: Throwable) {
                log.error("Exception caught when subscribing to Rabbit queues")
                log.error(ex.message)
                println(ex)
            }
        }
    }
}