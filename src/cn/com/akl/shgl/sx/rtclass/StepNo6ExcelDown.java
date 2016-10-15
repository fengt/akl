package cn.com.akl.shgl.sx.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import cn.com.akl.util.StrUtil;
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
import java.util.Hashtable;

/**
 * Created by huangming on 2015/7/29.
 */
public class StepNo6ExcelDown extends ExcelDownFilterRTClassA {

    public StepNo6ExcelDown(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("���ش�����");
    }

    /**
     * ���ϱ��
     */
    private static final int EXCEL_COL_WLBH = 0;
    /**
     * P/N��
     */
    private static final int EXCEL_COL_XH = 1;
    /**
     * ��������
     */
    private static final int EXCEL_COL_WLMC = 2;
    /**
     * �״�����SN
     */
    private static final int EXCEL_COL_SCSXSN = 3;
    /**
     * SN
     */
    private static final int EXCEL_COL_SN = 4;
    /**
     * �ձ��Ϻ�
     */
    private static final int EXCEL_COL_RBLH = 5;
    /**
     * ����CCP/N
     */
    private static final int EXCEL_COL_CCPN = 6;
    /**
     * ʹ��������
     */
    private static final int EXCEL_COL_SYRLX = 7;
    /**
     * ����
     */
    private static final int EXCEL_COL_SL = 8;
    /**
     * �ʱ�����
     */
    private static final int EXCEL_COL_ZBLX = 9;
    /**
     * ƾ֤���
     */
    private static final int EXCEL_COL_PZBH = 10;
    /**
     * �Ƿ��״��ʱ�
     */
    private static final int EXCEL_COL_SFSCZB = 11;
    /**
     * ��������
     */
    private static final int EXCEL_COL_GMRQ = 12;
    /**
     * �ʱ���ֹ����
     */
    private static final int EXCEL_COL_ZBJZRQ = 13;
    /**
     * �۸�
     */
    private static final int EXCEL_COL_JG = 14;
    /**
     * ��������
     */
    private static final int EXCEL_COL_GZTM = 15;
    /**
     * ����ԭ��
     */
    private static final int EXCEL_COL_GZYY = 16;
    /**
     * ����ԭ��ע
     */
    private static final int EXCEL_COL_GZYYBZ = 17;
    /**
     * ����ʽ
     */
    private static final int EXCEL_COL_CLFS = 18;
    /**
     * �������
     */
    private static final int EXCEL_COL_CLYJ = 19;
    /**
     * �Ƿ�����
     */
    private static final int EXCEL_COL_SFSJ = 20;
    /**
     * ��������
     */
    private static final int EXCEL_COL_SJLX = 21;
    /**
     * ��������
     */
    private static final int EXCEL_COL_SJMS = 22;
    /**
     * ��������
     */
    private static final int EXCEL_COL_PFMS = 23;
    /**
     * ��������
     */
    private static final int EXCEL_COL_SJMS2 = 24;
    /**
     * �Ƿ�����
     */
    private static final int EXCEL_COL_SFTP = 25;
    /**
     * ������
     */
    private static final int EXCEL_COL_TPH = 26;
    /**
     * �Ƿ����
     */
    private static final int EXCEL_COL_SFDC = 27;
    /**
     * ����
     */
    private static final int EXCEL_COL_FJ = 28;
    /**
     * �Ƿ��Ѽӹ�
     */
    private static final int EXCEL_COL_SFYJG = 29;


