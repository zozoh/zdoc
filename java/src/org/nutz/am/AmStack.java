package org.nutz.am;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.LinkedCharArray;

/**
 * 自动机堆栈
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @param <T>
 */
public abstract class AmStack<T> {

    /**
     * 消费字符
     * 
     * @param c
     *            字符
     * @return 返回 false 表示不能消费了
     */
    public AmStatus eat(char c) {
        if (isQuit(c))
            return AmStatus.DONE;

        Am<T> am = peekAm();
        AmStatus st = am.eat(this, c);
        if (st == AmStatus.CONTINUE) {
            return st;
        }
        if (st == AmStatus.DROP) {
            return st;
        }
        // 如果栈顶自动机完成了，那么调用它的 done
        // 是否能继续消费字符，取决于操作栈里是否还有自动机
        if (st == AmStatus.DONE) {
            done();
            return i_am >= 0 ? AmStatus.CONTINUE : AmStatus.DONE;
        }
        if (st == AmStatus.DONE_BACK) {
            done();
            if (i_am >= 0)
                return eat(c);
            return st;
        }
        throw Lang.impossible();
    }

    /**
     * 调用栈顶自动机的 done
     */
    public void done() {
        Am<T> am = peekAm();
        am.done(this);
    }

    @SuppressWarnings("unchecked")
    public T close() {
        // 按顺序，从栈头到尾依次调用自动机的 done
        Am<T>[] theAms = new Am[i_am + 1];
        for (int i = 0; i < theAms.length; i++) {
            theAms[i] = ams[i_am - i];
        }
        for (Am<T> am : theAms)
            am.done(this);

        // 最后从头到尾，依次调用对象的融合
        while (i_obj > 0) {
            T o = popObj();
            this.mergeHead(o);
        }

        // 弹出并返回根对象
        return popObj();
    }

    // 平行自动机会在这里记录自己所接受的字符，以便没有可选自动机时用作默认处理
    public LinkedCharArray raw;

    // 给自动机用的解析临时堆栈
    public LinkedCharArray buffer;

    protected T[] objs;
    protected Am<T>[] ams;
    private char[] qcs;
    private int[] sis;

    // 指向当前，初始为 -1
    private int i_obj;
    private int i_am;
    private int i_qc;
    private int i_si;

    // 堆栈项目最大深度
    protected int maxDepth;

    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String inds = Strings.dup("    ", indent);

        // 字符串缓冲
        sb.append(inds).append("C: '");
        for (int i = 0; i < buffer.size(); i++)
            sb.append(buffer.get(i));
        sb.append("' < ").append('\n');

        // 对象
        sb.append(inds).append("T:");
        for (int i = i_obj; i >= 0; i--) {
            sb.append('[');
            sb.append(objBrief(objs[i]));
            sb.append(']');
        }
        sb.append(" < ").append('\n');

        // 操作栈
        sb.append(inds).append("@:");
        for (int i = i_am; i >= 0; i--) {
            sb.append('[');
            sb.append(ams[i].name());
            sb.append(']');
        }
        sb.append(" < ").append('\n');

        // QC
        sb.append(inds).append("Q:");
        for (int i = i_qc; i >= 0; i--) {
            char c = qcs[i];
            if (c == 0) {
                sb.append(" 0 ");
            } else {
                sb.append('\'').append(c).append('\'');
            }
        }
        sb.append(" < ").append('\n');

        // SI
        sb.append(inds).append("I:");
        for (int i = i_si; i >= 0; i--) {
            if (sis[i] >= 0) {
                sb.append("  ").append(sis[i]);
            } else {
                sb.append(" ").append(sis[i]);
            }
        }
        sb.append(" < ").append('\n');

        // 子堆栈
        indent++;
        if (this.hasCandidates()) {
            for (AmStack<T> stack : candidates)
                sb.append(stack.toString(indent));
        }

        return sb.toString();
    }

    public List<AmStack<T>> candidates;

    public boolean hasCandidates() {
        return null != candidates && !candidates.isEmpty();
    }

    public boolean hasSi() {
        return i_si >= 0;
    }

    public boolean hasQc() {
        return i_qc >= 0;
    }

    public void addCandidate(AmStack<T> stack) {
        if (null == candidates)
            candidates = new LinkedList<AmStack<T>>();
        candidates.add(stack);
    }

    public AmStack(int maxDepth) {
        this.maxDepth = maxDepth;
        this.raw = new LinkedCharArray();
        this.buffer = new LinkedCharArray();
        this.qcs = new char[maxDepth];
        this.sis = new int[maxDepth];
        this.i_obj = -1;
        this.i_am = -1;
        this.i_qc = -1;
        this.i_si = -1;

    }

    protected abstract void merge(T a, T b);

    protected abstract AmStack<T> born();

    public abstract T bornObj();

    protected abstract String objBrief(T o);

    public void mergeHead(T o) {
        if (null != o) {
            if (hasObj()) {
                T head = peekObj();
                if (null != head) {
                    merge(head, o);
                }
            } else {
                pushObj(o);
            }
        }
    }

    public AmStack<T> pushSi(int n) {
        sis[++i_si] = n;
        return this;
    }

    public int popSi() {
        int n = sis[i_si];
        sis[i_si] = 0;
        i_si--;
        return n;
    }

    public int si() {
        return sis[i_si];
    }

    public void setSi(int n) {
        sis[i_si] = n;
    }

    public int si_size() {
        return i_si + 1;
    }

    public AmStack<T> pushQc(char c) {
        qcs[++i_qc] = c;
        return this;
    }

    public char popQc() {
        char c = qcs[i_qc];
        qcs[i_qc] = 0;
        i_qc--;
        return c;
    }

    public char qc() {
        return qcs[i_qc];
    }

    public boolean isQuit(char c) {
        if (i_qc >= 0)
            return qcs[i_qc] == c;
        return false;
    }

    public int qc_size() {
        return i_qc + 1;
    }

    public AmStack<T> pushObj(T obj) {
        objs[++i_obj] = obj;
        return this;
    }

    public T popObj() {
        T obj = objs[i_obj];
        objs[i_obj] = null;
        i_obj--;
        return obj;
    }

    public boolean hasObj() {
        return i_obj >= 0;
    }

    public int getObjSize() {
        return i_obj + 1;
    }

    public T peekObj() {
        return objs[i_obj];
    }

    public AmStack<T> pushAm(Am<T> am) {
        ams[++i_am] = am;
        return this;
    }

    public Am<T> popAm() {
        Am<T> am = ams[i_am];
        ams[i_am] = null;
        i_am--;
        return am;
    }

    public Am<T> peekAm() {
        return ams[i_am];
    }
}
