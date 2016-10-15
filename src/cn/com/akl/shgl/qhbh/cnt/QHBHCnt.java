package cn.com.akl.shgl.qhbh.cnt;

public class QHBHCnt {

	public static final String subTitle = "补货调拨流程";
	public static final String dblx = "新品配货";//调拨类型
	public static final String uuid = "42e5d26e5959793b50b23042933f2964";//调拨流程UUID
	
	public static final String failMessage0 = "库存不足配货失败";
	public static final String failMessage1 = "无替换规则配货失败";
	public static final String failMessage2 = "替换规则库存不足配货失败";
	
	public static final String is = "025000";//是
	public static final String no = "025001";//否
	
	public static final String zt0 = "070247";//在库
	
	public static final String qhzt2 = "076278";//已申请
	public static final String qhzt0 = "076279";//配货中
	public static final String qhzt1 = "076280";//已配货
	
	public static final String bhlx0 = "0";//欠货及特殊申请
	public static final String bhlx1 = "1";//安全库存
	
	public static final String phfs = "073265";//配货方式：安全库存
	
	public static final String xmlb0 = "061271";//罗技
	public static final String xmlb1 = "061272";//魔声
	
	public static final String sx0 = "066215";//RMA新品
	public static final String sx1 = "066216";//DOA新品
	
	public static final String sx2 = "066208";//FGER
	public static final String sx3 = "066209";//FG
	
	/**
	 * 主表字段查询(BO_AKL_QHBH_P)
	 */
	public static final String QUERY_P_QHBHDH = "SELECT QHBHDH FROM BO_AKL_QHBH_P WHERE BINDID=?";//补货单号
	public static final String QUERY_P_XMLB = "SELECT XMLB FROM BO_AKL_QHBH_P WHERE BINDID=?";//项目类别
	public static final String QUERY_P_BHLX = "SELECT BHLX FROM BO_AKL_QHBH_P WHERE BINDID=?";//补货类型
	public static final String QUERY_P_FHCKBM = "SELECT FHCKBM FROM BO_AKL_QHBH_P WHERE BINDID=?";//发货客服编码
	public static final String QUERY_WLMC = "SELECT WLMC FROM BO_AKL_CPXX WHERE WLBH=?";//物料名称
	public static final String QUERY_PN8L = "SELECT LPN8 FROM BO_AKL_CPXX WHERE WLBH=?";//型号8L
	public static final String QUERY_PN9L = "SELECT LPN9 FROM BO_AKL_CPXX WHERE WLBH=?";//型号9L
	public static final String QUERY_PN9L_WLBH = "SELECT WLBH FROM BO_AKL_CPXX WHERE LPN8=?";//型号9L的物料编号
	
	public static final String QUERY_USERNAME = "SELECT USERNAME FROM ORGUSER WHERE USERID=?";//用户名
	public static final String QUERY_USERID = "SELECT USERID FROM ORGUSER WHERE USERID=?";//用户名账号
	public static final String QUERY_KFCKMC = "SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=?";//仓库名称
	
	
	/**
	 * 子表信息查询(BO_AKL_QHBH_S)
	 */
	public static final String QUERY_S = "SELECT * FROM BO_AKL_QHBH_S WHERE BINDID=? ORDER BY YXJ DESC,SQSJ,SL";//补货子表数据
	public static final String QUERY_S_count = "SELECT COUNT(1) FROM BO_AKL_QHBH_S WHERE BINDID=?";//统计补货子表记录
	public static final String QUERY_S_HZ = "SELECT XMLB,SQCPWLBH,SQCPPN,PCH,HWDM,PHSX,SUM(SQCPSL)SL,JFKFBM FROM BO_AKL_QHBH_S WHERE BINDID=? AND JFKFBM=? GROUP BY XMLB,SQCPWLBH,SQCPPN,PCH,HWDM,PHSX,JFKFBM";//每个客服的补货子表汇总数据
	
	public static final String QUERY_DB_DETAIL = "SELECT * FROM BO_AKL_QHBH_S WHERE BINDID=? AND JFKFBM=?";//按客服中心推调拨数据（明细）
	public static final String QUERY_DB_GATHER = "SELECT SQCPWLBH,SQCPPN,SQCPZWMC,PHSX,JFKFBM,JFKFMC,SUM(SQCPSL)CKSL FROM BO_AKL_QHBH_S WHERE BINDID=? AND JFKFBM=? GROUP BY SQCPWLBH,SQCPPN,SQCPZWMC,PHSX,JFKFBM,JFKFMC";//按客服中心推调拨数据（汇总）
	public static final String QUERY_DB_GATHER_NEW = "SELECT SQCPWLBH, SQCPPN, SQCPZWMC, PHSX, JFKFBM, JFKFMC, cp.GG, cp.SJLH, cp.CPFL, SUM (ISNULL(kcsk.KCKYZ, 0)) KCKYZ, SUM (SQCPSL) CKSL FROM BO_AKL_QHBH_S bh LEFT JOIN ( SELECT kc.XMLB, kc.WLBH, kc.PCH, kc.SX, kc.CKDM, SUM (ISNULL(kc.KWSL, 0)) - SUM (ISNULL(sk.SDSL, 0)) KCKYZ FROM BO_AKL_SHKC_S kc LEFT JOIN ( SELECT XMLB, WLBH, PCH, SX, CKDM, SUM (SDSL) SDSL FROM BO_AKL_SH_KCSK GROUP BY XMLB, WLBH, PCH, SX, CKDM ) sk ON kc.XMLB = sk.XMLB AND kc.WLBH = sk.WLBH AND kc.PCH = sk.PCH AND kc.SX = sk.SX AND kc.CKDM = sk.CKDM AND kc.ZT = '070247' GROUP BY kc.XMLB, kc.WLBH, kc.PCH, kc.SX, kc.CKDM ) kcsk ON bh.XMLB = kcsk.XMLB AND bh.SQCPWLBH = kcsk.WLBH AND bh.PCH = kcsk.PCH AND bh.PHSX = kcsk.SX AND kcsk.CKDM =? LEFT JOIN BO_AKL_CPXX cp ON bh.SQCPWLBH = cp.WLBH WHERE bh.JFKFBM=? AND bh.BINDID =? GROUP BY SQCPWLBH, SQCPPN, SQCPZWMC, PHSX, JFKFBM, JFKFMC, cp.GG, cp.SJLH, cp.CPFL";//按客服中心推调拨数据（汇总）
	
