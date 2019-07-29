package com.ule.purchase.mall.controller.cart;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ule.dealer.common.redis.CartCacheCloudClientUtil;
import com.ule.purchase.mall.api.dto.ResultDTO;
import com.ule.purchase.mall.api.dto.order.OrderAddressDto;
import com.ule.purchase.mall.architect.constants.ErrorMsg;
import com.ule.purchase.mall.controller.BaseController;
import com.ule.purchase.mall.dto.DealerMallItemDto;
import com.ule.purchase.mall.dto.DealerMallListingDto;
import com.ule.purchase.mall.dto.QueryMallParamDto;
import com.ule.purchase.mall.dto.UserDto;
import com.ule.purchase.mall.dto.cart.CartDto;
import com.ule.purchase.mall.dto.cart.CartListingDto;
import com.ule.purchase.mall.dto.cart.CartParamsDto;
import com.ule.purchase.mall.dto.cart.CartResultDto;
import com.ule.purchase.mall.dto.cart.PromotionDto;
import com.ule.purchase.mall.dto.cart.SeriesDto;
import com.ule.purchase.mall.service.cart.CartService;
import com.ule.purchase.mall.service.order.OrderBaseService;
import com.ule.purchase.mall.util.BeanMapUtil;
import com.ule.purchase.mall.util.CookieUtil;
import com.ule.purchase.mall.util.ItemSearchUtil;

/**经销商购物车功能
 * 添加
 * 更新
 * 删除
 * 清空
 * 查询
 * @author leizhuang
 *
 */
