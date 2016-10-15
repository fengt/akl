package cn.com.akl.shgl.dwrk.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StepNo1SheetModelUp extends ExcelDownFilterRTClassA {

    /**
     * Excelģ�壺 ���ϱ��.
     */
    private static final int EXCEL_COL_WLBH = 0;
    /**
     * Excelģ�壺 �ͺ�.
     */
    private static final int EXCEL_COL_PN = 1;
    /**
     * Excelģ�壺 ��������.
     */
    private static final int EXCEL_COL_WLMC = 2;
    /**
     * Excelģ�壺 ���.
     */
    private static final int EXCEL_COL_GG = 3;
    /**
     * Excelģ�壺 9PN.
     */
    private static final int EXCEL_COL_9PN = 4;
    /**
     * Excelģ�壺 ����.
     */
    private static final int EXCEL_COL_SX = 5;
    /**
     * Excelģ�壺 �۸�.
     */
    private static final int EXCEL_COL_JG = 6;
    /**
     * Excelģ�壺 Ԥ�������.
     */
    private static final int EXCEL_COL_RKSL = 7;
    /**
     * Excelģ�壺 ʵ���������.
     */
    private static final int EXCEL_COL_SJRKSL = 8;
    /**
     * Excelģ�壺 ���κ�.
     */
    private static final int EXCEL_COL_PCH = 10;
    /**
     * Excelģ�壺 �ֿ����.
     */
    private static final int EXCEL_COL_CKDM = 11;
    /**
     * Excelģ�壺 �ֿ�����.
     */
    private static final int EXCEL_COL_CKMC = 12;
    /**
     * Excelģ�壺 ��λ����.
     */
    private static final int EXCEL_COL_HWDM = 13;

    public StepNo1SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("��������Զ����������Ϣ.");
    }

    @Override
    public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
        HSSFSheet sheet = arg0.getSheetAt(0);
        int maxRowNum = sheet.getLastRowNum();
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DBSql.open();

            // ��֤�Ƿ���д�˲ֿ�.
            String shkfckbm = DAOUtil.getStringOrNull(conn, "SELECT SHKFCKBM FROM BO_AKL_SH_DWRK_P WHERE BINDID=?", bindid);
            String shkfckmc = DAOUtil.getStringOrNull(conn, "SELECT SHKFCKMC FROM BO_AKL_SH_DWRK_P WHERE BINDID=?", bindid);
            String xmlb = DAOUtil.getStringOrNull(conn, "SELECT XMLX FROM BO_AKL_SH_DWRK_P WHERE BINDID=?", bindid);

            if (shkfckbm == null || shkfckbm.equals("")) {
                throw new RuntimeException("���ݴ���ٵ���!");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // ����м�¼
                fillRow(conn, row, xmlb, shkfckbm, shkfckmc);
            }
            return arg0;
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
    private void fillRow(Connection conn, HSSFRow row, String xmlb, String shkfckbm, String shkfckmc) throws SQLException {
        String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_PN));
        // �ջ��ֿ�����.
        ExcelUtil.setCellValue(row, EXCEL_COL_CKDM, shkfckbm);
        ExcelUtil.setCellValue(row, EXCEL_COL_HWDM, shkfckbm);
        ExcelUtil.setCellValue(row, EXCEL_COL_CKMC, shkfckmc);

        // ת��������Ϣ.
        String sx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SX));
        String sxNum = parseSX(conn, row, sx);
        if (sxNum == null) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�����[" + sx + "]��ϵͳ���Ҳ���!");
            ExcelUtil.setCellValue(row, EXCEL_COL_SX, "");
        } else {
            ExcelUtil.setCellValue(row, EXCEL_COL_SX, sxNum);
        }

        // ��ȡ������Ϣ.
        putWLXX(conn, row, xmlb, xh, sx, shkfckbm);

    }

    /**
     * ת��������Ϣ.
     *
     * @param conn
     * @param row
     */
    private String parseSX(Connection conn, HSSFRow row, String sx) throws SQLException {
        String sxNum = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERe DLBM='066' AND XLMC=?", sx);
        return sxNum;
    }

    /**
     * ���������Ϣ.
     *
     * @param conn
     * @param row
     * @param wlbh
     * @throws SQLException
     */
    private void putWLXX(Connection conn, HSSFRow row, String xmlb, String xh, String sx, String fhkfckbm) throws SQLException {
        /** ����. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM VIEW_AKL_SH_DWRK_CKLR WHERE XMLB=? AND LPN8=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, xh);
            if (reset.next()) {
                ExcelUtil.setCellValue(row, EXCEL_COL_WLBH, PrintUtil.parseNull(reset.getString("WLBH")));
                ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, PrintUtil.parseNull(reset.getString("WLMC")));
                ExcelUtil.setCellValue(row, EXCEL_COL_9PN, PrintUtil.parseNull(reset.getString("LPN9")));
                ExcelUtil.setCellValue(row, EXCEL_COL_GG, PrintUtil.parseNull(reset.getString("GG")));
                ExcelUtil.setCellValue(row, EXCEL_COL_JG, PrintUtil.parseNull(reset.getString("JG")));
            } else {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh + "] ��ϵͳ���Ҳ���!");
                return;
            }

            // ��ȡ����������.
            // ��ȡ���ϼ۸�.
//            if (wlbh == null || wlbh.equals("")) {
//                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh + "] ��ϵͳ���Ҳ���!");
//            } else {
//                int kcsl = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, fhkfckbm, sx);
//                ExcelUtil.setCellValue(row, EXCEL_COL_CKKCSL, String.valueOf(kcsl));
//
//                String jg = DAOUtil.getStringOrNull(conn, "SELECT JG FROM BO_AKL_SH_JGGL WHERE WLBH=? ORDER BY ID DESC", wlbh);
//                if (jg == null || jg.equals("")) {
//                    MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��" + (row.getRowNum() + 1) + "�е�PN[" + xh + "] ��ϵͳ��δά���۸�!");
//                    ExcelUtil.setCellValue(row, EXCEL_COL_JG, "0");
//                } else {
//                    ExcelUtil.setCellValue(row, EXCEL_COL_JG, jg);
//                }
//            }
        } finally {
            DBSql.close(ps, reset);
        }

    }

}
