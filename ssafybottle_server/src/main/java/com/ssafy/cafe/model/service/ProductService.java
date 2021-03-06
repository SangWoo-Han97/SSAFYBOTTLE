package com.ssafy.cafe.model.service;

import java.util.List;
import java.util.Map;

import com.ssafy.cafe.model.dto.Product;

public interface ProductService {
	
	/**
	 * 모든 상품 정보를 반환한다.
	 * @return
	 */
	List<Product> getProductList();
	
	List<Product> getBeverage();
	
	List<Product> getDessert();
	
	List<Product> searchProduct(String productName);

	/**
	 * backend 관통 과정에서 추가됨
	 * 상품의 정보, 판매량, 평점 정보를 함께 반환
	 * @param productId
	 * @return
	 */
	List<Map<String, Object>> selectWithComment(Integer productId);
	
	List<Map<String, Object>> recommendByRating();
	
	List<Map<String, Object>> recommendBySell();
	
	int getSalesAll();
	
	List<Product> getSalesOne();

}
