package com.ibtehaj.Ecom.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Models.SaleStatus;

import org.springframework.mail.SimpleMailMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private SaleItemService saleItemService;

    public void sendResetPasswordConfirmationEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Confirm Password Reset");
        message.setText("Please click the following link to confirm your password reset: " + resetUrl);
        javaMailSender.send(message);
    }
    
    public void sendSaleConfirmationEmail(Sale sale) {
        SimpleMailMessage message = new SimpleMailMessage();
        String toEmail = sale.getCustomer().getEmail();
        BigDecimal totalAmount = sale.getTotalAmount();
        LocalDateTime dateTime = sale.getSaleDateTime();
        SaleStatus status = sale.getStatus();
        List<SaleItem> saleItems = saleItemService.getAllSaleItemsBySale(sale);
        StringBuilder emailText = new StringBuilder();
        emailText.append("Dear " + sale.getCustomer().getCustomerName() + ",\n\n");
        emailText.append("Thank you for your purchase on " + dateTime.toString() + ".\n");
        emailText.append("Here is your order summary with order id:"+ sale.getId()+"\n\n");
        for(SaleItem saleItem : saleItems) {
            Product product = saleItem.getProductStock().getProduct();
            String productName = product.getProductName();
            int unitsBought = saleItem.getUnitsBought();
            BigDecimal unitPrice = saleItem.getUnitPrice();
            BigDecimal subTotal = saleItem.getSubTotal();
            emailText.append(productName + " (x" + unitsBought + ") - $" + unitPrice + " each - $" + subTotal + "\n");
        }
        emailText.append("\nTotal Amount: $" + totalAmount + "\n");
        emailText.append("Status: " + status.toString() + "\n\n");
        emailText.append("Thank you for shopping with us!\n");
        emailText.append("Best regards,\n");
        emailText.append("Ibtehaj");
        message.setTo(toEmail);
        message.setSubject("Order Confirmation - " + dateTime.toString());
        message.setText(emailText.toString());
        javaMailSender.send(message);
    }
}

