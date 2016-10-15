package cn.com.akl.shgl.jf.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;

public class DeliveryValidater {

    /**
     * 查询是否有其他单据已经使用了此交付记录.
     */
    private static final String QUERY_DYJFJL = "SELECT COUNT(*) FROM BO_AKL_WXJF_S jfs LEFT JOIN SYSFLOWSTEP step ON jfs.WORKFLOWSTEPID=step.ID WHERE jfs.SXCPHID=? AND jfs.ID<>? AND step.STEPNO>1";

    RepositoryBiz repositoryBiz = new RepositoryBiz();

    /**
     * 验证字段值： <br/>
     * 1、验证是否升级字段和处理方式.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @throws SQLException
     */
    public void validateJfInfo(Connection conn, ResultSet reset, int bindid) throws SQLException {
        String clfs = reset.getString("CLFS");
        String sfsj = reset.getString("SFSJ");
        if (clfs == null || clfs.equals("")) {
            throw new RuntimeException("请检查交付产品信息中是否有处理方式！");
        } else {
            if (sfsj == null || sfsj.equals("")) {
                throw new RuntimeException("请检查交付产品信息中“是否升级”字段是否填写！");
            } else {
                if (sfsj.equals(XSDDConstant.YES)) {
                    if (!clfs.equals(DeliveryConstant.CLFS_HX)) {
                        throw new RuntimeException("交付产品信息中“处理方式”为“" + DictionaryUtil.parseNoToChinese(clfs) + "“的记录不能进行替换规则升级处理！");
                    }
                }
            }
        }

        String sxid = reset.getString("SXCPHH");

        /** 查询送修记录状态. */
        int count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SX_S WHERE SXCPHH=? AND ZT=? ", sxid, DeliveryConstant.SX_B_ZT_YJC);
        if (count == 0) {
            throw new RuntimeException("送修产品行号：" + reset.getString("HH") + "，已经被其他交付单交付了！");
        }
    }

    /**
     * 验证送修单物料是否还能交付. <br/>
     * 1、验证送修记录状态. <br/>
     * 2、验证是否被其他单据办理了.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @throws SQLException
     */
    public void validateSxInfo(Connection conn, ResultSet reset, int bindid, String uid) throws SQLException {

        /** 查询其他交付单是否有此送修记录. */
        /*
        count = DAOUtil.getIntOrNull(conn, QUERY_DYJFJL, id, sxid);
        if (count > 0) {
            throw new RuntimeException("送修产品行号：" + reset.getString("HH") + "，已经被其他交付单办理了！");
        }
        */

        /** 是否首次质保查询. */
        int count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE JFSN=?", reset.getString("SN"));
        if (count > 0) {
            MessageQueue.getInstance().putMessage(uid, "送修产品行号：" + reset.getString("HH") + "，非首次质保！");
        }

    }

    /**
     * 缺货申请库存验证.<br/>
     * 1、如果到通知，那么就要检查库存是否足够. <br/>
     * 2、验证是否要缺货，如果发动缺货，库存缺足够需要阻止.<br/>
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param xmlb
     * @throws SQLException
     */
    public void validateRepository(Connection conn, int bindid, String uid, ResultSet reset, String xmlb) throws SQLException {
        String wlbh = reset.getString("WLBH");
        String wlmc = reset.getString("WLMC");
        String hwdm = reset.getString("HWDM");
        String pch = reset.getString("PCH");
        String sx = reset.getString("SX");
        int sl = reset.getInt("SL");
        String sfqhsq = reset.getString("SFQHSQ");

        int remaingNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);

        if (XSDDConstant.YES.equals(sfqhsq)) {
            if (sl <= remaingNum) {
                MessageQueue.getInstance().putMessage(uid, "物料名称：" + wlmc + "，有库存，您可以继续发起缺货申请!");
            }
        } else {
            if (sl > remaingNum) {
                throw new RuntimeException("物料名称：" + wlmc + "，库存不足!");
            }
        }
    }

    /**
     * 校验库存是否充足.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param xmlb
     * @throws SQLException
     */
    public void validateRepository2(Connection conn, ResultSet reset, int bindid, String xmlb) throws SQLException {
        String wlbh = reset.getString("WLBH");
        String wlmc = reset.getString("WLMC");
        String hwdm = reset.getString("HWDM");
        String ckdm = reset.getString("CKDM");
        String pch = reset.getString("PCH");
        String sx = reset.getString("SX");
        int sl = reset.getInt("SL");
        String sfjf = reset.getString("SFJF");
        if (XSDDConstant.YES.equals(sfjf)) {
            int remaingNum = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, ckdm, sx);
            if (sl > 0) {
                if (sl > remaingNum) {
                    throw new RuntimeException("物料名称：" + wlmc + "，库存不足!");
                }
            }
        }
    }

    /**
     * 验证配件数量.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param xmlb
     * @throws SQLException
     */
    public void validatePart(Connection conn, ResultSet reset, int bindid, String xmlb) throws SQLException {
        String wlbh = reset.getString("WLBH");
        String wlmc = reset.getString("MC");
        String hwdm = reset.getString("HWDM");
        String ckdm = reset.getString("CKDM");
        String sx = reset.getString("SX");
        String pch = reset.getString("PCH");
        int sl = reset.getInt("SHSL");
        int remaingNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);
        if (sl > 0) {
            if (sl > remaingNum) {
                throw new RuntimeException("物料名称：" + wlmc + "，库存不足!");
            }
        }
    }

    /**
     * 是否有为归还的代用品.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @throws SQLException
     */
    public boolean isHaveNoYetSubstitute(Connection conn, ResultSet reset, int bindid) throws SQLException {
        String sfsh = reset.getString("SFSH");
        if (sfsh == null || sfsh.equals("")) {
            throw new RuntimeException("代用品表中 ”是否收回“ 不能为空！");
        } else {
            return sfsh.equals(XSDDConstant.YES);
        }
    }
}
