package cn.com.akl.shgl.qscy.biz;

public class QSCYConstants {

	/** ���� . */
	public static final String QSCY_MAIN = "BO_AKL_SH_QSCY_P";
	/** �ӱ� . */
	public static final String QSCY_SUB = "BO_AKL_SH_QSCY_S";
	/** ǩ�ղ���. */
	public static final String QSCY_WORKFLOW_UUID = "ff287ac9bdaaf0d304915481a5ae1286";

    public static final String QSCYLX_JS=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_JS;
    public static final String QSCYLX_PS=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_PS;
    public static final String QSCYLX_QXDD=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_QXDD;
    public static final String QSCYLX_DHDS=cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_DHDS;
    public static final String QSCYLX_LF = cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_LF;
    public static final String QSCYLX_ZLCY = cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.QSCYLX_ZLCY;

	/** ��д�ⷿ������. */
	public static String[] cylxBack = cn.com.akl.ccgl.xsck.qscy.rtclass.QSCYConstants.cylxBack;

	/** ��ѯ��������������Ϣ. */
	public static final String QUERY_WLD = "SELECT wlds.DH CKDH,wldt.WLD WLDH,SHF SHDW,wlds.SHR SHR, wlds.SHRDH SHRLXFS, FZ SFZ,DZ MDZ, CYF CYS , YSLX YSFS FROM BO_AKL_WLYSD_S wlds INNER JOIN BO_AKL_WLYSD_P wldt ON wlds.bindid=wldt.bindid WHERE DH=?";
	/** ��ѯǩ����������. */
	public static final String QUERY_WLDS = "SELECT dfhs.ID, XMLB, SX, FHR, FHRDH, FHRYX, FHDZ, DJLB, DH, SHDZ, SHR, SHRDH, SHRYX, FHKFCKBM FHKFDM, FHKFCKMC FHKFMC, SHKFCKMC, SHKFCKBM, WLZT, FHF, FHFLX, SHF, SHFLX, SHS, SHSHI, SHQX, FHS, FHSHI, FHQX, WLBH WLH, XH CPXH, WLMC CPMC, SL, QSSL FROM BO_AKL_DFH_P dfhp LEFT JOIN BO_AKL_DFH_S dfhs ON dfhp.bindid = dfhs.bindid WHERE DH=? AND SHR=?";
	/** ��������. */
	public static final String QUERY_CYLX = "SELECT CYLX FROM BO_AKL_SH_QSCY_P WHERE BINDID=?";
	/** ǩ�ղ��쵥��. */
	public static final String QUEYR__QSCYDS = "SELECT * FROM " + QSCY_SUB + " WHERE BINDID=?";
	/** ǩ�ղ��쵥ͷ. */
	public static final String QUEYR__QSCYDT = "SELECT * FROM " + QSCY_MAIN + " WHERE BINDID=?";
	/** ��ѯ���ⵥ��. */
	public static final String QUEYR__CKDH = "SELECT CKDH FROM " + QSCY_MAIN + " WHERE BINDID=?";
	/** ��ѯ���ⵥ��������ǩ�ղ������. */
	public static final String QUERY_CKDH_COUNT = "SELECT COUNT(*) FROM BO_AKL_SH_QSCY_P WHERE CKDH=? AND BINDID<>? AND SHR=?";

	/** �������ͣ���������. */
	public static final String CYLX_SLCY = "��������";
	/** �������ͣ���������. */
	public static final String CYLX_ZLCY = "��������";

}
