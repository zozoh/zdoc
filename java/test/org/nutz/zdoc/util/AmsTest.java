package org.nutz.zdoc.util;

import static org.junit.Assert.assertEquals;
import static org.nutz.zdoc.ZDocEleType.IMG;
import static org.nutz.zdoc.ZDocEleType.INLINE;
import static org.nutz.zdoc.ZDocEleType.QUOTE;
import static org.nutz.zdoc.ZDocEleType.SUP;

import org.junit.Before;
import org.junit.Test;
import org.nutz.am.Am;
import org.nutz.am.AmFactory;
import org.nutz.am.AmStatus;
import org.nutz.lang.Lang;
import org.nutz.zdoc.AbstractParsingTest;
import org.nutz.zdoc.ZDocEle;
import org.nutz.zdoc.am.ZDocAmStack;
import org.nutz.zdoc.am.ZDocParallelAm;

public class AmsTest extends AbstractParsingTest {

    private AmFactory fa;

    @Before
    public void before() {
        fa = new AmFactory("org/nutz/zdoc/am/zdoc.js");
    }

    @Test
    public void test_simple_em() {
        ZDocEle root;
        // ...................................................
        root = _parse("{*A}");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", null);
        _Cmap("{'font-weight':'bold'}", root.style());
        // ...................................................
        root = _parse("{*/A}");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", null);
        _Cmap("{'font-weight':'bold','font-style':'italic'}", root.style());
        // ...................................................
        root = _parse("{*#008800;A}");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", null);
        _Cmap("{'font-weight':'bold','color':'#008800'}", root.style());
    }

    @Test
    public void test_simple_quote() {
        ZDocEle root;
        // ...................................................
        root = _parse("A`B`C");
        root.normalize();
        assertEquals(3, root.children().size());

        _Cele(root, 0, INLINE, "A", null);
        _Cele(root, 1, QUOTE, "B", null);
        _Cele(root, 2, INLINE, "C", null);
    }

    @Test
    public void test_simple_parallel() {
        ZDocEle root;
        // ...................................................
        root = _parse("A<x.png>B");
        root.normalize();
        assertEquals(3, root.children().size());

        _Cele(root, 0, INLINE, "A", null);
        _Cele(root, 1, IMG, null, "x.png");
        _Cele(root, 2, INLINE, "B", null);
    }

    @Test
    public void test_only_link() {
        ZDocEle root;

        // ...................................................
        root = _parse("A[b]C");
        root.normalize();
        assertEquals(3, root.children().size());

        _Cele(root, 0, INLINE, "A", null);
        _Cele(root, 1, INLINE, null, "b");
        _Cele(root, 2, INLINE, "C", null);
        // ...................................................
        root = _parse("[a.zdoc A<x.png>B]");
        root.normalize();
        assertEquals(3, root.children().size());

        _Cele(root, -1, INLINE, null, "a.zdoc");
        _Cele(root, 0, INLINE, "A", null);
        _Cele(root, 1, IMG, null, "x.png");
        _Cele(root, 2, INLINE, "B", null);
        // ...................................................
        root = _parse("[a.zdoc <x.png>]");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, IMG, null, "x.png");
        assertEquals(0, root.width());
        assertEquals(0, root.height());
        assertEquals("a.zdoc", root.href());
        // ...................................................
        root = _parse("[a.zdoc]").normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, null, "a.zdoc");
        // ...................................................
        root = _parse("[a.zdoc ABC]").normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "ABC", "a.zdoc");
    }

    @Test
    public void test_only_img() {
        ZDocEle root;
        // ...................................................
        root = _parse("<abc.png>");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, IMG, null, "abc.png");
        // ...................................................
        root = _parse("<100:abc.png>");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, IMG, null, "abc.png");
        assertEquals(100, root.width());
        assertEquals(0, root.height());
        // ...................................................
        root = _parse("<x80:abc.png>");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, IMG, null, "abc.png");
        assertEquals(0, root.width());
        assertEquals(80, root.height());
        // ...................................................
        root = _parse("<100x80:abc.png TheABC>");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, IMG, "TheABC", "abc.png");
        assertEquals(100, root.width());
        assertEquals(80, root.height());

    }

    @Test
    public void test_simple_block() {
        ZDocEle root;
        // ...................................................
        root = _parse("A{*#FF0;B}{^/b}C");
        root.normalize();

        assertEquals(4, root.children().size());
        _Cele(root, 0, INLINE, "A", null);
        _Cele(root, 1, INLINE, "B", null);
        _Cstl(root, 1, "{'font-weight':'bold',color:'#FF0'}");
        _Cele(root, 2, SUP, "b", null);
        _Cstl(root, 2, "{'font-style':'italic'}");
        _Cele(root, 3, INLINE, "C", null);
        // ...................................................
        root = _parse("this[http://nutzam.com] <abc.png>!!!");
        root.normalize();

        assertEquals(5, root.children().size());
        _Cele(root, 0, INLINE, "this", null);
        _Cele(root, 1, INLINE, null, "http://nutzam.com");
        _Cele(root, 2, INLINE, " ", null);
        _Cele(root, 3, IMG, null, "abc.png");
        _Cele(root, 4, INLINE, "!!!", null);
    }

    private ZDocEle _parse(String str) {
        char[] cs = str.toCharArray();
        Am<ZDocEle> am = fa.getAm(ZDocParallelAm.class, "zdocParagraph");
        ZDocAmStack stack = new ZDocAmStack(10);
        stack.pushObj(stack.bornObj());
        if (am.enter(stack, cs[0]) != AmStatus.CONTINUE) {
            throw Lang.impossible();
        }
        for (int i = 1; i < cs.length; i++) {
            char c = cs[i];
            AmStatus st = stack.eat(c);
            if (AmStatus.CONTINUE != st)
                throw Lang.impossible();
        }
        return stack.close();
    }
}
