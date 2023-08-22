package com.ibtehaj.Ecom.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
import com.ibtehaj.Ecom.Models.Review;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Models.SalesAnalysisReport;
import com.ibtehaj.Ecom.Repository.ReviewRepository;
import com.ibtehaj.Ecom.Repository.SaleRepository;
import com.ibtehaj.Ecom.Service.SaleItemService;

@RestController
@RequestMapping("/api/reports")
public class SalesAnalysisController {

	@Autowired
	private SaleRepository saleRepository;
	@Autowired
	private SaleItemService saleItemService;
	@Autowired
	private ReviewRepository reviewRepository;

	@GetMapping("/sales-analysis")
	public ResponseEntity<?> generateSalesAnalysisReport(
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			@RequestParam(name = "sortSaleItemsBy", required = false) String sortBy) {

		List<Sale> sales = saleRepository.findBySaleDateTimeBetween(startDate.atStartOfDay(),
				endDate.atTime(23, 59, 59));
		List<Review> reviews = reviewRepository.findByDateTimeBetween(startDate.atStartOfDay(),
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
		Product productWithHighestRevenue = new Product(); // product that produced highest revenue
		Product productWithLowestRevenue = new Product(); // product that produced lowest revenue
		Product mostReviewedProduct = new Product(); //product that was reviewed most // not implemented yet
		Product leastReviewedProduct = new Product(); // product that was least reviewed // not implemented yet
		Product productWithHighestRating = new Product(); // product with highest rating
		Product productWithLowestRating = new Product(); // product with lowest rating

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
		productWithMaxUnits = unitsBoughtByProduct.entrySet().stream().max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).orElse(null);
		// Find the product with the minimum units bought
		productWithMaxUnits = unitsBoughtByProduct.entrySet().stream().min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).orElse(null);
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
		        Collectors.summingInt(SaleItem::getUnitsBought)// Calculate the sum of units bought for each date
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
		
		// find the product with highest revenue
		productWithHighestRevenue = totalRevenueByProduct.entrySet().stream().max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).orElse(null);
		
		//find the product with least revenue
		productWithLowestRevenue = totalRevenueByProduct.entrySet().stream().min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).orElse(null);
		
		//find product with highest rating
		productWithHighestRating = reviews.stream()
		    .collect(Collectors.groupingBy(
		        review -> review.getProduct(), // Group reviews by their product
		        Collectors.summingInt(Review::getRating)// Calculate the sum of ratings for each product
		    ))
		    .entrySet().stream()
		    .max(Map.Entry.comparingByValue()) // Find the entry with the highest sum of rating
		    .map(Map.Entry::getKey) // Get the corresponding product key
		    .orElse(null); // If no product found, assign null to productWithHighestRating	
		
		//find product with lowest rating
		productWithLowestRating = reviews.stream()
		    .collect(Collectors.groupingBy(
		        review -> review.getProduct(), // Group reviews by their product
		        Collectors.summingInt(Review::getRating)// Calculate the sum of ratings for each product
		    ))
		    .entrySet().stream()
		    .min(Map.Entry.comparingByValue()) // Find the entry with the least sum of rating
		    .map(Map.Entry::getKey) // Get the corresponding product key
		    .orElse(null); // If no product found, assign null to productWithLowestRating	
		
		//sort the saleItems list according to passed parameter
		if(sortBy.equals("date")) {
			saleItems.sort(Comparator.comparing(s->s.getSale().getSaleDateTime()));
		}else if(sortBy.equals("totalAmount")) {
			saleItems.sort(Comparator.comparing(s->s.getSale().getTotalAmount()));
			Collections.reverse(saleItems);
		}else if(sortBy.equals("subTotal")) {
			saleItems.sort(Comparator.comparing(SaleItem::getSubTotal));
		}else if(sortBy.equals("unitsBought")) {
			saleItems.sort(Comparator.comparing(SaleItem::getUnitsBought));
		}
		
		
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
	    report.setProductWithHighestRevenue(productWithHighestRevenue);
	    report.setProductWithLowestRevenue(productWithLowestRevenue);
//	    report.setMostReviewedProduct();
//	    report.setLeastReviewedProduct();
	    report.setProductWithHighestRating(productWithHighestRating);
	    report.setProductWithLowestRating(productWithLowestRating);
		return ResponseEntity.ok(report);
	}
}
