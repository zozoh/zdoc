package org.nutz.zdoc;

public interface ZDocTemplateFactory {

    /**
     * 
     * @param key
     *            模板的键值
     *            <ul>
     *            <li><b>tmpl:xxxx</b> 表示获取模板
     *            <li><b>lib:xxxx</b> 表示获取代码片段
     *            </ul>
     * @return 文档模板对象
     */
    ZDocTemplate getTemplte(String key);

}
