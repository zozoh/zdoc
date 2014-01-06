package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.nutz.zdoc.ZLineType.BLOCKQUOTE;
import static org.nutz.zdoc.ZLineType.HR;
import static org.nutz.zdoc.ZLineType.OL;
import static org.nutz.zdoc.ZLineType.PARAGRAPH;
import static org.nutz.zdoc.ZLineType.UL;

import org.junit.Before;
import org.junit.Test;
import org.nutz.zdoc.Parsing;

public class ZDocScannerTest extends AbstractScannerTest {

    @Before
    public void before() {
        scanner = new ZDocScanner();
    }

    @Test
    public void test_li_indent() {
        String s = "\t # L1\n";
        s += "        # L11\n";
        s += "\t # L2\n";

        Parsing ing = scan(s);

        assertEquals(3, ing.blocks.size());
        _C(ing, 0, OL, 0, "L1", 1, OL, '#');
        _C(ing, 1, OL, 0, "L11", 2, OL, '#');
        _C(ing, 2, OL, 0, "L2", 1, OL, '#');
    }

    @Test
    public void test_multi_block() {
        String s = "A\n";
        s += "\n";
        s += "B\n";

        Parsing ing = scan(s);

        assertEquals(2, ing.blocks.size());
        assertEquals("A", ing.blocks.get(0).firstLine.text);
        assertEquals("B", ing.blocks.get(1).firstLine.text);
    }

    @Test
    public void test_blockquote_hr() {
        Parsing ing = scanf("org/nutz/zdoc/f/blockquote_hr.txt");

        assertEquals(4, ing.blocks.size());

        _C(ing, 0, PARAGRAPH, 0, "AAA", 0, PARAGRAPH);
        _C(ing, 1, HR, 0, null, 0, HR);

        _C(ing, 2, BLOCKQUOTE, 0, "BBB", 0, BLOCKQUOTE);
        _C(ing, 2, BLOCKQUOTE, 1, "CCC", 0, BLOCKQUOTE);
        _C(ing, 2, BLOCKQUOTE, 2, "D", 0, BLOCKQUOTE);
        _C(ing, 2, BLOCKQUOTE, 3, "    D", 0, PARAGRAPH);
        _C(ing, 2, BLOCKQUOTE, 4, "  D", 0, PARAGRAPH);

        _C(ing, 3, PARAGRAPH, 0, "FFF", 0, PARAGRAPH);
    }

    @Test
    public void test_simple_scan() {
        String s = "#A";
        s += "\n#B";
        s += "\n    * C1";
        s += "\n    * C2";
        s += "\n    a. D";

        Parsing ing = scan(s);
        assertEquals(2, ing.blocks.size());

        assertTrue(ing.root.attrs().getBoolean("A"));
        assertTrue(ing.root.attrs().getBoolean("B"));
        assertFalse(ing.root.attrs().getBoolean("C"));

        _C(ing, 0, UL, 0, "C1", 1, UL);
        _C(ing, 0, UL, 1, "C2", 1, UL);

        _C(ing, 1, OL, 0, "D", 1, OL, 'a');
    }

}
