package com.tradeix.concord.cordapp.funder.client.receiver.controllers

import com.tradeix.concord.cordapp.funder.flows.FundingResponseFlow
import com.tradeix.concord.shared.client.components.RPCConnectionProvider
import com.tradeix.concord.shared.client.webapi.ResponseBuilder
import com.tradeix.concord.shared.cordapp.mapping.fundingresponse.FundingResponseRequestMapper
import com.tradeix.concord.shared.domain.states.FundingResponseState
import com.tradeix.concord.shared.messages.TransactionResponseMessage
import com.tradeix.concord.shared.messages.fundingresponse.FundingResponseRequestMessage
import com.tradeix.concord.shared.services.VaultService
import com.tradeix.concord.shared.validation.ValidationException
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.node.services.Vault
import net.corda.core.utilities.getOrThrow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = arrayOf("/fundingresponse"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
class FundingResponseRequestController(private val rpc: RPCConnectionProvider) {

    private val vaultService = VaultService.fromCordaRPCOps<FundingResponseState>(rpc.proxy)
    private val fundingResonseRequestMapper = FundingResponseRequestMapper()

    @GetMapping()
    fun getFundingResponseStates(
            @RequestParam(name = "externalId", required = false) externalId: String?,
            @RequestParam(name = "status", required = false, defaultValue = "unconsumed") status: String,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "1") pageNumber: Int,
            @RequestParam(name = "pageSize", required = false, defaultValue = "50") pageSize: Int
    ): ResponseEntity<*> {

        return try {
            val stateStatus = Vault.StateStatus.valueOf(status.toUpperCase())

            if (externalId.isNullOrBlank()) {
                val fundingResponses = vaultService
                        .getPagedItems(pageNumber, pageSize, stateStatus)
                        .map { fundingResonseRequestMapper.map(it.state.data) }

                ResponseBuilder.ok(fundingResponses)
            } else {
                val fundingResponses = vaultService
                        .findByExternalId(externalId!!, pageNumber, pageSize, stateStatus)
                        .map { fundingResonseRequestMapper.map(it.state.data) }

                ResponseBuilder.ok(fundingResponses)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is IllegalArgumentException -> ResponseBuilder.badRequest(ex.message)
                else -> ResponseBuilder.internalServerError(ex.message)
            }
        }
    }

    @GetMapping(path = arrayOf("{externalId}"))
    fun getUnconsumedInvoiceStateByExternalId(@PathVariable externalId: String): ResponseEntity<*> {
        return try {
            val invoice = vaultService
                    .findByExternalId(externalId, status = Vault.StateStatus.UNCONSUMED)
                    .map { fundingResonseRequestMapper.map(it.state.data) }
                    .single()

            ResponseBuilder.ok(invoice)
        } catch (ex: Exception) {
            when (ex) {
                is IllegalArgumentException -> ResponseBuilder.badRequest(ex.message)
                else -> ResponseBuilder.internalServerError(ex.message)
            }
        }
    }

    @GetMapping(path = arrayOf("/count"))
    fun getUniqueInvoiceCount(): ResponseEntity<*> {
        return try {
            ResponseBuilder.ok(mapOf("count" to vaultService.getCount()))
        } catch (ex: Exception) {
            ResponseBuilder.internalServerError(ex.message)
        }
    }

    @GetMapping(path = arrayOf("/hash"))
    fun getMostRecentInvoiceHash(): ResponseEntity<*> {
        return try {
            ResponseBuilder.ok(mapOf("hash" to vaultService.getLatestHash()))
        } catch (ex: Exception) {
            ResponseBuilder.internalServerError(ex.message)
        }
    }

    @PostMapping(path = arrayOf("/issue"), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun issueInvoice(@RequestBody message: FundingResponseRequestMessage): ResponseEntity<*> {
        return try {
            val future = rpc.proxy.startTrackedFlow(::FundingResponseFlow, message)
            future.progress.subscribe { println(it) }
            val result = future.returnValue.getOrThrow()
            val response = TransactionResponseMessage(
                    assetIds = result.tx.outputsOfType<FundingResponseState>().map { it.linearId },
                    transactionId = result.tx.id.toString()
            )

            ResponseBuilder.ok(response)
        } catch (ex: Exception) {
            when (ex) {
                is ValidationException -> ResponseBuilder.validationFailed(ex.validationMessages)
                else -> ResponseBuilder.internalServerError(ex.message)
            }
        }
    }


}