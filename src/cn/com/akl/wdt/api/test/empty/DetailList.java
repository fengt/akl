package cn.com.akl.wdt.api.test.empty;
/**
 * ��Ʒ��ϸ��Ϣ
 * */
public class DetailList {
	private String SkuCode;//erp�ڻ�ƷΨһ��ʶ���
	private String SkuName;//��ƷSku����
	private String PlatformGoodsCode;//ƽ̨��Ʒ���
	private String PlatformGoodsName;//ƽ̨��Ʒ����
	private String PlatformSkuCode;//ƽ̨sku����
	private String PlatformSkuName;//ƽ̨sku����
	private String SellCount;//��������
	private String SellPrice;//�����۸�
	public String getSellCount() {
		return SellCount;
	}
	public void setSellCount(String sellCount) {
		SellCount = sellCount;
	}
	private String DiscountMoney;//��Ʒ�Żݽ��
	private int bGift;//�Ƿ���Ʒ����Ϊ1����Ϊ0
	private String ck;//�ֿ�
	private String wlmc;
	private String wlbh;
	private String TRADENO;//�������
	
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
