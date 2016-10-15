package cn.com.akl.ccgl.jgsq.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.com.akl.ccgl.jgsq.biz.JGGZBiz;
import cn.com.akl.ccgl.jgsq.biz.JGSQBiz;
import cn.com.akl.ccgl.jgsq.biz.ProductInfoBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		String uid = getUserContext().getUID();

		if (!"BO_AKL_JG_JGSQ_P".equals(tablename)) {
			return true;
		}

		JGSQBiz jgsqBiz = new JGSQBiz();
		JGGZBiz jggzBiz = new JGGZBiz();
		ProductInfoBiz productBiz = new ProductInfoBiz(jgsqBiz);

		Connection conn = null;
		Map<String, Integer> processingMaterialForProcessRules = null;
		Map<String, Integer> productForProcessRules = null;
		Map<String, Integer> processingMaterial = null;

		try {
			conn = DAOUtil.openConnectionTransaction();
			// 1.获取规则编号
			String gzbh = hashtable.get("GZBH");
			if (gzbh == null || gzbh.trim().equals("")) {
				throw new RuntimeException("请选择规则编号!");
			}

			// 2.获取规则原料
			processingMaterialForProcessRules = jggzBiz.getProcessingMaterialForProcessRules(conn, gzbh);
			// 3.获取当前原料，按比例计算加工几次
			processingMaterial = jgsqBiz.getProcessingMaterial(conn, bindid);
			// 4.获取规则成品
			productForProcessRules = jggzBiz.getProductForProcessRules(conn, gzbh);

			hashtable.put("GZYL", JGGZBiz.showRule(conn, processingMaterialForProcessRules, productBiz));
			hashtable.put("GZCP", JGGZBiz.showRule(conn, productForProcessRules, productBiz));

			if (processingMaterial.size() != processingMaterialForProcessRules.size()) {
				throw new RuntimeException("原料数量不匹配!");
			}

			int count = -1;
			Set<Entry<String, Integer>> processingMaterialForProcessRulesSet = processingMaterialForProcessRules.entrySet();
			for (Entry<String, Integer> entry : processingMaterialForProcessRulesSet) {
				String wlbh = entry.getKey();
				Integer ruleSl = entry.getValue();
				Integer sl = processingMaterial.get(wlbh);
				if (sl == null) {
					throw new RuntimeException("原料中缺少型号为：" + productBiz.getProductInfoXH(conn, wlbh) + "的物料.");
				}
				int countTemp = sl / ruleSl;
				if (count != -1) {
					if (count != countTemp) {
						throw new RuntimeException("物料型号为：" + productBiz.getProductInfoXH(conn, wlbh) + " 的物料比例不正确，请重新填写加工数量.");
					}
				}
				count = countTemp;
			}

			// 删除单身数据
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_JGSQ_CP_S", bindid);

			// 5.计算出成本数量
			Set<Entry<String, Integer>> productForProcessRulesEntrySet = productForProcessRules.entrySet();
			for (Entry<String, Integer> entry : productForProcessRulesEntrySet) {
				String wlbh = entry.getKey();
				Integer ruleSl = entry.getValue();
				int sl = ruleSl * count;
				Hashtable<String, String> productInfo = new Hashtable<String, String>(productBiz.getProductInfo(conn, wlbh));
				productInfo.put("SL", String.valueOf(sl));
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_JGSQ_CP_S", productInfo, bindid, getUserContext().getUID());
			}

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			DAOUtil.connectRollBack(conn);
			return true;
		} catch (SQLException e) {
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系管理员!", true);
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			return true;
		} catch (AWSSDKException e) {
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系管理员!", true);
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
