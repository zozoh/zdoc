package org.nutz.am;

/**
 * 封装了一个自动的执行逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface Am<T> {

    Am<T> name(String name);

    String name();

    AmStatus enter(AmStack<T> as, char c);

    AmStatus eat(AmStack<T> as, char c);

    void done(AmStack<T> as);

}
