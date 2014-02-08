package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.nutz.zdoc.ZDocEleType.*;
import static org.nutz.zdoc.ZDocNodeType.*;

import org.junit.Before;
import org.junit.Test;
import org.nutz.am.AmFactory;
import org.nutz.zdoc.BaseParserTest;
import org.nutz.zdoc.ZDocNode;

public class MdParserTest extends BaseParserTest {

    @Before
    public void before() {
        parser = new MdParser();
    }

    @Test
    public void test_blockquote_00() {
        String s = "#AAA\n";
        s += "> T\n";

        ZDocNode root = PS(s);

        _C(root, NODE, 1, "{}", "");
        _C(root, HEADER, 1, "{tagName:'h1'}", "AAA", 0);
        _C(root, BLOCKQUOTE, 1, "{}", "", 0, 0);
        _C(root, PARAGRAPH, 0, "{}", "T ", 0, 0, 0);
    }

    @Test
    public void test_blockquote_01() {
        String s = "#AAA\n";
        s += "> X\n";
        s += "> > A\n";
        s += "> > B\n";
        s += "> Y\n";

        ZDocNode root = PS(s);

        _C(root, NODE, 1, "{}", "");
        _C(root, HEADER, 1, "{tagName:'h1'}", "AAA", 0);
        _C(root, BLOCKQUOTE, 3, "{}", "", 0, 0);
        _C(root, PARAGRAPH, 0, "{}", "X ", 0, 0, 0);
        _C(root, BLOCKQUOTE, 1, "{}", "", 0, 0, 1);
        _C(root, PARAGRAPH, 0, "{}", "A B ", 0, 0, 1, 0);
        _C(root, PARAGRAPH, 0, "{}", "Y ", 0, 0, 2);
    }

    @Test
    public void test_hierachy_li_00() {
        String s = "#AAA\n";
        s += " 1. L0\n";
        s += " 2. L1\n";
        s += "     1. L11\n";
        s += " 1. L2\n";

        ZDocNode root = PS(s);

        _C(root, NODE, 1, "{}", "");
        _C(root, HEADER, 1, "{tagName:'h1'}", "AAA", 0);
        _C(root, OL, 3, "{'$line-type':'OL','$line-indent':0}", "", 0, 0);
        _C(root, LI, 0, "{}", "L0", 0, 0, 0);
        _C(root, LI, 1, "{}", "L1", 0, 0, 1);
        _C(root, OL, 1, "{'$line-type':'OL','$line-indent':1}", "", 0, 0, 1, 0);
        _C(root, LI, 0, "{}", "L11", 0, 0, 1, 0, 0);
        _C(root, LI, 0, "{}", "L2", 0, 0, 2);
    }

    @Test
    public void test_code_00() {
        String code = "abc";
        code += "\n|--|";
        code += "\nyyy";
        String s = "```md\n";
        s += code;
        s += "\n```";

        ZDocNode root = PS(s);

        _C(root, NODE, 1, "{}", "");
        _C(root, CODE, 0, "{'code-type':'md'}", code, 0);
    }

    @Test
    public void test_code_01() {
        String s = "    function(){\n";
        s += "    \talert('haha');\n";
        s += "    }";

        ZDocNode root = PS(s);

        _C(root, NODE, 1, "{}", "");
        _C(root,
           CODE,
           0,
           "{}",
           s.replaceAll("    ", "").replaceAll("\t", "    "),
           0);
    }

    @Test
    public void test_link() {
        ZDocNode root = PS("A[](a.zdoc)B");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AB", 0);

        ZDocNode nd = root.node(0);
        assertEquals(3, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, INLINE, "{href:'a.zdoc'}", "");
        _CE(nd, 2, INLINE, "{}", "B");
    }

    @Test
    public void test_link2() {
        ZDocNode root = PS("A[A](a.zdoc)B[](http://nutzam.com)C");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "AABC", 0);

        ZDocNode nd = root.node(0);
        assertEquals(5, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, INLINE, "{href:'a.zdoc'}", "A");
        _CE(nd, 2, INLINE, "{}", "B");
        _CE(nd, 3, INLINE, "{href:'http://nutzam.com'}", "");
        _CE(nd, 4, INLINE, "{}", "C");

    }

    @Test
    public void test_image() {
        ZDocNode root = PS("A![](a.png)B![](b.png)C");
        _C(root, NODE, 1, "{}", "");
        _C(root, PARAGRAPH, 0, "{}", "ABC", 0);

        ZDocNode nd = root.node(0);
        assertEquals(5, nd.eles().size());

        _CE(nd, 0, INLINE, "{}", "A");
        _CE(nd, 1, IMG, "{src:'a.png'}", "");
        _CE(nd, 2, INLINE, "{}", "B");
        _CE(nd, 3, IMG, "{src:'b.png'}", "");
        _CE(nd, 4, INLINE, "{}", "C");

    }

    @Test
    public void test_simple_table() {
        String str = "#AAAAAAA\n";
        str += " H1  | H2  \n";
        str += " --- | --- \n";
        str += " C11 | C12 \n";
        str += " C21 | C22 \n";
        ZDocNode root = PS(str);

        assertEquals(1, root.children().size());
        ZDocNode h1 = root.children().get(0);
        assertEquals("AAAAAAA", h1.text());
        assertEquals(HEADER, h1.type());

        assertEquals(1, h1.children().size());
        ZDocNode table = h1.children().get(0);

        _C(table, TABLE, 3, "{$cols:['auto','auto']}", "");
        _C(table, THEAD, 2, "{}", "", 0);
        _C(table, TH, 0, "{}", " H1  ", 0, 0);
        _C(table, TH, 0, "{}", " H2  ", 0, 1);

        _C(table, TR, 2, "{}", "", 1);
        _C(table, TD, 0, "{}", " C11 ", 1, 0);
        _C(table, TD, 0, "{}", " C12 ", 1, 1);

        _C(table, TR, 2, "{}", "", 2);
        _C(table, TD, 0, "{}", " C21 ", 2, 0);
        _C(table, TD, 0, "{}", " C22 ", 2, 1);

    }

    @Override
    protected AmFactory genAmFactory() {
        return NewAmFactory("markdown");
    }

    @Override
    protected String getRootAmName() {
        return "mdParagraph";
    }

}
