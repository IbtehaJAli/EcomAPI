package com.ibtehaj.Ecom.Service;

import com.ibtehaj.Ecom.Models.CustomerProfile;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.Review;
import com.ibtehaj.Ecom.Repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Create a new review
    public void createReview(Review review) {
        reviewRepository.save(review);
    }

    // Get all reviews
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Find review by customer
    public List<Review> findReviewsByCustomer(CustomerProfile customer) {
        return reviewRepository.findByCustomer(customer);
    }

    // Find review by product
    public List<Review> findReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    // Find review by ID
    public Optional<Review> findReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Update review
    public void updateReview(Review review) {
         reviewRepository.save(review);
    }

    // Delete review by ID
    public void deleteReviewById(Long id) {
        reviewRepository.deleteById(id);
    }

    // Check if a review already exists for a product by a customer
    public boolean doesReviewExist(Long productId, Long customerId) {
        return reviewRepository.existsByProduct_IdAndCustomer_Id(productId, customerId);
    }

    // Find review by product and customer
    public Review getReviewByProductAndCustomer(Long productId, Long customerId) {
        return reviewRepository.findByProduct_IdAndCustomer_Id(productId, customerId);
    }
    
    //Find the average rating for a product
    public double getAverageRatingForProduct(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);

        if (reviews.isEmpty()) {
            return 0.0; // Return 0 if there are no reviews
        }

        double totalRating = 0.0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }

        return totalRating / reviews.size();
    }
}
