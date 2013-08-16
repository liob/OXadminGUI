/*
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package at.sciencesoft.util;

import java.util.*;
import java.io.*;

/**
 * Helper class is for text replacment operations.
 * 
 * @author Peter Sauer
 */
public class ReplaceText {

    /**
     * Creates a replacement container for character array insertion.
     *
     * @param property
     *            String
     * @param valueArray
     *            char[]
     */
    public ReplaceText(String property, char[] valueArray) {
        this.property = property;
        this.valueString = null;
        this.valueArray = valueArray;
    }

    /**
     * Creates a replacement container for a number insertion.
     *
     * @param property
     *            key word
     * @param value
     *            int
     */
    public ReplaceText(String property, int value) {
        this.property = property;
        valueString = "" + value;
        valueArray = null;
    }

    /**
     * Creates a replacement container for a string insertion.
     *
     * @param property
     *            key word
     * @param valueString
     *            replacement string
     */
    public ReplaceText(String property, String valueString) {
        this.property = property;
        this.valueString = valueString;
        valueArray = null;
    }

    @Override
    public boolean equals(Object obj) {
        return property.equals(((ReplaceText) obj).property);
    }

    @Override
    public int hashCode() {
        return property.hashCode();
    }

    public static String replaceText(char[] source, ReplaceText[] replace) {
        int i, j, max, start;
        int size = 0;
        HashMap<String, ReplaceText> htable =
                new HashMap<String, ReplaceText>(replace.length * 2);
        // caluculate additional space
        for (i = 0; i < replace.length; ++i) {
            if (replace[i].valueString != null) {
                size += replace[i].valueString.length();
            } else {
                size += replace[i].valueArray.length;
            }
            htable.put(replace[i].property, replace[i]);
        }
        // perhaps multiple instances of a replace string
        // so add a memory scope - should help to avoid re-allocation
        int bufferSize = source.length + size + 1024;
        StringBuffer buffer = new StringBuffer(bufferSize);
        main_loop:
        for (i = 0; i < source.length; ++i) {
            if (source[i] == SUBST_SYMBOL) {
                // code stuffing?
                if (i < source.length - 1) {
                    if (source[i + 1] == SUBST_SYMBOL) {
                        buffer.append(SUBST_SYMBOL);
                        ++i;
                        continue;
                    }
                } else {
                    // last character is a SUBST_SYMBOL - ???
                    buffer.append(SUBST_SYMBOL);
                    break;
                }
                start = i + 1;
                max = Math.min(start + MAX_SUBSTVAR_LEN, source.length);
                for (j = start; j < max; ++j) {
                    if (source[j] == SUBST_SYMBOL) {
                        int len = j - start;
                        if (len > 0) {
                            ReplaceText r = htable.get(new String(source, start, len));
                            if (r != null) {
                                if (r.valueString != null) {
                                    buffer.append(r.valueString);
                                } else {
                                    buffer.append(r.valueArray);
                                }
                                i = j;
                                continue main_loop;
                            }
                        }
                    }
                }
            }
            buffer.append(source[i]);
        }
        return buffer.toString();
    }

    /**
     * @param source
     *            char[]
     * @param replace
     *            ReplaceText[]
     * @return String
     */
    public static String replaceText(String source, ReplaceText[] replace) {
        return replaceText(source.toCharArray(), replace);
    }

    /**
     *
     * @param test
     */
    public static String replaceText(File file, ReplaceText[] replace) throws IOException {
        FileReader reader = new FileReader(file);
        int fileLength = (int) file.length();
        char[] stream = new char[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return replaceText(stream, replace);
    }
    private final String property;
    private final String valueString;
    private final char[] valueArray;
    public static final char SUBST_SYMBOL = '$';
    public static final int MAX_SUBSTVAR_LEN = 25;
}
