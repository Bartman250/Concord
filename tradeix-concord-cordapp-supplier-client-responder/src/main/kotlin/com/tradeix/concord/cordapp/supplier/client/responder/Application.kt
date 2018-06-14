package com.tradeix.concord.cordapp.supplier.client.responder

import com.google.gson.GsonBuilder
import com.tradeix.concord.shared.client.components.Address
import com.tradeix.concord.shared.client.components.RPCConnectionProvider
import com.tradeix.concord.shared.client.http.HttpClient
import com.tradeix.concord.shared.domain.states.InvoiceState
import com.tradeix.concord.shared.extensions.getConfiguredSerializer
import com.tradeix.concord.shared.messages.TransactionResponseMessage
import com.tradeix.concord.shared.services.VaultService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application(address: Address, rpc: RPCConnectionProvider) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }

    private val repository = VaultService.fromCordaRPCOps<InvoiceState>(rpc.proxy)
    private val client = HttpClient("http://${address.host}:${address.port}/")
    private val serializer = GsonBuilder().getConfiguredSerializer()

    init {
        repository.observe {
            client.post("Invoice/Notify", serializer.toJson(TransactionResponseMessage(
                    assetIds = listOf(it.state.data.linearId),
                    transactionId = it.ref.txhash.toString()
            )))
        }
    }
}
