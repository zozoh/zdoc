package org.nutz.zdoc;

public interface ZDocTemplateFactory {

    /**
     * 
     * @param key
     *            模板的键值
     * @return 文档模板对象
     */
    ZDocTemplate getTemplte(String key);

    /**
     * @param key
     *            代码片段值
     * @return 文档模板对象
     */
    ZDocTemplate getLib(String key);

}
