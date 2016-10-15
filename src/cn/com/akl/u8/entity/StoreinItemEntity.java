/**
 * 
 */
package cn.com.akl.u8.entity;

import java.math.BigDecimal;
/**
 * @author hzy
 *
 */
public class StoreinItemEntity {

	//barcode	条形码	varchar	30	可空	必须是条形码档案中已有的条形码
		private String barCode;
		//inventorycode	存货编码	varchar	20	非空	必须是存货档案中已有的存货
		private String inventoryCode;
		/**free1	自由项1	varchar	20	可空	有自由项管理的存货必须非空；非空时必须是自由项档案中已有的自由项
		free2	自由项2	varchar	20	可空	同上
		free3	自由项3	varchar	20	可空	同上
		free4	自由项4	varchar	20	可空	同上
		free5	自由项5	varchar	20	可空	同上
		free6	自由项6	varchar	20	可空	同上
		free7	自由项7	varchar	20	可空	同上
		free8	自由项8	varchar	20	可空	同上
		free9	自由项9	varchar	20	可空	同上
		free10	自由项10	varchar	20	可空	同上*/
		//shouldquantity	应收（发）数量	float	8	可空	
		private BigDecimal shouldQuantity;
		//shouldnumber	应收（发）件数	float	8	可空	
		private BigDecimal shouldNumber;
		//quantity	数量（主记量数量）	float	8	可空	应收（发）数量和数量不能同时为空。
		private BigDecimal itemQuantity;
		//assitantunit	辅记量单位	varchar	10	可空	必须是当前存货的计量单位组中已有的计量单位。
		private String assitantunit;
		//number	件数	float	8	可空	有辅计量单位的存货不能为空。必须满足件数*换算率=数量
		private BigDecimal number;
		//unitcost	单价	float	8	可空	非负
		private BigDecimal unitcost;
		//price	金额	money	8	可空	必须满足数量*单价=金额
		private BigDecimal money;
		//planunitcost	计划价售价	float	8	可空	非负
		private BigDecimal planunitCost;
		//planprice	计划金额或售价金额	money	8	可空	必须满足数量*计划价（售价）=计划金额（售价金额）
		private BigDecimal planPrice;
		//serial	批号	varchar	20	可空	批次管理的存货
		private String itemSerial;	
		//makedate	生产日期			可空	
		private String markDate;
		//validdate	失效日期	datetime	8	可空	
		private String validDate;
		//transitionid	调拨单子表ID号	int	4	1	
		private int transitionId;
		//subbillcode	发票子表ID号	int	4	可空	
		private int subbillCode;
		//subpurchaseid	采购订单子表ID号	int	4	可空	
		private int subpurchaseID;
		//position	货位	varchar	20	可空	
		private String position;
		//itemclasscode	项目大类编码	varchar	10	可空	
		private String itemClassCode;
		//itemclassname	项目大类名称	varchar	20	可空	
		private String itemClassName;
		//itemcode	项目编码	varchar	20	可空	
		private String itemCode;
		//itemname	项目名称	varchar	60	可空	
	    /**define22	表体自定义项22	varchar	60	可空	
		define23	表体自定义项23	varchar	60	可空	
		define24	表体自定义项24	varchar	60	可空	
		define25	表体自定义项25	varchar	60	可空	
		define26	表体自定义项26	float	8	可空	
		define27	表体自定义项27	float	8	可空	
		define28	表体自定义项28	varchar	120	可空	
		define29	表体自定义项29	varchar	120	可空	
		define30	表体自定义项30	varchar	120	可空	
		define31	表体自定义项31	varchar	120	可空	
		define32	表体自定义项32	varchar	120	可空	
		define33	表体自定义项33	varchar	120	可空	
		define34	表体自定义项34	int	4	可空	
		define35	表体自定义项35	int	4	可空	
		define36	表体自定义项36	datetime		可空	
		define37	表体自定义项37	datetime		可空	*/
		
