package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nutz.zdoc.ZDocEleType.IMG;
import static org.nutz.zdoc.ZDocEleType.INLINE;
import static org.nutz.zdoc.ZDocNodeType.CODE;
import static org.nutz.zdoc.ZDocNodeType.HEADER;
import static org.nutz.zdoc.ZDocNodeType.NODE;
import static org.nutz.zdoc.ZDocNodeType.PARAGRAPH;
import static org.nutz.zdoc.ZDocNodeType.TABLE;
import static org.nutz.zdoc.ZDocNodeType.TD;
import static org.nutz.zdoc.ZDocNodeType.TH;
import static org.nutz.zdoc.ZDocNodeType.THEAD;
import static org.nutz.zdoc.ZDocNodeType.TR;

import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.zdoc.AbstractParsingTest;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.ZDocEleType;
import org.nutz.zdoc.ZDocNode;
import org.nutz.zdoc.ZDocNodeType;

public class ZDocParserTest extends AbstractParsingTest {

    private ZDocParser parser;

    @Before
    public void before() {
        parser = new ZDocParser();
    }

    @Test
    public void test_code_01() {
        String code = "abc";
        code += "\n|--|";
        code += "\nyyy";
        String s = "{{{\n";
        s += code;
        s += "\n}}}";

        ZDocNode root = PS(s);

        _C(root, NODE, 1, "{}", "");
        _C(root, CODE, 0, "{}", code, 0);
    }

    @Test
    public void test_image_in_link() {
        ZDocNode root = PS("A[a.zdoc <a.png>]B");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AB", 0);

        ZDocNode nd = root.node(0);
        assertEquals(3, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, IMG, "{href:'a.zdoc',src:'a.png'}", null);
        _CE(nd, 2, INLINE, "{}", "B");

        root = PS("A[a.zdoc X<a.png>]B");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AB", 0);

        nd = root.node(0);
        assertEquals(3, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, INLINE, "{href:'a.zdoc'}", null);
        _CE(nd, 1, INLINE, "{}", "X", 0);
        _CE(nd, 1, IMG, "{src:'a.png'}", null, 1);
        _CE(nd, 2, INLINE, "{}", "B");

        root = PS("A[a.zdoc <a.png>X]B");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AB", 0);

        nd = root.node(0);
        assertEquals(3, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, INLINE, "{href:'a.zdoc'}", null);
        _CE(nd, 1, IMG, "{src:'a.png'}", null, 0);
        _CE(nd, 1, INLINE, "{}", "X", 1);
        _CE(nd, 2, INLINE, "{}", "B");

    }

    @Test
    public void test_link() {
        ZDocNode root = PS("A[a.zdoc]B");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AB", 0);

        ZDocNode nd = root.node(0);
        assertEquals(3, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, INLINE, "{href:'a.zdoc'}", null);
        _CE(nd, 2, INLINE, "{}", "B");
    }

    @Test
    public void test_link2() {
        ZDocNode root = PS("A[a.zdoc A]B[http://nutzam.com]C");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AABC", 0);

        ZDocNode nd = root.node(0);
        assertEquals(5, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, INLINE, "{href:'a.zdoc'}", "A");
        _CE(nd, 2, INLINE, "{}", "B");
        _CE(nd, 3, INLINE, "{href:'http://nutzam.com'}", null);
        _CE(nd, 4, INLINE, "{}", "C");

    }

    @Test
    public void test_image() {
        ZDocNode root = PS("A<5x8:a.png>B<b.png>C");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "ABC", 0);

        ZDocNode nd = root.node(0);
        assertEquals(5, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, IMG, "{width:5,height:8,src:'a.png'}", null);
        _CE(nd, 2, INLINE, "{}", "B");
        _CE(nd, 3, IMG, "{src:'b.png'}", null);
        _CE(nd, 4, INLINE, "{}", "C");

    }

    @Test
    public void test_simple_table() {
        ZDocNode root = PSf("org/nutz/zdoc/f/header_table.txt");

        assertEquals(1, root.children().size());
        ZDocNode h1 = root.children().get(0);
        assertEquals("AAAAAAA", h1.text());
        assertEquals(HEADER, h1.type());

        assertEquals(1, h1.children().size());
        ZDocNode table = h1.children().get(0);

        _C(table, TABLE, 3, "{cols:[null,null]}", "");
        _C(table, THEAD, 2, "{}", "", 0);
        _C(table, TH, 0, "{}", "H1", 0, 0);
        _C(table, TH, 0, "{}", "H2", 0, 1);

        _C(table, TR, 2, "{}", "", 1);
        _C(table, TD, 0, "{}", "C11", 1, 0);
        _C(table, TD, 0, "{}", "C12", 1, 1);

        _C(table, TR, 2, "{}", "", 2);
        _C(table, TD, 0, "{}", "C21", 2, 0);
        _C(table, TD, 0, "{}", "C22", 2, 1);

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

    private ZDocNode PSf(String ph) {
        Parsing ing = INGf(ph);
        ing.fa = NewAmFactory("zdoc");
        parser.build(ing);
        return ing.root;
    }

    private ZDocNode PS(String str) {
        Parsing ing = ING(str);
        ing.fa = NewAmFactory("zdoc");
        parser.build(ing);
        return ing.root;
    }
}
