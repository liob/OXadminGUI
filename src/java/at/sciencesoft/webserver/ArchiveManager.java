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

import at.sciencesoft.system.Config;
import at.sciencesoft.util.Stream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ArchiveManager {
    /*
     * Internal class 
     */

    private class ArchiveEntry {

        ArchiveEntry(int pos, int length) {
            this.pos = pos;
            this.length = length;
        }
        public final int pos;   //
        public final int length;
    }

    /**
     *
     * @param archive
     * @param directory
     * @throws IOException
     */
    public ArchiveManager(String archive, String directory, String tmpFileName, boolean isCache) throws IOException, Exception {
        this.archive = archive;
        this.directory = directory;
        this.isCache = isCache;

        // setup temporary content file
        String tmpDir = Config.getTempDir();

        webData = new File(tmpDir + tmpFileName);

        // open zip/jar archive
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(webData));
        ZipFile zipFile = new ZipFile(archive);
        Enumeration entries = zipFile.entries();

        // traverse the content file list
        int len;
        int bytesWritten = 0;
        byte[] buffer = new byte[16384];
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (!entry.isDirectory()) {
                String name = entry.getName();
                // only files of the content directory!
                if (name.startsWith(directory)) {
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int bytesRead = 0;
                    while ((len = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                        bytesRead += len;
                    }
                    bis.close();
                    // store position & length of the content file
                    fileMap.put(name.substring(directory.length()), new ArchiveEntry(bytesWritten, bytesRead));
                    bytesWritten += bytesRead;
                }
            }
        }
        bos.flush();
        bos.close();
        if(isCache) {
            cache = Stream.readByteStream(webData);
        }
    }

    /**
     * @param path
     * @return file content
     * @throws FileNotFoundException
     * @throws IOException
     */
    public byte[] getFile(String path) throws FileNotFoundException, IOException {
        ArchiveEntry ae = fileMap.get(path);
        if (ae == null) {
            throw new FileNotFoundException("File not found: '" + path + "'");
        }
        byte[] data = new byte[ae.length];
        if (isCache) {
            System.arraycopy(cache, ae.pos, data,0,ae.length);
        } else {
            RandomAccessFile raf = new RandomAccessFile(webData, "r");
            try {
                raf.seek(ae.pos);
                raf.read(data, 0, ae.length);
                raf.close();
            } catch (IOException e) {
                throw e;
            } finally {
                if (raf != null) {
                    raf.close();
                }
            }
        }
        return data;
    }

    public void cleanup() {
        if (webData != null) {
            webData.delete();
        }
    }

    public String getArchive() {
        return archive;
    }

    public String getDirectory() {
        return directory;
    }

    public boolean isCache() {
        return isCache;
    }
    private String archive;
    private String directory;
    private File webData;
    private boolean isCache;
    byte[] cache;
    private HashMap<String, ArchiveEntry> fileMap = new HashMap<String, ArchiveEntry>();
}
