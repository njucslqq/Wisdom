package tongtong.qiangqiang.mind.app;

import tongtong.qiangqiang.mind.Algorithm;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * Created on 2016-03-03.
 */
public class AlgorithmManager extends JFrame {

    public final List<Algorithm> algorithms;

    public AlgorithmManager(List<Algorithm> algorithms){
        super("Algorithm Manager");
        this.algorithms = algorithms;

        final JPanel container = new JPanel(new GridLayout(1,1));
        final JTabbedPane tp = new JTabbedPane();
        tp.setPreferredSize(new Dimension(888, 1000));
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tp.setTabPlacement(JTabbedPane.NORTH);
        for (Algorithm a : algorithms) {
            tp.addTab(a.getName(), null, new AlgorithmPanel(a), a.getName());
        }
        container.add(tp);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(container);
        pack();
        setVisible(true);
    }

    public void vis(){
        algorithms.parallelStream().forEach(Algorithm::run);
    }
}
