package com.ibtehaj.Ecom;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;

@Service
public class StripeService {
	
	public Charge ProcessPayment(String stripeSecretKey, BigDecimal finalTotalAmount, Sale sale, CustomerProfile customer, String stripeToken) throws StripeException {
		Stripe.apiKey = stripeSecretKey;
		ChargeCreateParams params = ChargeCreateParams.builder()
				.setAmount(finalTotalAmount.multiply(BigDecimal.valueOf(100)).longValue())
				.setCurrency("usd").setDescription(customer.getEmail()+" Order Id: "+sale.getId().toString())
				.putMetadata("order_id", sale.getId().toString())
				.putMetadata("Customer_Email", customer.getEmail())
				.putMetadata("Customer_Name", customer.getCustomerName())
				.putMetadata("Customer_Phone", customer.getPhone())
				.setSource(stripeToken) // Payment token, card token, or source ID
				.setReceiptEmail(customer.getEmail())
				.setShipping(ChargeCreateParams.Shipping.builder().setName(customer.getCustomerName())
						.setPhone(customer.getPhone())
						.setAddress(ChargeCreateParams.Shipping.Address.builder()
								.setLine1("123 Main Street").setLine2("Apt 4A").setCity(customer.getAddress())
								.setState("CA").setCountry("US").setPostalCode("12345").build())
						.build())
				.build();

		Charge charge = Charge.create(params); // to-do add exceptional handler, also delete sale
		return charge;
	}
	
}
