package dev.tinyz.excel2json;

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

    public static String outFileName = "";

    public static void main(String[] args) {
        String root = "./";
        List<String> fileList = TinyFileReader.listFile(root, "xlsx");
        for (String filePath : fileList) {
            outFileName = "";
            String inPath = root + "\\" + filePath;

//            TinyZUtil.writeFile(outPath, String.valueOf(TinyZUtil.Excel2Json(inPath)));
            String cfgData = TinyZUtil.E2Json(inPath);
            if (cfgData != null) {
                String outPath = root + "\\" + outFileName + "." + "data";
                TinyZUtil.writeFile(outPath, String.valueOf(cfgData));
            }
        }
    }
}
