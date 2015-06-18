package dev.tinyz.excel2json;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件读取
 * Created by TinyZ on 2014/5/23.
 */
public class TinyFileReader {

    /**
     * 缺省路径
     */
    public static final String DEFAULT_PATH = new File("").getAbsolutePath();

    /**
     * 缺省后缀名
     */
    public static final String DEFAULT_SUFFIX_EXCEL_2007_MORE = "xlsx"; // xls
    public static final String DEFAULT_SUFFIX_EXCEL_2003 = "xls"; // xls

    /**
     *
     * @return
     */
    public static List<String> listFile() {
        List<String> list = listFile(DEFAULT_PATH, DEFAULT_SUFFIX_EXCEL_2007_MORE); // Excel 2007+
        list.addAll(listFile(DEFAULT_PATH, DEFAULT_SUFFIX_EXCEL_2003)); // Excel 2003
        return list;
    }

    public static List<String> listFile(String suffix) {
        return listFile(DEFAULT_PATH, suffix);
    }

    public static List<String> listFile(String path, String suffix) {
        List<String> list = new ArrayList<String>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                String filePath = files[i].getName();
                if (suffix != null) {
                    int begIndex = filePath.lastIndexOf(".");
                    String tempSuffix = "";
                    if (begIndex != -1) {
                        tempSuffix = filePath.substring(begIndex + 1, filePath.length());
                    }
                    if (tempSuffix.equals(suffix)) {
                        list.add(filePath);
                    }
                } else {
                    list.add(filePath);
                }
            }
        }
        return list;
    }
}
