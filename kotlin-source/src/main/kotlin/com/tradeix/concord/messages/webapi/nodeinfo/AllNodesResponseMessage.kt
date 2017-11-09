package com.tradeix.concord.messages.webapi.nodeinfo

import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class AllNodesResponseMessage(val nodes: List<CordaX500Name>)