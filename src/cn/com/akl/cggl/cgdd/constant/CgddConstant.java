package cn.com.akl.cggl.cgdd.constant;
/**
 * 定义采购订单常量
 * @author zhangran
 *
 */
public class CgddConstant {

	public static final String tableName0 = "BO_AKL_CGDD_HEAD";//订单单头
	public static final String tableName1 = "BO_AKL_CGDD_BODY";//订单单身
	public static final String tableName2 = "BO_AKL_CGDD_BACKLOG";//backlog表
	public static final String tableName3 = "BO_AKL_WLXX";//BO_AKL_WLXX表
	public static final String tableName4 = "BO_AKL_JGGL";//价格管理表
	public static final String tableName5 = "BO_AKL_CGDD_BACKLOG_LS";//backlog临时表
	public static final String tableName6 = "BO_AKL_KC_KCHZ_P";//库存汇总表
	public static final String reloName0 = "网销经理";//角色：网销经理
	public static final String reloName1 = "网销主管";//角色：网销副总
	
	public static final String hzbm0 = "01065";//货主编号（亚昆）
	public static final String dzt0 = "待采购";//采购单头状态
	public static final String dzt1 = "生效";//采购单头状态
	public static final String dzt2 = "";//采购单头状态
	public static final String zt = "未结束";//订单单身状态
	public static final String zt1 = "已入库";//订单单身状态
	public static final String zt2 = "部分入库";//订单单身状态
	
	public static final String sfyf = "是";//是否预付
	
	public static final String dbid0 = "02000A";//闪迪采购
	public static final String dbid1 = "02000B";//回采
	public static final String dbid2 = "02000C";//其他采购
	public static final String dbid3 = "02000D";//BG采购
	
	public static final double jshj = 0.0000;//计税合计
}
