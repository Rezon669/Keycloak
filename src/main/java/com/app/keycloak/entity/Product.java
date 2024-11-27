package com.app.keycloak.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long productid;

	@Column(nullable = false, length = 45)
	private String productname;

	@Column(nullable = false, length = 10)
	private int price;

	@Column(nullable = false, length = 10)
	private int quantity;

	@Column(nullable = false, length = 45)
	private String category;

	@Column(nullable = false, length = 45)
	private String searchkeyword;

	public long getProductid() {
		return productid;
	}

	public void setProductid(long productid) {
		this.productid = productid;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSearchkeyword() {
		return searchkeyword;
	}

	public void setSearchkeyword(String searchkeyword) {
		this.searchkeyword = searchkeyword;
	}

	@Override
	public String toString() {
		return "Product [productid=" + productid + ", productname=" + productname + ", price=" + price + ", quantity=" + quantity + ", category="
				+ category + ", searchkeyword=" + searchkeyword + "]";
	}

}