@RestController
@RequestMapping(value = "/cart")
public class CartController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(CartController.class);
	
	@Autowired
	private CartService cartService;
	@Autowired
	private OrderBaseService orderBaseService;
	
	private final String ITEMID = "itemId";
	private final String ITEMCOUNT = "itemCount";
	
	/**获取购物车
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	@PostMapping(value = "/cartView")
	public ResultDTO getCartView(HttpServletRequest request, CartParamsDto params) {
		logger.info("获取购物车参数：" + params);
		long startTime = System.currentTimeMillis();
		Object cartType = params.getCartType();
		if(verification(cartType)) {
			return ResultDTO.fail(ErrorMsg.PARAMISEMPTY.code(), ErrorMsg.PARAMISEMPTY.getMessage());
		}
		String userOnlyId = CookieUtil.getUsrOnlyid(request);
		String dealerId = CookieUtil.getMerchantId(request);
		String cartKey = userOnlyId+"_"+cartType;
		CartResultDto cartResultDto = new CartResultDto();
		cartResultDto.setCartType(cartType.toString());
		try {
			/*获取缓存购物车中所有商品
			 * Map<String, String> cart:
			 * 		key:itemId
			 * 		value:json->{"count":1,"creatTime":1535688987654}
			 */
			Map<String, String> cart = CartCacheCloudClientUtil.getCart(cartKey);
			Set<String> itemIdSet = cart.keySet();
			/*
			 *  1、遍历searchResult,将creatTime设置进去按creatTime排序
			 *  2、stream Collectors.partitioningBy分区，过滤出失效商品
			 *  3、将正常商品：
			 *  	a.先按起订量（该商家的系列）聚合商品
    		 *		b.再按促销聚合商品
    		 *	4、如果购物车商品数和搜索结果商品数不一致，清理缓存中的垃圾数据
			 */
			if(itemIdSet != null && itemIdSet.size() > 0) {
				// 缓存中商品id集合
				List<Long> itemids = new ArrayList<Long>();
				for (String itemid : itemIdSet) {
					itemids.add(Long.parseLong(itemid));
				}
				QueryMallParamDto queryMallParamDto = new QueryMallParamDto();
				queryMallParamDto.setItemIds(itemids);
				queryMallParamDto.setIsQueryAllIsSale((short)1);
				queryMallParamDto.setIsQueryAllItemState((short)1);
				queryMallParamDto.setDealerId(null);
				UserDto user = orderBaseService.getUser(Long.parseLong(userOnlyId));
				List<OrderAddressDto> orderAddressDtos = user.getOrderAddressDtos();
				if(orderAddressDtos == null || orderAddressDtos.size() <= 0) {
					logger.error("------------获取仓库异常----------------------");
					return ResultDTO.fail("获取仓库异常");
				}
				logger.info(MessageFormat.format("仓库所在provinceCode:{0}", orderAddressDtos.get(0).getTransProvinceCode()));
				queryMallParamDto.setAreaCode(orderAddressDtos.get(0).getTransProvinceCode());
				queryMallParamDto.setDealerId(Long.parseLong(dealerId));
				
				List<DealerMallListingDto> DealerMallListingDtoList = ItemSearchUtil.getDealerMallListing(queryMallParamDto);
				
				if(DealerMallListingDtoList != null && DealerMallListingDtoList.size() > 0) {
					/*
					 *	1、copy属性
					 *	2、设置购物车信息（数量，类型，加入时间）
					 *	3、按时间排序
					 *	4、分区（分为正常商品和失效商品）
					 */
					Map<Boolean, List<CartListingDto>> partitioningResult = DealerMallListingDtoList.stream()
							.map(list -> {
								List<DealerMallItemDto> dealMallItemDto = list.getDealMallItemDto();
								return dealMallItemDto.stream().map(item -> {
									CartListingDto cartListingDto = new CartListingDto();
									BeanMapUtil.copyBeanNotNull2Bean(list, cartListingDto);
									BeanMapUtil.copyBeanNotNull2Bean(item, cartListingDto);
									CartDto redisCart = JSONObject.parseObject(cart.get(item.getItemId() + ""),
											CartDto.class);
									cartListingDto.setItemCount(redisCart.getItemCount());
									cartListingDto.setCreateTime(new Date(redisCart.getCreatTime()));
									cartListingDto.setCartType(cartType.toString());
									return cartListingDto;
								}).collect(Collectors.toList());
								// 扁平化
							}).flatMap(List::stream)
							// 按加购时间排序
							.sorted(Comparator.comparing(CartListingDto::getCreateTime, Comparator.reverseOrder()))
							// 分区
							.collect(Collectors.partitioningBy(p -> p.getIsSale() == 0 && p.getItemState() == 0));
					// 设置失效商品
					cartResultDto.setInvalidListings(partitioningResult.get(false));
					/*
					 *	正常商品操作
					 *	1、按系列分组，设置系列信息
					 *	2、商品按促销类型分组，设置促销信息
					 */
					// 系列分组
					Map<String, List<CartListingDto>> seriesGroup = partitioningResult.get(true).stream().collect(Collectors
							.groupingBy(v -> groupingRules(v), LinkedHashMap::new, Collectors.toList()));
					// 系列集合
					List<SeriesDto> seriesList = new ArrayList<SeriesDto>();
					seriesGroup.forEach((k, v) -> {
						CartListingDto cartListingDto = v.get(0);
						// 系列
						SeriesDto seriesDto = new SeriesDto();
						seriesDto.setSeriesId(cartListingDto.getSeriesId());
						seriesDto.setSeriesName(cartListingDto.getSeriesName());
						seriesDto.setLimitType(cartListingDto.getLimitType());
						seriesDto.setLimitNum(cartListingDto.getLimitNum());
						seriesDto.setUnit(cartListingDto.getUnit());

						// 促销集合
						List<PromotionDto> promotionDtos = new ArrayList<PromotionDto>();
						try {
							// 促销分区（分为促销和普通商品集合）
							Map<Boolean, List<CartListingDto>> promotionPartitioning = v.stream()
									.collect(Collectors.partitioningBy(p -> !StringUtils.isEmpty(p.getActivityId())
											&& !StringUtils.isEmpty(p.getActivityDesc())));
							List<CartListingDto> isPromotion = promotionPartitioning.get(true);
							if(isPromotion != null && isPromotion.size() > 0) {
								// 促销商品进行分组
								Map<String, List<CartListingDto>> promotionGroup = isPromotion.stream()
								.collect(Collectors.groupingBy(groupingRules(CartListingDto::getActivityDesc),
										LinkedHashMap::new, Collectors.toList()));
								promotionGroup.forEach((k2, v2) -> {
									CartListingDto cartListingDto2 = v2.get(0);
									PromotionDto promotionDto = new PromotionDto();
									promotionDto.setActivityId(cartListingDto2.getActivityId());
									promotionDto.setActivityDesc(cartListingDto2.getActivityDesc());
									promotionDto.setCartListings(v2);
									promotionDtos.add(promotionDto);
								});
							}
							List<CartListingDto> noPromotion = promotionPartitioning.get(false);
							if(noPromotion != null && noPromotion.size() > 0) {
								PromotionDto promotionDto = new PromotionDto();
								promotionDto.setCartListings(noPromotion);
								promotionDtos.add(promotionDto);
							}
						} catch (Exception e) {
							logger.error("购物车促销分组异常:" + e.getMessage(), e);
						}
						seriesDto.setPromotionListingGroups(promotionDtos);
						seriesList.add(seriesDto);
					});
					// 设置系列商品集合
					cartResultDto.setSeriesListings(seriesList);
					
					// 3、如果购物车商品数和搜索结果商品数不一致，清理缓存中的垃圾数据
					Set<String> searchItems = DealerMallListingDtoList.stream().map(v -> {
						return v.getDealMallItemDto().stream().map(v2 -> v2.getItemId().toString())
								.collect(Collectors.toSet());
					}).flatMap(Set::stream).collect(Collectors.toSet());
					if (itemIdSet.size() != searchItems.size()) {
						itemIdSet.removeAll(searchItems);
						CartCacheCloudClientUtil.deleteCart(cartKey, itemIdSet.toArray(new String[] {}));
					}
					
					return ResultDTO.successWithData(cartResultDto);
				}
			}
		} catch (Exception e) {
			logger.error("获取购物车:"+e.getMessage(), e);
			return ResultDTO.fail(e.getMessage());
		} finally {
			logger.info("查询购物车耗时："+(System.currentTimeMillis()-startTime));
		}
		return ResultDTO.success();
	}
	
	/**添加购物车(支持多商品添加)
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/addCart")
	public ResultDTO addCart(HttpServletRequest request, CartParamsDto params) {
		Object cartType = params.getCartType();
		String details = params.getDetails();
		if(verification(cartType, details)) {
			return ResultDTO.fail(ErrorMsg.PARAMISEMPTY.code(), ErrorMsg.PARAMISEMPTY.getMessage());
		}
		String userOnlyId = CookieUtil.getUsrOnlyid(request);
		if(StringUtils.isBlank(userOnlyId)) {
			return ResultDTO.fail(ErrorMsg.LOGIN_EXPIRED.code(), ErrorMsg.LOGIN_EXPIRED.getMessage());
		}
		String cartKey = userOnlyId+"_"+cartType;
		Object result = null;
		logger.info("添加购物车参数：" + params);
		try {
			/*
			 * 	购物车redis hash表：fieldValue 
			 */
			Map<String, String>	fieldValue = new HashMap<String, String>();
			
			/*
			 * 	添加购物车 商品详情：json数组
			 * "details": [
			 *  	{
			 *    		"itemId": 66797801,
			 *    		"itemCount": 1 ：商品数量
			 *  	},
			 *  	{},
			 *  	{},
			 *  	...
		  	 *	]
			 */
			JSONArray addJSONdetails = JSONArray.parseArray(details);
			
			for (int i = 0; i < addJSONdetails.size(); i++) {
				JSONObject addJSONdetail = addJSONdetails.getJSONObject(i);
				String itemId = addJSONdetail.getString(ITEMID);
				// 添加的商品数量
				Integer addItemCount = addJSONdetail.getInteger(ITEMCOUNT);
				if(addItemCount == null || addItemCount <= 0) {
					return ResultDTO.fail(ErrorMsg.CART_COUNT_CHECK.code(), ErrorMsg.CART_COUNT_CHECK.getMessage());
				}
				// 查询该商品是否已加购物车
				String cart = CartCacheCloudClientUtil.getCart(cartKey, itemId);
				if(StringUtils.isNotEmpty(cart)) {
					// 已加，修改数量
					CartDto redisCart = JSONObject.parseObject(cart, CartDto.class);
					redisCart.setItemCount(redisCart.getItemCount() + addItemCount);
					fieldValue.put(itemId, JSONObject.toJSONString(redisCart));
				}else {
					Long cartLength = CartCacheCloudClientUtil.getCartLength(cartKey);
					if(cartLength != 0 && cartLength >= 100) {
						return ResultDTO.fail(ErrorMsg.CART_IS_FULL.code(), ErrorMsg.CART_IS_FULL.getMessage());
					}
					CartDto cartDto = new CartDto();
					cartDto.setItemId(Long.parseLong(itemId));
					cartDto.setCreatTime(new Date().getTime());
					cartDto.setItemCount(addItemCount);
					fieldValue.put(itemId, JSONObject.toJSONString(cartDto));
				}
				
			}
			result = CartCacheCloudClientUtil.addCart(cartKey, fieldValue);
		} catch (JSONException e) {
			logger.error("添加购物车json转换异常:"+e.getMessage(), e);
			return ResultDTO.fail("参数异常，请检查json格式");
		} catch (Exception e) {
			logger.error("添加购物车:"+e.getMessage(), e);
			return ResultDTO.fail(e.getMessage());
		}
		return ResultDTO.successWithData(result);
	}
	
	/**修改购物车
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/updateCart")
	public ResultDTO updateCart(HttpServletRequest request, CartParamsDto params) {
		Object cartType = params.getCartType();
		String details = params.getDetails();
		if(verification(cartType, details)) {
			return ResultDTO.fail(ErrorMsg.PARAMISEMPTY.code(), ErrorMsg.PARAMISEMPTY.getMessage());
		}
		String userOnlyId = CookieUtil.getUsrOnlyid(request);
		if(StringUtils.isBlank(userOnlyId)) {
			return ResultDTO.fail(ErrorMsg.LOGIN_EXPIRED.code(), ErrorMsg.LOGIN_EXPIRED.getMessage());
		}
		String cartKey = userOnlyId+"_"+cartType;
		Object result = null;
		logger.info("修改购物车参数：" + params);
		try {
			/*
			 * 	购物车redis hash表：fieldValue 
			 */
			Map<String, String>	fieldValue = new HashMap<String, String>();
			
			/*
			 * 	修改购物车 商品详情：json数组
			 * "details": [
			 *  	{
			 *    		"itemId": 66797801,
			 *    		"count": 1 ：商品数量
			 *  	},
			 *  	{},
			 *  	{},
			 *  	...
		  	 *	]
			 */
			JSONArray updateJSONdetails = JSONArray.parseArray(details);
			
			for(int i = 0; i < updateJSONdetails.size(); i++) {
				JSONObject updateJSONdetail = updateJSONdetails.getJSONObject(i);
				String itemId = updateJSONdetail.getString(ITEMID);
				Integer updateItemCount = updateJSONdetail.getInteger(ITEMCOUNT);
				if(updateItemCount <= 0) {
					return ResultDTO.fail(ErrorMsg.CART_COUNT_CHECK.code(), ErrorMsg.CART_COUNT_CHECK.getMessage());
				}
				// 查询原购物车信息
				String cart = CartCacheCloudClientUtil.getCart(cartKey, itemId);
				if(StringUtils.isNotEmpty(cart)) {
					CartDto redisCart = JSONObject.parseObject(cart, CartDto.class);
					redisCart.setItemCount(updateItemCount);
					fieldValue.put(itemId, JSONObject.toJSONString(redisCart));
				}else {
					logger.error(itemId+",此商品未加入购物车或已删除");
				}
			}
			if (fieldValue.size() > 0)
				result = CartCacheCloudClientUtil.updateCart(cartKey, fieldValue);
		} catch (JSONException e) {
			logger.error("修改购物车json转换异常:"+e.getMessage(), e);
			return ResultDTO.fail("参数异常，请检查json格式");
		} catch (Exception e) {
			logger.error("修改购物车:"+e.getMessage(), e);
			return ResultDTO.fail(e.getMessage());
		}
		return ResultDTO.successWithData(result);
	}
	
	/**删除购物车
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/deleteCart")
	public ResultDTO deleteCart(HttpServletRequest request, CartParamsDto params) {
		Object cartType = params.getCartType();
		String details = params.getDetails();
		if(verification(cartType, details)) {
			return ResultDTO.fail(ErrorMsg.PARAMISEMPTY.code(), ErrorMsg.PARAMISEMPTY.getMessage());
		}
		String userOnlyId = CookieUtil.getUsrOnlyid(request);
		if(StringUtils.isBlank(userOnlyId)) {
			return ResultDTO.fail(ErrorMsg.LOGIN_EXPIRED.code(), ErrorMsg.LOGIN_EXPIRED.getMessage());
		}
		return cartService.deleteCart(userOnlyId, cartType.toString(), details);
	}
	
	/**清空购物车
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/cleanCart")
	public ResultDTO cleanCart(HttpServletRequest request, CartParamsDto params) {
		logger.info("清空购物车参数：" + params);
		Object cartType = params.getCartType();
		if(verification(cartType)) {
			return ResultDTO.fail(ErrorMsg.PARAMISEMPTY.code(), ErrorMsg.PARAMISEMPTY.getMessage());
		}
		String userOnlyId = CookieUtil.getUsrOnlyid(request);
		if(StringUtils.isBlank(userOnlyId)) {
			return ResultDTO.fail(ErrorMsg.LOGIN_EXPIRED.code(), ErrorMsg.LOGIN_EXPIRED.getMessage());
		}
		String cartKey = userOnlyId+"_"+cartType;
		try {
			CartCacheCloudClientUtil.cleanCart(cartKey);
		} catch (Exception e) {
			logger.error("清空购物车:"+e.getMessage(), e);
			return ResultDTO.fail(e.getMessage());
		}
		return ResultDTO.successWithData("ok");
	}
	
	/**获取购物车总数
	 * @param request
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/getCartCount")
	public ResultDTO getCartCount(HttpServletRequest request, CartParamsDto params) {
		logger.info("获取购物车总数参数：" + params);
		Object cartType = params.getCartType();
		if(verification(cartType)) {
			return ResultDTO.fail(ErrorMsg.PARAMISEMPTY.code(), ErrorMsg.PARAMISEMPTY.getMessage());
		}
		String userOnlyId = CookieUtil.getUsrOnlyid(request);
		if(StringUtils.isBlank(userOnlyId)) {
			return ResultDTO.fail(ErrorMsg.LOGIN_EXPIRED.code(), ErrorMsg.LOGIN_EXPIRED.getMessage());
		}
		String cartKey = userOnlyId+"_"+cartType;
		Integer totalCount = 0;
		try {
			// 获取缓存中所有数据
			Map<String, String> cart = CartCacheCloudClientUtil.getCart(cartKey);
			Iterator<Entry<String, String>> iterator = cart.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, String> next = iterator.next();
				String value = next.getValue();
				totalCount += JSONObject.parseObject(value).getInteger(ITEMCOUNT);
			}
		} catch (Exception e) {
			logger.error("购物车总数量:"+e.getMessage(), e);
			return ResultDTO.fail(e.getMessage());
		}
		return ResultDTO.successWithData(totalCount);
	}
	
	/**参数非空检查
	 * @param strings
	 * @return
	 */
	public Boolean verification(Object... strings) {
		for (Object object : strings) {
			if (object == null) {
				return true;
			} else if (StringUtils.isBlank(object.toString()) || StringUtils.equals(object.toString(), "null")) {
				return true;
			}
		}
		return false;
	}
}
