package com.tradeix.concord.shared.messages

import com.tradeix.concord.shared.messages.invoices.InvoiceRequestMessage

typealias InvoiceTransactionRequestMessage = TransactionRequestMessage<InvoiceRequestMessage>
typealias InvoiceEligibilityTransactionRequestMessage = TransactionRequestMessage<InvoiceEligibilityRequestMessage>
