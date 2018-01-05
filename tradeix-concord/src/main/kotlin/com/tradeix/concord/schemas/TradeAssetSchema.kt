package com.tradeix.concord.schemas

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object TradeAssetSchema

object TradeAssetSchemaV1 : MappedSchema(
        schemaFamily = TradeAssetSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentTradeAssetSchemaV1::class.java)) {
    @Entity
    @Table(name = "trade_asset_states")
    class PersistentTradeAssetSchemaV1(
            @Column(name = "external_id")
            var externalId: String,

            @Column(name = "linear_id")
            var linearId: UUID,

            @Column(name = "value")
            var value: BigDecimal,

            @Column(name = "status")
            var status: String,

            @Column(name = "owner")
            var owner: AbstractParty,

            @Column(name = "buyer")
            var buyer: AbstractParty,

            @Column(name = "supplier")
            var supplier: AbstractParty,

            @Column(name = "conductor")
            var conductor: AbstractParty
    ) : PersistentState()
}