package org.nutz.zdoc;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Streams;

public class Parsing {

    public BufferedReader reader;

    public List<ZDocBlock> blocks;

    public int depth;

    public ZDocNode root;

    public ZDocNode current;

    public Parsing(Reader reader) {
        this.reader = Streams.buffr(reader);
        root = new ZDocNode();
        current = root;
        blocks = new LinkedList<ZDocBlock>();
    }

}
