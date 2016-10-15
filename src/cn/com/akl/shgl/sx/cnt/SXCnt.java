package cn.com.akl.shgl.sx.cnt;

/**
 * 定义送修常量
 * @author fengtao
 *
 */
public class SXCnt {

	public static final String is = "025000";//是
	public static final String no = "025001";//否
	public static final String empty = "";//空
	
	public static final String sx0 = "066317";//代用品
	public static final String sx1 = "066354";//坏品
	public static final String sx2 = "066326";//待检品
	
	public static final String zt0 = "070247";//在库
	public static final String zt1 = "069239";//待交付(单头)
	public static final String zt2 = "069243";//已检测(单身)
	
	public static final String zblx0 = "063281";//发票//质保类型
	public static final String zblx1 = "063282";//PID//质保类型
	public static final String zblx2 = "063283";//收据//质保类型
	
	public static final String djlx = "送修单";//待发货记录，单据类型
	public static final String wlzt = "071254";//已签收
	public static final String wlzt1 = "071252";//待处理
	
	public static final String sxfs = "1";//送修方式（邮寄送修）
	
	public static final String ywlx0 = "083323";//赠送
	public static final String ywlx1 = "083324";//销售
	
	public static final String stepNO = "1";//第一节点
	public static final String stepNO2 = "0";//其他节点
	
	public static final String clfs0 = "064286";//换新
	public static final String clfs1 = "064220";//升级复检
	public static final String clfs2 = "064221";//升级换新
	public static final String clfs3 = "064222";//升级退回
	public static final String clfs4 = "064287";//保内维修
	public static final String clfs5 = "064223";//保外维修
	public static final String clfs6 = "064737";//销售
	public static final String clfs7 = "064738";//赠送
	public static final String clfs8 = "064290";//无实物更换
	
	public static final String tphScript = "TPH@replace(@date,-)@formatZero(3,@sequencefordateandkey(SX-TPH))";//特批号生成规则
	
	/**
	 * 代用品
	 */
	public static final String QUERY_isDYP = "SELECT SFYDYP FROM BO_AKL_SX_P WHERE BINDID=?";//查询'是否有代用品'
	public static final String UPDATE_DYP_SFYKKC = "UPDATE BO_AKL_DYPXX SET SFYKKC='"+is+"' WHERE BINDID=?";//更新代用品'是否已扣库存'
	public static final String QUERY_DYP_SFYX = "SELECT * FROM BO_AKL_DYPXX WHERE SFYX='"+no+"' AND BINDID=?";//查询代用品'是否有效'
	
	public static final String QUERY_DYP = "SELECT COUNT(1)n FROM BO_AKL_DYPXX WHERE BINDID=?";//查询代用品单身
	public static final String QUERY_DYP_DE = "SELECT * FROM BO_AKL_DYPXX WHERE SFYX='"+is+"' AND SFYKKC='"+no+"' AND BINDID=?";//查询代用品单身信息
	public static final String QUERY_DYP_IN = "SELECT * FROM BO_AKL_DYPXX WHERE SFYX='"+no+"' AND SFYKKC='"+is+"' AND BINDID=?";//查询代用品单身信息
	
