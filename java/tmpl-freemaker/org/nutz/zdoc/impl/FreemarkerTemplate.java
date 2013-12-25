package org.nutz.zdoc.impl;

import java.io.IOException;
import java.io.Writer;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.zdoc.ZDocTemplate;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerTemplate implements ZDocTemplate {

    private Template tmpl;

    public FreemarkerTemplate(Template tmpl) {
        this.tmpl = tmpl;
    }

    @Override
    public void outputTo(Writer wr, NutMap map) {
        try {
            tmpl.process(map, wr);
        }
        catch (TemplateException e) {
            throw Lang.wrapThrow(e);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

}
