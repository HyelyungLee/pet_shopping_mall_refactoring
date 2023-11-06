package com.shop.app.review.service;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import com.shop.app.common.entity.ImageAttachment;
import com.shop.app.pet.entity.Pet;
import com.shop.app.product.dto.ProductDetailDto;
import com.shop.app.product.dto.ProductInfoDto;
import com.shop.app.review.dto.ProductReviewAvgDto;
import com.shop.app.review.dto.ReviewDetailDto;
import com.shop.app.review.dto.ReviewListDto;
import com.shop.app.review.dto.ReviewProductDto;
import com.shop.app.review.entity.Review;
import com.shop.app.review.entity.ReviewDetails;

public interface ReviewService {

	// 리뷰 작성
	int insertReview(Review review);

	// 리뷰 삭제
	int reviewDelete(int reviewId);

	// 내가 쓴 리뷰 목록 조회
	List<ReviewListDto> findReviewAll(Map<String, Object> params);
	
	// 리뷰 상세조회
	ReviewDetailDto findReviewId(int reviewId);

	// 리뷰 상세 - 이미지 조회
	ReviewDetails findImageAttachmentsByReviewId(int reviewId);

	// 리뷰 상세 - 구매한 상품내역 조회
	ReviewProductDto findProductReviewId(int reviewId);

	// 리뷰 수정
	int updateReview(Review review);

	// 리뷰 전체 카운트
	int findTotalReviewCount(String reviewMemberId);

	// 상품 - 리뷰 평균 별점
	ProductReviewAvgDto productReviewStarAvg(int productId);

	// 상품 상세 페이지 - 리뷰 전체개수 확인
	int findProductListReviewTotalCount(int productId);
   
	List<Integer> getStarRate(int productId);

	ReviewDetails createReview(@Valid ReviewCreateDto _review, List<ImageAttachment> attachments, Pet pet);

}