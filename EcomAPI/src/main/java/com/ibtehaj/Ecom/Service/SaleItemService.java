package com.ibtehaj.Ecom.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Models.CustomerProfile;
import com.ibtehaj.Ecom.Models.ProductStock;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Models.SaleStatus;
import com.ibtehaj.Ecom.Repository.ProductStockRepository;
import com.ibtehaj.Ecom.Repository.SaleItemRepository;
import com.ibtehaj.Ecom.Repository.SaleRepository;

import jakarta.transaction.Transactional;

@Service
public class SaleItemService {

	private final SaleItemRepository saleItemRepository;
	private final SaleRepository saleRepository;
	private final ProductStockRepository productStockRepository;
	
	@Autowired
	private  RabbitTemplate rabbitTemplate;
	
	public SaleItemService(SaleItemRepository saleItemRepository, SaleRepository saleRepository,ProductStockRepository productStockRepository) {
		this.saleItemRepository = saleItemRepository;
		this.saleRepository = saleRepository;
		this.productStockRepository = productStockRepository;
	}

	public void createSaleItem(SaleItem saleItem) {
		saleItemRepository.save(saleItem);
	}
	
	public SaleItem getSaleItemById(Long Id) {
		return saleItemRepository.findById(Id).orElse(null);
	}
	
	public List<SaleItem> getAllSaleItemsBySale (Sale sale){
		return saleItemRepository.findBySale(sale);
	}
	
	public List<SaleItem> getAllSaleItemsBySaleId(Long saleId) {
		return saleItemRepository.findBySaleId(saleId);
	}
	
	public List<SaleItem> getAllSaleItems (){
		return saleItemRepository.findAll();
	}
	
	@Transactional
	public boolean deleteAllSaleItemsforSale(Long saleId, CustomerProfile customer) throws CustomAccessDeniedException {
	    // Retrieve the sale by by sale ID
	    Optional<Sale> optionalSale = saleRepository.findById(saleId);
	    if (optionalSale.isPresent()) {
	        Sale sale = optionalSale.get();
	        if (sale.getCustomer().getEmail().equals(customer.getEmail())) {
	            if (!sale.getStatus().equals(SaleStatus.PENDING)) {
	                throw new CustomAccessDeniedException(customer.getCustomerName() + " you are not allowed to delete this order.");
	            }
	        List<SaleItem> saleItems = saleItemRepository.findBySale(sale);
	        for (SaleItem saleItem : saleItems) {
	        	ProductStock productStock = saleItem.getProductStock();
	        	int returningQuantity = saleItem.getUnitsBought();
	        	Long remainingQunatity = productStock.getAvailableUnits();
	        	productStock.setAvailableUnits(remainingQunatity+returningQuantity);
	        	productStockRepository.save(productStock);
				rabbitTemplate.convertAndSend("product.stock.update", productStock.getProduct().getCode());
	        	saleItemRepository.delete(saleItem);
	        	//also delete the sale entity
	        	saleRepository.delete(sale);
	        }
	        System.out.println("sale items deleted");
	        }else {
	        	throw new CustomAccessDeniedException(customer.getCustomerName()+" you are not allowed to delete this order");
	        } 
	        return true;
	    } else {
	        return false;
	    }
	}

}
