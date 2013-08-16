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

import java.util.HashMap;
import java.io.File;

/**
 * <p>
 * Diese Klasse implementiert eine einfache Verwaltung von mehrsprachigen Texten und 
 * Zeichenketten im XML-Format. Die aktuelle Impelementierung unterstützt mometan
 * folgende Sprachen: <b>DE</b>,<b>EN</b> und <b>FR</b>.
 * </p>
 */
public class TextLib {

    /**
     * Lädt eine XML-Sprachdatei.
     * @param contentName interner Name der Sprachdatei
     * @param path Pfad der XML-Sprachdatei
     * @return
     * @throws Exception
     */
    public static synchronized Text loadFile(String contentName, String path) throws Exception {
        
        Text text = (Text) libMap.get(contentName);
        long dolm = new File(path).lastModified();
        if (text == null || dolm != text.getLastModfied()) {
            path = "file:///" + path.replace('\\', '/');
            text = new Text(path,false);
            libMap.put(contentName, text);
        }
        return text;
    }

    public static synchronized Text loadURL(String contentName, String url) throws Exception {
        Text text = (Text) libMap.get(contentName);
        if (text == null) {
            text = new Text(url,true);
            libMap.put(contentName, text);
        }
        return text;
    }

    public static Text getText(String textName) {
        return libMap.get(textName);
    }
   
    private static HashMap<String, Text> libMap = new HashMap<String, Text>();
 
}
