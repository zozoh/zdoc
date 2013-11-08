package org.nutz.am;

/**
 * 并联自动机
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ParallelAm<T> extends ComposAm<T> {

    @Override
    public AmStatus enter(AmStack<T> as, char c) {
        if (c == theChar) {
            as.pushAm(this).pushQc(theChar);
            return AmStatus.DONE;
        }

        selectCandidates(as, c);

        if (as.hasCandidates()) {
            // 那么就会将自身压入母堆栈
            // []
            // [] ...
            // [+]... # 仅仅在当前堆栈压入自身
            // ...
            as.pushAm(this);

            return AmStatus.CONTINUE;
        }
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
                stack = as.born().pushObj(as.bornObj()).pushQc(theChar);
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

            this.selectCandidates(as, c);

            // 如果有候选就返回继续
            if (as.hasCandidates())
                return AmStatus.CONTINUE;

            return AmStatus.DROP;
        }

        T o;
        for (AmStack<T> stack : as.candidates) {
            AmStatus st = stack.eat(c);
            switch (st) {
            case DROP:
                as.candidates.remove(stack);
                break;
            case CONTINUE:
                continue;
            case DONE:
                o = stack.close();
                as.mergeHead(o);
                as.candidates.clear();
                return st;
            case DONE_BACK:
                o = stack.close();
                as.mergeHead(o);
                as.candidates.clear();
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

        // 还有候选的话，就继续
        if (as.hasCandidates())
            return AmStatus.CONTINUE;

        return AmStatus.DROP;
    }

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
        if (as.hasCandidates()) {
            AmStack<T> stack = as.candidates.get(0);
            T o = stack.close();
            as.mergeHead(o);
            as.pushQc(stack.popQc());
            as.candidates.clear();

            // 执行堆栈的真正弹出
            // []
            // [] ... # 将 T 组合到之前的对象中
            // [] ... # 清除了自动机
            // ...... # 清除了退出字符
            as.popAm();
            as.popQc();
        }

    }

}
