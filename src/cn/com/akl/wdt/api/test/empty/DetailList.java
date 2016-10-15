package cn.com.akl.wdt.api.test.empty;
/**
 * 货品明细信息
 * */
public class DetailList {
	private String SkuCode;//erp内货品唯一标识编号
	private String SkuName;//货品Sku名称
	private String PlatformGoodsCode;//平台货品编号
	private String PlatformGoodsName;//平台货品名称
	private String PlatformSkuCode;//平台sku编码
	private String PlatformSkuName;//平台sku名称
	private String SellCount;//卖出数量
	private String SellPrice;//卖出价格
	public String getSellCount() {
		return SellCount;
	}
	public void setSellCount(String sellCount) {
		SellCount = sellCount;
	}
	private String DiscountMoney;//货品优惠金额
	private int bGift;//是否赠品，是为1，否为0
	private String ck;//仓库
	private String wlmc;
	private String wlbh;
	private String TRADENO;//订单编号
	
	public String getTRADENO() {
		return TRADENO;
	}
	public void setTRADENO(String tRADENO) {
		TRADENO = tRADENO;
	}
	public String getWlmc() {
		return wlmc;
	}
	public void setWlmc(String wlmc) {
		this.wlmc = wlmc;
	}
	public String getWlbh() {
		return wlbh;
	}
	public void setWlbh(String wlbh) {
		this.wlbh = wlbh;
	}
	public String getCk() {
		return ck;
	}
	public void setCk(String ck) {
		this.ck = ck;
	}
	public String getSkuCode() {
		return SkuCode;
	}
	public void setSkuCode(String skuCode) {
		SkuCode = skuCode;
	}
	public String getSkuName() {
		return SkuName;
	}
	public void setSkuName(String skuName) {
		SkuName = skuName;
	}
	public String getPlatformGoodsCode() {
		return PlatformGoodsCode;
	}
	public void setPlatformGoodsCode(String platformGoodsCode) {
		PlatformGoodsCode = platformGoodsCode;
	}
	public String getPlatformGoodsName() {
		return PlatformGoodsName;
	}
	public void setPlatformGoodsName(String platformGoodsName) {
		PlatformGoodsName = platformGoodsName;
	}
	public String getPlatformSkuCode() {
		return PlatformSkuCode;
	}
	public void setPlatformSkuCode(String platformSkuCode) {
		PlatformSkuCode = platformSkuCode;
	}
	public String getPlatformSkuName() {
		return PlatformSkuName;
	}
	public void setPlatformSkuName(String platformSkuName) {
		PlatformSkuName = platformSkuName;
	}
	
	public String getSellPrice() {
		return SellPrice;
	}
	public void setSellPrice(String sellPrice) {
		SellPrice = sellPrice;
	}
	public String getDiscountMoney() {
		return DiscountMoney;
	}
	public void setDiscountMoney(String discountMoney) {
		DiscountMoney = discountMoney;
	}
	public int getbGift() {
		return bGift;
	}
	public void setbGift(int bGift) {
		this.bGift = bGift;
	}
	
}
