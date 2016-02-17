package tongtong.qiangqiang.data;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-22.
 */
public class FileEcho {

    public static final DecimalFormat DOUBLE_FMT= new DecimalFormat("#.000000");

    private String file;

    private FileWriter w;

    public FileEcho(String file) {
        this.file = file;
        try {
            w = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeList(List<Double>... lists) {


    }

    public void writeList(List<Double> a, List<Double> b) {
        for (int i = 0; i < a.size() && i < b.size(); i++)
            writeln(a.get(i), b.get(i));
    }

    public void writeln(Object... values) {
        writeln(Arrays.asList(values));
    }

    public void writeln(List<Object> values) {
        boolean first = true;
        try {
            for (Object v : values) {
                if (!first)
                    w.write(",");
                else
                    first = false;
                if (v instanceof Double)
                    w.write(String.format("%10f", v));
                else
                    w.write(v.toString());
            }
            w.write("\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(Object... values) {
        boolean first = true;
        try {
            for (Object v : values) {
                if (!first)
                    w.write(",");
                else
                    first = false;
                w.write(v.toString());
            }
            w.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
