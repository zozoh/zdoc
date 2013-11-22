package org.nutz.zdoc;

import org.nutz.lang.Files;
import org.nutz.vfs.simple.ZSimpleDir;
import org.nutz.vfs.simple.ZSimpleIO;

public class ZDoc {

    public static final String VERSION = "2.0";

    public static void main(String[] args) {

        ZDocHome home = new ZDocHome(new ZSimpleIO());
        ZSimpleDir src = new ZSimpleDir(Files.findFile(args[0]));
        home.src(src);
        System.out.println(home.index());
    }

}
