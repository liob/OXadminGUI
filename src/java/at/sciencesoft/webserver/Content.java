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

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Content {

    public Content(String textData, MimeType mimeType) {
        this.textData = textData;
        this.mimeType = mimeType;
        binaryData = null;
    }

    public Content(byte[] binaryData, MimeType mimeType) {
        this.binaryData = binaryData;
        this.mimeType = mimeType;
        textData = null;
    }

    public String getTextData() {
        return textData;
    }
 
    public byte[] getBinaryData() {
        return binaryData;
    }

    public MimeType getMimeType() {
        return mimeType;
    }
    private MimeType mimeType;
    private String textData;
    private byte[] binaryData;
}
