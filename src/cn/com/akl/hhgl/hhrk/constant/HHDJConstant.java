package cn.com.akl.hhgl.hhrk.constant;
/**
 * 定义采购入库常量
 * @author qjc
 *
 */
public class HHDJConstant {

	public static final String uuid = "3c5b284b4d33b2973c18cc8c9f4d87de";//库存维护流程的UUID
	public static final String tableName0 = "BO_AKL_CCB_RKD_HEAD";//入库单单头
	public static final String tableName1 = "BO_AKL_CCB_RKD_BODY";//入库单单身
	public static final String tableName2 = "BO_AKL_CCB_RKD_ZCXX";//转仓明细表
	public static final String tableName3 = "BO_AKL_KC_KCHZ_P";//库存汇总表
	public static final String tableName4 = "BO_AKL_KC_KCMX_S";//库存明细表
	
	public static final String tableName6 = "BO_AKL_CGDD_HEAD";//采购订单单头信息表
	public static final String tableName7 = "BO_AKL_CGDD_BODY";//采购订单单身信息表BO_AKL_WLXX
	
	public static final String tableName8 = "BO_AKL_WLXX";//物料基础数据_物料属性信息表
	public static final String tableName9 = "BO_AKL_WLKWGXB";//料号货位关系表(BO_AKL_WLKWGXB)
	public static final String tableName10 = "BO_AKL_JGGL";//价格管理信息表(BO_AKL_JGGL)应付信息表(BO_AKL_YF)
	public static final String tableName11 = "BO_AKL_YF";//应付信息表(BO_AKL_YF)
	
	public static final String ddzt0 = "已转仓";//采购订单单身订单状态
	public static final String ddzt1 = "部分转仓";//采购订单单身订单状态
	public static final String ddzt2 = "已提货";//采购订单单身订单状态
	public static final String ddzt3 = "转仓数量异常";//采购订单单身订单状态
	public static final String ddzt4 = "已入库";//采购订单单身订单状态
	public static final String ddzt5 = "部分入库";//采购订单单身订单状态
	public static final String ddzt6 = "入库数量异常";//采购订单单身订单状态
	public static final String ddzt7 = "部分提货";//采购订单单身订单状态
	public static final String ddzt8 = "提货数量异常";//采购订单单身订单状态
	//public static final String ddzt9 = "未结束";//采购订单单身订单状态
	//public static final String ddzt10 = "已结束";//采购订单单身订单状态
	public static final String kczt = "042023";//库存汇总表ZT
	public static final String kczt2 = "042022";//库存汇总表ZT
	
	public static final String rkdb1 = "011168";//借货入库单别：(BG入库)
	public static final String rkdb2 = "011169";//还货入库单别：(BG入库)
}
