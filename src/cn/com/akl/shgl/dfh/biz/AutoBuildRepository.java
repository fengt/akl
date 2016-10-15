package cn.com.akl.shgl.dfh.biz;

import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import cn.com.akl.shgl.kc.biz.RepositoryConstant;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class AutoBuildRepository {

    public void run(int bindid, String uid) throws AWSSDKException {

        Vector<Hashtable<String, String>> cacheWlVector = new Vector<Hashtable<String, String>>(200);
        Vector<Hashtable<String, String>> cacheXlhVector = new Vector<Hashtable<String, String>>(200);

        int kwsl = randInt(1000);

        Vector<Hashtable<String, String>> cpxxVector = BOInstanceAPI.getInstance().getBODatasBySQL("BO_AKL_CPXX", "WHERE 1=1");
        Vector<Hashtable<String, String>> kfckVector = BOInstanceAPI.getInstance().getBODatasBySQL("BO_AKL_KFCK", "WHERE 1=1");
        Vector<Hashtable<String, String>> sxVector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DATA_DICT_S", 388646);

        System.out.println(cpxxVector.size() * kfckVector.size() * sxVector.size());

        for (Hashtable<String, String> cpxx : cpxxVector) {
            for (Hashtable<String, String> kfck : kfckVector) {
                String kfckbm = kfck.get("KFCKBM");
                if (kfckbm.equals("KF009")) {
                    for (Hashtable<String, String> sx : sxVector) {
                        kwsl = randInt(1000);

                        String wlbh = cpxx.get("WLBH");
                        if (wlbh.equals("00100041") && wlbh.equals("00100042") && wlbh.equals("00100043")) {
                            continue;
                        }

                        Hashtable<String, String> hashtable = new Hashtable<String, String>();
                        hashtable.put("XMLB", cpxx.get("XMLB"));
                        hashtable.put("WLBH", cpxx.get("WLBH"));
                        hashtable.put("WLMC", cpxx.get("WLMC"));
                        hashtable.put("GG", cpxx.get("GG"));
                        hashtable.put("XH", cpxx.get("LPN8"));
                        hashtable.put("CKDM", kfck.get("KFCKBM"));
                        hashtable.put("HWDM", kfck.get("KFCKBM"));
                        hashtable.put("JLDW", cpxx.get("DW"));
                        hashtable.put("KWSL", String.valueOf(kwsl));
                        hashtable.put("SX", sx.get("XLBM"));

                        hashtable.put("ZT", RepositoryConstant.WL_ZT_ZK);
                        cacheWlVector.add(hashtable);

                        hashtable.put("ZT", RepositoryConstant.GZTM_ZT_ZK);
                        cacheXlhVector.add(hashtable);

                        if (cacheWlVector.size() == 300) {
                            BOInstanceAPI.getInstance().createBOData("BO_AKL_SHKC_S", cacheWlVector, bindid, uid);
                        }
                        if (cacheXlhVector.size() == 300) {
                            for (int i = 0; i < kwsl - randInt(kwsl); i++) {
                                hashtable.put("GZTM", UUID.randomUUID().toString().replaceAll("-", ""));
                                hashtable.put("KWSL", "1");
                                BOInstanceAPI.getInstance().createBOData("BO_AKL_SHKC_XLH_S", cacheXlhVector, bindid, uid);
                            }
                        }
                    }
                }
            }
        }

        if (cacheWlVector.size() != 0) {
            BOInstanceAPI.getInstance().createBOData("BO_AKL_SHKC_S", cacheWlVector, bindid, uid);
        }
        if (cacheXlhVector.size() != 0) {
            BOInstanceAPI.getInstance().createBOData("BO_AKL_SHKC_XLH_S", cacheXlhVector, bindid, uid);
        }
    }

    public String getrRondomSX(String xmlb) {
        return null;
    }

    public int randInt(int num) {
        return (int) (Math.random() * num);
    }

    public double randDouble() {
        return Math.random() * 1000;
    }

}
