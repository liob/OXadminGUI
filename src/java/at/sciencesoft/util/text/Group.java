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

/**
 * <p> 
 * Diese Klasse fasst Texte und Zeichenketten, die einem Zusammenhang stehen,
 * in Form einer Gruppe zusammen. Jeder Gruppe hat einen eindeutigen Namen. Eine Gruppe
 * kann beinhaltet z.B. die mehrsprachigen Texte einer JSP/HTML-Seite.
 * </p>
 */
public class Group {

    public Group(Group g) {
        this.language = g.language;
        this.name = g.name;
        this.map = g.map;
    }

    private  Group() {
       
    }

    /**
     * Setzt die Sprache fest.
     * @param lang Sprachkürzel
     */
    public void setLang(String lang) {
        language = Text.LANG_DE;
        if (lang != null) {
            if ("en".equals(lang)) {
                language = Text.LANG_EN;
            } else if ("fr".equals(lang)) {
                language = Text.LANG_FR;
            } else if ("it".equals(lang)) {
                language = Text.LANG_IT;
            } else if ("es".equals(lang)) {
                language = Text.LANG_ES;
            } else if ("nl".equals(lang)) {
                language = Text.LANG_NL;
            }
        }

    }

   

    /**
     * Setzt die Sprache (Deutsch) und die JSP-Ausgabe fest.
     * @param out JSP-Ausgabekanal
     */
    public void setLangDE() {
        language = Text.LANG_DE;
    }

    /**
     * Setzt die Sprache (Englisch) und die JSP-Ausgabe fest.
     * @param out JSP-Ausgabekanal
     */
    public void setLangEN() {
        language = Text.LANG_EN;
    }

    /**
     * Setzt die Sprache (Französisch) und die JSP-Ausgabe fest.
     * @param out JSP-Ausgabekanal
     */
    public void setLangFR() {
        language = Text.LANG_FR;
    }

    /**
     * Setzt die Sprache (Italienisch) und die JSP-Ausgabe fest.
     * @param out JSP-Ausgabekanal
     */
    public void setLangIT() {
        language = Text.LANG_IT;
    }

    /**
     * Gibt einen Text für einen bestimmten Schlüssel zurück.
     * @param id Schlüssel
     * @return Text, der mit dem Schlüssel assoziert ist.
     */
    public String get(String id) {
        String t = (String) map.get(id + "_" + language);
        if (t == null) {
            t = (String) map.get(id);
        }
        return t;
    }

    /**
     * Gibt die aktuelle Sprache als Zahl zurück.
     */
    public int getLanguage() {
        return language;
    }

    /**
     * Liefert für einen bestimmten Schlüssel den deutschen Text zurück.
     * @param id Schlüssel
     * @return Text
     */
    public String getDE(String id) {
        return (String) map.get(id + "_" + Text.LANG_DE);
    }

    /**
     * Liefert für eine bestimmte ID den englischen Text zurück.
     * @param id Text ID
     * @return Text
     */
    public String getEN(String id) {
        return (String) map.get(id +  "_" + Text.LANG_EN);
    }

    /**
     * Liefert für eine bestimmte ID den französischen Text zurück.
     * @param id Text ID
     * @return Text
     */
    public String getFR(String id) {
        return (String) map.get(id + "_" + Text.LANG_FR);
    }

    /**
     * Speichert einen deuschten Text im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putDE(String id, String text) {
        map.put(id + "_" + Text.LANG_DE, text);
    }

    /**
     * Speichert einen englischen Text im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putEN(String id, String text) {
        map.put(id + "_" + Text.LANG_EN, text);
    }

    /**
     * Speichert einen französischen Text im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putFR(String id, String text) {
        map.put(id + "_" + Text.LANG_FR, text);
    }

    /**
     * Speichert einen italienischen Text im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putIT(String id, String text) {
        map.put(id + "_" + Text.LANG_IT, text);
    }

     /**
     * Speichert einen spanischen Text im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putES(String id, String text) {
        map.put(id + "_" + Text.LANG_ES, text);
    }
    
     /**
     * Speichert einen niederländischen test im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putNL(String id, String text) {
        map.put(id + "_" + Text.LANG_NL, text);
    }


    /**
     * Speichert einen sprachunabhängigen Text im Kontext mit einem Schlüssel.
     * @param id Schlüssel
     * @param text Text
     */
    public void putSYS(String id, String text) {
        map.put(id, text);
    }

    /**
     * Konstruktor
     * @param group Name der Gruppe
     */
    public Group(String name) {
        map = new HashMap<String, String>();
        this.name = name;
    }

    /**
     * Gibt den Namen der Gruppe zur�ck.
     * @return group Name der Gruppe.
     */
    public String getName() {
        return name;
    }
    private int language = Text.LANG_DE;
    private HashMap<String, String> map;
    private String name;

}