		//subconsignmentid	发货单子表ID	int	4	可空	包括普通发货单（Dispatchlist,Dispatchlists）和委托代销发货单（EnDispatch,EnDispatchs）
		private int subconsignmentId;
		//delegateconsignmentid	委托代销发货单子表	int	4	可空	
		private int delegateconsignmentId;
		//subproducingid	生产订单子表ID	int	4	可空	回写已领用数量使用长度同订单子表ID
		private int subproducingId;
		//subcheckid	检验单子表ID	int	4	可空	回写已入库数量长度同检验单子表ID
		private int subcheckId;
		/**
		 * @return the barCode
		 */
		public String getBarCode() {
			return barCode;
		}
		/**
		 * @param barCode the barCode to set
		 */
		public void setBarCode(String barCode) {
			this.barCode = barCode;
		}
		/**
		 * @return the inventoryCode
		 */
		public String getInventoryCode() {
			return inventoryCode;
		}
		/**
		 * @param inventoryCode the inventoryCode to set
		 */
		public void setInventoryCode(String inventoryCode) {
			this.inventoryCode = inventoryCode;
		}
		/**
		 * @return the shouldQuantity
		 */
		public BigDecimal getShouldQuantity() {
			return shouldQuantity;
		}
		/**
		 * @param shouldQuantity the shouldQuantity to set
		 */
		public void setShouldQuantity(BigDecimal shouldQuantity) {
			this.shouldQuantity = shouldQuantity;
		}
		/**
		 * @return the shouldNumber
		 */
		public BigDecimal getShouldNumber() {
			return shouldNumber;
		}
		/**
		 * @param shouldNumber the shouldNumber to set
		 */
		public void setShouldNumber(BigDecimal shouldNumber) {
			this.shouldNumber = shouldNumber;
		}
		/**
		 * @return the itemQuantity
		 */
		public BigDecimal getItemQuantity() {
			return itemQuantity;
		}
		/**
		 * @param itemQuantity the itemQuantity to set
		 */
		public void setItemQuantity(BigDecimal itemQuantity) {
			this.itemQuantity = itemQuantity;
		}
		/**
		 * @return the assitantunit
		 */
		public String getAssitantunit() {
			return assitantunit;
		}
		/**
		 * @param assitantunit the assitantunit to set
		 */
		public void setAssitantunit(String assitantunit) {
			this.assitantunit = assitantunit;
		}
		/**
		 * @return the number
		 */
		public BigDecimal getNumber() {
			return number;
		}
		/**
		 * @param number the number to set
		 */
		public void setNumber(BigDecimal number) {
			this.number = number;
		}
		/**
		 * @return the unitcost
		 */
		public BigDecimal getUnitcost() {
			return unitcost;
		}
		/**
		 * @param unitcost the unitcost to set
		 */
		public void setUnitcost(BigDecimal unitcost) {
			this.unitcost = unitcost;
		}
		/**
		 * @return the money
		 */
		public BigDecimal getMoney() {
			return money;
		}
		/**
		 * @param money the money to set
		 */
		public void setMoney(BigDecimal money) {
			this.money = money;
		}
		/**
		 * @return the planunitCost
		 */
		public BigDecimal getPlanunitCost() {
			return planunitCost;
		}
		/**
		 * @param planunitCost the planunitCost to set
		 */
		public void setPlanunitCost(BigDecimal planunitCost) {
			this.planunitCost = planunitCost;
		}
		/**
		 * @return the planPrice
		 */
		public BigDecimal getPlanPrice() {
			return planPrice;
		}
		/**
		 * @param planPrice the planPrice to set
		 */
		public void setPlanPrice(BigDecimal planPrice) {
			this.planPrice = planPrice;
		}
		/**
		 * @return the itemSerial
		 */
		public String getItemSerial() {
			return itemSerial;
		}
		/**
		 * @param itemSerial the itemSerial to set
		 */
		public void setItemSerial(String itemSerial) {
			this.itemSerial = itemSerial;
		}
		/**
		 * @return the markDate
		 */
		public String getMarkDate() {
			return markDate;
		}
		/**
		 * @param markDate the markDate to set
		 */
		public void setMarkDate(String markDate) {
			this.markDate = markDate;
		}
		/**
		 * @return the validDate
		 */
		public String getValidDate() {
			return validDate;
		}
		/**
		 * @param validDate the validDate to set
		 */
		public void setValidDate(String validDate) {
			this.validDate = validDate;
		}
		/**
		 * @return the transitionId
		 */
		public int getTransitionId() {
			return transitionId;
		}
		/**
		 * @param transitionId the transitionId to set
		 */
		public void setTransitionId(int transitionId) {
			this.transitionId = transitionId;
		}
		/**
		 * @return the subbillCode
		 */
		public int getSubbillCode() {
			return subbillCode;
		}
		/**
		 * @param subbillCode the subbillCode to set
		 */
		public void setSubbillCode(int subbillCode) {
			this.subbillCode = subbillCode;
		}
		/**
		 * @return the subpurchaseID
		 */
		public int getSubpurchaseID() {
			return subpurchaseID;
		}
		/**
		 * @param subpurchaseID the subpurchaseID to set
		 */
		public void setSubpurchaseID(int subpurchaseID) {
			this.subpurchaseID = subpurchaseID;
		}
		/**
		 * @return the position
		 */
		public String getPosition() {
			return position;
		}
		/**
		 * @param position the position to set
		 */
		public void setPosition(String position) {
			this.position = position;
		}
		/**
		 * @return the itemClassCode
		 */
		public String getItemClassCode() {
			return itemClassCode;
		}
		/**
		 * @param itemClassCode the itemClassCode to set
		 */
		public void setItemClassCode(String itemClassCode) {
			this.itemClassCode = itemClassCode;
		}
		/**
		 * @return the itemClassName
		 */
		public String getItemClassName() {
			return itemClassName;
		}
		/**
		 * @param itemClassName the itemClassName to set
		 */
		public void setItemClassName(String itemClassName) {
			this.itemClassName = itemClassName;
		}
		/**
		 * @return the itemCode
		 */
		public String getItemCode() {
			return itemCode;
		}
		/**
		 * @param itemCode the itemCode to set
		 */
		public void setItemCode(String itemCode) {
			this.itemCode = itemCode;
		}
		/**
		 * @return the subconsignmentId
		 */
		public int getSubconsignmentId() {
			return subconsignmentId;
		}
		/**
		 * @param subconsignmentId the subconsignmentId to set
		 */
		public void setSubconsignmentId(int subconsignmentId) {
			this.subconsignmentId = subconsignmentId;
		}
		/**
		 * @return the delegateconsignmentId
		 */
		public int getDelegateconsignmentId() {
			return delegateconsignmentId;
		}
		/**
		 * @param delegateconsignmentId the delegateconsignmentId to set
		 */
		public void setDelegateconsignmentId(int delegateconsignmentId) {
			this.delegateconsignmentId = delegateconsignmentId;
		}
		/**
		 * @return the subproducingId
		 */
		public int getSubproducingId() {
			return subproducingId;
		}
		/**
		 * @param subproducingId the subproducingId to set
		 */
		public void setSubproducingId(int subproducingId) {
			this.subproducingId = subproducingId;
		}
		/**
		 * @return the subcheckId
		 */
		public int getSubcheckId() {
			return subcheckId;
		}
		/**
		 * @param subcheckId the subcheckId to set
		 */
		public void setSubcheckId(int subcheckId) {
			this.subcheckId = subcheckId;
		}
	
}
