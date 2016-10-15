package cn.com.akl.shgl.sx.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

public class StepNo6SheetModelUp extends ExcelDownFilterRTClassA {

    /** ���ϱ�� */
    private static final int EXCEL_COL_WLBH=0;
    /** P/N�� */
    private static final int EXCEL_COL_XH=1;
    /** �������� */
    private static final int EXCEL_COL_WLMC=2;
    /** �״�����SN */
    private static final int EXCEL_COL_SCSXSN=3;
    /** SN */
    private static final int EXCEL_COL_SN=4;
    /** �ձ��Ϻ� */
    private static final int EXCEL_COL_RBLH=5;
    /** ����CCP/N */
    private static final int EXCEL_COL_CCPN=6;
    /** ʹ�������� */
    private static final int EXCEL_COL_SYRLX=7;
    /** ���� */
    private static final int EXCEL_COL_SL=8;
    /** �ʱ����� */
    private static final int EXCEL_COL_ZBLX=9;
    /** ƾ֤��� */
    private static final int EXCEL_COL_PZBH=10;
    /** �Ƿ��״��ʱ� */
    private static final int EXCEL_COL_SFSCZB=11;
    /** �������� */
    private static final int EXCEL_COL_GMRQ=12;
    /** �ʱ���ֹ���� */
    private static final int EXCEL_COL_ZBJZRQ=13;
    /** �۸� */
    private static final int EXCEL_COL_JG=14;
    /** �������� */
    private static final int EXCEL_COL_GZTM=15;
    /** ����ԭ�� */
    private static final int EXCEL_COL_GZYY=16;
    /** ����ԭ��ע */
    private static final int EXCEL_COL_GZYYBZ=17;
    /** ����ʽ */
    private static final int EXCEL_COL_CLFS=18;
    /** ������� */
    private static final int EXCEL_COL_CLYJ=19;
    /** �Ƿ����� */
    private static final int EXCEL_COL_SFSJ=20;
    /** �������� */
    private static final int EXCEL_COL_SJLX=21;
    /** �������� */
    private static final int EXCEL_COL_SJMS=22;
    /** �������� */
    private static final int EXCEL_COL_PFMS=23;
    /** �������� */
    private static final int EXCEL_COL_SJMS2=24;
    /** �Ƿ����� */
    private static final int EXCEL_COL_SFTP=25;
    /** ������ */
    private static final int EXCEL_COL_TPH=26;
    /** �Ƿ���� */
    private static final int EXCEL_COL_SFDC=27;
    /** ���� */
    private static final int EXCEL_COL_FJ=28;
    /** �Ƿ��Ѽӹ� */
    private static final int EXCEL_COL_SFYJG=29;