	public static final String UPDATE_KCHZ_DYP_DE = "UPDATE BO_AKL_SHKC_P SET CKSL=ISNULL(CKSL,0)+?,PCSL=ISNULL(PCSL,0)-? WHERE XMLB=? AND WLBH=? AND PCH=?";//更新代用品库存汇总，减
	public static final String UPDATE_KCMX_DYP_DE = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)-? WHERE SX='"+sx0+"' AND ZT='"+zt0+"' AND XMLB=? AND WLBH=? AND PCH=? AND HWDM=?";//更新代用品库存明细，减
	public static final String UPDATE_KCHZ_DYP_IN = "UPDATE BO_AKL_SHKC_P SET RKSL=ISNULL(RKSL,0)+?,PCSL=ISNULL(PCSL,0)+? WHERE XMLB=? AND WLBH=? AND PCH=?";//更新代用品库存汇总，加
	public static final String UPDATE_KCMX_DYP_IN = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE SX='"+sx0+"' AND ZT='"+zt0+"' AND WLBH=? AND HWDM=? AND XMLB=? AND PCH=?";//更新代用品库存明细，加
	
	
	/**
	 * 送修产品
	 */
	public static final String QUERY_CKDM = "SELECT KFCKBM FROM BO_AKL_SH_BMCKGX a,"
			+ "(SELECT a.ID FROM ORGDEPARTMENT a LEFT JOIN ORGUSER b ON a.ID=b.DEPARTMENTID WHERE b.USERID=(SELECT CREATEUSER FROM BO_AKL_SX_P WHERE BINDID=?))b "
			+ "WHERE a.BMBH=b.ID";//查询仓库代码
	
	public static final String QUERY_HWXX = "SELECT a.QDM,a.DDM,a.KWDM,a.HWDM FROM BO_AKL_SH_WLKWGX a,"//查询库位信息
			+ "(SELECT MAX(ID)ID FROM BO_AKL_SH_WLKWGX WHERE XMLB=? AND WLBH=? AND CKDM=? AND SFYX='"+is+"' GROUP BY XMLB,WLBH,CKDM)c WHERE a.ID=c.ID";
	
	public static final String QUERY_CKMC = "SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=?";//查询仓库名称
	public static final String QUERY_SXDH = "SELECT SXDH FROM BO_AKL_SX_P WHERE BINDID=?";//查询送修单号
	public static final String QUERY_XMLB = "SELECT XMLB FROM BO_AKL_SX_P WHERE BINDID=?";//查询项目类别
	public static final String QUERY_SXFS = "SELECT SXFS FROM BO_AKL_SX_P WHERE BINDID=?";//查询送修方式
	public static final String QUERY_YWLX = "SELECT YWLX FROM BO_AKL_SX_P WHERE BINDID=?";//查询业务类型
	public static final String QUERY_XMKF = "SELECT XMKF FROM BO_AKL_SX_P WHERE BINDID=?";//查询项目库房
	public static final String QUERY_SRSX = "SELECT SRSX FROM BO_AKL_SH_YWSXGX WHERE XMLB=? AND CLFS=? AND YWLX=?";//查询物料收入属性
	public static final String QUERY_HCSX = "SELECT HCSX FROM BO_AKL_SH_YWSXGX WHERE XMLB=? AND CLFS=? AND YWLX=?";//查询物料换出属性
	public static final String QUERY_ZBNX = "SELECT ZBNX FROM BO_AKL_CPXX WHERE XMLB=? AND WLBH=? AND LPN8=?";//查询质保年限
	public static final String QUERY_ZBJZRQ = "SELECT ZBJZRQ FROM BO_AKL_WXJF_S WHERE WLBH=? AND SN=? AND ISEND=1";//查询交付质保截止日期
	
	public static final String QUERY_SX_P = "SELECT * FROM BO_AKL_SX_P WHERE BINDID=?";//查询送修单头信息
	public static final String QUERY_SXHZ = "SELECT WLBH,WLMC,XH,SUM(SL)SL FROM BO_AKL_SX_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH";//查询送修产品单身信息（汇总）
	public static final String QUERY_SXMX = "SELECT * FROM BO_AKL_SX_S WHERE BINDID=?";//查询送修产品单身信息（明细）
	public static final String QUERY_isSXExist = "SELECT COUNT(1)N FROM BO_AKL_SX_S WHERE BINDID=?";//查询是否有送修产品
	
	public static final String QUERY_isSFSJ = "SELECT COUNT(1)n FROM BO_AKL_SX_S WHERE SFSJ='"+is+"' AND BINDID=?";//查询送修产品中是否有升级记录
	public static final String QUERY_isSFZCSJ = "SELECT COUNT(1)n FROM BO_AKL_SX_S WHERE SFZCSJ='"+is+"' AND BINDID=?";//查询送修产品中是否有再次升级记录
	public static final String QUERY_isExistKCHZ = "SELECT COUNT(1) n FROM BO_AKL_SHKC_P WHERE XMLB=? AND WLBH=? AND PCH=?";//查询是否已存在库存
	public static final String QUERY_isExistKCMX = "SELECT COUNT(1) n FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//查询是否已存在库存
	public static final String QUERY_isExistGZMX = "SELECT COUNT(1) n FROM BO_AKL_SHKC_XLH_S WHERE XMLB=? AND WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=? AND GZTM=?";//查询是否已存在故障条码
	
	/**
	 * 单据及库存更新
	 */
	public static final String UPDATE_KCHZ = "UPDATE BO_AKL_SHKC_P SET RKSL=ISNULL(RKSL,0)+?,PCSL=ISNULL(PCSL,0)+? WHERE XMLB=? AND WLBH=? AND XH=? AND PCH=?";//更新库存信息
	public static final String UPDATE_KCMX_KWSL = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX=? AND ZT='"+zt0+"'";//更新库存（数量）
	public static final String UPDATE_KCMX_SX = "UPDATE BO_AKL_SHKC_S SET SX=? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX='"+sx2+"' AND ZT='"+zt0+"'";//更新库存（属性）

	public static final String UPDATE_SX_P_SUM = "UPDATE BO_AKL_SX_P SET YSFYHJ=ISNULL(YSPJCB,0)+ISNULL(YSYJF,0)+ISNULL(YSWXF,0)+ISNULL(YSQTF,0)+ISNULL(YSYJ,0) WHERE BINDID=?";//更新送修单头(预收费用合计)
	public static final String UPDATE_SX_P_YSYJ = "UPDATE BO_AKL_SX_P SET YSYJ=(SELECT ISNULL(SUM(ISNULL(YJ,0)),0)ZYJ FROM BO_AKL_DYPXX WHERE BINDID=BO_AKL_SX_P.BINDID AND SFSYJ='025000') WHERE BINDID=?";//更新送修单头(预收押金)
	public static final String UPDATE_SX_P_ZT = "UPDATE BO_AKL_SX_P SET ZT='"+zt1+"' WHERE BINDID=?";//更新送修单头（状态）
	public static final String UPDATE_SX_S_ZT = "UPDATE BO_AKL_SX_S SET ZT='"+zt2+"' WHERE BINDID=?";//更新送修单身（状态）
	public static final String UPDATE_SX_S_CLFSAndHH = "UPDATE BO_AKL_SX_S SET CLFS=?,SXCPHH=? WHERE ID=?";//更新送修单身（处理方式和行号），用于赠送|销售
	public static final String UPDATE_ZBJZRQ = "UPDATE BO_AKL_SX_S SET SFSCZB=?,ZBJZRQ=? WHERE ID=?";//更新质保截止日期
	public static final String UPDATE_TPH = "UPDATE BO_AKL_SX_S SET TPH=? WHERE ID=?";//更新特批号
	
	public static final String UPDATE_SX_PCH = "UPDATE BO_AKL_SX_S SET PCH=? WHERE BINDID=?";//跟新送修单身(批次号)
	public static final String UPDATE_SX_HHAndHWDMAndSX = "UPDATE BO_AKL_SX_S SET SXCPHH=?,HWDM=?,SX=? WHERE ID=?";//跟新送修单身(行号、货位代码、属性)
	
}
