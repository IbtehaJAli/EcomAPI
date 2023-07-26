package com.ibtehaj.Ecom.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Service.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
@RestController
public class JsonSimilarityController {

	private final ProductService productService;

    public JsonSimilarityController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/similarProducts/{productId}")
    public List<Product> findSimilarProducts(@PathVariable Long productId) throws Exception {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new Exception("Product not found with ID: " + productId);
            }

            List<Product> allProducts = productService.getAllProducts();
            List<Product> similarProducts = new ArrayList<>();

            // Convert the attributes of the given product into a set of key-value pairs
            Set<String> targetAttributesSet = convertAttributesToSet(product.getAttributes());

            for (Product otherProduct : allProducts) {
                // Avoid comparing the given product with itself
                if (!otherProduct.getId().equals(productId)) {
                    // Convert the attributes of the other product into a set of key-value pairs
                    Set<String> otherAttributesSet = convertAttributesToSet(otherProduct.getAttributes());

                    // Compute the Jaccard index between the two attribute sets
                    double jaccardIndex = computeJaccardIndex(targetAttributesSet, otherAttributesSet);

                    // If the similarity percentage is greater or equal to 50%, consider it as similar
                    if (jaccardIndex >= 0.5) {
                        similarProducts.add(otherProduct);
                    }
                }
            }

            return similarProducts;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error occurred while finding similar products.");
        }
    }

    // A helper method to convert the attributes of a product into a set of key-value pairs
    public static Set<String> convertAttributesToSet(String attributes) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(attributes);
            return convertJsonToSet(node);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error occurred while converting attributes to set.");
        }
    }

    // A helper method to convert a JsonNode into a set of key-value pairs
    public static Set<String> convertJsonToSet(JsonNode node) {
        Set<String> set = new HashSet<>();
        // Iterate over the fields of the node
        node.fields().forEachRemaining(entry -> {
            // Get the key and the value as strings
            String key = entry.getKey();
            String value = entry.getValue().toString();
            // Add the key-value pair to the set
            set.add(key + ":" + value);
        });
        return set;
    }

    
    // A helper method to compute the Jaccard index between two sets
    public static double computeJaccardIndex(Set<String> set1, Set<String> set2) {
        // Create a copy of set1 and set2 to avoid modifying them
        Set<String> intersection = new HashSet<>(set1);
        Set<String> union = new HashSet<>(set1);

        // Retain only the elements that are common to both sets
        intersection.retainAll(set2);

        // Add all the elements that are in either set
        union.addAll(set2);

        // Return the ratio of the intersection size to the union size
        return (double) intersection.size() / union.size();
    }

}
