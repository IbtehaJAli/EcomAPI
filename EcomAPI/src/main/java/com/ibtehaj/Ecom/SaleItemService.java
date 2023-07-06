package com.ibtehaj.Ecom;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SaleItemService {

	private final SaleItemRepository saleItemRepository;
	
	public SaleItemService(SaleItemRepository saleItemRepository) {
		this.saleItemRepository = saleItemRepository;
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
	
	public List<SaleItem> getAllSaleItems (){
		return saleItemRepository.findAll();
	}
}
