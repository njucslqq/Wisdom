package tongtong.qiangqiang.mock;

import cn.quanttech.quantera.CONST;
import tongtong.qiangqiang.mock.algorithm.DEMAResearch;

import static cn.quanttech.quantera.datacenter.DataCenterUtil.setNetDomain;

/**
 * Author: Qiangqiang Li
 * <p>
 * Coding is another way to meet the curiousness of a curious mind
 * <p>
 * 2015-12-24.
 */
public class MockDriver {

    public static void main(String[] args) {
        setNetDomain(CONST.INTRA_QUANDIS_URL);

        DEMAResearch m = new DEMAResearch();
        m.init();
        m.simulate();
        m.onComplete();
    }
}
