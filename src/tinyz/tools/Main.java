package tinyz.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;


/**
 * Excel(.xlsx) to JSON(.data)
 * <p/>
 * Excel tools based poi-3.10-final
 *
 * @author tinyz
 * @version 1.0
 */
public class Main {

    //private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String PATH = new File("").getAbsolutePath();

    public static void main(String[] args) {
        try {
            List<String> fileList = TinyFileReader.listFile();
            for (String filePath : fileList) {
                String outPath = PATH + "\\" + filePath.substring(0, filePath.lastIndexOf(".")) + "." + "data";
                String inPath = PATH + "\\" + filePath;

                StringBuffer sb = TinyZUtil.Excel2Json(inPath);
                TinyZUtil.writeFile(outPath, sb);

                //LOG.info("Successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
