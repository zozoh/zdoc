package org.nutz.am;

import org.nutz.lang.Lang;

public abstract class SeriesAm<T> extends ComposAm<T> {

    @Override
    public AmStatus enter(AmStack<T> as, char c) {
        if (theChar == c) {
            as.pushObj(as.bornObj());
            as.pushAm(this);
            as.pushSi(-1);
            return AmStatus.CONTINUE;
        }
        return AmStatus.DROP;
    }

    @Override
    public AmStatus eat(AmStack<T> as, char c) {
        // 得到当前子机的下标
        int si = as.si();
        // 如果发现当前子机下标小于 0，则表示子机未被执行 enter ，那么就
        // 将下标变成正数，并调用对应子机的 enter
        // （注意，这里的下标是 1 base，需要转换成 0 base 使用）
        //
        // [] #
        // [T] ... # 还是那个对象
        // [&] ... # 串联自动机
        // 1 ... # 下标变成正数，并调用子机的 enter
        int index = si - 1;
        Am<T> am;
        // 小于 0 表示这个子机没有被 enter 过
        if (si < 0) {
            si = si * -1;
            if (si > ams.length) {
                return AmStatus.DROP;
            }
            // 得到对应的子机下标
            index = si - 1;
            am = ams[index];
            AmStatus st = am.enter(as, c);

            // 设置一下，表示被 enter 过了
            as.setSi(si);

            // 根据返回值做判断
            switch (st) {
            case CONTINUE:
            case DROP:
                return st;
            case DONE:
                am.done(as);
                si++;
                as.setSi(si * (-1));
                if (si > ams.length) {
                    return st;
                }
                return AmStatus.CONTINUE;
            case DONE_BACK:
                am.done(as);
                si++;
                as.setSi(si * (-1));
                if (si > ams.length) {
                    return st;
                }
                return eat(as, c);
            }
            return st;
        }
        // 没有更多的子机，则不接受这个字符
        else if (si > ams.length) {
            return AmStatus.DROP;
        }

        // 串联自动机每次 eat 如果当前子机 DROP 了，
        // 那么自己也返回 DROP
        // 如果返回的状态是 DONE 或者 DONE_BACK,
        // 会调用当前子机的 done，并试图切换到下一个子机
        //
        // [] #
        // [T] ... # 还是那个对象
        // [&] ... # 串联自动机
        // 1 ... # 下标指向下一个子机，并调用子机的 enter
        //
        // 如果没有下一个子机了，则返回 DONE | DONE_BACK
        am = ams[index];
        AmStatus st = am.eat(as, c);

        switch (st) {
        case CONTINUE:
        case DROP:
            return st;
        case DONE:
            am.done(as);
            si++;
            as.setSi(si * (-1));
            if (si > ams.length)
                return st;
            return AmStatus.CONTINUE;
        case DONE_BACK:
            am.done(as);
            si++;
            as.setSi(si * (-1));
            if (si > ams.length)
                return st;
            return eat(as, c);
        }
        throw Lang.impossible();
    }

    @Override
    public void done(AmStack<T> as) {
        // 如果没有达到最后一个子机，那么调用当前子机的 done
        int si = as.si();
        if (si > 0 && si <= ams.length) {
            ams[si - 1].done(as);
        }
        // 然后将将自己退栈
        //
        // []
        // [] ... # 将 T 组合到之前的对象中
        // [] ... # 清除自己
        // ... # 清除了指示下标
        as.buffer.clear();
        if (as.getObjSize() > 1) {
            T o = as.popObj();
            as.mergeHead(o);
        }
        as.popAm();
        as.popSi();
    }
}
