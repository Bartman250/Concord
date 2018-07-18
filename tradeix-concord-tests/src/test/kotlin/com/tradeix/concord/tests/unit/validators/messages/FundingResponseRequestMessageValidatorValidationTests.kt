package com.tradeix.concord.tests.unit.validators.messages

import com.tradeix.concord.shared.mockdata.MockFundingResponses
import com.tradeix.concord.shared.validators.FundingResponseRequestMessageValidator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FundingResponseRequestMessageValidatorValidationTests {

    @Test
    fun `FundingResponseRequestMessageValidator produces the expected validation messages`(){
        val validator = FundingResponseRequestMessageValidator()
        val expectedValidationMessages = listOf(
                "Property 'externalId' must not be null, empty or blank.",
                "Property 'invoiceExternalIds' must not be null.",
                "Property 'invoiceExternalIds' must contain at least one element.",
                "Property 'invoiceExternalIds[index]' must not be null, empty or blank.",
                "Property 'supplier' must not be null.",
                "Property 'supplier' must be a valid X500 name.",
                "Property 'funder' must be a valid X500 name.",
                "Property 'purchaseValue' must not be null.",
                "Property 'purchaseValue' must be greater than the specified value.",
                "Property 'currency' must not be null.",
                "Property 'currency' must be a valid currency code.",
                "Property 'advanceInvoiceValue' must not be null.",
                "Property 'advanceInvoiceValue' must be greater than the specified value.",
                "Property 'discountValue' must not be null.",
                "Property 'discountValue' must be greater than the specified value.",
                "Property 'baseRate' must not be null.",
                "Property 'baseRate' must be greater than the specified value."
                )
        val actualValidationMessages: Iterable<String> = validator.getValidationMessages()

        assertEquals(expectedValidationMessages.count(), actualValidationMessages.count())
        expectedValidationMessages.forEach {
            assertTrue {
                actualValidationMessages.contains(it)
            }
        }
    }

    @Test
    fun `FundingResponseRequestMessageValidator does not throw a ValidationException when the message state is valid`() {
        val message = MockFundingResponses.FUNDING_RESPONSE_REQUEST_MESSAGE
        val validator = FundingResponseRequestMessageValidator()
        validator.validate(message)
    }
}