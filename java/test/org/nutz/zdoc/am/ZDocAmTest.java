package org.nutz.zdoc.am;

import static org.junit.Assert.assertEquals;
import static org.nutz.zdoc.ZDocEleType.IMG;
import static org.nutz.zdoc.ZDocEleType.INLINE;
import static org.nutz.zdoc.ZDocEleType.QUOTE;
import static org.nutz.zdoc.ZDocEleType.SUP;

import org.junit.Before;
import org.junit.Test;
import org.nutz.zdoc.ZDocEle;

public class ZDocAmTest extends AmTest {

    @Before
    public void before() {
        fa = NewAmFactory("zdoc");
        rootAmName = "zdocParagraph";
    }
    
    @Test
    public void test_simple_abc(){
        ZDocEle root = _parse("{#888;@Column}");
        root.normalize();
        assertEquals(0, root.children().size());
        
        _Cele(root, -1, INLINE, "@Column", null);
        _Cmap("{'color':'#888'}", root.style());
    }

    @Test
    public void test_em_in_link() {
        ZDocEle root;
        // ...................................................
        root = _parse("[a.zdoc {*A}]");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, INLINE, "A", "a.zdoc");
        _Cmap("{'font-weight':'bold'}", root.style());
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
        assertEquals(-1, root.width());
        assertEquals(-1, root.height());
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
        assertEquals(-1, root.height());
        // ...................................................
        root = _parse("<x80:abc.png>");
        root.normalize();
        assertEquals(0, root.children().size());

        _Cele(root, -1, IMG, null, "abc.png");
        assertEquals(-1, root.width());
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
        root = _parse("A[x Z<y>]B");
        root.normalize();

        assertEquals(3, root.children().size());
        _Cele(root, 0, INLINE, "A", null);
        _Cele(root, 1, INLINE, null, "x");

        ZDocEle e = root.ele(1);
        _Cele(e, 0, INLINE, "Z", null);
        _Cele(e, 1, IMG, null, "y");

        _Cele(root, 2, INLINE, "B", null);
        // ...................................................
        root = _parse("A[x <y>]B");
        root.normalize();

        assertEquals(3, root.children().size());
        _Cele(root, 0, INLINE, "A", null);

        _Cele(root, 1, IMG, null, "y");
        assertEquals("x", root.ele(1).href());

        _Cele(root, 2, INLINE, "B", null);
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

}
