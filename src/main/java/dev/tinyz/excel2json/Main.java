package dev.tinyz.excel2json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;


/**
 * Excel(.xlsx or .xls) to JSON(.data)
 * <p/>
 * Excel tools based poi-3.12 and fastjson 1.2.6
 *
 * @author tinyz
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
//        String root = "./excel/";
        String root = "./";
        List<String> fileList = TinyFileReader.listFile(root, "xlsx");
        for (String filePath : fileList) {
            String inPath = root + "\\" + filePath;
            File file = new File(inPath);
            String fileName = file.getName();
            String substring = fileName.substring(0, fileName.length() - 5);
            try {
                String json = TiUtil.excel2JsonV3(new FileInputStream(file));
                if (json != null) {
                    String outPath = root + "\\" + substring + "." + "data";
                    TinyZUtil.writeFile(outPath, String.valueOf(json));
                }
            } catch (Exception e) {
                System.out.println("Load file error. File name : " + fileName);
                e.printStackTrace();
            }
        }
    }
}
