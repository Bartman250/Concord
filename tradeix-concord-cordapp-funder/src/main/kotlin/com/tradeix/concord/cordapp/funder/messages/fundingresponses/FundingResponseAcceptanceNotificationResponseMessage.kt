package com.tradeix.concord.cordapp.funder.messages.fundingresponses

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class FundingResponseAcceptanceNotificationResponseMessage(val batchUploadId: String)