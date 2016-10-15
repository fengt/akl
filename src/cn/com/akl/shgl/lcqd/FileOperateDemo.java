package cn.com.akl.shgl.lcqd;  
  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.util.ArrayList;
import java.util.List;
  
/* 
 * Java实现文件复制、剪切、删除操作 
 *  
 */  
  
public class FileOperateDemo {  
  
    /** 
     * 复制文件或文件夹 
     *  
     * @param srcPath 源文件目录
     * @param destDir 更改后目录
     *              
     * @return 
     */  
    public static boolean copyGeneralFile(String srcPath, String destDir) {  
        boolean flag = false;  
        File file = new File(srcPath);  
        if (!file.exists()) {  
            System.out.println("源文件或源文件夹不存在!");  
            return false;  
        }  
        if (file.isFile()) { // 源文件  
            System.out.println("下面进行文件复制!");  
            flag = copyFile(srcPath, destDir);  
        }  
        return flag;  
    }  
  
    /** 
     * 复制文件 
     *  
     * @param srcPath 
     *            源文件绝对路径 
     * @param destDir 
     *            目标文件所在目录 
     * @return boolean 
     */  
    private static boolean copyFile(String srcPath, String destDir) {  
        boolean flag = false;  
  
        File srcFile = new File(srcPath);  
        if (!srcFile.exists()) { // 源文件不存在  
            System.out.println("源文件不存在");  
            return false;  
        }  
        // 获取待复制文件的文件名  
        String fileName = srcPath.substring(srcPath.lastIndexOf("/"));  
        String destPath = destDir + fileName;//文件路径  
        if (destPath.equals(srcPath)) { // 源文件路径和目标文件路径重复  
            System.out.println("源文件路径和目标文件路径重复!");   
            return false;  
        }  
        File destFile = new File(destPath);  
        if (destFile.exists() && destFile.isFile()) { // 该路径下已经有一个同名文件  
            System.out.println("目标目录下已有同名文件!");  
            return false;  
        }  
  
        File destFileDir = new File(destDir);  
        destFileDir.mkdirs();//创建此抽象路径指定的目录
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
            System.out.println("复制文件成功!");  
        }  
  
        return flag;  
    }  
  
     
     
    /** 
     * 删除文件或文件夹 
     *  
     * @param path 
     *            待删除的文件的绝对路径 
     * @return boolean 
     */  
    public static boolean deleteGeneralFile(String path) {  
        boolean flag = false;  
  
        File file = new File(path);  
        if (!file.exists()) { // 文件不存在  
            System.out.println("要删除的文件不存在！");  
        }  
        if (file.isFile()) {  
            flag = deleteFile(file);  
        }  
        if (flag) {  
            System.out.println("删除文件成功!");  
        }  
  
        return flag;  
    }  
  
    /** 
     * 删除文件 
     *  
     * @param file 
     * @return boolean 
     */  
    private static boolean deleteFile(File file) {  
        return file.delete();  
    }  
  
    
  
    /** 
     * 由上面方法延伸出剪切方法：复制+删除 
     *  
     * @param destDir 
     *            同上 
     */  
    public static boolean cutGeneralFile(String srcPath, String destDir) {  
        if (!copyGeneralFile(srcPath, destDir)) {  
            System.out.println("复制失败导致剪切失败!");  
            return false;  
        }  
        if (!deleteGeneralFile(srcPath)) {  
            System.out.println("删除源文件失败导致剪切失败!");  
            return false;  
        }  
  
        System.out.println("剪切成功!");  
        return true;  
    } 
    /**
     * 获得文件夹下的所有文件名
     * @param url 文件夹路径
     * @return 文件名集合
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
    	
    	
//        copyGeneralFile("E://Assemble.txt", "E://New.txt"); // 复制文件  
//        copyGeneralFile("E://hello", "E://world"); // 复制文件夹  
//        deleteGeneralFile("E://onlinestockdb.sql"); // 删除文件  
//        deleteGeneralFile("E://woman"); // 删除文件夹  
//        cutGeneralFile("E://hello", "E://world"); // 剪切文件夹  
//        cutGeneralFile("E://Difficult.java", "E://Cow//"); // 剪切文件  
    }  
  
}  