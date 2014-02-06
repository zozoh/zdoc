package org.nutz.zdoc;

import static org.junit.Assert.*;

import org.junit.Test;

public class ZLinkInfoTest {

    @Test
    public void test_00() {
        ZLinkInfo zi = new ZLinkInfo().parse("http://nutzam.com 'Nutz'");
        assertEquals("http://nutzam.com", zi.link());
        assertEquals("Nutz", zi.title());
    }

    @Test
    public void test_01() {
        ZLinkInfo zi = new ZLinkInfo().parse("http://nutzam.com");
        assertEquals("http://nutzam.com", zi.link());
        assertNull(zi.title());
    }
}
