package cn.com.akl.wdt.api.test.empty;

import java.util.List;


/**
 * 订单信息
 * */
public class TradeListJ {

	private String TradeNo;//ERP内订单编号
	private String TradeNO2;//来源单号
	private String WarehouseNo;//仓库编码
	private String RegTime;//订单创建时间
	private String TradeTime;//交易时间
	private String PayTime;//付款时间
	private String ChkTime;//审单时间
	private String StockOutTime;//出库时间
	private String SndTime;//发货时间
	private String LastModifyTime;//最后修改时间
	/*订单状态订单状态
	(已取消cancel_trade)
	(预订单pre_trade)
	(待审核check_trade)
	(待财审finance_trade)
	待发货wait_send_trade
	(已完成 over_trade)*/
	private String TradeStatus;
	/*
	 * 退款状态
(未退款trade_no_refund
(等待客服确认trade_wait_seller_agree)
(订单部分商品已退款，请与客服确认trade_part_refunded)
(订单已退款trade_refunded

	 * */
	private String RefundStatus;
	private int bInvoice;//是否需要发票
	private String InvoiceTitle;//发票抬头
	private String InvoiceContent;//发票内容
	private String NickName;//客户网名
	private String SndTo;//收件人姓名
	private String Country;//收件人国家
	private String Province;//收件人省份
	private String City;//收件人城市
	private String Town;//收件人区县
	private String Adr;//收件人地址
	private String Tel;//收件人电话
	private String Zip;//收件人邮编
	/*
	 * 付款方式
(1担保交易)(2 银行收款)
(3现金收款)(4货到付款)
(5欠款记应收)(6客户预存款)

	 * */
	private String ChargeType;
	private String SellSkuCount;//货品数量
	private String GoodsTotal;//货品总额
	private String PostageTotal;//应收邮费
	private String FavourableTotal;//订单总优惠
	private String AllTotal;//应收金额
	private String LogisticsCode;//物流公司编码
	private String PostID;//货运单号
	private String CustomerRemark;//买家留言
	private String Remark;//卖家留言
	private String ShopType;//平台店铺类型
	private String ShopName;//平台店铺名称
	private String TradeFlag;//erp中的订单标记名称
	private List<DetailList>  DetaiList;
	public List<DetailList> getDetaiList() {
		return DetaiList;
	}
	public void setDetaiList(List<DetailList> detaiList) {
		DetaiList = detaiList;
	}
	public int getbInvoice() {
		return bInvoice;
	}
	public void setbInvoice(int bInvoice) {
		this.bInvoice = bInvoice;
	}
	String ChkOperatorName;//审单员名称
	public String getTradeNo() {
		return TradeNo;
	}
	public void setTradeNo(String tradeNo) {
		TradeNo = tradeNo;
	}
	public String getTradeNO2() {
		return TradeNO2;
	}
	public void setTradeNO2(String tradeNO2) {
		TradeNO2 = tradeNO2;
	}
	public String getWarehouseNo() {
		return WarehouseNo;
	}
	public void setWarehouseNo(String warehouseNo) {
		WarehouseNo = warehouseNo;
	}
	public String getRegTime() {
		return RegTime;
	}
	public void setRegTime(String regTime) {
		RegTime = regTime;
	}
	public String getTradeTime() {
		return TradeTime;
	}
	public void setTradeTime(String tradeTime) {
		TradeTime = tradeTime;
	}
	public String getPayTime() {
		return PayTime;
	}
	public void setPayTime(String payTime) {
		PayTime = payTime;
	}
	public String getChkTime() {
		return ChkTime;
	}
	public void setChkTime(String chkTime) {
		ChkTime = chkTime;
	}
	public String getStockOutTime() {
		return StockOutTime;
	}
	public void setStockOutTime(String stockOutTime) {
		StockOutTime = stockOutTime;
	}
	public String getSndTime() {
		return SndTime;
	}
	public void setSndTime(String sndTime) {
		SndTime = sndTime;
	}
	public String getLastModifyTime() {
		return LastModifyTime;
	}
	public void setLastModifyTime(String lastModifyTime) {
		LastModifyTime = lastModifyTime;
	}
	public String getTradeStatus() {
		return TradeStatus;
	}
	public void setTradeStatus(String tradeStatus) {
		TradeStatus = tradeStatus;
	}
	public String getRefundStatus() {
		return RefundStatus;
	}
	public void setRefundStatus(String refundStatus) {
		RefundStatus = refundStatus;
	}
	
