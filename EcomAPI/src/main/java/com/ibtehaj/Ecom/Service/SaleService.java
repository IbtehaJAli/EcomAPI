package com.ibtehaj.Ecom.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ibtehaj.Ecom.Models.CustomerProfile;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleStatus;
import com.ibtehaj.Ecom.Repository.SaleRepository;

@Service
public class SaleService {
private final SaleRepository saleRepository;
	
	public SaleService(SaleRepository saleRepository) {
		this.saleRepository = saleRepository;
	}

	public void saveSale(Sale sale) {
		saleRepository.save(sale);
	}

	public Sale getSaleById(Long Id) {
		return saleRepository.findById(Id).orElse(null);
	}
	
	public List<Sale> getSaleByCustomerProfile(CustomerProfile customer) {
		return saleRepository.findByCustomer(customer);
	}
	
	public List<Sale> getSaleByCustomerId(Long customerId) {
		return saleRepository.findByCustomerId(customerId);
	}
	
	public List<Sale> getAllSales(){
		return saleRepository.findAll();
	}
	
	public void deleteSale(Sale sale) {
		saleRepository.delete(sale);
	}
	
	public boolean updateSaleStatus(Long saleId, SaleStatus status) {
		 Optional<Sale> optionalSale = saleRepository.findById(saleId);
		 if (optionalSale.isPresent()) {
		        Sale sale = optionalSale.get();
		        sale.setStatus(status);
		        saleRepository.save(sale);
		        return true;
		    }else {
		    	return false;
		    }
	}
	/*public boolean deleteSale(CustomerProfile customer) {
		Optional<Sale> optionalSale = saleRepository.findByCustomerProfile(customer);
		if (optionalSale.isPresent()) {
			Sale sale = optionalSale.get();
			saleRepository.delete(sale);
			return true;
		}else {
			return false;
		}
	}*/

	
}
