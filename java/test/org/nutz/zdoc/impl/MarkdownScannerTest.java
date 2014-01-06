package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.nutz.zdoc.ZLineType.CODE;
import static org.nutz.zdoc.ZLineType.PARAGRAPH;
import static org.nutz.zdoc.ZLineType.TABLE;

import org.junit.Test;
import org.nutz.zdoc.Parsing;

public class MarkdownScannerTest extends AbstractScannerTest {

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

        Parsing ing = scan(s);
        assertEquals(3, ing.blocks.size());

        _C(ing, 0, PARAGRAPH, 0, "#A", 0, PARAGRAPH);
        _C(ing, 0, PARAGRAPH, 1, "aaa", 1, PARAGRAPH);

        _C(ing, 1, CODE, 0, "cccc", 1, PARAGRAPH);
        _C(ing, 1, CODE, 1, "dddd", 2, PARAGRAPH);

        _C(ing, 2, PARAGRAPH, 0, "#B", 0, PARAGRAPH);
        _C(ing, 2, PARAGRAPH, 1, "bbb", 0, PARAGRAPH);
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

        Parsing ing = scan(s);
        assertEquals(3, ing.blocks.size());

        _C(ing, 0, PARAGRAPH, 0, "#A", 0, PARAGRAPH);
        _C(ing, 0, PARAGRAPH, 1, "aaa", 0, PARAGRAPH);

        _C(ing, 1, TABLE, 0, " A | B ", 0, PARAGRAPH);
        _C(ing, 1, TABLE, 1, "---|---", 0, PARAGRAPH);
        _C(ing, 1, TABLE, 2, "aaa|bbb", 0, PARAGRAPH);
        _C(ing, 1, TABLE, 3, "ccc|ddd", 0, PARAGRAPH);

        _C(ing, 2, PARAGRAPH, 0, "#B", 0, PARAGRAPH);
        _C(ing, 2, PARAGRAPH, 1, "bbb", 0, PARAGRAPH);
    }

}
