package com.app.keycloak.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.keycloak.entity.Product;
import com.app.keycloak.exceptions.CustomException;
import com.app.keycloak.service.ProductService;

@RestController
@RequestMapping("/easybuy")
public class ProductController {

    private static final Logger logger = LogManager.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @PostMapping("/admin/addproduct")
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        logger.info("Attempting to add product: {}", product);
        try {
            productService.addProducts(product);
            logger.info("Product added successfully: {}", product);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (CustomException e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add product");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/user/getproducts")
    public ResponseEntity<?> getProducts() {
        try {
            List<Product> products = productService.getProducts();
            return ResponseEntity.ok(products);
        } catch (CustomException e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch products");
        }
    }

    @GetMapping("/user/searchproducts")
    public ResponseEntity<?> searchProducts(@RequestParam("searchkeyword") String searchKeyword) {
        logger.info("Searching products with keyword: {}", searchKeyword);
        try {
            List<Product> products = productService.searchProduct(searchKeyword);
            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No products found for the given keyword");
            }
            return ResponseEntity.ok(products);
        } catch (CustomException e) {
            logger.error("Error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to search products");
        }
    }

    @PutMapping("/admin/updateproduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        logger.info("Updating product with ID: {}", id);
        try {
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            productService.updateProduct(updatedProduct, id);
            return ResponseEntity.ok("Product updated successfully");
        } catch (CustomException e) {
            logger.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update product");
        }
    }

    @DeleteMapping("/admin/deleteproduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with ID: {}", id);
        try {
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            productService.deleteProduct(existingProduct);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (CustomException e) {
            logger.error("Error deleting product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete product");
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        logger.info("Finding product by ID: {}", id);
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            return ResponseEntity.ok(product);
        } catch (CustomException e) {
            logger.error("Error fetching product by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch product");
        }
    }

    @GetMapping("/user/productlist")
    public ResponseEntity<?> findAllByIds(@RequestParam("ids") List<Long> productIds) {
        logger.info("Fetching products by IDs: {}", productIds);
        try {
            List<Product> products = productIds.stream().map(id -> {
                try {
                    return productService.getProductById(id);
                } catch (CustomException e) {
                    logger.error("Error fetching product by ID: {}", id, e);
                    return null;
                }
            }).filter(product -> product != null).collect(Collectors.toList());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error fetching products by IDs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch products");
        }
    }
}
