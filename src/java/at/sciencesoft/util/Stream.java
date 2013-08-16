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


import java.io.*;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Stream {

    /**
     * Reads a byte stream.
     * @param file <tt>File</tt>-object
     * @return byte array
     */
    public static byte[] readByteStream(File file) throws Exception {
        FileInputStream reader = new FileInputStream(file);
        int fileLength = (int) file.length();
        byte[] stream = new byte[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return stream;
    }

    /**
     * Reads a byte stream.
     * @param fileName file path
     * @return byte array
     */
    public static byte[] readByteStream(String fileName) throws Exception {
        File file = new File(fileName);
        FileInputStream reader = new FileInputStream(file);
        int fileLength = (int) file.length();
        byte[] stream = new byte[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return stream;
    }

    /**
     * Reads a character stream.
     * @param file <tt>File</tt> object
     * @return character array
     */
    public static char[] readCharStream(File file) throws Exception {
        FileReader reader = new FileReader(file);
        int fileLength = (int) file.length();
        char[] stream = new char[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return stream;
    }

    /**
     * Reads a character stream.
     * @param fileName file path
     * @return character array
     */
    public static char[] readCharStream(String fileName) throws Exception {
        File file = new File(fileName);
        FileReader reader = new FileReader(file);
        int fileLength = (int) file.length();
        char[] stream = new char[fileLength];
        reader.read(stream, 0, fileLength);
        reader.close();
        return stream;
    }

    /**
     * Writes a character stream.
     * @param fileName file path
     * @return character array
     */
    public static void writeCharStream(String fileName, char[] stream) throws Exception {
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(stream, 0, stream.length);
        writer.close();
    }

    /**
     * Writes a character stream.
     * @param fileName file path
     * @return character array
     */
    public static void writeByteStream(String fileName, byte[] stream) throws Exception {
        File file = new File(fileName);
        FileOutputStream writer = new FileOutputStream(file);
        writer.write(stream, 0, stream.length);
        writer.close();
    }

    /**
     * Writes a character stream.
     * @param fileName file path
     * @return character array
     */
    public static void writeString(String fileName, String stream) throws Exception {
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(stream, 0, stream.length());
        writer.close();
    }

    public static void writeString(File file, String stream) throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(stream, 0, stream.length());
        writer.close();
    }

    public static void copy(String src, String dst) throws IOException {
        InputStream in = new FileInputStream(new File(src));
        OutputStream out = new FileOutputStream(new File(dst));

        // Transfer bytes from in to out
        byte[] buf = new byte[1024 * 64];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
