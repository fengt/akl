package cn.com.akl.shgl.jf.biz;

import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

public class DeliveryConstant {
	/** 物料属性：新品. */
	public static final String WLSX_XP = "066216";

	/** 升级类型，替换方案.*/
	public static final String SJLX_THFA = "089346";
	/** 升级类型，赠送.*/
	public static final String SJLX_ZS = "089347";
	/** 升级类型，一换多.*/
	public static final String SJlX_YHD = "089348";

	/** 业务类型: 赠送.*/
	public static final String YWLX_XS = "083324";
	/** 业务类型: 销售.*/
	public static final String YWLX_ZS = "083323";

	/** 审核结果. */
	public static final String SHJG_TY = "同意";
	public static final String SHJG_BH = "驳回";

	/** 换新 */
	public static final String CLFS_HX = "064286";
	/** 保内维修 */
	public static final String CLFS_BNWX = "064287";
	/** 保外维修.*/
	public static final String CLFS_BWWX = "064223";
	/** 退回 */
	public static final String CLFS_TH = "064288";
	/** 代收货 */
	public static final String CLFS_DSH = "064289";
	/** 无实物更换 */
	public static final String CLFS_WSWGH = "064290";
	/** 复检换新. */
	public static final String CLFS_FJHX = "064222";
	/** 升级退回. */
	public static final String CLFS_SJTH = "064221";
	/** 销售. */
	public static final String CLFS_XS = "064738";
	/** 赠送. */
	public static final String CLFS_ZS = "064737";

	/** 送修单头状态，待交付. */
	public static final String SX_H_ZT_DJF = "069239";
	/** 送修单头状态，已交付. */
	public static final String SX_H_ZT_YJF = "069240";
	/** 送修单身状态，已检测. */
	public static final String SX_B_ZT_YJC = "069243";
	/** 送修单身状态，已交付. */
	public static final String SX_B_ZT_YJF = "069244";

	/** 缺货等待. */
	public static final String JF_JLZT_QHDD = "090351";
	/** 已通知. */
	public static final String JF_JLZT_YTZ = "090352";
	/** 已交付. */
	public static final String JF_JLZT_YJF = "090353";
	/** 待交付. */
	public static final String JF_JLZT_DJF = "090350";

	/** 录入信息. */
	public static final int STEP_LRXX = 1;
	/** 异地交付审核. */
	public static final int STEP_YDJFSH = 2;
	/** 替换规则升级处理. */
	public static final int STEP_SJCL = 3;
	/** 替换规则升级处理确认. */
	public static final int STEP_SJCLKFQR = 4;
	/** 缺货申请. */
	public static final int STEP_QHSQ = 5;
	/** 通知客户取货. */
	public static final int STEP_TZKHQH = 7;
	/** 交付. */
	public static final int STEP_JF = 7;
	/** 交付. */
	public static final int STEP_ZPSH = 8;

