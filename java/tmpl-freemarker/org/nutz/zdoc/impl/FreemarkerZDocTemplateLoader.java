package org.nutz.zdoc.impl;

import java.io.IOException;
import java.io.Reader;

import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocHome;
import org.nutz.zdoc.ZDocTmplObj;

import freemarker.cache.TemplateLoader;

public class FreemarkerZDocTemplateLoader implements TemplateLoader {

    private ZDocHome home;

    public FreemarkerZDocTemplateLoader(ZDocHome home) {
        this.home = home;
    }

    /**
     * 根据传入的名字获取模板或者代码片段:
     * 
     * <ul>
     * <li><b>tmpl:xxxx</b> 表示获取模板
     * <li><b>lib:xxxx</b> 表示获取代码片段
     * </ul>
     */
    @Override
    public Object findTemplateSource(String name) throws IOException {
        int p0 = name.indexOf(':');
        if (p0 <= 0) {
            throw Lang.makeThrow("wrong tmpl key '%s', shoud start with 'tmpl:' or 'lib:'",
                                 name);
        }
        int p1 = name.indexOf('_', p0 + 1);
        if (p1 <= p0) {
            throw Lang.makeThrow("Freemarker didn't append the local name as suffix already? fuck!");
        }

        String prefix = name.substring(0, p0);
        String key = name.substring(p0 + 1, p1);

        // 这个是返回对象
        Object re = null;
        // 模板
        if (prefix.equals("tmpl")) {
            re = home.tmpl().get(key);
        }
        // 代码片段
        else if (prefix.equals("lib")) {
            re = home.libs().get(key);
        }

        // 没找到抛错咯
        if (null == re)
            throw Lang.makeThrow("Freemarkder fail to found '%s'", name);

        return re;
    }

    @Override
    public long getLastModified(Object templateSource) {
        ZDocTmplObj zto = (ZDocTmplObj) templateSource;
        return zto.file().lastModified();
    }

    @Override
    public Reader getReader(Object templateSource, String encoding)
            throws IOException {
        ZDocTmplObj zto = (ZDocTmplObj) templateSource;
        return Lang.inr(zto.raw());
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {}

}
