package org.nutz.zdoc.impl;

import java.io.IOException;

import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocHome;
import org.nutz.zdoc.ZDocTemplate;
import org.nutz.zdoc.ZDocTemplateFactory;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

public class FreemarkerTemplateFactory implements ZDocTemplateFactory {

    private Configuration cfg;

    public FreemarkerTemplateFactory(ZDocHome home) {
        FreemarkerZDocTemplateLoader loader = new FreemarkerZDocTemplateLoader(home);
        this.cfg = new Configuration();
        this.cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        this.cfg.setTemplateLoader(loader);
        this.cfg.setDefaultEncoding("utf-8");
        this.cfg.setOutputEncoding("utf-8");
    }

    @Override
    public ZDocTemplate getTemplte(String key) {
        try {
            Template tmpl = cfg.getTemplate("tmpl:" + key, "utf-8");
            return new FreemarkerTemplate(tmpl);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public ZDocTemplate getLib(String key) {
        try {
            Template tmpl = cfg.getTemplate("lib" + key, "utf-8");
            return new FreemarkerTemplate(tmpl);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }
}
