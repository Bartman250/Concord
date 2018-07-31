package com.tradeix.concord.cordapp.supplier.flows.invoices

import co.paralleluniverse.fibers.Suspendable
import com.tradeix.concord.cordapp.supplier.mappers.invoices.InvoiceIssuanceMapper
import com.tradeix.concord.cordapp.supplier.messages.invoices.InvoiceTransactionRequestMessage
import com.tradeix.concord.cordapp.supplier.validators.invoices.InvoiceTransactionRequestMessageValidator
import com.tradeix.concord.shared.cordapp.flows.CollectSignaturesInitiatorFlow
import com.tradeix.concord.shared.cordapp.flows.ObserveTransactionInitiatorFlow
import com.tradeix.concord.shared.domain.contracts.InvoiceContract
import com.tradeix.concord.shared.domain.contracts.InvoiceContract.Companion.INVOICE_CONTRACT_ID
import com.tradeix.concord.shared.domain.states.InvoiceState
import com.tradeix.concord.shared.extensions.*
import com.tradeix.concord.shared.services.IdentityService
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

@StartableByRPC
@InitiatingFlow
class InvoiceIssuanceInitiatorFlow(
        private val message: InvoiceTransactionRequestMessage
) : FlowLogic<SignedTransaction>() {

    override val progressTracker = getProgressTrackerWithObservationStep()

    @Suspendable
    override fun call(): SignedTransaction {

        val validator = InvoiceTransactionRequestMessageValidator()
        val identityService = IdentityService(serviceHub)
        val mapper = InvoiceIssuanceMapper(serviceHub)

        validator.validate(message)

        // Step 1 - Generating Unsigned Transaction
        progressTracker.currentStep = GeneratingTransactionStep
        val invoiceOutputStates: Iterable<InvoiceState> = mapper.mapMany(message.assets)

        val command = Command(
                InvoiceContract.Issue(),
                identityService.getParticipants(invoiceOutputStates).toOwningKeys())

        val transactionBuilder = TransactionBuilder(identityService.getNotary())
                .addOutputStates(invoiceOutputStates, INVOICE_CONTRACT_ID)
                .addCommand(command)

        Configurator.setLevel(logger.name, Level.DEBUG)
        invoiceOutputStates.forEach {
            logger.debug("*** INVOICE ISSUANCE >> Invoice external Id: " + it.linearId.externalId)
        }

        // Step 2 - Validate Unsigned Transaction
        progressTracker.currentStep = ValidatingTransactionStep
        transactionBuilder.verify(serviceHub)

        // Step 3 - Sign Unsigned Transaction
        progressTracker.currentStep = SigningTransactionStep
        val partiallySignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        // Step 4 - Gather Counterparty Signatures
        progressTracker.currentStep = GatheringSignaturesStep
        val fullySignedTransaction = subFlow(
                CollectSignaturesInitiatorFlow(
                        partiallySignedTransaction,
                        identityService.getWellKnownParticipantsExceptMe(invoiceOutputStates),
                        GatheringSignaturesStep.childProgressTracker()
                )
        )

        // Step 5 - Finalize Transaction
        progressTracker.currentStep = FinalizingTransactionStep
        val finalizedTransaction = subFlow(
                FinalityFlow(
                        fullySignedTransaction,
                        FinalizingTransactionStep.childProgressTracker()
                )
        )

        // Step 6 - Send Transaction To Observers
        progressTracker.currentStep = SendTransactionToObserversStep
        subFlow(
                ObserveTransactionInitiatorFlow(
                        fullySignedTransaction,
                        message.observers.map { identityService.getPartyFromLegalNameOrThrow(CordaX500Name.parse(it)) }
                )
        )

        return finalizedTransaction
    }
}