package com.tradeix.concord.services.messaging

import com.google.gson.GsonBuilder
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.tradeix.concord.interfaces.IQueueDeadLetterProducer
import com.tradeix.concord.messages.rabbit.RabbitMessage
import com.tradeix.concord.messages.rabbit.RabbitResponseMessage
import com.tradeix.concord.messages.rabbit.tradeasset.TradeAssetIssuanceRequestMessage
import com.tradeix.concord.serialization.CordaX500NameSerializer
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps

class MessageConsumerFactory(
        val services: CordaRPCOps,
        private val responderConfigurations: Map<String, RabbitProducerConfiguration>,
        private val rabbitConnectionProvider: RabbitMqConnectionProvider) {

    fun <T : RabbitMessage> getMessageConsumer(
            channel: Channel,
            type: Class<T>,
            deadLetterProducer: IQueueDeadLetterProducer<RabbitMessage>,
            maxRetries: Int): Consumer {
        return when {
            type.isAssignableFrom(TradeAssetIssuanceRequestMessage::class.java) -> {
                val responder = RabbitMqProducer<RabbitResponseMessage>(
                        responderConfigurations["cordatix_issuance_response"]!!,
                        rabbitConnectionProvider
                )

                val cordaNameSerialiser = GsonBuilder()
                        .registerTypeAdapter(CordaX500Name::class.java, CordaX500NameSerializer())
                        .disableHtmlEscaping()
                        .create()

                IssuanceMessageConsumer(services, channel, deadLetterProducer, maxRetries, responder, cordaNameSerialiser)
            }
            else -> throw ClassNotFoundException()
        }
    }
}