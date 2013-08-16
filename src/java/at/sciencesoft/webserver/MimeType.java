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

import java.util.HashMap;

/**
 *
 * @author Peter Sauer
 */
public class MimeType {

    private MimeType() {       
    }

    public MimeType(String mimeType,boolean isBinary) {
        this.mimeType = mimeType;
        this.isBinary = isBinary;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public MimeType getMimeType(String uri) throws Exception {
        int pos = uri.lastIndexOf('.');
        if(pos == -1) {
            throw new Exception("Missing file extension. URI: '" + uri + "'");
        }
        String extension = uri.substring(pos + 1,uri.length());
        String mime = mimeMap.get(extension.toLowerCase());
        if(mime == null) {
            throw new Exception("Unsupported mime type: " + extension + " URI: '" + uri + "'");
        }
        if(Character.isUpperCase(mime.charAt(0))) {
            return new MimeType(mime.toLowerCase(),true);
        }
        return new MimeType(mime,false);
    }

    public void addTextMimeType(String ext,String mime) {
        if(ext != null && mime != null && !ext.equals("") && !mime.equals("")) {
            mimeMap.put(ext,mime);
        }
    }
    public void addBinaryMimeType(String ext,String mime) {
         if(ext != null && mime != null && !ext.equals("") && mime.length() > 1) {
            mimeMap.put(ext,Character.toUpperCase(mime.charAt(0)) + mime.substring(1));
         }
    }

    public void setExpireDate(String mimeType,int sec) throws Exception{
        if(mimeMap.get(mimeType) == null) {
             throw new Exception("Unsupported mime type: " + mimeType);
        }
        mimeExpireMap.put(mimeType, sec);
    }

    public Integer getExpireDate(String  mimeType) throws Exception {
        if(mimeMap.get(mimeType) == null) {
             throw new Exception("Unsupported mime type: " + mimeType);
        }
        return mimeExpireMap.get(mimeType);
    }

    public Integer getExpireDate(MimeType  mimeType) throws Exception {
        String tmp = mimeType.getMimeType();
        return mimeExpireMap.get(tmp.substring(tmp.indexOf('/') + 1));
    }

    public static MimeType getInstance() {
        if(mimeTypeInstance == null) {
             mimeTypeInstance = new MimeType();
             mimeMap = new HashMap<String,String>();
             // binary data
             mimeMap.put("jpeg","Image/jpeg"); // first letter uppercase == binary data!
             mimeMap.put("jpg","Image/jpeg");
             mimeMap.put("pdf","Application/pdf");
             mimeMap.put("png","Image/png");
             mimeMap.put("gif","Image/gif");
             // text data
             mimeMap.put("htm","text/html");
             mimeMap.put("html","text/html");
             mimeMap.put("fmt","text/html"); // FreeMarker Template (fmt)
             mimeMap.put("ftl","text/html"); //FreeMarker Template (ftl) due offical docu
             mimeMap.put("js","application/x-javascript");
             mimeMap.put("txt","text/plain");
             mimeMap.put("css","text/css");
             mimeMap.put("xml","text/xml");
             mimeMap.put("dtd","text/xml");
             mimeExpireMap =  new HashMap<String,Integer>();
        }
        return mimeTypeInstance;
    }
    
    private boolean isBinary;
    private String mimeType;

    private static MimeType mimeTypeInstance;
    private static HashMap<String,String> mimeMap;
    private static HashMap<String,Integer> mimeExpireMap;

}
