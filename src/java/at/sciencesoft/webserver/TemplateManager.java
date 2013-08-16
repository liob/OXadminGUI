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

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;

;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class TemplateManager {

    public TemplateManager() {
        clazzMap = new HashMap<String, TemplateHandler>();
        fmCfg = new Configuration();
        fmCfg.setNumberFormat("0.######");
        fmStringLoader = new StringTemplateLoader();
        fmCfg.setTemplateLoader(fmStringLoader);
        fmCfg.setObjectWrapper(new DefaultObjectWrapper());
    }

    public void addTemplate(String uri, String content) {
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        fmStringLoader.putTemplate(uri, content);
    }

    private void throwBindException(String uri) throws Exception {
        throw new Exception("Unable to bind URI '" + uri + "' to a template handler class!");
    }

    public void process(String uri, String template, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, IOException, TemplateException, Exception {
        // remove leading '/' - necessary for StringTemplateLoader.putTemplate()
        uri = uri.substring(1);
        int pos = template.indexOf('\n');
        if (pos == -1) {
            throwBindException(uri);
        }
        String clazz = template.substring(0, pos).trim();
        if (!clazz.startsWith(fmCommentStart)) {
            throwBindException(uri);
        }

        // search end of comment - comment can consist of muliple lines!
        pos = template.indexOf(fmCommentEnd);
        clazz = template.substring(0, pos).trim();

        if (pos == -1) {
            throwBindException(uri);
        }

        clazz = clazz.substring(fmCommentStart.length(), clazz.length()).trim();
        if (!clazz.startsWith("@")) {
            throwBindException(uri);
        }

        pos = clazz.indexOf('#');
        int pos2 = clazz.indexOf('|');

        if (pos != - 1) {
            // overwrite default MIME Type (HTML) of a FreeMarker template
            int end;
            if (pos2 == -1) {
                end = clazz.length();
            } else {
                end = pos2;
            }

            String mineType = clazz.substring(pos + 1, end).trim();
            MimeType mt = MimeType.getInstance().getMimeType("." + mineType);
            if (mt.isBinary()) {
                throw new Exception("Mime type overwriting is not valid for binary mime types.");
            }
            response.setContentType(mt.getMimeType());
        }

        // preload templates
        if (pos2 != -1) {
            String[] files = clazz.substring(pos2 + 1).split("\\|");
            WebServerConfig wsc = WebServerConfig.getInstance();
            for (int i = 0; i < files.length; ++i) {
                String file = files[i].trim();
                if (!file.startsWith("/")) {
                    file = "/" + file;
                }
                fmStringLoader.putTemplate(file.substring(1), wsc.getFileManager().getFile(file).getTextData());
            }
        }

        if (pos != - 1) {
            clazz = clazz.substring(1, pos);
        } else if (pos2 != -1) {
            clazz = clazz.substring(1, pos2);
        } else {
            clazz = clazz.substring(1);
        }

        TemplateHandler tm = clazzMap.get(clazz);

        if (tm == null) {
            tm = (TemplateHandler) Class.forName(clazz).newInstance();
            tm.init();
            clazzMap.put(clazz, tm);
        }
        fmStringLoader.putTemplate(uri, template);
        HashMap map = tm.process(request,response);
        if(map.get(REDIRECT_FLAG) == null) {
            Template temp = fmCfg.getTemplate(uri);
            temp.process(map, out);
        }
        out.flush();
    }

    public static void setRedirectFlag(HashMap map) {
        map.put(REDIRECT_FLAG, "*");
    }

    public void preloadTemplate(String path) throws Exception {
        WebServerConfig wsc = WebServerConfig.getInstance();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        fmStringLoader.putTemplate(path.substring(1), wsc.getFileManager().getFile(path).getTextData());
    }

    private HashMap<String, TemplateHandler> clazzMap;
    private StringTemplateLoader fmStringLoader;
    private Configuration fmCfg;
    private final static String fmCommentStart = "<#--";
    private final static String fmCommentEnd = "-->";
    private final static String REDIRECT_FLAG = "_ReDiReCt_";
}
