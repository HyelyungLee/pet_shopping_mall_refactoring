package com.shop.app.review.service;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.app.review.dto.ReviewCreateDto;
import com.shop.app.common.entity.ImageAttachment;
import com.shop.app.order.repository.OrderRepository;
import com.shop.app.order.service.OrderService;
import com.shop.app.pet.entity.Pet;
import com.shop.app.pet.repository.PetRepository;
import com.shop.app.pet.service.PetService;
import com.shop.app.product.dto.ProductDetailDto;
import com.shop.app.product.dto.ProductInfoDto;
import com.shop.app.product.entity.Product;
import com.shop.app.product.repository.ProductRepository;
import com.shop.app.review.dto.ProductReviewAvgDto;
import com.shop.app.review.dto.ReviewDetailDto;
import com.shop.app.review.dto.ReviewListDto;
import com.shop.app.review.dto.ReviewProductDto;
import com.shop.app.review.entity.Review;
import com.shop.app.review.entity.ReviewDetails;
import com.shop.app.review.repository.ReviewRepository;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private PetService petService;

	@Autowired
	private ReviewRepository reviewRepository;
	
	@Autowired
	private PetRepository petRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	// 리뷰 추가 db저장
	public ReviewDetails createReview(ReviewCreateDto _review, List<ImageAttachment> attachments, Pet pet) {
		
		ReviewDetails reviews = ReviewDetails.builder()
				.reviewId(_review.getReviewId())
				.petId(pet.getPetId())
				.orderId(_review.getOrderId())
				.productId(_review.getProductId())
				.productDetailId(_review.getProductDetailId())
				.reviewMemberId(_review.getReviewMemberId())
				.reviewStarRate(_review.getReviewStarRate())
				.reviewTitle(_review.getReviewTitle())
				.reviewContent(_review.getReviewContent())
				.attachments(attachments)
				.build();
		
		// 리뷰 작성자의 petId 연결하기
		String memberId = _review.getReviewMemberId();
		List<Pet> petInfo = petService.findPetsByMemberId(memberId); // 리뷰작성자의 펫정보 가져오기
		
		if (!petInfo.isEmpty()) { // 펫정보가 비어있지 않다면
			Pet firstPet = petInfo.get(0); // 첫번째 Pet 객체 가져오기
			reviews.setPetId(firstPet.getPetId()); // db에 pet정보 저장
		} else {
			reviews.setPetId(null);
		}
		return reviews;
	}
	
	// 리뷰추가
	@Override
	public int insertReview(Review review) {
		int result = 0;
		// review 저장
		result = reviewRepository.insertReview(review);
		int refId = review.getReviewId();
		
		// attachment 저장
		List<ImageAttachment> attachments = ((ReviewDetails) review).getAttachments();
		if(attachments != null && !attachments.isEmpty()) {
			for(ImageAttachment attach : attachments) {
				
				// 1. 이미지 파일 DB에 저장
				int result2 = reviewRepository.insertAttachment(attach);
				// 2. 이미지 파일 DB 저장 후 생성된 이미지 ID 가져오기
				int imageId = attach.getImageId();
				// 3. 리뷰 ID와 이미지 ID를 사용하여 매핑 정보를 DB에 저장
				int reviewIdImageId = reviewRepository.insertMapping(refId, imageId);
				
			}
		}
		int orderId = review.getOrderId();
		int productDetailId = review.getProductDetailId();
		int productId = review.getProductId();
		int newStatus = 6;
		orderRepository.updateOrderStatusWithDetail(orderId, productDetailId, newStatus, productId);
		
		return result;
	}

	// 리뷰 삭제
	@Override
	public int reviewDelete(int reviewId) {
		return reviewRepository.reviewDelete(reviewId);
	}

	// 내가 쓴 리뷰목록 조회
	@Override
	public List<ReviewListDto> findReviewAll(Map<String, Object> params) {
		int limit = (int) params.get("limit");
		int page = (int) params.get("page");
		int offset = (page - 1) * limit;
		String reviewMemberId = (String) params.get("reviewMemberId");
		RowBounds rowBounds = new RowBounds(offset, limit);
		return reviewRepository.findReviewAll(reviewMemberId, rowBounds);
	}

	// 리뷰 전체 카운트
	@Override
	public int findTotalReviewCount(String reviewMemberId) {
		return reviewRepository.findTotalReviewCount(reviewMemberId);
	}

	// 리뷰 상세조회
	@Override
	@Transactional
	public ReviewDetailDto findReviewId(int reviewId) {
		Review review = reviewRepository.findReviewId(reviewId);
		
	    ReviewDetailDto reviewDetailDto = new ReviewDetailDto();
	    reviewDetailDto.setReviewId(review.getReviewId());
	    reviewDetailDto.setReviewTitle(review.getReviewTitle());
	    reviewDetailDto.setReviewContent(review.getReviewContent());
	    reviewDetailDto.setReviewStarRate(review.getReviewStarRate());
	    reviewDetailDto.setReviewCreatedAt(review.getReviewCreatedAt());

	    // 펫정보 연결
	    Integer petId = review.getPetId();

	    // 펫정보가 없는 경우
	    if (petId != null ) {
	    Pet pet = petRepository.findPetById(review.getPetId());
	    reviewDetailDto.setPetId(pet.getPetId());
	    reviewDetailDto.setPetName(pet.getPetName());
	    reviewDetailDto.setPetAge(pet.getPetAge());
	    reviewDetailDto.setPetBreed(pet.getPetBreed());
	    reviewDetailDto.setPetWeight(pet.getPetWeight());
	    reviewDetailDto.setPetGender(pet.getPetGender());
	    
	    } 
	    
	    return reviewDetailDto;
	}
	

	// 리뷰 상세조회 - 이미지 조회
	@Override
	public ReviewDetails findImageAttachmentsByReviewId(int reviewId) {
		return reviewRepository.findImageAttachmentsByReviewId(reviewId);
	}
	
	// 리뷰 상세조회 - 상품조회
	@Override
	public ReviewProductDto findProductReviewId(int reviewId) {
		return reviewRepository.findProductReviewId(reviewId);
	}

	// 리뷰 수정
	@Override
	public int updateReview(Review review) {
		return reviewRepository.updateReview(review);
	}

	// 상품 - 리뷰 평점
	@Override
	public ProductReviewAvgDto productReviewStarAvg(int productId) {
		return reviewRepository.productReviewStarAvg(productId);
	}

	@Override
	public int findProductListReviewTotalCount(int productId) {
		return reviewRepository.findProductListReviewTotalCount(productId);
	}

	@Override
	public List<Integer> getStarRate(int productId) {
		return reviewRepository.getStarRate(productId);
	}


}





