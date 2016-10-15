package cn.com.akl.shgl.lcqd;  
  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.util.ArrayList;
import java.util.List;
  
/* 
 * Javaʵ���ļ����ơ����С�ɾ������ 
 *  
 */  
  
public class FileOperateDemo {  
  
    /** 
     * �����ļ����ļ��� 
     *  
     * @param srcPath Դ�ļ�Ŀ¼
     * @param destDir ���ĺ�Ŀ¼
     *              
     * @return 
     */  
    public static boolean copyGeneralFile(String srcPath, String destDir) {  
        boolean flag = false;  
        File file = new File(srcPath);  
        if (!file.exists()) {  
            System.out.println("Դ�ļ���Դ�ļ��в�����!");  
            return false;  
        }  
        if (file.isFile()) { // Դ�ļ�  
            System.out.println("��������ļ�����!");  
            flag = copyFile(srcPath, destDir);  
        }  
        return flag;  
    }  
  
    /** 
     * �����ļ� 
     *  
     * @param srcPath 
     *            Դ�ļ�����·�� 
     * @param destDir 
     *            Ŀ���ļ�����Ŀ¼ 
     * @return boolean 
     */  
    private static boolean copyFile(String srcPath, String destDir) {  
        boolean flag = false;  
  
        File srcFile = new File(srcPath);  
        if (!srcFile.exists()) { // Դ�ļ�������  
            System.out.println("Դ�ļ�������");  
            return false;  
        }  
        // ��ȡ�������ļ����ļ���  
        String fileName = srcPath.substring(srcPath.lastIndexOf("/"));  
        String destPath = destDir + fileName;//�ļ�·��  
        if (destPath.equals(srcPath)) { // Դ�ļ�·����Ŀ���ļ�·���ظ�  
            System.out.println("Դ�ļ�·����Ŀ���ļ�·���ظ�!");   
            return false;  
        }  
        File destFile = new File(destPath);  
        if (destFile.exists() && destFile.isFile()) { // ��·�����Ѿ���һ��ͬ���ļ�  
            System.out.println("Ŀ��Ŀ¼������ͬ���ļ�!");  
            return false;  
        }  
  
        File destFileDir = new File(destDir);  
        destFileDir.mkdirs();//�����˳���·��ָ����Ŀ¼
        try {  
            FileInputStream fis = new FileInputStream(srcPath);  
            FileOutputStream fos = new FileOutputStream(destFile);  
            byte[] buf = new byte[1024];  
            int c;  
            while ((c = fis.read(buf)) != -1) {  
                fos.write(buf, 0, c);  
            }  
            fis.close();  
            fos.close();  
  
            flag = true;  
        } catch (IOException e) {  
            //  
        }  
  
        if (flag) {  
            System.out.println("�����ļ��ɹ�!");  
        }  
  
        return flag;  
    }  
  
     
     
    /** 
     * ɾ���ļ����ļ��� 
     *  
     * @param path 
     *            ��ɾ�����ļ��ľ���·�� 
     * @return boolean 
     */  
    public static boolean deleteGeneralFile(String path) {  
        boolean flag = false;  
  
        File file = new File(path);  
        if (!file.exists()) { // �ļ�������  
            System.out.println("Ҫɾ�����ļ������ڣ�");  
        }  
        if (file.isFile()) {  
            flag = deleteFile(file);  
        }  
        if (flag) {  
            System.out.println("ɾ���ļ��ɹ�!");  
        }  
  
        return flag;  
    }  
  
    /** 
     * ɾ���ļ� 
     *  
     * @param file 
     * @return boolean 
     */  
    private static boolean deleteFile(File file) {  
        return file.delete();  
    }  
  
    
  
    /** 
     * �����淽����������з���������+ɾ�� 
     *  
     * @param destDir 
     *            ͬ�� 
     */  
    public static boolean cutGeneralFile(String srcPath, String destDir) {  
        if (!copyGeneralFile(srcPath, destDir)) {  
            System.out.println("����ʧ�ܵ��¼���ʧ��!");  
            return false;  
        }  
        if (!deleteGeneralFile(srcPath)) {  
            System.out.println("ɾ��Դ�ļ�ʧ�ܵ��¼���ʧ��!");  
            return false;  
        }  
  
        System.out.println("���гɹ�!");  
        return true;  
    } 
    /**
     * ����ļ����µ������ļ���
     * @param url �ļ���·��
     * @return �ļ�������
     */
    public static List<String> SystemFileName(String url){
    	List<String> filename=new ArrayList<String>();
    	File f = new File(url);
    	if(f.isDirectory()){
    		File[] fileList = f.listFiles();
    		for(File fs:fileList){
    			if(fs.isFile()){
    				filename.add(fs.getName());
    			}
    		}
    	}
    	return filename;
    }
  
    public static void main(String[] args) { 
    	
    	
//        copyGeneralFile("E://Assemble.txt", "E://New.txt"); // �����ļ�  
//        copyGeneralFile("E://hello", "E://world"); // �����ļ���  
//        deleteGeneralFile("E://onlinestockdb.sql"); // ɾ���ļ�  
//        deleteGeneralFile("E://woman"); // ɾ���ļ���  
//        cutGeneralFile("E://hello", "E://world"); // �����ļ���  
//        cutGeneralFile("E://Difficult.java", "E://Cow//"); // �����ļ�  
    }  
  
}  