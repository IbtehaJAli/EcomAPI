package com.ibtehaj.Ecom.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibtehaj.Ecom.Models.Review;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Repository.ReviewRepository;
import com.ibtehaj.Ecom.Repository.SaleRepository;
import com.ibtehaj.Ecom.Service.SaleItemService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
public class DataExportController {
	
	@Autowired
	private SaleRepository saleRepository;
	@Autowired
	private SaleItemService saleItemService;
	@Autowired
	private ReviewRepository reviewRepository;


    @GetMapping("/sales-data")
    public ResponseEntity<Map<String, Object>> exportSalesData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // Fetch the List<Sale> and List<Review> data from your repository
        List<Sale> sales = saleRepository.findBySaleDateTimeBetween(startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MIDNIGHT.minus(1, ChronoUnit.SECONDS)));
        List<Review> reviews = reviewRepository.findByDateTimeBetween(startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));
        //find the sale items for our sales
        List<SaleItem> saleItems = new ArrayList<>();
        for(Sale sale :sales) {
        	saleItems.addAll(saleItemService.getAllSaleItemsBySale(sale));
        }

        // Create a map to structure the data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("sales", sales);
        responseData.put("reviews", reviews);
        responseData.put("saleItems", saleItems);

        return ResponseEntity.ok(responseData);
    }

}