    @Override
    public HSSFWorkbook fixExcel(HSSFWorkbook hssfWorkbook) {
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int startRow = ExcelUtil.EXCEL_START;
        int curRow = startRow;
        Connection conn = null;
        try {
            conn = DBSql.open();

            /** ����. */
            PreparedStatement ps = null;
            ResultSet reset = null;
            try {
                ps = conn.prepareStatement("SELECT * FROM BO_AKL_SX_S WHERE BINDID=?");
                reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
                while (reset.next()) {
                    HSSFRow row = sheet.createRow(curRow++);
                    ExcelUtil.setCellValue(row, EXCEL_COL_SL, PrintUtil.parseNull(reset.getString("SL")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SJLX, PrintUtil.parseNull(reset.getString("SJLX")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_CLFS, PrintUtil.parseNull(reset.getString("CLFS")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, PrintUtil.parseNull(reset.getString("WLMC")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_FJ, PrintUtil.parseNull(reset.getString("FJ")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_GMRQ, PrintUtil.parseNull(reset.getString("GMRQ")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_RBLH, PrintUtil.parseNull(reset.getString("RBLH")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_GZYY, PrintUtil.parseNull(reset.getString("GZYY")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_GZYYBZ, PrintUtil.parseNull(reset.getString("GZYYBZ")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_JG, PrintUtil.parseNull(reset.getString("JG")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SJMS2, PrintUtil.parseNull(reset.getString("SJMS2")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SFSJ, PrintUtil.parseNull(reset.getString("SFSJ")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SFSCZB, PrintUtil.parseNull(reset.getString("SFSCZB")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_CCPN, PrintUtil.parseNull(reset.getString("CCPN")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SYRLX, PrintUtil.parseNull(reset.getString("SYRLX")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_XH, PrintUtil.parseNull(reset.getString("XH")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_WLBH, PrintUtil.parseNull(reset.getString("WLBH")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_PFMS, PrintUtil.parseNull(reset.getString("PFMS")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SFDC, PrintUtil.parseNull(reset.getString("SFDC")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SCSXSN, PrintUtil.parseNull(reset.getString("SCSXSN")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_PZBH, PrintUtil.parseNull(reset.getString("PZBH")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_ZBLX, PrintUtil.parseNull(reset.getString("ZBLX")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_CLYJ, PrintUtil.parseNull(reset.getString("CLYJ")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SN, PrintUtil.parseNull(reset.getString("SN")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SFTP, PrintUtil.parseNull(reset.getString("SFTP")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_TPH, PrintUtil.parseNull(reset.getString("TPH")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_SJMS, PrintUtil.parseNull(reset.getString("SJMS")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_GZTM, PrintUtil.parseNull(reset.getString("GZTM")));
                    ExcelUtil.setCellValue(row, EXCEL_COL_ZBJZRQ, PrintUtil.parseNull(reset.getString("ZBJZRQ")));

                    String SYRLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SYRLX));
                    if (SYRLXStr != null && !SYRLXStr.equals("")) {
                        String SYRLX = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='062' AND (XLMC=? or XLBM=?)", SYRLXStr, SYRLXStr);
                        if (SYRLX == null || SYRLX.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�ʹ�������� " + SYRLXStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_SYRLX, SYRLX);
                    }
                    String ZBLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBLX));
                    if (ZBLXStr != null && !ZBLXStr.equals("")) {
                        String ZBLX = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='063' AND (XLMC=? or XLBM=?)", ZBLXStr, ZBLXStr);
                        if (ZBLX == null || ZBLX.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��ʱ����� " + ZBLXStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_ZBLX, ZBLX);
                    }
                    String SFSCZBStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSCZB));
                    if (SFSCZBStr != null && !SFSCZBStr.equals("")) {
                        String SFSCZB = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSCZBStr, SFSCZBStr);
                        if (SFSCZB == null || SFSCZB.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ��״��ʱ� " + SFSCZBStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_SFSCZB, SFSCZB);
                    }
                    String GZYYStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_GZYY));
                    if (GZYYStr != null && !GZYYStr.equals("")) {
                        String GZYY = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='075' AND  CHARINDEX((select XLMC FROM BO_AKL_DATA_DICT_S WHERE XLBM='$XMLB'), XLMC)<>0 AND (XLMC=? or XLMC=?)", GZYYStr, GZYYStr);
                        if (GZYY == null || GZYY.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�����ԭ�� " + GZYYStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_GZYY, GZYY);
                    }
                    String CLFSStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CLFS));
                    if (CLFSStr != null && !CLFSStr.equals("")) {
                        String CLFS = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='064' AND (XLMC=? or XLBM=?)", CLFSStr, CLFSStr);
                        if (CLFS == null || CLFS.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�����ʽ " + CLFSStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_CLFS, CLFS);
                    }
                    String SFSJStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSJ));
                    if (SFSJStr != null && !SFSJStr.equals("")) {
                        String SFSJ = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSJStr, SFSJStr);
                        if (SFSJ == null || SFSJ.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ����� " + SFSJStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_SFSJ, SFSJ);
                    }
                    String SFTPStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFTP));
                    if (SFTPStr != null && !SFTPStr.equals("")) {
                        String SFTP = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFTPStr, SFTPStr);
                        if (SFTP == null || SFTP.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ����� " + SFTPStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_SFTP, SFTP);
                    }
                    String SFDCStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFDC));
                    if (SFDCStr != null && !SFDCStr.equals("")) {
                        String SFDC = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFDCStr, SFDCStr);
                        if (SFDC == null || SFDC.equals("")) {
                            throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У��Ƿ���� " + SFDCStr + " �����ڣ�����!");
                        }
                        ExcelUtil.setCellValue(row, EXCEL_COL_SFDC, SFDC);
                    }
                }
            } finally {
                DBSql.close(ps, reset);
            }

            return hssfWorkbook;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return ExcelUtil.getClearWorkBook(hssfWorkbook);
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!", true);
            return ExcelUtil.getClearWorkBook(hssfWorkbook);
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
