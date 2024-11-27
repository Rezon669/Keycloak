package com.app.keycloak.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.keycloak.entity.Product;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// List findBy(String Category, String category);

	// List findBy(String category, String category2);

	@Query("SELECT p FROM Product p WHERE p.searchkeyword LIKE %?1%")
	public List<Product> searchProduct(String searchkeyword);

	@Query("SELECT p FROM Product p WHERE p.productid=:id")
	public Product getProductDetails(Object id);

	@Modifying
	@Transactional
	@Query("UPDATE Product p SET p.productname = :productname, p.quantity = :quantity, p.price = :price, p.category = :category, p.searchkeyword = :searchkeyword WHERE p.id = :id")
	public void updateProduct(String productname, int price, int quantity, String category, String searchkeyword,
			Long id);

}