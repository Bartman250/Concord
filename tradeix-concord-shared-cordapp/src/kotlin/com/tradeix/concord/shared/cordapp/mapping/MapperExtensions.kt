package com.tradeix.concord.shared.cordapp.mapping

import com.tradeix.concord.shared.cordapp.mapping.purchaseorders.PurchaseOrderAmendmentMapperConfiguration
import com.tradeix.concord.shared.cordapp.mapping.purchaseorders.PurchaseOrderIssuanceMapperConfiguration
import com.tradeix.concord.shared.data.VaultRepository
import com.tradeix.concord.shared.domain.states.InvoiceState
import com.tradeix.concord.shared.domain.states.PurchaseOrderState
import com.tradeix.concord.shared.extensions.fromValueAndCurrency
import com.tradeix.concord.shared.extensions.getPartyFromLegalNameOrMe
import com.tradeix.concord.shared.extensions.getPartyFromLegalNameOrNull
import com.tradeix.concord.shared.extensions.tryParse
import com.tradeix.concord.shared.mapper.Mapper
import com.tradeix.concord.shared.mapper.MapperConfiguration
import com.tradeix.concord.shared.mapper.ServiceHubMapperConfiguration
import com.tradeix.concord.shared.messages.invoices.InvoiceRequestMessage
import com.tradeix.concord.shared.messages.purchaseorders.PurchaseOrderRequestMessage
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import java.util.*

