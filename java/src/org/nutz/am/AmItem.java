package org.nutz.am;

import java.util.List;

/**
 * 保存一个自动机以及其内部一些状态
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @param <T>
 */
public class AmItem<T> {

    /**
     * 自动机对象
     */
    public Am<T> am;

    /**
     * 对于串联自动机需要用到的子自动机下标（指向当前正在使用的自动机）
     */
    public int seriesIndex;

    /**
     * 对于并联自动机，如果遇到了两个或更多自动机能接受字符，先暂存到候选堆栈
     */
    public List<AmStack<T>> candidates;

    public boolean hasCandidates() {
        return null != candidates && !candidates.isEmpty();
    }

    public AmStack<T> stack() {
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    public AmItem(Am<T> am) {
        this.am = am;
        this.seriesIndex = 0;
    }

}