    public StepNo6SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("�������Զ����������Ϣ.");
    }

    @Override
    public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
        HSSFSheet sheet = arg0.getSheetAt(0);
        int maxRowNum = sheet.getLastRowNum();
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DBSql.open();

            Hashtable<String, String> sxpData = BOInstanceAPI.getInstance().getBOData("BO_AKL_SX_P", bindid);
            // ��֤�Ƿ���д����Ŀ���,ҵ������.
            String xmlb = sxpData.get("XMLB");
            Vector<Hashtable<String, String>> sxds = BOInstanceAPI.getInstance().getBODatas("BO_AKL_SX_S", bindid);

            if (xmlb == null || xmlb.equals("")) {
                throw new RuntimeException("���ݴ�����ϴ�!");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // ����м�¼
                fillRow(conn, row, xmlb, sxds.get(i - ExcelUtil.EXCEL_START));
            }
            return ExcelUtil.getClearWorkBook(arg0);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return ExcelUtil.getClearWorkBook(arg0);
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!", true);
            return ExcelUtil.getClearWorkBook(arg0);
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ����м�¼�����ݿͻ����ϱ�Ż��ͺţ�ѯ��������������.
     *
     * @param row
     * @throws SQLException
     */
    private void fillRow(Connection conn, HSSFRow row, String xmlb, Hashtable<String, String> reData) throws SQLException {
        // ��ȡ������Ϣ.
        bmzh(conn, row, xmlb);

        // У��ÿ����Ϣ�Ƿ�ı�.
        String WLBHStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_WLBH));
        String WLBHStr2 = reData.get("WLBH");
        if (!isEquals(WLBHStr, WLBHStr2)) {
            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У����ϱ�� " + WLBHStr + " ��ԭ����" + WLBHStr2 + "����������!");
        }

        String XHStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_XH));
        String XHStr2 = reData.get("XH");
        if (!isEquals(XHStr, XHStr2)) {
            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�P/N�� " + XHStr + " ��ԭ����" + XHStr2 + "����������!");
        }

        String SNStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SN));
        String SNStr2 = reData.get("SN");
        if (!isEquals(SNStr, SNStr2)) {
            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�SN " + SNStr + " ��ԭ����" + SNStr2 + "����������!");
        }

        String WLMCStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_WLMC));
        String WLMCStr2 = reData.get("WLMC");
        if (!isEquals(WLMCStr, WLMCStr2)) {
            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��������� " + WLMCStr + " ��ԭ����" + WLMCStr2 + "����������!");
        }

        // ���¿���д�ֶ�.
        String id = reData.get("ID");
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SX_S SET RBLH=?,CCPN=?,SYRLX=?,ZBLX=?,PZBH=?,SFSCZB=?,GMRQ=?,ZBJZRQ=?,GZTM=?,GZYY=?,GZYYBZ=?,CLFS=?,CLYJ=?,SFTP=?,SFDC=? WHERE ID=?",
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_RBLH)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CCPN)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SYRLX)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBLX)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_PZBH)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSCZB)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GMRQ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBJZRQ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZTM)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYY)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYYBZ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLFS)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLYJ)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFTP)),
                ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFDC)),
                id);
    }

    public boolean isEquals(String str1, String str2) {
        if (str1 == null && str2 != null) {
            return false;
        }
        if (str1 == str2 || str1.equals(str2)) {
            return true;
        }
        return false;
    }

    /**
     * ����ת��.
     *
     * @param conn
     * @param row
     * @throws SQLException
     */
    private void bmzh(Connection conn, HSSFRow row, String xmlb) throws SQLException {
        String SYRLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SYRLX));
        if (SYRLXStr != null && !SYRLXStr.equals("")) {
            String SYRLX = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='062' AND (XLMC=? or XLBM=?)", SYRLXStr, SYRLXStr);
            if (SYRLX == null || SYRLX.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�ʹ�������� " + SYRLXStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SYRLX, SYRLX);
        }
        String ZBLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBLX));
        if (ZBLXStr != null && !ZBLXStr.equals("")) {
            String ZBLX = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='063' AND (XLMC=? or XLBM=?)", ZBLXStr, ZBLXStr);
            if (ZBLX == null || ZBLX.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��ʱ����� " + ZBLXStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_ZBLX, ZBLX);
        }
        String SFSCZBStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSCZB));
        if (SFSCZBStr != null && !SFSCZBStr.equals("")) {
            String SFSCZB = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSCZBStr, SFSCZBStr);
            if (SFSCZB == null || SFSCZB.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ��״��ʱ� " + SFSCZBStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFSCZB, SFSCZB);
        }
        String GZYYStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYY));
        if (GZYYStr != null && !GZYYStr.equals("")) {
            String GZYY = DAOUtil.getStringOrNull(conn, "SELECT REPLACE(XLMC, '/', '-') XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='075' AND  CHARINDEX((select XLMC FROM BO_AKL_DATA_DICT_S WHERE XLBM=?), XLMC)<>0 AND (XLMC like '%'+? or XLMC like ?+'%')", xmlb, GZYYStr, GZYYStr);
            if (GZYY == null || GZYY.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�����ԭ�� " + GZYYStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_GZYY, GZYY);
        }
        String CLFSStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLFS));
        if (CLFSStr != null && !CLFSStr.equals("")) {
            String CLFS = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='064' AND (XLMC=? or XLBM=?)", CLFSStr, CLFSStr);
            if (CLFS == null || CLFS.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�����ʽ " + CLFSStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_CLFS, CLFS);
        }
        String SFSJStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSJ));
        if (SFSJStr != null && !SFSJStr.equals("")) {
            String SFSJ = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSJStr, SFSJStr);
            if (SFSJ == null || SFSJ.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ����� " + SFSJStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFSJ, SFSJ);
        }
        String SFTPStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFTP));
        if (SFTPStr != null && !SFTPStr.equals("")) {
            String SFTP = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFTPStr, SFTPStr);
            if (SFTP == null || SFTP.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ����� " + SFTPStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFTP, SFTP);
        }
        String SFDCStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFDC));
        if (SFDCStr != null && !SFDCStr.equals("")) {
            String SFDC = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFDCStr, SFDCStr);
            if (SFDC == null || SFDC.equals("")) {
                throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ���� " + SFDCStr + " �����ڣ�����!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFDC, SFDC);
        }
    }
}
