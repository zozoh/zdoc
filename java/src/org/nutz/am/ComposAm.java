package org.nutz.am;

public abstract class ComposAm<T> extends AbstractAm<T> {

    protected Am<T>[] ams;

    /**
     * 对于串联自动机就是进入字符，对于并联自动机就是退出字符
     */
    protected char theChar;

}
