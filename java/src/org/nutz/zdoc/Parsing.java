package org.nutz.zdoc;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.nutz.am.Am;
import org.nutz.lang.Streams;

public class Parsing {

    public BufferedReader reader;

    public List<ZDocBlock> blocks;

    public LinkedList<Am> ams;

    public LinkedList<ZDocEle> eles;

    public int depth;

    public ZDocNode root;

    public ZDocNode current;

    public Am am() {
        return ams.peekLast();
    }

    public ZDocEle ele() {
        return eles.peekLast();
    }

    public Parsing(Reader reader) {
        this.reader = Streams.buffr(reader);
        root = new ZDocNode();
        current = root;
        blocks = new LinkedList<ZDocBlock>();
        ams = new LinkedList<Am>();
        eles = new LinkedList<ZDocEle>();
    }

}
