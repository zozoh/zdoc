package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.nutz.zdoc.ZDocLineType.BLANK;
import static org.nutz.zdoc.ZDocLineType.BLOCKQUOTE;
import static org.nutz.zdoc.ZDocLineType.COMMENT;
import static org.nutz.zdoc.ZDocLineType.COMMENT_BEGIN;
import static org.nutz.zdoc.ZDocLineType.COMMENT_END;
import static org.nutz.zdoc.ZDocLineType.HR;
import static org.nutz.zdoc.ZDocLineType.OL;
import static org.nutz.zdoc.ZDocLineType.PARAGRAPH;
import static org.nutz.zdoc.ZDocLineType.TABLE;
import static org.nutz.zdoc.ZDocLineType.THEAD;
import static org.nutz.zdoc.ZDocLineType.TR;
import static org.nutz.zdoc.ZDocLineType.TSEP;
import static org.nutz.zdoc.ZDocLineType.UL;

import org.junit.Before;
import org.junit.Test;
import org.nutz.zdoc.AbstractParsingTest;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZDocBlock;
import org.nutz.zdoc.ZDocLine;
import org.nutz.zdoc.ZDocLineType;

public class ZDocScannerTest extends AbstractParsingTest {

    private ZDocScanner scanner;

    @Before
    public void before() {
        scanner = new ZDocScanner();
    }

    @Test
    public void test_table() {
        String s = "H0 | H1";
        s += "\n --- | ---";
        s += "\n A | B";
        s += "\n C | D";

        Parsing ing = scan(s);

        assertEquals(1, ing.blocks.size());
        assertEquals(4, ing.blocks.get(0).lines.size());
        _C(ing, 0, TABLE, 0, "H0 | H1", 0, THEAD);
        _C(ing, 0, TABLE, 1, "--- | ---", 0, TSEP);
        _C(ing, 0, TABLE, 2, " A | B", 0, TR);
        _C(ing, 0, TABLE, 3, " C | D", 0, TR);
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
    public void test_html_comment() {
        Parsing ing = scanf("org/nutz/zdoc/f/html_comment.txt");

        assertEquals(5, ing.blocks.size());

        _C(ing, 0, PARAGRAPH, 0, "#A", 0, PARAGRAPH);

        _C(ing, 1, OL, 0, "B1", 0, OL, '#');
        _C(ing, 1, OL, 1, "B2", 0, OL, '1');
        _C(ing, 1, OL, 2, "B3", 0, OL, 'I');

        _C(ing, 2, COMMENT, 0, "this is comment", 0, COMMENT);

        _C(ing, 3, PARAGRAPH, 0, "[http://nutzam.com]", 0, PARAGRAPH);

        _C(ing, 4, COMMENT, 0, "", 0, COMMENT_BEGIN);
        _C(ing, 4, COMMENT, 1, "THIS IS", 0, PARAGRAPH);
        _C(ing, 4, COMMENT, 2, "    multi-lines", 0, PARAGRAPH);
        _C(ing, 4, COMMENT, 3, "", 0, BLANK);
        _C(ing, 4, COMMENT, 4, "comments", 0, COMMENT_END);
    }

    @Test
    public void test_simple_scan() {
        String s = "#A";
        s += "\n#B";
        s += "\n    * C1";
        s += "\n    * C2";
        s += "\n    a. D";

        Parsing ing = scan(s);
        assertEquals(3, ing.blocks.size());

        _C(ing, 0, PARAGRAPH, 0, "#A", 0, PARAGRAPH);
        _C(ing, 0, PARAGRAPH, 1, "#B", 0, PARAGRAPH);

        _C(ing, 1, UL, 0, "C1", 1, UL);
        _C(ing, 1, UL, 1, "C2", 1, UL);

        _C(ing, 2, OL, 0, "D", 1, OL, 'a');
    }

    private void _C(Parsing ing,
                    int bIndex,
                    ZDocLineType expectBlockType,
                    int lIndex,
                    String expectText,
                    int expectIndent,
                    ZDocLineType expectLineType) {
        this._C(ing,
                bIndex,
                expectBlockType,
                lIndex,
                expectText,
                expectIndent,
                expectLineType,
                (char) 0);
    }

    private void _C(Parsing ing,
                    int bIndex,
                    ZDocLineType expectBlockType,
                    int lIndex,
                    String expectText,
                    int expectIndent,
                    ZDocLineType expectLineType,
                    char expectIType) {
        ZDocBlock bl = ing.blocks.get(bIndex);

        assertEquals(expectBlockType, bl.type);

        ZDocLine line = bl.lines.get(lIndex);
        assertEquals(expectIndent, line.indent);
        if (null == expectText) {
            assertNull(line.text);
        } else {
            assertEquals(expectText, line.text);
        }
        assertEquals(expectLineType, line.type);
        assertEquals(expectIType, line.itype);
    }

    Parsing scan(String str) {
        Parsing ing = ING(str);
        scanner.scan(ing);
        return ing;
    }

    Parsing scanf(String ph) {
        Parsing ing = INGf(ph);
        scanner.scan(ing);
        return ing;
    }

}
