package org.nutz.zdoc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.nutz.zdoc.ZDocEleType.IMG;
import static org.nutz.zdoc.ZDocEleType.INLINE;

import org.junit.Before;
import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.zdoc.AbstractParsingTest;
import org.nutz.zdoc.Am;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.am.QuoteAm;
import org.nutz.zdoc.am.ZDocImageAm;
import org.nutz.zdoc.am.ZDocLinkAm;
import org.nutz.zdoc.impl.ZDocScanner;

public class AmsTest extends AbstractParsingTest {

    private ZDocScanner scanner;
    private Am[] amList;

    @Before
    public void before() {
        scanner = new ZDocScanner();
        amList = Lang.array(new QuoteAm('`'),
                            new ZDocImageAm(),
                            new ZDocLinkAm());
    }

    @Test
    public void test_simple_block() {
        String str = "this[http://nutzam.com] <abc.png>!!!";

        ZDocNode nd = _parse(str);

        assertEquals(5, nd.eles().size());
        _C(nd, 0, INLINE, "this", null);
        _C(nd, 1, INLINE, null, "http://nutzam.com");
        _C(nd, 2, INLINE, " ", null);
        _C(nd, 3, IMG, "abc.png", null);
        _C(nd, 4, INLINE, "!!!", null);
    }

    private void _C(ZDocNode nd,
                    int i,
                    ZDocEleType expectType,
                    String str,
                    String lnk) {
        ZDocEle ele = nd.eles().get(i);
        assertEquals(expectType, ele.type());
        if (IMG == expectType) {
            assertEquals(str, ele.src());
        } else {
            if (null == str) {
                assertNull(ele.text());
            } else {
                assertEquals(str, ele.text());
            }
        }
        if (null == lnk) {
            assertNull(ele.href());
        } else {
            assertEquals(lnk, ele.href());
        }
    }

    private ZDocNode _parse(String str) {
        Parsing ing = ING(str);
        scanner.scan(ing);
        Ams.fillEles(amList, ing, ZD.merge(ing.blocks));
        return ing.current.normalize();
    }

}
