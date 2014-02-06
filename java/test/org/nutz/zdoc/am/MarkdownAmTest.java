package org.nutz.zdoc.am;

import static org.junit.Assert.assertEquals;
import static org.nutz.zdoc.ZDocEleType.INLINE;
import static org.nutz.zdoc.ZDocEleType.QUOTE;

import org.junit.Before;
import org.junit.Test;
import org.nutz.zdoc.ZDocEle;

public class MarkdownAmTest extends AmTest {

    @Before
    public void before() {
        fa = NewAmFactory("markdown");
        rootAmName = "mdParagraph";
    }
    
    @Test
    public void test_link_byid() {
        ZDocEle root;
        // ...................................................
        root = _parse("[A]  [id]");
        root.normalize();
        assertEquals(2, root.children().size());

        _Cele(root, 0, INLINE, "[A]  ", null);
        _Cele(root, 1, INLINE, "[id]", null);
        // ...................................................
        root = _parse("[A] [id]");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", "$id");
        // ...................................................
        root = _parse("[A][id]");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", "$id");
    }
    

    @Test
    public void test_link_normal() {
        ZDocEle root;
        // ...................................................
        root = _parse("[A]  (L)");
        root.normalize();
        assertEquals(2, root.children().size());

        _Cele(root, 0, INLINE, "[A]  ", null);
        _Cele(root, 1, INLINE, "(L)", null);
        // ...................................................
        root = _parse("[A] (http://nutzam.com 'abc')");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", "http://nutzam.com");
        assertEquals("abc", root.title());
        // ...................................................
        root = _parse("[A](http://nutzam.com)");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", "http://nutzam.com");
        // ...................................................
        root = _parse("[A] (http://nutzam.com)");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", "http://nutzam.com");
    }

    @Test
    public void test_escape_char() {
        ZDocEle root;
        // ...................................................
        root = _parse("X\\*Y\\*Z");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "X*Y*Z", null);
    }

    @Test
    public void test_combo_em() {
        ZDocEle root;
        // ...................................................
        root = _parse("X**Y**Z");
        root.normalize();
        assertEquals(3, root.children().size());

        _Cele(root, 0, INLINE, "X", null);
        _Cele(root, 1, INLINE, "Y", null);
        _Cmap("{'font-weight':'bold'}", root.ele(1).style());
        _Cele(root, 2, INLINE, "Z", null);
        // ...................................................
        root = _parse("*A***B**C ");
        root.normalize();
        assertEquals(3, root.children().size());

        _Cele(root, 0, INLINE, "A", null);
        _Cmap("{'font-style':'italic'}", root.ele(0).style());
        _Cele(root, 1, INLINE, "B", null);
        _Cmap("{'font-weight':'bold'}", root.ele(1).style());
        _Cele(root, 2, INLINE, "C ", null);

    }

    @Test
    public void test_simple_em() {
        ZDocEle root;
        // ...................................................
        root = _parse("**A**");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", null);
        _Cmap("{'font-weight':'bold'}", root.style());
        // ...................................................
        root = _parse("*A*");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", null);
        _Cmap("{'font-style':'italic'}", root.style());
        // ...................................................
        root = _parse("__A__");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", null);
        _Cmap("{'font-weight':'bold'}", root.style());
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

}
