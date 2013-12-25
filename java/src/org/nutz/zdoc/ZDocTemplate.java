package org.nutz.zdoc;

import java.io.Writer;

import org.nutz.lang.util.NutMap;

public interface ZDocTemplate {

    void outputTo(Writer wr, NutMap map);

}
