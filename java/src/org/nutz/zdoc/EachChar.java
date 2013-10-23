package org.nutz.zdoc;

public interface EachChar {

    /**
     * @param index
     *            第几个字符
     * @param c
     *            当前字符
     * 
     * @return 是否继续, true 表示退出循环
     */
    boolean isBreak(int index, char c);

}
