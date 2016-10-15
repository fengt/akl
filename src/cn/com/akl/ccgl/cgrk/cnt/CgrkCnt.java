package cn.com.akl.ccgl.cgrk.cnt;
/**
 * 定义采购入库常量
 * @author zhangran
 *
 */
public class CgrkCnt {

	public static final String uuid = "3c5b284b4d33b2973c18cc8c9f4d87de";//库存维护流程的UUID
	public static final String tableName0 = "BO_AKL_CCB_RKD_HEAD";//入库单单头
	public static final String tableName1 = "BO_AKL_CCB_RKD_BODY";//入库单单身
	public static final String tableName2 = "BO_AKL_CCB_RKD_ZCXX";//转仓明细表
	public static final String tableName3 = "BO_AKL_KC_KCHZ_P";//库存汇总表
	public static final String tableName4 = "BO_AKL_KC_KCMX_S";//库存明细表
	
	public static final String tableName6 = "BO_AKL_CGDD_HEAD";//采购订单单头信息表
	public static final String tableName7 = "BO_AKL_CGDD_BODY";//采购订单单身信息表
	
	public static final String tableName8 = "BO_AKL_WLXX";//物料基础数据_物料属性信息表
	public static final String tableName9 = "BO_AKL_WLKWGXB";//料号货位关系表
	public static final String tableName10 = "BO_AKL_JGGL";//价格管理信息表
	public static final String tableName11 = "BO_AKL_YF";//应付信息表
	
	public static final String tableName12 = "BO_AKL_DGKC_KCHZ_P";//代管库存汇总表
	public static final String tableName13 = "BO_AKL_DGKC_KCMX_S";//代管库存明细表
	public static final String tableName14 = "BO_AKL_GYSKHGXB";//供应商_客户关系表
	
	public static final String ddzt0 = "已转仓";//采购订单单身订单状态
	public static final String ddzt1 = "部分转仓";//采购订单单身订单状态
	public static final String ddzt2 = "已提货";//采购订单单身订单状态
	public static final String ddzt3 = "转仓数量异常";//采购订单单身订单状态
	public static final String ddzt4 = "已入库";//采购订单单身订单状态
	public static final String ddzt5 = "部分入库";//采购订单单身订单状态
	public static final String ddzt6 = "入库数量异常";//采购订单单身订单状态
	public static final String ddzt7 = "部分提货";//采购订单单身订单状态
	public static final String ddzt8 = "提货数量异常";//采购订单单身订单状态
	public static final String ddzt9 = "未结束";//采购订单单身订单状态
	//public static final String ddzt10 = "已结束";//采购订单单身订单状态
	public static final String kczt = "042023";//库存汇总表ZT(在途)
	public static final String kczt2 = "042022";//库存汇总表ZT(正常)
	public static final String kczt3 = "042681";//库存汇总表ZT(审核中)
	
	public static final String sx0 = "049088";//库存明细属性：(新品)
	
	public static final String rkdb0 = "011001";//入库单别：(闪迪采购入库)
	public static final String rkdb1 = "011132";//入库单别：(回采入库)
	public static final String rkdb2 = "011004";//入库单别：(其他采购入库)
	public static final String rkdb3 = "011137";//入库单别：(其他入库)
	public static final String rkdb4 = "011148";//入库单别：(BG入库)
	public static final String rkdb5 = "011693";//入库单别：(旺店通入库)
	
	public static final String khdm0 ="4021289";//客户代码：(AKL Beijing Limited)
	
	public static final String zt = "未付";//应付表中状态
	public static final String lb1 = "1";//1(供应商)，0(库存)
	
	public static final String sfyf = "是";//是否预付
	
	
	public static final int EXCEL_COL_KHDDH = 0;//客户订单号
	public static final int EXCEL_COL_LH = 5;//型号(转仓Excel)
	
	public static final int EXCEL_COL_XH = 4;//型号(其他入库Excel)
}
