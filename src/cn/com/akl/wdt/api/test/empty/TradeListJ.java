package cn.com.akl.wdt.api.test.empty;

import java.util.List;


/**
 * ������Ϣ
 * */
public class TradeListJ {

	private String TradeNo;//ERP�ڶ������
	private String TradeNO2;//��Դ����
	private String WarehouseNo;//�ֿ����
	private String RegTime;//��������ʱ��
	private String TradeTime;//����ʱ��
	private String PayTime;//����ʱ��
	private String ChkTime;//��ʱ��
	private String StockOutTime;//����ʱ��
	private String SndTime;//����ʱ��
	private String LastModifyTime;//����޸�ʱ��
	/*����״̬����״̬
	(��ȡ��cancel_trade)
	(Ԥ����pre_trade)
	(�����check_trade)
	(������finance_trade)
	������wait_send_trade
	(����� over_trade)*/
	private String TradeStatus;
	/*
	 * �˿�״̬
(δ�˿�trade_no_refund
(�ȴ��ͷ�ȷ��trade_wait_seller_agree)
(����������Ʒ���˿����ͷ�ȷ��trade_part_refunded)
(�������˿�trade_refunded

	 * */
	private String RefundStatus;
	private int bInvoice;//�Ƿ���Ҫ��Ʊ
	private String InvoiceTitle;//��Ʊ̧ͷ
	private String InvoiceContent;//��Ʊ����
	private String NickName;//�ͻ�����
	private String SndTo;//�ռ�������
	private String Country;//�ռ��˹���
	private String Province;//�ռ���ʡ��
	private String City;//�ռ��˳���
	private String Town;//�ռ�������
	private String Adr;//�ռ��˵�ַ
	private String Tel;//�ռ��˵绰
	private String Zip;//�ռ����ʱ�
	/*
	 * ���ʽ
(1��������)(2 �����տ�)
(3�ֽ��տ�)(4��������)
(5Ƿ���Ӧ��)(6�ͻ�Ԥ���)

	 * */
	private String ChargeType;
	private String SellSkuCount;//��Ʒ����
	private String GoodsTotal;//��Ʒ�ܶ�
	private String PostageTotal;//Ӧ���ʷ�
	private String FavourableTotal;//�������Ż�
	private String AllTotal;//Ӧ�ս��
	private String LogisticsCode;//������˾����
	private String PostID;//���˵���
	private String CustomerRemark;//�������
	private String Remark;//��������
	private String ShopType;//ƽ̨��������
	private String ShopName;//ƽ̨��������
	private String TradeFlag;//erp�еĶ����������
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
	String ChkOperatorName;//��Ա����
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
