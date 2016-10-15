package cn.com.akl.shgl.sx.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
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

public class StepNo1SheetModelUp extends ExcelDownFilterRTClassA {

    /** 物料编号 */
    private static final int EXCEL_COL_WLBH=0;
    /** P/N号 */
    private static final int EXCEL_COL_XH=1;
    /** 物料名称 */
    private static final int EXCEL_COL_WLMC=2;
    /** 首次送修SN */
    private static final int EXCEL_COL_SCSXSN=3;
    /** SN */
    private static final int EXCEL_COL_SN=4;
    /** 日报料号 */
    private static final int EXCEL_COL_RBLH=5;
    /** 北京CCP/N */
    private static final int EXCEL_COL_CCPN=6;
    /** 使用人类型 */
    private static final int EXCEL_COL_SYRLX=7;
    /** 数量 */
    private static final int EXCEL_COL_SL=8;
    /** 质保类型 */
    private static final int EXCEL_COL_ZBLX=9;
    /** 凭证编号 */
    private static final int EXCEL_COL_PZBH=10;
    /** 是否首次质保 */
    private static final int EXCEL_COL_SFSCZB=11;
    /** 购买日期 */
    private static final int EXCEL_COL_GMRQ=12;
    /** 质保截止日期 */
    private static final int EXCEL_COL_ZBJZRQ=13;
    /** 价格 */
    private static final int EXCEL_COL_JG=14;
    /** 故障原因备注 */
    private static final int EXCEL_COL_GZYYBZ=15;
    /** 处理意见 */
    private static final int EXCEL_COL_CLYJ=16;
    /** 升级类型 */
    private static final int EXCEL_COL_SJLX=17;
    /** 升级描述 */
    private static final int EXCEL_COL_SJMS=18;
    /** 批复描述 */
    private static final int EXCEL_COL_PFMS=19;
    /** 三级描述 */
    private static final int EXCEL_COL_SJMS2=20;
    /** 是否特批 */
    private static final int EXCEL_COL_SFTP=21;
    /** 特批号 */
    private static final int EXCEL_COL_TPH=22;
    /** 附件 */
    private static final int EXCEL_COL_FJ=23;

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public StepNo1SheetModelUp(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("调拨单自动填充物料信息.");
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

            // 验证是否填写了项目类别,业务类型.
            String xmlb = sxpData.get("XMLB");
            String ywlx = sxpData.get("YWLX");
            String kfckbm = sxpData.get("XMKF");

            if (xmlb == null || ywlx == null || kfckbm == null || xmlb.equals("") || ywlx.equals("") || kfckbm.equals("")) {
                throw new RuntimeException("请暂存后再上传!");
            }

            for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
                HSSFRow row = sheet.getRow(i);
                // 填充行记录
                fillRow(conn, row, xmlb, ywlx, kfckbm);
            }
            return arg0;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return ExcelUtil.getClearWorkBook(arg0);
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员!", true);
            return ExcelUtil.getClearWorkBook(arg0);
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 填充行记录，根据客户物料编号或型号，询单数量，答复数量.
     *
     * @param row
     * @throws SQLException
     */
    private void fillRow(Connection conn, HSSFRow row, String xmlb, String ywlx, String kfckbm) throws SQLException {
        // 获取物料信息.
        putWLXX(conn, row, xmlb);
        putJG(conn, row, xmlb);
        bmzh(conn, row);
        ExcelUtil.setCellValue(row, EXCEL_COL_SL, "1");
    }

    /**
     * 插入价格.
     *
     * @param conn
     * @param row
     * @param xmlb
     * @throws SQLException
     */
    private void putJG(Connection conn, HSSFRow row, String xmlb) throws SQLException {
        String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_XH));
        // 获取库存可用数量.
        // 获取物料价格.
        if (xh == null || xh.equals("")) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "第" + (row.getRowNum() + 1) + "行的PN[" + xh + "] 在系统中找不到!");
        } else {
            String jg = DAOUtil.getStringOrNull(conn, "SELECT JG FROM BO_AKL_SH_JGGL WHERE XMLB=? AND XH=? ORDER BY ID DESC", xmlb, xh);
            if (jg == null || jg.equals("")) {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "第" + (row.getRowNum() + 1) + "行的价格在系统中未维护价格!");
                ExcelUtil.setCellValue(row, EXCEL_COL_JG, "0");
            } else {
                ExcelUtil.setCellValue(row, EXCEL_COL_JG, jg);
            }
        }
    }


    /**
     * 填充物料信息.
     *
     * @param conn
     * @param row
     * @throws SQLException
     */
    private void putWLXX(Connection conn, HSSFRow row, String xmlb) throws SQLException {
        String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_XH));
        /** 单身. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_CPXX WHERE XMLB=? AND LPN8=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, xh);
            if (reset.next()) {
                ExcelUtil.setCellValue(row, EXCEL_COL_WLBH, PrintUtil.parseNull(reset.getString("WLBH")));
                ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, PrintUtil.parseNull(reset.getString("WLMC")));
                ExcelUtil.setCellValue(row, EXCEL_COL_SFTP, PrintUtil.parseNull(reset.getString("SFDC")));
            } else {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "第" + (row.getRowNum() + 1) + "行的PN[" + xh + "] 在系统中找不到!");
                return;
            }

        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 编码转换.
     *
     * @param conn
     * @param row
     * @throws SQLException
     */
    private void bmzh(Connection conn, HSSFRow row) throws SQLException {
        String SYRLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SYRLX));
        if (SYRLXStr != null && !SYRLXStr.equals("")) {
            String SYRLX = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='062' AND (XLMC=? or XLBM=?)", SYRLXStr, SYRLXStr);
            if (SYRLX == null || SYRLX.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，使用人类型 " + SYRLXStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SYRLX, SYRLX);
        }
        String ZBLXStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_ZBLX));
        if (ZBLXStr != null && !ZBLXStr.equals("")) {
            String ZBLX = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='063' AND (XLMC=? or XLBM=?)", ZBLXStr, ZBLXStr);
            if (ZBLX == null || ZBLX.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，质保类型 " + ZBLXStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_ZBLX, ZBLX);
        }
        String SFSCZBStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFSCZB));
        if (SFSCZBStr != null && !SFSCZBStr.equals("")) {
            String SFSCZB = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFSCZBStr, SFSCZBStr);
            if (SFSCZB == null || SFSCZB.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，是否首次质保 " + SFSCZBStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFSCZB, SFSCZB);
        }
        String SFTPStr = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SFTP));
        if (SFTPStr != null && !SFTPStr.equals("")) {
            String SFTP = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM  BO_AKL_DATA_DICT_S WHERE DLBM='025' AND (XLMC=? or XLBM=?)", SFTPStr, SFTPStr);
            if (SFTP == null || SFTP.equals("")) {
                throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，是否特批 " + SFTPStr + " 不存在，请检查!");
            }
            ExcelUtil.setCellValue(row, EXCEL_COL_SFTP, SFTP);
        }
    }
}
