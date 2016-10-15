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

	//barcode	������	varchar	30	�ɿ�	�����������뵵�������е�������
		private String barCode;
		//inventorycode	�������	varchar	20	�ǿ�	�����Ǵ�����������еĴ��
		private String inventoryCode;
		/**free1	������1	varchar	20	�ɿ�	�����������Ĵ������ǿգ��ǿ�ʱ������������������е�������
		free2	������2	varchar	20	�ɿ�	ͬ��
		free3	������3	varchar	20	�ɿ�	ͬ��
		free4	������4	varchar	20	�ɿ�	ͬ��
		free5	������5	varchar	20	�ɿ�	ͬ��
		free6	������6	varchar	20	�ɿ�	ͬ��
		free7	������7	varchar	20	�ɿ�	ͬ��
		free8	������8	varchar	20	�ɿ�	ͬ��
		free9	������9	varchar	20	�ɿ�	ͬ��
		free10	������10	varchar	20	�ɿ�	ͬ��*/
		//shouldquantity	Ӧ�գ���������	float	8	�ɿ�	
		private BigDecimal shouldQuantity;
		//shouldnumber	Ӧ�գ���������	float	8	�ɿ�	
		private BigDecimal shouldNumber;
		//quantity	������������������	float	8	�ɿ�	Ӧ�գ�������������������ͬʱΪ�ա�
		private BigDecimal itemQuantity;
		//assitantunit	��������λ	varchar	10	�ɿ�	�����ǵ�ǰ����ļ�����λ�������еļ�����λ��
		private String assitantunit;
		//number	����	float	8	�ɿ�	�и�������λ�Ĵ������Ϊ�ա������������*������=����
		private BigDecimal number;
		//unitcost	����	float	8	�ɿ�	�Ǹ�
		private BigDecimal unitcost;
		//price	���	money	8	�ɿ�	������������*����=���
		private BigDecimal money;
		//planunitcost	�ƻ����ۼ�	float	8	�ɿ�	�Ǹ�
		private BigDecimal planunitCost;
		//planprice	�ƻ������ۼ۽��	money	8	�ɿ�	������������*�ƻ��ۣ��ۼۣ�=�ƻ����ۼ۽�
		private BigDecimal planPrice;
		//serial	����	varchar	20	�ɿ�	���ι���Ĵ��
		private String itemSerial;	
		//makedate	��������			�ɿ�	
		private String markDate;
		//validdate	ʧЧ����	datetime	8	�ɿ�	
		private String validDate;
		//transitionid	�������ӱ�ID��	int	4	1	
		private int transitionId;
		//subbillcode	��Ʊ�ӱ�ID��	int	4	�ɿ�	
		private int subbillCode;
		//subpurchaseid	�ɹ������ӱ�ID��	int	4	�ɿ�	
		private int subpurchaseID;
		//position	��λ	varchar	20	�ɿ�	
		private String position;
		//itemclasscode	��Ŀ�������	varchar	10	�ɿ�	
		private String itemClassCode;
		//itemclassname	��Ŀ��������	varchar	20	�ɿ�	
		private String itemClassName;
		//itemcode	��Ŀ����	varchar	20	�ɿ�	
		private String itemCode;
		//itemname	��Ŀ����	varchar	60	�ɿ�	
	    /**define22	�����Զ�����22	varchar	60	�ɿ�	
		define23	�����Զ�����23	varchar	60	�ɿ�	
		define24	�����Զ�����24	varchar	60	�ɿ�	
		define25	�����Զ�����25	varchar	60	�ɿ�	
		define26	�����Զ�����26	float	8	�ɿ�	
		define27	�����Զ�����27	float	8	�ɿ�	
		define28	�����Զ�����28	varchar	120	�ɿ�	
		define29	�����Զ�����29	varchar	120	�ɿ�	
		define30	�����Զ�����30	varchar	120	�ɿ�	
		define31	�����Զ�����31	varchar	120	�ɿ�	
		define32	�����Զ�����32	varchar	120	�ɿ�	
		define33	�����Զ�����33	varchar	120	�ɿ�	
		define34	�����Զ�����34	int	4	�ɿ�	
		define35	�����Զ�����35	int	4	�ɿ�	
		define36	�����Զ�����36	datetime		�ɿ�	
		define37	�����Զ�����37	datetime		�ɿ�	*/
		
		//subconsignmentid	�������ӱ�ID	int	4	�ɿ�	������ͨ��������Dispatchlist,Dispatchlists����ί�д�����������EnDispatch,EnDispatchs��
		private int subconsignmentId;
		//delegateconsignmentid	ί�д����������ӱ�	int	4	�ɿ�	
		private int delegateconsignmentId;
		//subproducingid	���������ӱ�ID	int	4	�ɿ�	��д����������ʹ�ó���ͬ�����ӱ�ID
		private int subproducingId;
		//subcheckid	���鵥�ӱ�ID	int	4	�ɿ�	��д�������������ͬ���鵥�ӱ�ID
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
