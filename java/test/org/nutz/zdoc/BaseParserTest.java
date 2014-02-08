package org.nutz.zdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.nutz.am.AmFactory;
import org.nutz.json.Json;
import org.nutz.lang.Lang;

public abstract class BaseParserTest extends ZDocBaseTest {

    protected void _C(ZDocNode nd,
                      ZDocNodeType exType,
                      int exChildCount,
                      String exAttrs,
                      String exText,
                      int... iPaths) {
        ZDocNode nd2 = nd.node(iPaths);

        Object exMap = Json.fromJson(exAttrs);
        Object attMap = Json.fromJson(Json.toJson(nd2.attrs().getInnerMap()));

        assertEquals(exType, nd2.type());
        assertEquals(exChildCount, nd2.children().size());
        assertTrue(Lang.equals(exMap, attMap));
        assertEquals(exText, nd2.text());
    }

    protected void _CE(ZDocNode nd,
                       int index,
                       ZDocEleType exType,
                       String exAttrs,
                       String exText,
                       int... iPaths) {
        ZDocEle ele = nd.eles().get(index).ele(iPaths);

        Object exMap = Json.fromJson(exAttrs);
        Object attMap = Json.fromJson(ele.attrsAsJson());

        assertEquals(exType, ele.type());
        assertTrue(Lang.equals(exMap, attMap));
        assertEquals(exText, ele.text());
    }

    protected abstract AmFactory genAmFactory();

    protected abstract String getRootAmName();

    protected ZDocNode PSf(String ph) {
        Parsing ing = INGf(ph);
        ing.rootAmName = getRootAmName();
        ing.fa = genAmFactory();
        parser.build(ing);
        return ing.root;
    }

    protected ZDocNode PS(String str) {
        Parsing ing = ING(str);
        ing.rootAmName = getRootAmName();
        ing.fa = genAmFactory();
        parser.build(ing);
        return ing.root;
    }

}
