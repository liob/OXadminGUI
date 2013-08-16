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

package at.sciencesoft.webserver;

import at.sciencesoft.util.Stream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class FileManager {

    public FileManager(String directory, CharSet charSet) {
        // modify name seperator - windows only!
        directory = directory.replace('\\', '/');
        if (directory.endsWith("/")) {
            directory = directory.substring(0,directory.length() - 1);
        }
        this.directory = directory;
        this.charSet = charSet;
        isArchive = false;
    }

    public FileManager(String archive, String archivePath,String tmpFileName, CharSet charSet) throws IOException,Exception {
        if(archivePath.startsWith("/")) {
            archivePath = archivePath.substring(1);
        }
        if(archivePath.endsWith("/")) {
            archivePath = archivePath.substring(0,archivePath.length()-1);
        }
        this.charSet = charSet;
        isArchive = true;
        archiveManager = new ArchiveManager(archive, archivePath,tmpFileName,true);
    }

    public void cleanup() {
        if(isArchive) {
            archiveManager.cleanup();
        }
    }

    public Content getFile(String uri) throws Exception {
        Content webFile = null;
        byte[] data = null;
        if (!isArchive) {
            String path = directory + uri;
            File f = new File(path);
            if (!f.exists()) {
                throw new FileNotFoundException("File not found: '" + path + "'");
            }
            data = Stream.readByteStream(f);
        } else {
            data = archiveManager.getFile(uri);
        }

        MimeType mimeInstance = MimeType.getInstance();
        MimeType mimeType = mimeInstance.getMimeType(uri);
        if (mimeType.isBinary()) {
            webFile = new Content(data, mimeType);
        } else {

            int start = 0;
            int length = data.length;

            // skip BOM marker
            if (charSet == CharSet.UTF_8) {
                if (length >= 3) {
                    if (data[0] == BOM[0] && data[1] == BOM[1] && data[2] == BOM[2]) {
                        start = 3;
                        length -= 3;
                    }
                }
                webFile = new Content(new String(data, start, length, charSetList[CharSet.UTF_8.ordinal()]), mimeType);
            } else if (charSet == CharSet.ISO_8859_1) {
                webFile = new Content(new String(data, start, length, charSetList[CharSet.ISO_8859_1.ordinal()]), mimeType);
            } else if (charSet == CharSet.AUTO) {
                CharSet cs;
                if (length >= 3) {
                    if (data[0] == BOM[0] && data[1] == BOM[1] && data[2] == BOM[2]) {
                        start = 3;
                        length -= 3;
                        cs = CharSet.UTF_8;
                    } else {
                        cs = CharSet.ISO_8859_1;
                    }
                } else {
                    cs = CharSet.ISO_8859_1;
                }
                webFile = new Content(new String(data, start, length, charSetList[cs.ordinal()]), mimeType);
            }
        }

        return webFile;
    }

    public static void main(String[] args) {
        WebServerConfig wsc = WebServerConfig.getInstance();
        try {
            wsc.setFileManager(new FileManager("c:/temp/hp/hp.zip", "hp","miniserver.content" ,FileManager.CharSet.AUTO));
            System.out.println(wsc.getFileManager().getFile("/readme.txt").getTextData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCharSet() {
        return charSetList[charSet.ordinal()];
    }
    private boolean isArchive;
    private String directory;
    private CharSet charSet;
    private ArchiveManager archiveManager;
    private static final String[] charSetList = {"ISO-8859-1", "UTF-8"};
    // Byte Order Mark UTF-8
    private static final byte[] BOM = {(byte) 0xef, (byte) 0xBB, (byte) 0xBF};

    // Hint: UTF-8 files should be marked by a BOM!
    public enum CharSet {

        ISO_8859_1, UTF_8, AUTO
    };
}
