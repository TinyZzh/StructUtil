package org.struct.binary;

import org.xerial.snappy.SnappyFramedInputStream;
import org.xerial.snappy.SnappyFramedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

public class FileWatcherService {

    /**
     * the root path.
     */
    private static volatile String TEMP_DIR;

    private FileSystem fs;
    /**
     * Watch file changed and load template data when file modified.
     */
    private WatchService watchService;

    static {
        TEMP_DIR = System.getProperty("snappy.tmp.dir", "./tmp/");
    }

    public void test(String path, Object obj) {
        try (FileOutputStream fis = new FileOutputStream(new File(path));
             SnappyFramedOutputStream sfis = new SnappyFramedOutputStream(fis);
             ObjectOutputStream oos = new ObjectOutputStream(sfis)) {

            oos.writeObject(obj);
        } catch (Exception e) {

        }



        try (FileInputStream fis = new FileInputStream(new File(""));
             SnappyFramedInputStream sfis = new SnappyFramedInputStream(fis)) {
            sfis.skip(10000L);


        } catch (Exception e) {

        }
    }


    public void watchPath(String filePath) {
        Path path1 = fs.getPath(filePath);

        try {
            Path path = fs.getPath(this.TEMP_DIR);
            path.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
