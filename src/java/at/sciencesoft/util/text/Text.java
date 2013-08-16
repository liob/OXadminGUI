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

package at.sciencesoft.util.text;

import java.util.*;
import java.io.*;

/**
 * <p>
 * Diese Klasse verwaltet mehrsprachige Text in Form einer XML Datei.
 * </p>
 */
public class Text {

    /**
     * Konstruktor
     *
     * @param path
     *            Pfad der XML-Datei
     */
    public Text(String path, boolean isURL) throws Exception {
        this.path = path;
        this.isURL = isURL;
        parser = new TextParser(this);
        parser.parse();
    }

    /**
     * Speichert die letzte Änderung der XML-Datei.
     *
     * @param time
     */
    public void setLastModified(long time) {
        lastModified = time;
    }

    /**
     * Gibt den Zeitpunkt der letzten �nderung der XML-Datei in Millisekunden
     * zurück.
     *
     * @return
     */
    public long getLastModfied() {
        return lastModified;
    }

    /**
     * Speichert eine <code>Group</code>
     *
     * @param group
     *            <code>Group</code>
     */
    public void putGroup(Group group) {
        groups.put(group.getName(), group);
    }

    /**
     * Gibt eine <code>Group</code> zurück.
     *
     * @param name
     *            Name der Gruppe
     * @return group <code>Group</code>
     */
    public Group getGroup(String name) throws Exception {
        // wurde die XML-Datei zwischenzeitlich geändert?

        if (!isURL) {
            long lm = new File(getFilePath()).lastModified();
            if (lm != lastModified) {
                // XML-Datei neu laden!
                lastModified = lm;
                parser.parse();
            }
        }

        Group g = groups.get(name);
        if (g != null) {
            g = new Group(g);
        }
        return g;
    }

    /**
     * Gibt eine <code>Group</code> zurück, ohne zu Überprüfen, ob
     * die XML-Datei zwischenzeitlich geändert wurde.
     * @param name
     *            Name der Gruppe
     * @return group <code>Group</code>
     */
    public Group getGroupWithoutCheck(String name) {
        return (Group) groups.get(name);
    }

    /**
     * Gibt den Dateipfad der XML-Datei zur�ck.
     * @return Dateipfad der XML-Datei
     */
    public String getPath() {
        return path;
    }

    public String getFilePath() {
        return path.substring("file:///".length());
    }

    private HashMap<String, Group> groups = new HashMap<String, Group>();
    /**
     * Deutsch
     */
    public static final int LANG_DE = 0;
    /**
     * Englisch
     */
    public static final int LANG_EN = 1;
    /**
     * Französisch
     */
    public static final int LANG_FR = 2;
    public static final int LANG_IT = 3;
    public static final int LANG_ES = 4;
    public static final int LANG_NL = 5;
    private String path;
    private boolean isURL;
    static private int primaryLanguage = LANG_DE;
    private TextParser parser;
    private long lastModified = 0;
}
