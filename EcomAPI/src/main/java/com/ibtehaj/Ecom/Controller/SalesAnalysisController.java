package com.ibtehaj.Ecom.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStock;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Models.SalesAnalysisReport;
import com.ibtehaj.Ecom.Repository.SaleRepository;
import com.ibtehaj.Ecom.Service.SaleItemService;

@RestController
@RequestMapping("/api/reports")
public class SalesAnalysisController {

	@Autowired
	private SaleRepository saleRepository;
	@Autowired
	private SaleItemService saleItemService;

	@GetMapping("/sales-analysis")
	public ResponseEntity<?> generateSalesAnalysisReport(
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

		List<Sale> sales = saleRepository.findBySaleDateTimeBetween(startDate.atStartOfDay(),
				endDate.atTime(23, 59, 59));

		BigDecimal totalSales = BigDecimal.ZERO; //total revenue 
		List<SaleItem> saleItems = new ArrayList<>(); // all sale items for all sales
		Integer totalUnitsSold = 0; // total units sold
		Product productWithMaxUnits = new Product(); // product with max units sold
		Product productWithMinUnits = new Product(); // product with least units sold
		Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>(); // Tracks revenue by date
		LocalDate dateWithHighestRevenue; // date where highest revenue was generated
		LocalDate dateWithLowestRevenue; // date where least revenue was generated
		LocalDate dateWithMostUnitsBought; // date with highest number of units were sold
		LocalDate dateWithLeastUnitsBought; // date where least number of units were sold
		Map<Product, BigDecimal> totalRevenueByProduct = new HashMap<>(); // Tracks revenue by product
		Map<Product, Integer> unitsBoughtByProduct = new HashMap<>(); // Tracks units bought by product

		for (Sale sale : sales) {
			totalSales.add(sale.getTotalAmount());
			saleItems.addAll(saleItemService.getAllSaleItemsBySale(sale));
			// Increment revenue for the sale date
			LocalDate saleDate = sale.getSaleDateTime().toLocalDate();
			BigDecimal currentRevenue = revenueByDate.getOrDefault(saleDate, BigDecimal.ZERO);
			revenueByDate.put(saleDate, currentRevenue.add(sale.getTotalAmount()));
			
		}
		for (SaleItem saleItem : saleItems) {
			totalUnitsSold += saleItem.getUnitsBought();
			
			// Calculate total revenue by each product
	        Product product = saleItem.getProductStock().getProduct();
	        BigDecimal productRevenue = totalRevenueByProduct.getOrDefault(product, BigDecimal.ZERO);
	        totalRevenueByProduct.put(product, productRevenue.add(saleItem.getSubTotal()));
	        
	        // Calculate units bought by each product
	        Integer productUnits  = unitsBoughtByProduct.getOrDefault(product, 0);
	        unitsBoughtByProduct.put(product, productUnits+saleItem.getUnitsBought());
			
		}
		// Find the product with the maximum units bought
		productWithMaxUnits = saleItems.stream().max(Comparator.comparingInt(SaleItem::getUnitsBought))
				.map(SaleItem::getProductStock).map(ProductStock::getProduct).orElse(null);
		// Find the product with the minimum units bought
		productWithMaxUnits = saleItems.stream().min(Comparator.comparingInt(SaleItem::getUnitsBought))
				.map(SaleItem::getProductStock).map(ProductStock::getProduct).orElse(null);
		// Find the date with the highest revenue
		dateWithHighestRevenue = revenueByDate.entrySet().stream().max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).orElse(null);
		// Find the date with the lowest revenue
		dateWithLowestRevenue = revenueByDate.entrySet().stream()
		    .min(Map.Entry.comparingByValue())
		    .map(Map.Entry::getKey)
		    .orElse(null);
		// Find the date with the most units bought
		dateWithMostUnitsBought = saleItems.stream()
		    .collect(Collectors.groupingBy(
		        saleItem -> saleItem.getSale().getSaleDateTime().toLocalDate(), // Group SaleItems by their sale date
		        Collectors.summingInt(SaleItem::getUnitsBought) // Calculate the sum of units bought for each date
		    ))
		    .entrySet().stream()
		    .max(Map.Entry.comparingByValue()) // Find the entry with the highest sum of units bought
		    .map(Map.Entry::getKey) // Get the corresponding LocalDate key
		    .orElse(null); // If no date found, assign null to dateWithMostUnitsBought	
		
//		We start by creating a stream from the saleItems list using saleItems.stream().
//		We then apply the collect() operation to group the SaleItem objects by their sale date and calculate the sum of units bought for each date. This operation returns a map where the keys are the sale dates (LocalDate objects) and the values are the sums of units bought (Integer values).
//		After collecting the data into the map, we retrieve the entry set of the map using entrySet().
//		Next, we create a stream from the entry set using stream().
//		We apply the max() operation on the stream of entries, using Map.Entry.comparingByValue() as the comparator to find the entry with the highest sum of units bought.
//		Finally, we retrieve the corresponding LocalDate key using map(Map.Entry::getKey) and assign it to the dateWithMostUnitsBought variable.
		
		// Find the date with the most units bought
		dateWithLeastUnitsBought = saleItems.stream()
		    .collect(Collectors.groupingBy(
		        saleItem -> saleItem.getSale().getSaleDateTime().toLocalDate(), // Group SaleItems by their sale date
		        Collectors.summingInt(SaleItem::getUnitsBought) // Calculate the sum of units bought for each date
		    ))
		    .entrySet().stream()
		    .min(Map.Entry.comparingByValue()) // Find the entry with the least sum of units bought
		    .map(Map.Entry::getKey) // Get the corresponding LocalDate key
		    .orElse(null); // If no date found, assign null to dateWithMostUnitsBought	
		
		// Sort the map entries based on the sale date
		revenueByDate = revenueByDate.entrySet()
		        .stream()
		        .sorted(Map.Entry.comparingByKey())
		        .collect(Collectors.toMap(
		                Map.Entry::getKey,
		                Map.Entry::getValue,
		                (oldValue, newValue) -> oldValue,
		                TreeMap::new // Use TreeMap to store the sorted entries
		        ));
		
		SalesAnalysisReport report = new SalesAnalysisReport();
		report.setTotalSales(totalSales);
	    report.setSaleItems(saleItems);
	    report.setTotalUnitsSold(totalUnitsSold);
	    report.setProductWithMaxUnits(productWithMaxUnits);
	    report.setProductWithMinUnits(productWithMinUnits);
	    report.setRevenueByDate(revenueByDate);
	    report.setDateWithHighestRevenue(dateWithHighestRevenue);
	    report.setDateWithLowestRevenue(dateWithLowestRevenue);
	    report.setDateWithMostUnitsBought(dateWithMostUnitsBought);
	    report.setDateWithLeastUnitsBought(dateWithLeastUnitsBought);
	    report.setTotalRevenueByProduct(totalRevenueByProduct);
	    report.setUnitsBoughtByProduct(unitsBoughtByProduct);
	    
		return ResponseEntity.ok(report);
	}
}
