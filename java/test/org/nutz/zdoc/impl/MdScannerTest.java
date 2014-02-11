package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.nutz.zdoc.ZLineType.*;

import org.junit.Before;
import org.junit.Test;
import org.nutz.zdoc.Parsing;

public class MdScannerTest extends AbstractScannerTest {

    @Before
    public void before() {
        scanner = new MdScanner();
    }

    @Test
    public void test_link_define() {
        String s = "[a0]: http://nutzam.com 'Nutz'\n";
        s += "ABC\n";
        s += "[a1]: http://www.google.com 'Google'\n";
        // .............................................
        Parsing ing = scan(s);
        // .............................................
        assertEquals(1, ing.blocks.size());
        // .............................................
        _C(ing, 0, PARAGRAPH, 0, "ABC", 0, PARAGRAPH);
        // .............................................
        assertEquals(2, ing.root.links().size());
        assertEquals("http://nutzam.com", ing.root.links().get("a0").link());
        assertEquals("Nutz", ing.root.links().get("a0").title());
        // .............................................
        assertEquals("http://www.google.com", ing.root.links().get("a1").link());
        assertEquals("Google", ing.root.links().get("a1").title());
    }

    @Test
    public void test_blockquote_after_p() {
        String s = "**A**\n";
        s += "> q";
        // .............................................
        Parsing ing = scan(s);
        // .............................................
        assertEquals(2, ing.blocks.size());
        // .............................................
        _C(ing, 0, PARAGRAPH, 0, "**A**", 0, PARAGRAPH);
        // .............................................
        _C(ing, 1, BLOCKQUOTE, 0, "q", 0, BLOCKQUOTE);
    }

    @Test
    public void test_code_after_list() {
        String s = "* A";
        s += "\n";
        s += "\n    aaa";
        // .............................................
        Parsing ing = scan(s);
        // .............................................
        assertEquals(2, ing.blocks.size());
        // .............................................
        _C(ing, 0, UL, 0, "A", 0, UL);
        _C(ing, 0, UL, 1, "", 0, BLANK);
        // .............................................
        _C(ing, 1, CODE, 0, "aaa", 0, CODE);
    }

    @Test
    public void test_simple_meta() {
        String s = "---\n";
        s += "title:abc\n";
        s += "---\n";
        s += "xyz";
        // .............................................
        Parsing ing = scan(s);
        // .............................................
        assertEquals(1, ing.blocks.size());
        assertEquals("abc", ing.root.attrs().get("title"));
        // .............................................
        _C(ing, 0, PARAGRAPH, 0, "xyz", 0, PARAGRAPH);
    }

    @Test
    public void test_simple_scan() {
        String s = "#A";
        s += "\n    aaa";
        s += "\n";
        s += "\n    cccc";
        s += "\n        dddd";
        s += "\n";
        s += "\n#B";
        s += "\nbbb";
        // .............................................
        Parsing ing = scan(s);
        // .............................................
        assertEquals(4, ing.blocks.size());
        // .............................................
        _C(ing, 0, HEADER, 0, "A", 0, HEADER);
        // .............................................
        _C(ing, 1, CODE, 0, "aaa", 0, CODE);
        _C(ing, 1, CODE, 1, "", 0, CODE);
        _C(ing, 1, CODE, 2, "cccc", 0, CODE);
        _C(ing, 1, CODE, 3, "    dddd", 0, CODE);
        _C(ing, 1, CODE, 4, "", 0, CODE);
        // .............................................
        _C(ing, 2, HEADER, 0, "B", 0, HEADER);
        // .............................................
        _C(ing, 3, PARAGRAPH, 0, "bbb", 0, PARAGRAPH);
    }

    @Test
    public void test_table_scan() {
        String s = "\n#A";
        s += "\naaa";
        s += "\n";
        s += "\n A | B ";
        s += "\n---|---";
        s += "\naaa|bbb";
        s += "\nccc|ddd";
        s += "\n";
        s += "\n#B";
        s += "\nbbb";
        // .............................................
        Parsing ing = scan(s);
        // .............................................
        assertEquals(5, ing.blocks.size());
        // .............................................
        _C(ing, 0, HEADER, 0, "A", 0, HEADER);
        // .............................................
        _C(ing, 1, PARAGRAPH, 0, "aaa", 0, PARAGRAPH);
        // .............................................
        _C(ing, 2, TABLE, 0, " A | B ", 0, PARAGRAPH);
        _C(ing, 2, TABLE, 1, "---|---", 0, TABLE);
        _C(ing, 2, TABLE, 2, "aaa|bbb", 0, PARAGRAPH);
        _C(ing, 2, TABLE, 3, "ccc|ddd", 0, PARAGRAPH);
        // .............................................
        _C(ing, 3, HEADER, 0, "B", 0, HEADER);
        // .............................................
        _C(ing, 4, PARAGRAPH, 0, "bbb", 0, PARAGRAPH);
    }

}
