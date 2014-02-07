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
