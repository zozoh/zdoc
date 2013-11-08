package org.nutz.am;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;

/**
 * 自动机的工厂类，它通过一个 Ioc 容器管理所有的 Am
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AmFactory {

    /**
     * 从工厂中获取一个自动机的实例
     * 
     * @param amType
     *            自动机的类型
     * 
     * @param amName
     *            自动机的名称
     * 
     * @return 自动机实例
     */
    public <T extends Am<?>> T getAm(Class<T> amType, String amName) {
        return ioc.get(amType, amName);
    }

    private Ioc ioc;

    public AmFactory(String path) {
        this(new NutIoc(new JsonLoader(path)));
    }

    public AmFactory(Ioc ioc) {
        this.ioc = ioc;
    }

}
