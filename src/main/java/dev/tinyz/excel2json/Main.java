package dev.tinyz.excel2json;

import java.io.File;
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

    public static final String PATH = new File("").getAbsolutePath();

    public static void main(String[] args) {
        List<String> fileList = TinyFileReader.listFile();
        for (String filePath : fileList) {
            String outPath = PATH + "\\" + filePath.substring(0, filePath.lastIndexOf(".")) + "." + "data";
            String inPath = PATH + "\\" + filePath;

            TinyZUtil.writeFile(outPath, String.valueOf(TinyZUtil.Excel2Json(inPath)));
        }
    }
}