	/** 查询物料替换规则对应的替换物料. */
	public static final String QUERY_REPLACE_WLBH = "SELECT thgz.WLBH FROM BO_AKL_THGZ thgz WHERE thgz.HCKJ=? AND THGZ=? AND ISNULL(YXJFZ,0)>=ISNULL(?,0) AND SX=? ORDER BY THYXJ DESC";
	/** 查询替换规则分组. */
	public static final String QUERY_THGZ = "SELECT DISTINCT THGZ FROM BO_AKL_THGZ WHERE XMLB=? AND SRKJ=? AND WLBH=? AND SX=?";
	/** 查询替换优先级. */
	public static final String QUERY_YXJFZ = "SELECT YXJFZ FROM BO_AKL_THGZ WHERE XMLB=? AND SRKJ=? AND WLBH=? AND THGZ=? AND SX=?";
	/** 查询项目类别. */
	public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询送修单号. */
	public static final String QUERY_SXDH = "SELECT SXDH FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询是否邮寄. */
	public static final String QUERY_JFDH = "SELECT JFDH FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询交付单号. */
	public static final String QUERY_SFYJ = "SELECT SFYJ FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询是否异地交付. */
	public static final String QUERY_SFYDJF = "SELECT SFYDJF FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询赔偿金 */
	public static final String QUERY_PCJ = "SELECT PCJ FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询当前客服仓库. */
	public static final String QUERY_BDCKDM = "SELECT BDKFCKBM FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询异地送修客服. */
	public static final String QUERY_YDCKDM = "SELECT YDJFKFBM FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询当前业务类型. */
	public static final String QUERY_KHLX = "SELECT KHLX FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询当前客户类型. */
	public static final String QUERY_YWLX = "SELECT YWLX FROM BO_AKL_WXJF_P WHERE BINDID=?";
	/** 查询交付送修单身. */
	public static final String QUERY_JF_SXDS = "SELECT * FROM BO_AKL_WXJF_SX_S WHERE BINDID=?";
	/** 查询交付交付单身. */
	public static final String QUERY_JF_JFDS = "SELECT * FROM BO_AKL_WXJF_S WHERE BINDID=?";
	/** 查询交付配件单身. */
	public static final String QUERY_JF_PJDS = "SELECT * FROM BO_AKL_PJCP WHERE BINDID=?";
	/** 查询交付代用品单身. */
	public static final String QUERY_JF_DYPDS = "SELECT * FROM BO_AKL_WXJF_DYP_S WHERE BINDID=?";
	/** 查询交付对应的配件信息. */
	public static final String QUERY_JF_PJXX = "SELECT PJCPBH, PJCPXH, PJCPMC, PJXHSL FROM BO_AKL_SH_WXPJGX WHERE WXCPBH=? AND WXBW=?";
	/** 查询物料价格. */
	public static final String QUERY_WL_JG = "SELECT JG FROM BO_AKL_SH_JGGL WHERE XLMB=? AND WLBH=?";
	/** 查询配件信息重复记录. */
	public static final String QUERY_PJXX_CFJL = "SELECT COUNT(*) FROM BO_AKL_PJCP WHERE BINDID=? AND WLBH=? AND CKDM=? AND HWDM=? AND PCH=?";
	/** 更新交付记录的状态. */
	public static final String UPDATE_JFJL_ZT = "UPDATE BO_AKL_WXJF_S SET ZT=? WHERE BINDID=?";

	/** 查询交付缺货申请记录. */
	public static final String QUERY_JF_QHSQJL2 = "SELECT * FROM BO_AKL_WXJF_P p LEFT JOIN BO_AKL_WXJF_S s ON p.bindid=s.bindid WHERE p.BINDID=? AND SFQHSQ=?";
	/** 更新代用品库存. */
	public static final String UPDATE_DYP_KC = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)-? WHERE SX='049304' AND WLBH=? AND HWDM=?";
	/** 更新送修单头状态. */
	public static final String UPDATE_SXDT_ZT = "UPDATE BO_AKL_SX_P SET ZT=? WHERE SXDH=?";
	/** 更新送修记录状态. */
	public static final String UPDATE_SXDS_ZT = "UPDATE BO_AKL_SX_S SET ZT=? WHERE ID=?";
	/** 更新故障条码状态. */
	public static final String UPDATE_GZTM_ZT = "UPDATE BO_AKL_SHKC_XLH_S SET ZT=? WHERE GZTM=?";
	/** 更新配件信息数量 */
	public static final String UPDATE_PJXX_SL = "UPDATE BO_AKL_PJCP SET SL=ISNULL(SL,0)+? WHERE BINDID=? AND WLBH=? AND CKDM=? AND HWDM=? AND PCH=?";

	/** 自动匹配行号 */
	public static final String AUTO_MATCH_ROWNUM_SXDS = "SELECT MIN (rownum) FROM ( SELECT rownum FROM ( SELECT row_number () OVER (ORDER BY id) rownum FROM BO_AKL_WXJF_SX_S WHERE bindid = ? ) rowt WHERE rownum NOT IN ( SELECT ISNULL(HH,0) FROM BO_AKL_WXJF_SX_S WHERE bindid = ? ) UNION SELECT COUNT (*) + 1 rownum FROM BO_AKL_WXJF_SX_S WHERE bindid = ? ) a";
	public static final String AUTO_MATCH_ROWNUM_JFDS = "SELECT MIN (rownum) FROM ( SELECT rownum FROM ( SELECT row_number () OVER (ORDER BY id) rownum FROM BO_AKL_WXJF_S WHERE bindid = ? ) rowt WHERE rownum NOT IN ( SELECT ISNULL(HH,0) FROM BO_AKL_WXJF_S WHERE bindid = ? ) UNION SELECT COUNT (*) + 1 rownum FROM BO_AKL_WXJF_S WHERE bindid = ? ) a";

}
