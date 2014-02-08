package org.nutz.zdoc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.nutz.zdoc.ZDocBaseTest;
import org.nutz.zdoc.Parsing;
import org.nutz.zdoc.ZBlock;
import org.nutz.zdoc.ZLine;
import org.nutz.zdoc.ZLineType;

public class AbstractScannerTest extends ZDocBaseTest {

    protected AbstractScanner scanner;

    protected void _C(Parsing ing,
                      int bIndex,
                      ZLineType expectBlockType,
                      int lIndex,
                      String expectText,
                      int expectIndent,
                      ZLineType expectLineType) {
        this._C(ing,
                bIndex,
                expectBlockType,
                lIndex,
                expectText,
                expectIndent,
                expectLineType,
                (char) 0);
    }

    protected void _C(Parsing ing,
                      int bIndex,
                      ZLineType expectBlockType,
                      int lIndex,
                      String expectText,
                      int expectIndent,
                      ZLineType expectLineType,
                      char expectIType) {
        ZBlock bl = ing.blocks.get(bIndex);

        assertEquals(expectBlockType, bl.type);

        ZLine line = bl.lines.get(lIndex);
        assertEquals(expectIndent, line.indent);
        if (null == expectText) {
            assertNull(line.text());
        } else {
            assertEquals(expectText, line.text());
        }
        assertEquals(expectLineType, line.type);
        assertEquals(expectIType, line.itype);
    }

    protected Parsing scan(String str) {
        Parsing ing = ING(str);
        scanner.scan(ing);
        return ing;
    }

    protected Parsing scanf(String ph) {
        Parsing ing = INGf(ph);
        scanner.scan(ing);
        return ing;
    }
}