	public String getInvoiceTitle() {
		return InvoiceTitle;
	}
	public void setInvoiceTitle(String invoiceTitle) {
		InvoiceTitle = invoiceTitle;
	}
	public String getInvoiceContent() {
		return InvoiceContent;
	}
	public void setInvoiceContent(String invoiceContent) {
		InvoiceContent = invoiceContent;
	}
	public String getNickName() {
		return NickName;
	}
	public void setNickName(String nickName) {
		NickName = nickName;
	}
	public String getSndTo() {
		return SndTo;
	}
	public void setSndTo(String sndTo) {
		SndTo = sndTo;
	}
	public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
	public String getProvince() {
		return Province;
	}
	public void setProvince(String province) {
		Province = province;
	}
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		City = city;
	}
	public String getTown() {
		return Town;
	}
	public void setTown(String town) {
		Town = town;
	}
	public String getAdr() {
		return Adr;
	}
	public void setAdr(String adr) {
		Adr = adr;
	}
	public String getTel() {
		return Tel;
	}
	public void setTel(String tel) {
		Tel = tel;
	}
	public String getZip() {
		return Zip;
	}
	public void setZip(String zip) {
		Zip = zip;
	}
	public String getChargeType() {
		return ChargeType;
	}
	public void setChargeType(String chargeType) {
		ChargeType = chargeType;
	}
	public String getSellSkuCount() {
		return SellSkuCount;
	}
	public void setSellSkuCount(String sellSkuCount) {
		SellSkuCount = sellSkuCount;
	}
	public String getGoodsTotal() {
		return GoodsTotal;
	}
	public void setGoodsTotal(String goodsTotal) {
		GoodsTotal = goodsTotal;
	}
	public String getPostageTotal() {
		return PostageTotal;
	}
	public void setPostageTotal(String postageTotal) {
		PostageTotal = postageTotal;
	}
	public String getFavourableTotal() {
		return FavourableTotal;
	}
	public void setFavourableTotal(String favourableTotal) {
		FavourableTotal = favourableTotal;
	}
	public String getAllTotal() {
		return AllTotal;
	}
	public void setAllTotal(String allTotal) {
		AllTotal = allTotal;
	}
	public String getLogisticsCode() {
		return LogisticsCode;
	}
	public void setLogisticsCode(String logisticsCode) {
		LogisticsCode = logisticsCode;
	}
	public String getPostID() {
		return PostID;
	}
	public void setPostID(String postID) {
		PostID = postID;
	}
	public String getCustomerRemark() {
		return CustomerRemark;
	}
	public void setCustomerRemark(String customerRemark) {
		CustomerRemark = customerRemark;
	}
	public String getRemark() {
		return Remark;
	}
	public void setRemark(String remark) {
		Remark = remark;
	}
	public String getShopType() {
		return ShopType;
	}
	public void setShopType(String shopType) {
		ShopType = shopType;
	}
	public String getShopName() {
		return ShopName;
	}
	public void setShopName(String shopName) {
		ShopName = shopName;
	}
	public String getTradeFlag() {
		return TradeFlag;
	}
	public void setTradeFlag(String tradeFlag) {
		TradeFlag = tradeFlag;
	}
	public String getChkOperatorName() {
		return ChkOperatorName;
	}
	public void setChkOperatorName(String chkOperatorName) {
		ChkOperatorName = chkOperatorName;
	}
	
}
