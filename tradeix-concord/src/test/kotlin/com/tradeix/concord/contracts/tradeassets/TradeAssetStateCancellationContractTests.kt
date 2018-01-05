package com.tradeix.concord.contracts.tradeassets

import com.tradeix.concord.TestValueHelper.BUYER
import com.tradeix.concord.TestValueHelper.BUYER_PUBKEY
import com.tradeix.concord.TestValueHelper.CONDUCTOR
import com.tradeix.concord.TestValueHelper.CONDUCTOR_PUBKEY
import com.tradeix.concord.TestValueHelper.FUNDER
import com.tradeix.concord.TestValueHelper.LINEAR_ID
import com.tradeix.concord.TestValueHelper.ONE_POUND
import com.tradeix.concord.TestValueHelper.SUPPLIER
import com.tradeix.concord.TestValueHelper.SUPPLIER_PUBKEY
import com.tradeix.concord.contracts.TradeAssetContract
import com.tradeix.concord.contracts.TradeAssetContract.Companion.TRADE_ASSET_CONTRACT_ID
import com.tradeix.concord.states.TradeAssetState
import net.corda.testing.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class TradeAssetStateCancellationContractTests {
    @Before
    fun setup() {
        setCordappPackages("com.tradeix.concord.contracts")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }

    @Test
    fun `On cancellation, the transaction must include the Cancel command`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                fails()
                command(BUYER_PUBKEY, SUPPLIER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                verifies()
            }
        }
    }

    @Test
    fun `On cancellation, only one input should be consumed`() {
        ledger {
            transaction {
                output(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(BUYER_PUBKEY, SUPPLIER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On cancellation, zero output states should be created`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                output(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(BUYER_PUBKEY, SUPPLIER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On cancellation, nobody can cancel unless the owner is the supplier`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = FUNDER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(BUYER_PUBKEY, SUPPLIER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_CANCEL)
            }
        }
    }

    @Test
    fun `On cancellation, all participants must sign the transaction (owner must sign)`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(BUYER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `All participants must sign the transaction (buyer must sign)`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(SUPPLIER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `All participants must sign the transaction (supplier must sign)`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(BUYER_PUBKEY, CONDUCTOR_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `All participants must sign the transaction (conductor must sign)`() {
        ledger {
            transaction {
                input(TRADE_ASSET_CONTRACT_ID) {
                    TradeAssetState(
                            linearId = LINEAR_ID,
                            owner = SUPPLIER,
                            buyer = BUYER,
                            supplier = SUPPLIER,
                            conductor = CONDUCTOR,
                            amount = ONE_POUND,
                            status = TradeAssetState.TradeAssetStatus.INVOICE)
                }
                command(BUYER_PUBKEY, SUPPLIER_PUBKEY) {
                    TradeAssetContract.Commands.Cancel()
                }
                failsWith(TradeAssetContract.Commands.Cancel.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}