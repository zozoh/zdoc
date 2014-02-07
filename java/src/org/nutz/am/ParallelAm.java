package org.nutz.am;

import java.util.ArrayList;
import java.util.List;

/**
 * 并联自动机
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ParallelAm<T> extends ComposAm<T> {

    /**
     * 是否为受限的平行自动机
     * <ul>
     * <li>如果为 true，必须有一个子机接受了字符才可进入
     * <li>否则，任何字符都是可入的，将会依靠 as.raw 字段继续消费字符
     * </ul>
     */
    private boolean limited;

    @Override
    public AmStatus enter(AmStack<T> as, char c) {
        // 如果用退出字符进入，直接返回完成
        if (c == theChar) {
            // as.pushAm(this).pushQc(theChar);
            return AmStatus.DONE;
        }

        // 尝试选择几个候选堆栈
        selectCandidates(as, c);

        // 选择完了候选堆栈，如果有，那么就表示可以继续消费字符
        if (as.hasCandidates()) {
            // 处理之前积累下来的字符
            whenNoCandidate(as);

            // 那么就会将自身压入母堆栈，同时也要在母堆栈标识退出字符
            // []
            // [] ...
            // [+]... # 仅仅在当前堆栈压入自身
            // ']'... # 压入自己的退出字符
            as.pushQc(theChar).pushAm(this);
            as.raw.push(c);
            return AmStatus.CONTINUE;
        }
        // 没有退出字符的平行自动机，即使没有候选堆栈，也可以接受任何字符
        // 待遇到了生成候选堆栈的字符，之前接受的字符(as.raw)则会被子类虚函数处理
        if (!limited) {
            as.pushQc(theChar).pushAm(this);
            as.raw.push(c);
            return AmStatus.CONTINUE;
        }
        // 否则就表示本自动机遇到意外字符，必须 drop
        return AmStatus.DROP;
    }

    private void selectCandidates(AmStack<T> as, char c) {
        // 构建新堆栈:
        // [C]
        // [] .# 不要准备对象
        // [@] # 表示有子自动机进入了
        // ']' # 自己的退出字符
        AmStack<T> stack = as.born().pushQc(theChar);

        //
        // 如果超过一个自动机进入了，那么将堆栈变成
        //
        // {候选堆栈A} \
        // {候选堆栈B} -?- {并联自动机所在的堆栈}
        // {候选堆栈C} /
        // ...
        for (Am<T> am : ams) {
            if (AmStatus.DROP != am.enter(stack, c)) {
                as.addCandidate(stack);
                stack = as.born().pushQc(theChar);
            }
        }
    }

    @Override
    public AmStatus eat(AmStack<T> as, char c) {
        // 如果没有候选堆栈，则本自动机将执行选择一个候选堆栈
        if (!as.hasCandidates()) {
            if (theChar == c) {
                return AmStatus.DONE;
            }

            // 如果是转移字符，不需要选择候选堆栈
            if (as.raw.last() == '\\') {
                as.raw.push(c);
                return AmStatus.CONTINUE;
            }

            // 试图看看有木有子机可以进入 ...
            this.selectCandidates(as, c);

            // 如果有候选就返回继续
            if (as.hasCandidates()) {
                // 处理之前积累下来的字符
                whenNoCandidate(as);

                // 继续处理
                as.raw.push(c);
                return AmStatus.CONTINUE;
            }

            if (!limited) {
                as.raw.push(c);
                return AmStatus.CONTINUE;
            }

            return AmStatus.DROP;
        }

        // 记录自己接受的字符
        as.raw.push(c);

        // 依次处理所有的候选堆栈
        T o;
        List<AmStack<T>> dels = new ArrayList<AmStack<T>>(as.candidates.size());
        for (AmStack<T> stack : as.candidates) {
            AmStatus st = stack.eat(c);
            switch (st) {
            case DROP:
                dels.add(stack);
                break;
            case CONTINUE:
                break;
            case DONE:
                o = stack.close();
                as.mergeHead(o);
                as.candidates.clear();
                as.raw.clear();
                return st;
            case DONE_BACK:
                o = stack.close();
                as.mergeHead(o);
                as.candidates.clear();
                as.raw.clear();
                // 判断是不是退出字符
                if (theChar == c) {
                    return AmStatus.DONE;
                }
                // 选择候选堆栈
                this.selectCandidates(as, c);

                // 如果有候选就返回继续
                if (as.hasCandidates())
                    return AmStatus.CONTINUE;

                return AmStatus.DROP;
            }
        }

        // 清除需要删除的候选堆栈
        as.candidates.removeAll(dels);

        // 还有候选的话，就继续
        if (as.hasCandidates())
            return AmStatus.CONTINUE;

        return whenNoCandidate(as);
    }

    abstract protected AmStatus whenNoCandidate(AmStack<T> as);

    @Override
    public void done(AmStack<T> as) {
        // 如果没有候选堆栈，那么就什么也不做
        // 如果有多个候选堆栈，并联自动机会首先调用候选A的栈顶自动机的 done
        //
        // {候选堆栈A}.close() => T
        // {self}.mergeHead(T)
        // {self}.pushQc({候选堆栈A}.popQc())
        //
        // 并将其压入自己所在堆栈，否则，就会调用自己堆栈栈顶自动机的 done，
        // 让自己的的堆栈状态为:
        // [] # 字符缓冲
        // [] ... # 这个是自己的对象
        // [+] ... # 头部就只有自己
        // ']' ... # 自己的退出字符在顶部
        if (as.hasWinner()) {
            AmStack<T> stack = as.candidates.get(0);
            T o = stack.close();
            as.mergeHead(o);
            as.candidates.clear();

            // 执行堆栈的真正弹出
            // []
            // [] ... # 将 T 组合到之前的对象中
            // [] ... # 清除了自动机
            // ...... # 清除了退出字符
            as.popAm();
        }
        // 没有候选的话 ...
        else {
            whenNoCandidate(as);
        }

    }

}
