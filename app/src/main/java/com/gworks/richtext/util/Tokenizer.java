package com.gworks.richtext.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by durgadass on 1/8/17.
 */
public class Tokenizer implements Iterable<String>{

    private String string;

    private String[] delimiters;

    private boolean returnDelimiters;

    /**
     * Constructs a new {@code Tokenizer} for the parameter string using
     * whitespace as the delimiter. The {@code returnDelimiters} flag is set to
     * {@code false}.
     *
     * @param string the string to be tokenized.
     */
    public Tokenizer(String string) {
        this(string, new String[]{" ", "\t", "\n", "\r", "\f"}, false);
    }

    /**
     * Constructs a new {@code Tokenizer} for the parameter string using
     * the specified delimiters. The {@code returnDelimiters} flag is set to
     * {@code false}.
     *
     * @param string     the string to be tokenized.
     * @param delimiters the delimiters to use.
     */
    public Tokenizer(String string, String[] delimiters) {
        this(string, delimiters, false);
    }

    /**
     * Constructs a new {@code Tokenizer} for the parameter string using
     * the specified delimiters, returning the delimiters as tokens if the
     * parameter {@code returnDelimiters} is {@code true}.
     *
     * @param string           the string to be tokenized.
     * @param delimiters       the delimiters to use.
     * @param returnDelimiters {@code true} to return each delimiter as a token.
     */
    public Tokenizer(String string, String[] delimiters,
                     boolean returnDelimiters) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        this.string = string;
        this.delimiters = delimiters == null ? new String[0]: Arrays.copyOf(delimiters, delimiters.length);
        this.returnDelimiters = returnDelimiters;
    }

    @Override
    public Iterator<String> iterator() {
        return new MyIterator();
    }

    public Enumeration<String> tokens(){
        return new MyIterator();
    }

    private class MyIterator implements Iterator<String> , Enumeration<String> {

        private int position;

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            return null;
        }

        @Override
        public boolean hasMoreElements() {
            return hasNext();
        }

        @Override
        public String nextElement() {
            return next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove");
        }
    }
}
