package com.shop.app.product.controller;

import java.lang.reflect.Member;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shop.app.common.entity.ImageAttachment;
import com.shop.app.common.entity.Thumbnail;
import com.shop.app.member.entity.MemberDetails;
import com.shop.app.member.service.MemberService;
import com.shop.app.order.dto.OrderReviewListDto;
import com.shop.app.order.service.OrderService;
import com.shop.app.pet.entity.Pet;
import com.shop.app.pet.service.PetService;
import com.shop.app.product.dto.ProductCreateDto;
import com.shop.app.product.dto.ProductDetailDto;
import com.shop.app.product.dto.ProductInfoDto;
import com.shop.app.product.dto.ProductSearchDto;
import com.shop.app.product.entity.Product;
import com.shop.app.product.entity.ProductCategory;
import com.shop.app.product.entity.ProductDetail;
import com.shop.app.product.entity.ProductImages;
import com.shop.app.product.service.ProductService;
import com.shop.app.review.dto.ProductReviewAvgDto;
import com.shop.app.review.dto.ProductReviewDto;
import com.shop.app.review.dto.ReviewDetailDto;
import com.shop.app.review.entity.Review;
import com.shop.app.review.entity.ReviewDetails;
import com.shop.app.review.service.ReviewService;
import com.shop.app.wishlist.service.WishlistService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/product")
@Controller
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private WishlistService wishlistService;

	@Autowired
	private PetService petService;

	/**
	 * @author 전수경
	 * 
	 * @author 이혜령 리뷰
	 * 
	 * @author 강선모 찜하기
	 */
	@GetMapping("/productDetail.do")
	public void productDetail(@RequestParam int productId, @RequestParam(defaultValue = "1") int page,
			@AuthenticationPrincipal MemberDetails member, Model model) {
		int limit = 3;
		Map<String, Object> params = Map.of("page", page, "limit", limit, "productId", productId);
		
		// 성능개선 리팩토링
		ProductDetailDto productDetail = productService.productDetails(productId, params);
		
		//log.debug("productDetail = {}", productDetail);
		
		int totalCount = productDetail.getReviewCnt();
		int totalPages = (int) Math.ceil((double) totalCount / limit);
		
		int[] starCounts = new int[6];
		
		List<Integer> reviewStarRate = reviewService.getStarRate(productId);
		
		for (Integer rate : reviewStarRate) {
			starCounts[rate]++;
		}

		double[] starPercentages = new double[6]; // 각 별점별 백분율을 저장할 배열

		for (int i = 1; i <= 5; i++) {
			starPercentages[i] = (double) starCounts[i] / totalCount * 100;
		}

		// starPercentages 배열을 계산하고, 백분율로 변환하여 소수점 없이 포맷팅한 리스트를 생성
		List<String> formattedPercentages = new ArrayList<>();
		for (double percentage : starPercentages) {
			String formatted = new DecimalFormat("###").format(percentage); // 소수점 없이 포맷팅
			formattedPercentages.add(formatted);
		}

		model.addAttribute("formattedPercentages", formattedPercentages);
		
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("productDetail", productDetail);
		
		if (member != null) {
			model.addAttribute("likeState", wishlistService.getLikeProduct(productId, member.getMemberId()));
		}
		
		
	}

	
	
	/**
	 * @author 김담희
	 * 상품 조회 후 반환
	 * 사용자가 정렬 기능을 선택했을 때, 정렬에 맞춰 값 반환
	 * 
	 * @author 전수경
	 * 페이지네이션 처리
	 */
	@GetMapping("/productList.do")
	public void productList(@RequestParam("categoryId") String _categoryId, @RequestParam(defaultValue = "1") int page,
			Model model, @RequestParam(required = false) String align) {

		int categoryId = Integer.parseInt(_categoryId);

		int limit = 8;

		Map<String, Object> params = Map.of("page", page, "limit", limit, "categoryId", categoryId);

		int totalCount = productService.findTotalProductCountByCategory(categoryId);
		int totalPages = (int) Math.ceil((double) totalCount / limit);

		model.addAttribute("totalPages", totalPages);
		
		ProductCategory category = productService.findProductCategoryById(categoryId);
		model.addAttribute("category", category);
		
		List<ProductSearchDto> productInfos = productService.searchProductsById(params);
		model.addAttribute("productInfos", productInfos);

		
		
		String alignType = "";
		String inOrder = "";

		if (align != null) {
			List<ProductSearchDto> _productInfos = null;
			if (align.equals("신상품")) {
				alignType = "byNewDate";

			} else if (align.equals("낮은가격")) {
				alignType = "byPrice";
				inOrder = "asc";

			} else if (align.equals("높은가격")) {
				alignType = "byPrice";
				inOrder = "desc";

			} else if (align.equals("별점높은순")) {
				alignType = "byHighReviewStar";

			} else {
				alignType = "byReviewCnt";
			}
			_productInfos = productService.alignProducts(categoryId, alignType, inOrder);
			model.addAttribute("productInfos", _productInfos);
		}
	}
	
	@PostMapping("/productList.do")
	public String productList(@RequestParam("categoryId") String categoryId, @RequestParam("align") String align) {
		return "redirect:/product/productList.do?categoryId=" + categoryId + "&align=" + align;
	}
	
	

	/**
	 * @author 강선모 -하트 클릭 이벤트
	 */
	@ResponseBody
	@PostMapping("/insertPick.do")
	public Map insertPick(@Valid @RequestBody Map<String, Object> param,
			@AuthenticationPrincipal MemberDetails member) {
		Map<String, Object> resultMap = new HashMap<>();
		String state = "insert".equals(param.get("state").toString()) ? "등록" : "삭제"; // 화면에서 받은 state 값으로 등록/삭제 여부 판단 ->
																						// 화면상 코드 : var state =
																						// $("#clickHeart").text().indexOf("♥")
																						// > -1 ? "delete" : "insert";
		String getProductId = param.get("productId").toString(); // 화면에서 받은 상품 Id
		int productId = getProductId.isEmpty() ? 0 : Integer.parseInt(getProductId);
		resultMap.put("rs", "fail");
		resultMap.put("msg", "찜 " + state + "에 실패하였습니다.");

		if (productId != 0) {
			// 데이터 Setting
			param.put("productId", productId);

			if ("insert".equals(param.get("state").toString())) {
				param.put("cnt", 1); // 쿼리에서 해당 상품의 찜 증가에 사용될 변수

				if (wishlistService.insertPick(productId, member.getMemberId()) > 0) { // wishList 테이블에 값 Insert :
																						// return값 : Insert 쿼리로 추가된 행 개수
					if (productService.updateLikeCnt(param) > 0) { // product 테이블의 찜 컬럼 변경
						resultMap.put("rs", "insertS");
						resultMap.put("msg", "찜 " + state + "에 성공하였습니다.");
					}
				}
			} else if ("delete".equals(param.get("state").toString())) {
				param.put("cnt", -1); // 쿼리에서 해당 상품의 찜 감소에 사용될 변수

				if (wishlistService.deletePick(productId, member.getMemberId()) > 0) { // wishList 테이블에 값 Delete :
																						// return값 : Delete 쿼리로 제거된 행 개수
					if (productService.updateLikeCnt(param) > 0) { // product 테이블의 찜 컬럼 변경
						resultMap.put("rs", "deleteS");
						resultMap.put("msg", "찜 " + state + "에 성공하였습니다.");
					}
				}
			}
		}
		return resultMap;
	}

	
	
	/**
	 * @author 김담희
	 * index 페이지에서 상품명 전체 검색 후 결과 반환
	 */
	@GetMapping("/searchProduct.do")
	public void searchProducts(Model model, @RequestParam String searchQuery) {
		List<ProductSearchDto> productInfos = productService.searchProducts(searchQuery);
		model.addAttribute("productInfos", productInfos);

	}

}