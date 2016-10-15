package cn.com.akl.shgl.kc.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.timer.SequenceUpdateTimer;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RepositoryBiz {

    /**
     * ��ȡ���κ�.
     *
     * @param conn
     * @param xmlb
     * @return
     * @throws SQLException
     */
    public synchronized static String getPCH(Connection conn, String xmlb) throws SQLException {
        if (1 == 1) return "20150604";

        String key = SequenceUpdateTimer.SEQ_KEY_PREFIX + xmlb;
        String seq = DAOUtil.getStringOrNull(conn, "SELECT SEQUENCEVALUE+1 AS SEQ FROM SYSSEQUENCE WHERE SEQUENCENAME=?", key);
        if (seq == null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String pchPrefix = format.format(calendar.getTime());
            seq = pchPrefix + "001";
            DAOUtil.executeUpdate(conn, "INSERT INTO SYSSEQUENCE VALUES(?, ?, ?)", key, pchPrefix + "001", 1);
        } else {
            String zq = DAOUtil.getStringOrNull(conn, "SELECT ZQ FROM BO_AKL_SH_PCHSCGZ WHERE XMLB=?", xmlb);
            if (SequenceUpdateTimer.ZQ_MD.equals(zq)) {
                // ��ȡ���κŵ����ӹ�������ÿ��һ�Σ�����и���.
                DAOUtil.executeUpdate(conn, "UPDATE SYSSEQUENCE SET SEQUENCEVALUE=? WHERE SEQUENCENAME=? ", seq, key);
            }
        }
        return seq;
    }

    /**
     * �������к�.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param kcid
     * @param xmlb
     * @param wlbh
     * @param xh
     * @param hwdm
     * @param sx
     * @throws AWSSDKException
     */
    public void insertSequenceNo(Connection conn, int bindid, String uid, int kcid, String xmlb, String wlbh, String xh, String hwdm, String sx,
                                 String gztm) throws AWSSDKException {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("XMLB", PrintUtil.parseNull(xmlb));
        hashtable.put("SX", PrintUtil.parseNull(sx));
        hashtable.put("WLBH", PrintUtil.parseNull(wlbh));
        hashtable.put("XH", PrintUtil.parseNull(xh));
        hashtable.put("HWDM", PrintUtil.parseNull(hwdm));
        hashtable.put("ZT", PrintUtil.parseNull(RepositoryConstant.WL_ZT_ZK));
        hashtable.put("GZTM", PrintUtil.parseNull(gztm));
        hashtable.put("KCID", String.valueOf(kcid));
        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_XLH_S", hashtable, bindid, uid);
    }

    /**
     * ��������.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param xmlb
     * @param wlbh
     * @param xh
     * @param pch
     * @param hwdm
     * @param sx
     * @param sl
     * @throws AWSSDKException
     */
    public void insertLock(Connection conn, int bindid, String uid, String xmlb, String wlbh, String xh, String pch, String ckdm, String hwdm,
                           String sx, int sl) throws AWSSDKException {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("SX", PrintUtil.parseNull(sx));
        hashtable.put("WLBH", PrintUtil.parseNull(wlbh));
        hashtable.put("XMLB", PrintUtil.parseNull(xmlb));
        hashtable.put("XH", PrintUtil.parseNull(xh));
        hashtable.put("PCH", PrintUtil.parseNull(pch));
        hashtable.put("CKDM", PrintUtil.parseNull(ckdm));
        hashtable.put("HWDM", PrintUtil.parseNull(hwdm));
        hashtable.put("SDSL", String.valueOf(sl));
        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_KCSK", hashtable, bindid, uid);
    }

    /**
     * ��ѯ����¼.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param pch
     * @param hwdm
     * @param sx
     * @param wlzt
     * @return
     * @throws SQLException
     */
    public Hashtable<String, String> queryRecordHashtable(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx, String wlzt)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        Hashtable<String, String> hashtable = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, pch, hwdm, sx, wlzt);
            if (reset.next()) {
                hashtable = new Hashtable<String, String>();
                hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
                hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
                hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
                hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
                hashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
                hashtable.put("CKDM", PrintUtil.parseNull(reset.getString("CKDM")));
                hashtable.put("CKMC", PrintUtil.parseNull(reset.getString("CKMC")));
                hashtable.put("QDM", PrintUtil.parseNull(reset.getString("QDM")));
                hashtable.put("DDM", PrintUtil.parseNull(reset.getString("DDM")));
                hashtable.put("KWDM", PrintUtil.parseNull(reset.getString("KWDM")));
                hashtable.put("HWDM", PrintUtil.parseNull(reset.getString("HWDM")));
                hashtable.put("KWSL", PrintUtil.parseNull(reset.getString("KWSL")));
                hashtable.put("ZJM", PrintUtil.parseNull(reset.getString("ZJM")));
                hashtable.put("BZQ", PrintUtil.parseNull(reset.getString("BZQ")));
                hashtable.put("FZSX", PrintUtil.parseNull(reset.getString("FZSX")));
                hashtable.put("SCRQ", PrintUtil.parseNull(reset.getString("SCRQ")));
                hashtable.put("JLDW", PrintUtil.parseNull(reset.getString("JLDW")));
                hashtable.put("SX", PrintUtil.parseNull(reset.getString("SX")));
                hashtable.put("XMLB", PrintUtil.parseNull(reset.getString("XMLB")));
                hashtable.put("ZT", PrintUtil.parseNull(reset.getString("ZT")));
            }
            return hashtable;
        } finally {
            DBSql.close(ps, reset);
        }

    }

    /**
     * �Ƴ����е�����.
     *
     * @param conn
     * @param bindid
     */
    public void removeLock(Connection conn, int bindid) {
        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_KCSK", bindid);
    }

    /**
     * ��ѯ�˼�¼�ĳ��ֵĴ���.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param hwdm
     * @param sx
     * @param wlzt
     * @return
     * @throws SQLException
     */
    public int queryRecordCount(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx, String wlzt) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_RECORD_COUNT, xmlb, wlbh, pch, hwdm, sx, wlzt);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * �������ϵĿ�λ������
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param hwdm
     * @param sx
     * @param wlzt   ����״̬.
     * @param addNum
     * @return
     * @throws SQLException
     */
    public int updateMaterialInfo(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx, String wlzt, int addNum)
            throws SQLException {
        return DAOUtil.executeUpdate(conn, RepositoryConstant.UPDATE_MATERIAL_REMAINING_NUMBER, addNum, xmlb, wlbh, pch, hwdm, sx, wlzt, addNum);
    }

    /**
     * �������ϵĿ�λ������
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param hwdm
     * @param sx
     * @param wlzt   ����״̬.
     * @param addNum
     * @return
     * @throws SQLException
     */
    public int updateMaterialInfoHz(Connection conn, String xmlb, String wlbh, String pch, int addNum)
            throws SQLException {
        if (addNum > 0) {
            return DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SHKC_P SET RKSL=RKSL+?,PCSL=PCSL+? WHERE XMLB=? AND WLBH=? AND PCH=?", addNum, addNum, xmlb, wlbh, pch);
        } else {
            return DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SHKC_P SET CKSL=CKSL-? WHERE XMLB=? AND WLBH=? AND PCH=?", addNum, xmlb, wlbh, pch);
        }
    }

    /**
     * ��ѯ�����ڿⷿ��ʣ������.
     *
     * @param conn JDBC����
     * @param xmlb ��Ŀ���
     * @param wlbh ���ϱ��
     * @param ckdm �ֿ����
     * @return ��������
     * @throws SQLException
     */
    private int queryMaterialRemainingNumberInRepository(Connection conn, String xmlb, String wlbh, String ckdm, String sx) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER_INKF, xmlb, wlbh, ckdm, sx,
                RepositoryConstant.WL_ZT_ZK);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ��ѯ��ǰ��λ������ʣ�����.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param sx
     * @return
     * @throws SQLException
     */
    private int queryMaterialRemainingNumberInStorage(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx)
            throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER, xmlb, wlbh, pch, hwdm, sx,
                RepositoryConstant.WL_ZT_ZK);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ��ѯ�ڿ�����к�.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param pch
     * @param sx
     * @param gztm
     * @return
     * @throws SQLException
     */
    public int queryXLHCount(Connection conn, String xmlb, String wlbh, String pch, String ckdm, String sx, String gztm) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_XLH_COUNT, xmlb, wlbh, pch, ckdm, sx, RepositoryConstant.WL_ZT_ZK, gztm);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ͨ���ͺŲ�ѯ�ڿ�����к�.
     *
     * @param conn
     * @param xmlb
     * @param xh
     * @param gztm
     * @return
     * @throws SQLException
     */
    public int queryXLHCount(Connection conn, String xmlb, String xh, String ckdm, String gztm) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_XLH_COUNT_XH, xmlb, xh, gztm);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ��ѯ��λ�����ϼ��������Ժ������.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param pch
     * @param hwdm
     * @param sx
     * @return
     * @throws SQLException
     */
    public int queryMaterialCanUse(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx) throws SQLException {
        int lockNum = queryMaterialLockNumber(conn, xmlb, wlbh, pch, hwdm, sx);
        int hasNum = queryMaterialRemainingNumberInStorage(conn, xmlb, wlbh, pch, hwdm, sx);
        return hasNum - lockNum;
    }

    /**
     * ��ѯ�����ڲֿ��ʣ���������.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param ckdm
     * @param sx
     * @return
     * @throws SQLException
     */
    public int queryMaterialCanUseInCK(Connection conn, String xmlb, String wlbh, String ckdm, String sx) throws SQLException {
        int lockNum = queryMaterialLockNumberInRepository(conn, xmlb, wlbh, ckdm, sx);
        int hasNum = queryMaterialRemainingNumberInRepository(conn, xmlb, wlbh, ckdm, sx);
        return hasNum - lockNum;
    }

    /**
     * ��ѯ���ϵ���;����.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param hwdm
     * @param sx
     * @return
     * @throws SQLException
     */
    public int queryMaterialRemainingNumberOnTheWay(Connection conn, String xmlb, String wlbh, String hwdm, String sx) throws SQLException {
        Integer cc = DAOUtil
                .getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER, xmlb, wlbh, hwdm, sx, RepositoryConstant.WL_ZT_ZK);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ��ѯ���ϵ���������.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param hwdm
     * @param sx
     * @return
     * @throws SQLException
     */
    public int queryMaterialRemainingNumberAll(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER_ALL, xmlb, wlbh, hwdm, sx);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ��ȡ��λ��������.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param pch
     * @param hwdm
     * @param sx
     * @return
     * @throws SQLException
     */
    private int queryMaterialLockNumber(Connection conn, String xmlb, String wlbh, String pch, String hwdm, String sx) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER_LOCK, xmlb, wlbh, pch, hwdm, sx);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ��ѯ�ֿ���������.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param sx
     * @return
     * @throws SQLException
     */
    private int queryMaterialLockNumberInRepository(Connection conn, String xmlb, String wlbh, String ckdm, String sx) throws SQLException {
        Integer cc = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER_INKF_LOCK, xmlb, wlbh, ckdm, sx);
        return cc == null ? 0 : cc.intValue();
    }

    /**
     * ӳ���λ����.
     *
     * @param conn
     * @param xmlb
     * @param ckdm
     * @param wlbh
     * @param sx
     * @return
     */
    public String mapPoistion(Connection conn, String xmlb, String ckdm, String wlbh, String sx) {
        return ckdm;
    }

    /**
     * ת����λ����Ϊ �ֿ⡢����������λ��
     *
     * @param hwdm
     * @return
     */
    public Hashtable<String, String> parseHwdm(String hwdm) {
        return new Hashtable<String, String>();
    }

    /**
     * �Զ�ץȡ�������ϲ�����SQL�����λ��������Ϣ.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @param ckdm
     * @param sx
     * @param sl
     * @param updateSql
     * @throws SQLException
     * @throws AWSSDKException
     */
    private boolean autoFetch(Connection conn, int bindid, String uid, String xmlb, String wlbh, String xh, String ckdm, String sx, int sl,
                              Hashtable<String, String> hashtable, String updateSql, int upateId, boolean isLock) throws SQLException, AWSSDKException {
        int total = sl;

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(RepositoryConstant.QUERY_KYWL);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, ckdm, sx, RepositoryConstant.WL_ZT_ZK);
            while (total > 0 && reset.next()) {
                String pch = reset.getString("PCH");
                String hwdm = reset.getString("HWDM");
                int hasSl = reset.getInt("KWSL");
                int lockNum = queryMaterialLockNumber(conn, xmlb, wlbh, pch, hwdm, sx);
                if (hasSl > lockNum) {
                    int useSl = hasSl - lockNum;
                    if (useSl < total) {
                        continue;
                    }

                    // ����ID���½����л�λ����.
                    if (hashtable == null) {
                        int updateCount = DAOUtil.executeUpdate(conn, updateSql, pch, hwdm, upateId);
                        if (updateCount != 1) {
                            throw new RuntimeException("���»�λ����ʧ�ܣ�");
                        }
                    } else {
                        hashtable.put("PCH", PrintUtil.parseNull(pch));
                        hashtable.put("HWDM", PrintUtil.parseNull(hwdm));
                        hashtable.put("KWSL", String.valueOf(useSl));
                    }

                    if (isLock) {
                        insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, total);
                    }

                    total = 0;
                    break;
                }
            }
            return total == 0;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * �Զ�ץȡ����.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param xmlb
     * @param wlbh
     * @param xh
     * @param ckdm
     * @param sx
     * @param sl
     * @param hashtable
     * @return
     * @throws SQLException
     * @throws AWSSDKException
     */
    public Vector<Hashtable<String, String>> autoFetchs(Connection conn, int bindid, String uid, String xmlb, String wlbh, String xh, String ckdm,
                                                        String sx, int sl, Hashtable<String, String> hashtable) throws SQLException, AWSSDKException {
        Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();

        int total = sl;
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(RepositoryConstant.QUERY_KYWL);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, ckdm, sx, RepositoryConstant.WL_ZT_ZK);
            while (total > 0 && reset.next()) {
                String pch = reset.getString("PCH");
                String hwdm = reset.getString("HWDM");
                int hasSl = reset.getInt("KWSL");
                int lockNum = queryMaterialLockNumber(conn, xmlb, wlbh, pch, hwdm, sx);
                if (hasSl > lockNum) {
                    int useSl = hasSl - lockNum;
                    hashtable.put("PCH", PrintUtil.parseNull(pch));
                    hashtable.put("HWDM", PrintUtil.parseNull(hwdm));
                    hashtable.put("KWSL", String.valueOf(hasSl));

                    if (total > useSl) {
                        hashtable.put("SL", String.valueOf(useSl));
                        vector.add((Hashtable<String, String>) hashtable.clone());
                        insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, useSl);
                        total -= useSl;
                    } else {
                        hashtable.put("SL", String.valueOf(total));
                        vector.add((Hashtable<String, String>) hashtable.clone());
                        insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, total);
                        total = 0;
                    }
                }
            }

            if (total != 0) {
                throw new RuntimeException("�ͺţ� " + xh + "�����ԣ�" + sx + " ����п����������㣡");
            }

            return vector;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * �Զ��ҳ��������ϣ�ͨ��updateSQL�������κźͻ�λ���롣
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param xmlb
     * @param wlbh
     * @param xh
     * @param ckdm
     * @param sx
     * @param sl
     * @param updateSql
     * @param upateId
     * @return true����ɹ���false���ϲ���.
     * @throws SQLException
     * @throws AWSSDKException
     */
    public boolean autoFetch1(Connection conn, int bindid, String uid, String xmlb, String wlbh, String xh, String ckdm, String sx, int sl,
                              String updateSql, int upateId) throws SQLException, AWSSDKException {
        return autoFetch(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, sl, null, updateSql, upateId, true);
    }

    /**
     * �Զ��ҳ��������ϣ�ͨ������hashtable�����ֵ.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param xmlb
     * @param wlbh
     * @param xh
     * @param ckdm
     * @param sx
     * @param sl
     * @param hashtable
     * @return true����ɹ���false���ϲ���.
     * @throws SQLException
     * @throws AWSSDKException
     */
    public boolean autoFetch(Connection conn, int bindid, String uid, String xmlb, String wlbh, String xh, String ckdm, String sx, int sl,
                             Hashtable<String, String> hashtable) throws SQLException, AWSSDKException {
        return autoFetch(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, sl, hashtable, null, 0, true);
    }

    /**
     * ץȡ���ϣ���������.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param xmlb
     * @param wlbh
     * @param xh
     * @param ckdm
     * @param sx
     * @param sl
     * @param hashtable
     * @return
     * @throws SQLException
     * @throws AWSSDKException
     */
    public boolean autoFetchNoLock(Connection conn, int bindid, String uid, String xmlb, String wlbh, String xh, String ckdm, String sx, int sl,
                                   Hashtable<String, String> hashtable) throws SQLException, AWSSDKException {
        return autoFetch(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, sl, hashtable, null, 0, false);
    }

    /**
     * ��佻��������Ϣ.
     *
     * @param conn
     * @param hashtable
     * @throws SQLException
     */
    public void fillDeliveryMaterialInfo(Connection conn, Hashtable<String, String> hashtable) throws SQLException {
        String wlbh = hashtable.get("WLBH");
        PreparedStatement stat = null;
        ResultSet reset = null;
        try {
            stat = conn.prepareStatement("SELECT WLBH, WLMC, LPN8, GG FROM BO_AKL_CPXX WHERE WLBH=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, stat, wlbh);
            if (reset.next()) {
                hashtable.put("WLMC", reset.getString("WLMC"));
                hashtable.put("XH", PrintUtil.parseNull(reset.getString("LPN8")));
                hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
            } else {
                throw new RuntimeException("���ϱ��:" + wlbh + "������!");
            }
        } finally {
            DBSql.close(stat, reset);
        }
    }

}
