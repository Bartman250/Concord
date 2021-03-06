package com.tradeix.concord.services.messaging.subscribers

import com.google.gson.Gson
import com.tradeix.concord.messages.rabbit.purchaseorder.PurchaseOrderOwnershipRequestMessage
import com.typesafe.config.Config
import net.corda.core.messaging.CordaRPCOps

class PurchaseOrderChangeOwnerFlowQueuesSubscriber(
        cordaRpcService: CordaRPCOps,
        config: Config,
        serializer: Gson
) : FlowQueuesSubscriber<PurchaseOrderOwnershipRequestMessage>(
        cordaRpcService,
        config,
        serializer,
        "tix-integration",
        "purchaseOrderCancellationConsumeConfiguration",
        "purchaseOrderCancellationDeadLetterConfiguration",
        "purchaseOrderCancellationResponseConfiguration",
        "cordatix_cancel_purchase_order_response",
        PurchaseOrderOwnershipRequestMessage::class.java
)