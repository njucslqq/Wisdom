package tongtong.qiangqiang.research;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-22.
 */
public class FileEcho {

    private String file;

    private FileWriter w;

    public FileEcho(String file){
        this.file = file;
        try {
            w = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeln(Object...values){
        boolean first = true;
        try {
            for(Object v : values) {
                if (!first)
                    w.write(",");
                else
                    first = false;
                w.write(v.toString());
            }
            w.write("\r\n");
            w.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void write(Object...values){
        boolean first = true;
        try {
            for(Object v : values) {
                if (!first)
                    w.write(",");
                else
                    first = false;
                w.write(v.toString());
            }
            w.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}