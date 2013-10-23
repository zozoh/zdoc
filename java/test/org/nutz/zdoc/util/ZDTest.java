package org.nutz.zdoc.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.nutz.lang.Nums;

public class ZDTest {

    @Test
    public void test_splitToListEscapeQuote() {
        List<String> ss;

        ss = SS("||A||", "|");
        assertEquals(1, ss.size());
        assertEquals("A", ss.get(0));

        ss = SS("|A|", "|");
        assertEquals(1, ss.size());
        assertEquals("A", ss.get(0));

        ss = SS("A", "|");
        assertEquals(1, ss.size());
        assertEquals("A", ss.get(0));

        ss = SS("|A", "|");
        assertEquals(1, ss.size());
        assertEquals("A", ss.get(0));

        ss = SS("| A`|` || B'|' |C{|}||", "|");
        assertEquals(3, ss.size());
        assertEquals(" A`|` ", ss.get(0));
        assertEquals(" B'|' ", ss.get(1));
        assertEquals("C{|}", ss.get(2));
    }

    private static List<String> SS(String str, String sep) {
        List<String> ss = ZD.splitToListEscapeQuote(str,
                                                    sep,
                                                    Nums.arrayC('`', '\'', '{'),
                                                    Nums.arrayC('`', '\'', '}'));
        return ss;
    }

}