	public static final String QUERY_KFFZR = "SELECT a.JFKFBM,b.KFCKMC,b.KFFZR FROM BO_AKL_QHBH_S a, BO_AKL_KFCK b WHERE a.JFKFBM=b.KFCKBM AND a.BINDID=? GROUP BY a.JFKFBM,b.KFCKMC,b.KFFZR";//客服仓库负责人
	
	public static final String QUERY_S_WLBH = "SELECT SQCPWLBH,SX,SUM(SQCPSL)SQZSL FROM BO_AKL_QHBH_S WHERE BINDID=? GROUP BY SQCPWLBH,SX";//补货产品汇总
	
	/**
	 * 库存查询(BO_AKL_SHKC_S)
	 */
	public static final String QUERY_KCMX_SUM = "SELECT SUM(KWSL)KWSL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";//库位数量
	
	public static final String QUERY_SK_SUM = "SELECT SUM(SDSL)SDSL FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";//锁库数量
	public static final String QUERY_SK_All_SUM = "SELECT SUM(SDSL)SDSL FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND SX=? AND CKDM=?";//锁库总量
	
	public static final String isLockExsit = "SELECT COUNT(1)NUM FROM BO_AKL_SH_KCSK WHERE YDH=? AND XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";//是否存在锁库
	
	public static final String QUERY_KCMX_PCHAndHWDM = "SELECT PCH,HWDM,KWSL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND KWSL>0 AND CKDM=? AND ZT='"+zt0+"' ORDER BY PCH,KWSL DESC";//根据项目,物料编号,属性自动匹配库存批次和货位
	
	public static final String QUERY_LimtInventory = "SELECT KCXX FROM BO_AKL_SH_ZXKCL WHERE XMLB=? AND CKBM=? AND WLBH=? AND SXID=?";//总部库存下限
	
	public static final String QUERY_All_Shortage = "SELECT SUM(ISNULL(SL,0))QHZL FROM BO_AKL_QHBH_S WHERE BINDID=? AND PHFS='"+phfs+"' AND XMLB=? AND YCPWLBH=? AND SX=?";//该物料总缺货量
	
	public static final String QUERY_Sum = "SELECT SUM(ISNULL(KWSL,0))KCZL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND CKDM=? AND ZT='"+zt0+"'";//该物料总部库存量
	
	/**
	 * 更新操作
	 */
	public static final String UPDATE_S_PCAndHWDM = "UPDATE BO_AKL_QHBH_S SET SQCPWLBH=?,SQCPPN=?,SQCPZWMC=?,SQCPSL=?,PHSX=?,PCH=?,HWDM=?,ZT=? WHERE ID=?";//更新(申请产品物料编号/申请产品型号/申请产品中文名称/申请产品数量/批次/货位)
	
	public static final String UPDATE_S_PCAndHWDM_TH = "UPDATE BO_AKL_QHBH_S SET PCH=NULL,HWDM=NULL WHERE BINDID=?";//批次和货位(节点退回时)
	
	public static final String UPDATE_LockNum = "UPDATE BO_AKL_SH_KCSK SET SDSL=ISNULL(SDSL,0)+? WHERE YDH=? AND XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";// 更新锁库数量
	
	public static final String UPDATE_S_ZT = "UPDATE BO_AKL_QHBH_S SET ZT=? WHERE BINDID=?";//更新补货子表--状态
	public static final String UPDATE_QHJL_ZT = "UPDATE BO_AKL_QHJL SET ZT=? WHERE XMLB=? AND WLBH=? AND SX=? AND JFKFBM=? AND SFJSTH=? AND ZT=? AND SXDH=? AND SXCPHH=? AND JFCPHH=?";//更新缺货记录表--状态
	public static final String UPDATE_QHJL_SCWCGPHYY = "UPDATE BO_AKL_QHJL SET SCWCGPHYY=? WHERE XMLB=? AND WLBH=? AND SX=? AND JFKFBM=? AND SFJSTH=? AND ZT=?";//更新缺货记录表--上次未成功配货原因
	
	/**
	 * 删除补货未成功的记录
	 */
	public static final String DELETE_QHBH_S = "DELETE FROM BO_AKL_QHBH_S WHERE ID=?";
	
	
}
