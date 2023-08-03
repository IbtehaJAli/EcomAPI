package com.ibtehaj.Ecom.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStock;
import com.ibtehaj.Ecom.Models.ProductSummary;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Repository.ProductRepository;
import com.ibtehaj.Ecom.Repository.ProductStockRepository;
import com.ibtehaj.Ecom.Requests.ProductRequest;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ReviewService reviewService;
    private final SaleItemService saleItemService;

    public ProductService(ProductRepository productRepository, ProductStockRepository productStockRepository, ReviewService reviewService, 
    		SaleItemService saleItemService) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
        this.reviewService = reviewService;
        this.saleItemService = saleItemService;
    }
    public Product findProductByCode(String code) {
    	return productRepository.findByCode(code);
    }
    public void createProduct(ProductRequest request) {
        // Create Product entity
        Product product = new Product();
        product.setProductName(request.getName());
        product.setCode(request.getCode());
        ObjectMapper mapper = new ObjectMapper();
        String str = null;
		try {
			str = mapper.writeValueAsString(request.getAttributes());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println("me doing");
			e.printStackTrace();
		}
        //System.out.println(str);
        product.setAttributes(str);
        
        productRepository.save(product);
        }
    
    public Product getProductById(Long productId) {
        // Retrieve the product by ID
        return productRepository.findById(productId).orElse(null);
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public boolean updateProduct(Long productId, ProductRequest request) {
        // Retrieve the product by ID
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            // Update the product properties
            product.setId(productId);
            product.setProductName(request.getName());
            product.setCode(request.getCode());
            ObjectMapper mapper = new ObjectMapper();
            String str = null;
    		try {
    			str = mapper.writeValueAsString(request.getAttributes());
    		} catch (JsonProcessingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            //System.out.println(str);
            product.setAttributes(str);
            // Save the updated product
            productRepository.save(product);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteProduct(Long productId) {
        // Retrieve the product by ID
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            // Delete the product
            productRepository.delete(product);
            return true;
        } else {
            return false;
        }
    }
    
    public List<ProductSummary> getProductListWithStockSummary(String sortBy) {
        // Retrieve list of all products
        List<Product> productList = productRepository.findAll();

        // Create a new list for storing products with stock summary
        List<ProductSummary> productListWithStockSummary = new ArrayList<>();

        // Iterate over each product in the list
        for (Product product : productList) {
            // Retrieve all product stocks for the current product
            List<ProductStock> productStockList = productStockRepository.findByProduct(product);

            // Calculate the total available units and weighted average unit price for the product
            Long totalAvailableUnits = 0L;
            BigDecimal totalUnitPrice = BigDecimal.ZERO;
            for (ProductStock productStock : productStockList) {
                totalAvailableUnits += productStock.getAvailableUnits();
                totalUnitPrice = totalUnitPrice.add(productStock.getUnitPrice().multiply(BigDecimal.valueOf(productStock.getAvailableUnits())));
            }
            if (totalAvailableUnits == 0) {
                continue; // Skip this product if total available units is zero
            }
            BigDecimal weightedAvgUnitPrice = totalUnitPrice.divide(BigDecimal.valueOf(totalAvailableUnits), 2, RoundingMode.HALF_UP);
            
            // Create a new ProductSummary instance with the stock summary information and add it to the list
            ProductSummary productSummary = new ProductSummary();
            productSummary.setId(product.getId());
            productSummary.setProductName(product.getProductName());
            productSummary.setCode(product.getCode());
            productSummary.setAttributes(product.getAttributes());
            productSummary.setTotalAvailableUnits(totalAvailableUnits);
            productSummary.setWeightedAvgUnitPrice(weightedAvgUnitPrice);
            productSummary.setReviewCount(reviewService.findReviewsByProduct(product).size());
            productSummary.setAverageRating(reviewService.getAverageRatingForProduct(product));
            productSummary.setTotalUnitsSold(saleItemService.findSaleItemsByProduct(product).stream().mapToInt(SaleItem::getUnitsBought).sum()); // Set total units sold
            productSummary.setLastestDate(productStockList.stream()
                    .max(Comparator.comparing(ProductStock::getStockDate))// returns the object with latest date
                    .map(ProductStock::getStockDate)// to extract the desired property i-e date in this case, from the productStock object
                    .orElse(null));
            productListWithStockSummary.add(productSummary);
        }
        // Sort the list based on the sortBy parameter
		if ("lowToHigh".equals(sortBy)) {
			productListWithStockSummary.sort(Comparator.comparing(ProductSummary::getWeightedAvgUnitPrice));
		} else if ("highToLow".equals(sortBy)) {
			productListWithStockSummary.sort(Comparator.comparing(ProductSummary::getWeightedAvgUnitPrice).reversed());
		} else if ("nameAsc".equals(sortBy)) {
			productListWithStockSummary.sort(Comparator.comparing(ProductSummary::getProductName));
		} else if ("nameDesc".equals(sortBy)) {
			productListWithStockSummary.sort(Comparator.comparing(ProductSummary::getProductName).reversed());
		} else if ("mostReviewed".equals(sortBy)) {
			productListWithStockSummary.sort(Comparator.comparingInt(ProductSummary::getReviewCount).reversed());
		} else if ("topRated".equals(sortBy)) {
			productListWithStockSummary.sort(Comparator.comparingDouble(ProductSummary::getAverageRating).reversed());
		} else if ("bestSelling".equals(sortBy)) {
	        productListWithStockSummary.sort(Comparator.comparingInt(ProductSummary::getTotalUnitsSold).reversed());
	    } else if ("oldestFirst".equals(sortBy)) {
	        productListWithStockSummary.sort(Comparator.comparing(ProductSummary::getLastestDate));
	    } else if ("recentFirst".equals(sortBy)) {
	            productListWithStockSummary.sort(Comparator.comparing(ProductSummary::getLastestDate).reversed());
	        }


        return productListWithStockSummary;
    }
    
    public List<ProductSummary> searchProductListByKeyword(List<ProductSummary> productList, String keyword) {
        List<ProductSummary> matchedProducts = new ArrayList<>();
        Set<String> keywordTokens = new HashSet<>(Arrays.asList(keyword.split("\\s+")));

        for (ProductSummary productSummary : productList) {
            String attributesJson = productSummary.getAttributes();

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode attributesNode = objectMapper.readTree(attributesJson);

                if (hasAllTokensInJsonNode(attributesNode, keywordTokens)) {
                    matchedProducts.add(productSummary);
                }
            } catch (JsonProcessingException e) {
                // Handle JSON processing exception
            }
        }

        return matchedProducts;
    }

    public boolean hasAllTokensInJsonNode(JsonNode node, Set<String> tokens) {
        Set<String> values = getNodeValues(node);

        return values.containsAll(tokens);
    }

    public Set<String> getNodeValues(JsonNode node) {
        Set<String> values = new HashSet<>();

        if (node.isTextual()) {
            values.add(node.asText());
        }

        if (node.isArray()) {
            for (JsonNode childNode : node) {
                values.addAll(getNodeValues(childNode));
            }
        }

        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                values.addAll(getNodeValues(node.get(fieldName)));
            }
        }

        return values;
    }

}
