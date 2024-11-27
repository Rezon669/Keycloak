package com.app.keycloak.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.keycloak.entity.Product;
import com.app.keycloak.exceptions.CustomException;
import com.app.keycloak.repository.ProductRepository;

@Service
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public String addProducts(Product product) throws CustomException {
        logger.info("Attempting to add product: {}", product);
        
        if (product.getProductname() == null || product.getProductname().isEmpty()) {
            logger.warn("Product name is mandatory");
            throw new CustomException("Product name is mandatory");
        }
        if (product.getCategory() == null || product.getCategory().isEmpty() || product.getCategory().equalsIgnoreCase("select one")) {
            logger.warn("Invalid category selection");
            throw new CustomException("Please select a valid category type");
        }
        if (product.getSearchkeyword() == null || product.getSearchkeyword().isEmpty()) {
            logger.warn("Search keyword is mandatory");
            throw new CustomException("Search keyword is mandatory");
        }
        if (product.getPrice() <= 0) {
            logger.warn("Price must be greater than zero");
            throw new CustomException("Price must be greater than zero");
        }
        if (product.getQuantity() <= 0) {
            logger.warn("Quantity must be greater than zero");
            throw new CustomException("Quantity must be greater than zero");
        }

        productRepository.save(product);
        logger.info("Product added successfully: {}", product);
        return "Product details are added successfully";
    }

    public List<Product> getProducts() throws CustomException {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            logger.warn("No products found");
            throw new CustomException("No products are found");
        }
        logger.info("Products retrieved successfully: {}", products);
        return products;
    }

    public List<Product> searchProduct(String searchKeyword) throws CustomException {
        logger.info("Searching products with keyword: {}", searchKeyword);
        List<Product> products = productRepository.searchProduct(searchKeyword);
        if (products.isEmpty()) {
            logger.warn("No products found for keyword: {}", searchKeyword);
            throw new CustomException("No products are found with the given search keyword");
        }
        logger.info("Products found for keyword: {}", searchKeyword);
        return products;
    }

    public Product getProductById(Long id) throws CustomException {
        logger.info("Fetching product with ID: {}", id);
        return productRepository.findById(id).orElseThrow(() -> {
            logger.warn("Product not found with ID: {}", id);
            return new CustomException("Product not found");
        });
    }

    public void updateProduct(Product updatedProduct, Long id) throws CustomException {
        logger.info("Updating product with ID: {}", id);
        Product existingProduct = getProductById(id); // Ensures product exists or throws exception
        productRepository.updateProduct(
            updatedProduct.getProductname(),
            updatedProduct.getPrice(),
            updatedProduct.getQuantity(),
            updatedProduct.getCategory(),
            updatedProduct.getSearchkeyword(),
            id
        );
        logger.info("Product updated successfully with ID: {}", id);
    }

    public void deleteProduct(Product existingProduct) throws CustomException {
        logger.info("Deleting product: {}", existingProduct);
        if (existingProduct == null) {
            logger.warn("Attempted to delete a null product");
            throw new CustomException("Product cannot be null");
        }
        productRepository.delete(existingProduct);
        logger.info("Product deleted successfully: {}", existingProduct);
    }

    public Product getProductById(Object id) throws CustomException {
        logger.info("Fetching product details by object ID: {}", id);
        Product product = productRepository.getProductDetails(id);
        if (product == null) {
            logger.warn("No product found for ID: {}", id);
            throw new CustomException("Product not found");
        }
        return product;
    }
}
