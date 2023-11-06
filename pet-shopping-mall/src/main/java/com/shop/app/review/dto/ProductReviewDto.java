package com.shop.app.review.dto;

import java.sql.Timestamp;
import java.util.List;

import com.shop.app.common.entity.ImageAttachment;
import com.shop.app.pet.entity.Pet;
import com.shop.app.product.dto.ProductDetailDto;
import com.shop.app.product.entity.ProductDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class ProductReviewDto {

	private int reviewId;
	private int reviewStarRate;
	private String reviewMemberId;
	private String reviewTitle;
	private String reviewContent;
	private Timestamp reviewCreatedAt;
	
	private List<ImageAttachment> reviewImages;
	
	private List<Pet> pets;
	
}
