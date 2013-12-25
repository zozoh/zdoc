package org.nutz.zdoc.impl;

import java.io.IOException;

import org.nutz.lang.Lang;
import org.nutz.zdoc.ZDocHome;
import org.nutz.zdoc.ZDocTemplate;
import org.nutz.zdoc.ZDocTemplateFactory;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreemarkerTemplateFactory implements ZDocTemplateFactory {

    private Configuration cfg;

    public FreemarkerTemplateFactory(ZDocHome home) {
        FreemarkerZDocTemplateLoader loader = new FreemarkerZDocTemplateLoader(home);
        this.cfg = new Configuration();
        this.cfg.setObjectWrapper(new DefaultObjectWrapper());
        this.cfg.setTemplateLoader(loader);
    }

    @Override
    public ZDocTemplate getTemplte(String key) {
        try {
            Template tmpl = cfg.getTemplate(key);
            return new FreemarkerTemplate(tmpl);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }
}
