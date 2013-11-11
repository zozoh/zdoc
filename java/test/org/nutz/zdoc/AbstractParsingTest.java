package org.nutz.zdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nutz.zdoc.ZDocEleType.IMG;

import java.util.HashMap;
import java.util.Map;

import org.nutz.am.AmFactory;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;

public class AbstractParsingTest {

    protected AmFactory NewAmFactory(String name) {
        return new AmFactory("org/nutz/zdoc/am/" + name + ".js");
    }

    protected Parsing ING(String str) {
        return new Parsing(Lang.inr(str));
    }

    protected Parsing INGf(String ph) {
        return new Parsing(Streams.fileInr(ph));
    }

    protected void _Cstl(ZDocEle root, int i, String expectStyle) {
        ZDocEle ele = i >= 0 ? root.ele(i) : root;
        _Cmap(expectStyle, ele.style());
    }

    protected void _Cele(ZDocEle root,
                         int i,
                         ZDocEleType expectType,
                         String str,
                         String lnk) {
        ZDocEle ele = i >= 0 ? root.ele(i) : root;
        assertEquals(expectType, ele.type());

        if (null == str) {
            assertNull(ele.text());
        } else {
            assertEquals(str, ele.text());
        }

        // 图片检查的有所不同
        if (IMG == expectType) {
            if (null == lnk) {
                assertNull(ele.src());
            } else {
                assertEquals(lnk, ele.src());
            }
        }
        // 其他
        else {
            if (null == lnk) {
                assertNull(ele.href());
            } else {
                assertEquals(lnk, ele.href());
            }
        }

    }

    @SuppressWarnings("rawtypes")
    protected void _Cmap(String expect, Map map) {
        String mapstr = Json.toJson(map);
        Map m0 = Json.fromJson(HashMap.class, expect);
        Map m1 = Json.fromJson(HashMap.class, mapstr);
        assertTrue(Lang.equals(m0, m1));
    }
}
