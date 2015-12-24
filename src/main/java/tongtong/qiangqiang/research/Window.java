package tongtong.qiangqiang.research;

import tongtong.qiangqiang.func.WindowHandler;

import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-23.
 */
public class Window {

    public static void win(String file, List<Double> value, int size, WindowHandler handler){
        FileEcho echo = new FileEcho(file);
        echo.writeln("value", "window");
        for(int i=0; i<value.size(); i++){
            int index = i*size;
            if (index < value.size())
                echo.writeln(value.get(i), (Double)handler.handle(value.subList(index, Math.min(index+size, value.size()))));
            else
                echo.writeln(value.get(i));
        }
    }
}
