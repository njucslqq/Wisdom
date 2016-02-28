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

    private final LinkedList<T> window = new LinkedList<>();

    public final int capacity;

    public SlidingWindow(int capacity) {
        this.capacity = capacity;
    }

    public void add(T data) {
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

    public T prev() {
        if (window.isEmpty())
            throw new RuntimeException("SlidingWindow is empty");
        return window.getLast();
    }

    public List<T> sub(int from, int to) {
        if (from < 0 || to < 0 || from > window.size() || to > window.size() || from >= to)
            throw new RuntimeException("index is illegal");
        return window.subList(from, to);
    }

    public List<T> all() {
        return sub(0, size());
    }

    public boolean isEmpty() {
        return window.isEmpty();
    }

    public int size() {
        return window.size();
    }
}
