package cn.com.akl.shgl.qscy.biz;

public class QSCYConstants {

	/** 主表 . */
	public static final String QSCY_MAIN = "BO_AKL_SH_QSCY_P";
	/** 子表 . */
	public static final String QSCY_SUB = "BO_AKL_SH_QSCY_S";
	/** 签收差异. */
	public static final String QSCY_WORKFLOW_UUID = "ff287ac9bdaaf0d304915481a5ae1286";

    public static final String QSCYLX_JS=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_JS;
    public static final String QSCYLX_PS=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_PS;
    public static final String QSCYLX_QXDD=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_QXDD;
    public static final String QSCYLX_DHDS=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_DHDS;
    public static final String QSCYLX_LF = cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_LF;
    public static final String QSCYLX_ZLCY = cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_ZLCY;

	/** 反写库房的类型. */
	public static String[] cylxBack = cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.cylxBack;

	/** 查询物流单的物流信息. */
	public static final String QUERY_WLD = "SELECT wlds.DH CKDH,wldt.WLD WLDH,SHF SHDW,wlds.SHR SHR, wlds.SHRDH SHRLXFS, FZ SFZ,DZ MDZ, CYF CYS , YSLX YSFS FROM BO_AKL_WLYSD_S wlds INNER JOIN BO_AKL_WLYSD_P wldt ON wlds.bindid=wldt.bindid WHERE DH=?";
	/** 查询签收物料详情. */
	public static final String QUERY_WLDS = "SELECT dfhs.ID, XMLB, SX, FHR, FHRDH, FHRYX, FHDZ, DJLB, DH, SHDZ, SHR, SHRDH, SHRYX, FHKFCKBM FHKFDM, FHKFCKMC FHKFMC, SHKFCKMC, SHKFCKBM, WLZT, FHF, FHFLX, SHF, SHFLX, SHS, SHSHI, SHQX, FHS, FHSHI, FHQX, WLBH WLH, XH CPXH, WLMC CPMC, SL, QSSL FROM BO_AKL_DFH_P dfhp LEFT JOIN BO_AKL_DFH_S dfhs ON dfhp.bindid = dfhs.bindid WHERE DH=? AND SHR=?";
	/** 差异类型. */
	public static final String QUERY_CYLX = "SELECT CYLX FROM BO_AKL_SH_QSCY_P WHERE BINDID=?";
	/** 签收差异单身. */
	public static final String QUEYR__QSCYDS = "SELECT * FROM " + QSCY_SUB + " WHERE BINDID=?";
	/** 签收差异单头. */
	public static final String QUEYR__QSCYDT = "SELECT * FROM " + QSCY_MAIN + " WHERE BINDID=?";
	/** 查询出库单号. */
	public static final String QUEYR__CKDH = "SELECT CKDH FROM " + QSCY_MAIN + " WHERE BINDID=?";
	/** 查询出库单号做过的签收差异次数. */
	public static final String QUERY_CKDH_COUNT = "SELECT COUNT(*) FROM BO_AKL_SH_QSCY_P WHERE CKDH=? AND BINDID<>? AND SHR=?";

	/** 差异类型：数量差异. */
	public static final String CYLX_SLCY = "数量差异";
	/** 差异类型：质量差异. */
	public static final String CYLX_ZLCY = "质量差异";

}
