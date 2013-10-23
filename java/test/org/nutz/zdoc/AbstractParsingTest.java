package org.nutz.zdoc;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;

public class AbstractParsingTest {

    protected Parsing ING(String str) {
        return new Parsing(Lang.inr(str));
    }

    protected Parsing INGf(String ph) {
        return new Parsing(Streams.fileInr(ph));
    }
}
