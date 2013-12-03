package org.nutz.zdoc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.zdoc.am.AmTest;
import org.nutz.zdoc.impl.ZDocParserTest;
import org.nutz.zdoc.impl.ZDocScannerTest;
import org.nutz.zdoc.util.ZDTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ZDTest.class,
                     ZDocScannerTest.class,
                     AmTest.class,
                     ZDocParserTest.class})
public class AllZDocTest {}