fun Mapper.registerInvoiceMappers() {

    this.addConfiguration("issuance", object : ServiceHubMapperConfiguration<InvoiceRequestMessage, InvoiceState>() {
        override fun map(source: InvoiceRequestMessage, serviceHub: ServiceHub): InvoiceState {
            val repository = VaultRepository.fromServiceHub<InvoiceState>(serviceHub)

            val state = repository
                    .findByExternalId(source.externalId!!, status = Vault.StateStatus.UNCONSUMED)
                    .singleOrNull()

            if (state != null) {
                throw FlowException("An InvoiceState with '${source.externalId}' already exists.")
            }

            val buyer = serviceHub.networkMapCache.getPartyFromLegalNameOrNull(
                    CordaX500Name.tryParse(source.buyer)
            )

            val supplier = serviceHub.networkMapCache.getPartyFromLegalNameOrMe(
                    serviceHub,
                    CordaX500Name.tryParse(source.supplier)
            )

            return InvoiceState(
                    linearId = UniqueIdentifier(source.externalId!!),
                    owner = supplier,
                    buyer = buyer,
                    supplier = supplier,
                    invoiceVersion = source.invoiceVersion!!,
                    invoiceVersionDate = source.invoiceVersionDate!!,
                    tixInvoiceVersion = source.tixInvoiceVersion!!,
                    invoiceNumber = source.invoiceNumber!!,
                    invoiceType = source.invoiceType!!,
                    reference = source.reference!!,
                    dueDate = source.dueDate!!,
                    offerId = source.offerId,
                    amount = Amount.fromValueAndCurrency(source.amount!!, source.currency!!),
                    totalOutstanding = Amount.fromValueAndCurrency(source.totalOutstanding!!, source.currency!!),
                    created = source.created!!,
                    updated = source.updated!!,
                    expectedSettlementDate = source.expectedSettlementDate!!,
                    settlementDate = source.settlementDate,
                    mandatoryReconciliationDate = source.mandatoryReconciliationDate,
                    invoiceDate = source.invoiceDate!!,
                    status = source.status!!,
                    rejectionReason = source.rejectionReason,
                    eligibleValue = Amount.fromValueAndCurrency(source.eligibleValue!!, source.currency!!),
                    invoicePurchaseValue = Amount.fromValueAndCurrency(source.invoicePurchaseValue!!, source.currency!!),
                    tradeDate = source.tradeDate,
                    tradePaymentDate = source.tradePaymentDate,
                    invoicePayments = Amount.fromValueAndCurrency(source.invoicePayments!!, source.currency!!),
                    invoiceDilutions = Amount.fromValueAndCurrency(source.invoiceDilutions!!, source.currency!!),
                    cancelled = source.cancelled!!,
                    closeDate = source.closeDate,
                    originationNetwork = source.originationNetwork!!,
                    currency = Currency.getInstance(source.currency!!),
                    siteId = source.siteId!!,
                    purchaseOrderNumber = source.purchaseOrderNumber!!,
                    purchaseOrderId = source.purchaseOrderId!!,
                    composerProgramId = source.composerProgramId
            )
        }
    })

    this.addConfiguration("amendment", object : ServiceHubMapperConfiguration<InvoiceRequestMessage, InvoiceState>() {
        override fun map(source: InvoiceRequestMessage, serviceHub: ServiceHub): InvoiceState {
            val repository = VaultRepository.fromServiceHub<InvoiceState>(serviceHub)

            val inputState = repository
                    .findByExternalId(source.externalId!!, status = Vault.StateStatus.UNCONSUMED)
                    .singleOrNull()

            if (inputState == null) {
                throw FlowException("InvoiceState with externalId '${source.externalId}' does not exist.")
            } else {

                val buyer = serviceHub.networkMapCache.getPartyFromLegalNameOrNull(
                        CordaX500Name.tryParse(source.buyer)
                )

                val supplier = serviceHub.networkMapCache.getPartyFromLegalNameOrMe(
                        serviceHub,
                        CordaX500Name.tryParse(source.supplier)
                )

                return inputState.state.data.copy(
                        owner = supplier,
                        buyer = buyer,
                        supplier = supplier,
                        invoiceVersion = source.invoiceVersion!!,
                        invoiceVersionDate = source.invoiceVersionDate!!,
                        tixInvoiceVersion = source.tixInvoiceVersion!!,
                        invoiceNumber = source.invoiceNumber!!,
                        invoiceType = source.invoiceType!!,
                        reference = source.reference!!,
                        dueDate = source.dueDate!!,
                        offerId = source.offerId,
                        amount = Amount.fromValueAndCurrency(source.amount!!, source.currency!!),
                        totalOutstanding = Amount.fromValueAndCurrency(source.totalOutstanding!!, source.currency!!),
                        created = source.created!!,
                        updated = source.updated!!,
                        expectedSettlementDate = source.expectedSettlementDate!!,
                        settlementDate = source.settlementDate,
                        mandatoryReconciliationDate = source.mandatoryReconciliationDate,
                        invoiceDate = source.invoiceDate!!,
                        status = source.status!!,
                        rejectionReason = source.rejectionReason!!,
                        eligibleValue = Amount.fromValueAndCurrency(source.eligibleValue!!, source.currency!!),
                        invoicePurchaseValue = Amount.fromValueAndCurrency(source.invoicePurchaseValue!!, source.currency!!),
                        tradeDate = source.tradeDate,
                        tradePaymentDate = source.tradePaymentDate,
                        invoicePayments = Amount.fromValueAndCurrency(source.invoicePayments!!, source.currency!!),
                        invoiceDilutions = Amount.fromValueAndCurrency(source.invoiceDilutions!!, source.currency!!),
                        cancelled = source.cancelled!!,
                        closeDate = source.closeDate,
                        originationNetwork = source.originationNetwork!!,
                        currency = Currency.getInstance(source.currency!!),
                        siteId = source.siteId!!,
                        purchaseOrderNumber = source.purchaseOrderNumber!!,
                        purchaseOrderId = source.purchaseOrderId!!,
                        composerProgramId = source.composerProgramId
                )
            }
        }
    })

    this.addConfiguration("response", object : MapperConfiguration<InvoiceState, InvoiceRequestMessage>() {
        override fun map(source: InvoiceState): InvoiceRequestMessage {
            return InvoiceRequestMessage(
                    externalId = source.linearId.externalId,
                    buyer = source.buyer?.nameOrNull()?.toString(),
                    supplier = source.supplier.nameOrNull()?.toString(),
                    invoiceVersion = source.invoiceVersion,
                    invoiceVersionDate = source.invoiceVersionDate,
                    tixInvoiceVersion = source.tixInvoiceVersion,
                    invoiceNumber = source.invoiceNumber,
                    invoiceType = source.invoiceType,
                    reference = source.reference,
                    dueDate = source.dueDate,
                    offerId = source.offerId,
                    amount = source.amount.toDecimal(),
                    totalOutstanding = source.totalOutstanding.toDecimal(),
                    created = source.created,
                    updated = source.updated,
                    expectedSettlementDate = source.expectedSettlementDate,
                    settlementDate = source.settlementDate,
                    mandatoryReconciliationDate = source.mandatoryReconciliationDate,
                    invoiceDate = source.invoiceDate,
                    status = source.status,
                    rejectionReason = source.rejectionReason,
                    eligibleValue = source.eligibleValue.toDecimal(),
                    invoicePurchaseValue = source.invoicePurchaseValue.toDecimal(),
                    tradeDate = source.tradeDate,
                    tradePaymentDate = source.tradePaymentDate,
                    invoicePayments = source.invoicePayments.toDecimal(),
                    invoiceDilutions = source.invoiceDilutions.toDecimal(),
                    cancelled = source.cancelled,
                    closeDate = source.closeDate,
                    originationNetwork = source.originationNetwork,
                    currency = source.currency.currencyCode,
                    siteId = source.siteId,
                    purchaseOrderNumber = source.purchaseOrderNumber,
                    purchaseOrderId = source.purchaseOrderId,
                    composerProgramId = source.composerProgramId
            )
        }
    })
}

fun Mapper.registerPurchaseOrderMappers() {
    this.addConfiguration("issuance", PurchaseOrderIssuanceMapperConfiguration())
    this.addConfiguration("amendment", PurchaseOrderAmendmentMapperConfiguration())
}