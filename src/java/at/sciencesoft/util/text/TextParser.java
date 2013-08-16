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

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;

/**
 * <p>
 * Diese Klasse liest die XML-Datei, die mehrsprachigen Text und Zeichenketten
 * beinhaltet ein.
 * </p>
 */
public class TextParser extends DefaultHandler {

    /**
     * Liest die XML-Datei mit den Sprachdaten ein.
     * @throws SAXException
     * @throws SAXParseException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public Text parse()
            throws
            SAXException,
            SAXParseException,
            ParserConfigurationException,
            IOException {


        saxParser.parse(text.getPath(), this);
        return text;
    }

    /**
     * Konstruktor
     * @param text
     * @throws Exception
     */
    public TextParser(Text text) throws Exception {
        factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        saxParser = factory.newSAXParser();
        this.text = text;
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXParseException {
        throw e;
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] buf, int offset, int len) {
        if (inText) {
            textBuffer.append(buf, offset, len);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(
            String namespaceURI,
            String sName,
            String qName,
            Attributes attrs)
            throws SAXException {
        if (qName.equals("group")) {
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String tmp = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    if ("name".equals(tmp)) {
                        if (text != null) {
                            actualGroup = text.getGroupWithoutCheck(value);
                        }
                        if (actualGroup == null) {
                            actualGroup = new Group(value);
                        }
                    }
                }
            }
        }
        if (qName.equals("text")) {
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String tmp = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    if ("id".equals(tmp)) {
                        inText = true;
                        id = value;
                    }
                }
            }
        }
        if (qName.equals("lang")) {
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String tmp = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    if ("id".equals(tmp)) {
                        lang = value;
                        inLanguage = true;
                        textBuffer = new StringBuffer();
                    }
                }
            }

        }

    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String namespaceURI, String sName, String qName)
            throws SAXException {

        if (qName.equals("group")) {
            text.putGroup(actualGroup);
        }

        if (qName.equals("lang")) {
            inLanguage = false;
            if (lang.equals("de")) {
                actualGroup.putDE(id, textBuffer.toString());
            } else if (lang.equals("en")) {
                actualGroup.putEN(id, textBuffer.toString());
            } else if (lang.equals("fr")) {
                actualGroup.putFR(id, textBuffer.toString());
            } else if (lang.equals("sys")) {
                actualGroup.putSYS(id, textBuffer.toString());
            }  else if (lang.equals("it")) {
                actualGroup.putIT(id, textBuffer.toString());
            } else if (lang.equals("es")) {
                actualGroup.putES(id, textBuffer.toString());
            } else if (lang.equals("nl")) {
                actualGroup.putNL(id, textBuffer.toString());
            }
        }

        if (qName.equals("text")) {
            inText = false;
        }
    }

    /**
     * Returns a Text object.
     * @return
     */
    public Text getText() {
        return text;
    }

    private String lang;
    private String id;
    private boolean inText = false;
    private boolean inLanguage = true;
    private Group actualGroup;
    private Text text;
    private StringBuffer textBuffer;
    private SAXParserFactory factory;
    private SAXParser saxParser;
}
