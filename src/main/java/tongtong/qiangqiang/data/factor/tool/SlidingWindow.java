package tongtong.qiangqiang.data.factor.tool;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-02-24.
 */
public class SlidingWindow<T> {

    private final LinkedList<T> window;

    private final int capacity;

    public SlidingWindow(int capacity) {
        this.capacity = capacity;
        this.window = new LinkedList<>();
    }

    public void push(T data) {
        window.addLast(data);
        if (window.size() > capacity)
            window.removeFirst();
    }

    public T last(int index) {
        if (index < 0 || index >= window.size())
            throw new RuntimeException("index out of range");
        return window.get(window.size() - index - 1);
    }

    public T first(int index) {
        if (index < 0 || index >= window.size())
            throw new RuntimeException("index out of range");
        return window.get(index);
    }

    public T head() {
        if (window.isEmpty())
            throw new RuntimeException("SlidingWindow is empty");
        return window.getFirst();
    }

    public T tail() {
        if (window.isEmpty())
            throw new RuntimeException("SlidingWindow is empty");
        return window.getLast();
    }

    public List<T> lastn(int n) {
        if (n > window.size())
            throw new RuntimeException("required " + n + " elements, while SlidingWindow only has " + window.size() + " elements");
        return sub(window.size() - n, window.size());
    }

    public List<T> firstn(int n) {
        if (n > window.size())
            throw new RuntimeException("required " + n + " elements, while SlidingWindow only has " + window.size() + " elements");
        return sub(0, n);
    }

    public List<T> sub(int from, int to) {
        if (from < 0 || from >= window.size() || to < 1 || to > window.size() || from >= to)
            throw new RuntimeException("index is illegal");
        return window.subList(from, to);
    }

    public List<T> all() {
        return window;
    }

    public boolean isEmpty() {
        return window.isEmpty();
    }

    public int size() {
        return window.size();
    }

    public int capacity() {
        return capacity;
    }
}
