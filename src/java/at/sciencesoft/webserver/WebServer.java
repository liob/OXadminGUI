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

import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.OXserverInfo;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.ReplaceText;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.util.Stream;
import at.sciencesoft.util.StringUtil;
import at.sciencesoft.util.text.TextLib;
import java.io.BufferedWriter;
import java.io.File;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class WebServer extends HttpServlet {

    public static void main(String[] args) {
        try {
            System.out.println("Hello world!");
            FileManager fm = new FileManager("d:/develop/netbeans/OXadminGUI/oxserver/at.sciencesoft.oxadmingui.jar", "web", "miniwebserver.content", FileManager.CharSet.UTF_8);
            System.out.println(fm.getFile("/xml/content.xml").getTextData().length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void preventCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
        response.setHeader("Pragma", "no-cache"); //HTTP 1.0
        response.setDateHeader("Expires", 0); //prevent caching at the proxy server
    }

    public static void setCacheExpireDate(HttpServletResponse response,
            int seconds) {
        if(seconds == 0) {
            preventCaching(response);
        } else{
            Calendar cal = new GregorianCalendar();
            cal.roll(Calendar.SECOND, seconds);
            response.setHeader("Cache-Control", "PUBLIC, max-age=" + seconds + ", must-revalidate");
            response.setHeader("Expires", htmlExpiresDateFormat().format(cal.getTime()));
        }
    }

    public static DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String os = System.getProperty("os.name");
        try {
            WebServerConfig wsc = WebServerConfig.getInstance();

           
            OXserverInfo oxsi = OXserverInfo.getInstance();

            System.out.println("Starting Peter's OX Server Admin GUI on OS '" + os + "'");

            // windows development
            if (os.toLowerCase().indexOf("windows") >= 0) {
               
                String path =config.getServletContext().getRealPath("/psoxgui.properties");
                String subPath = path.substring(0,path.indexOf("OXadminGUI") + "OXadminGUI".length() + 1);
                Config.getInstance().init(path);
                wsc.setFileManager(new FileManager(subPath + "web", FileManager.CharSet.UTF_8));
                wsc.setServletMapping("oxadmingui/server/");
                oxsi.setRMIhost(Config.getInstance().getRMIhost());
                Config.getInstance().setText(TextLib.loadFile("guitext", subPath + "web/xml/content.xml"));
            } else {
                // move init to OSGI activator to register plugins
                Config.getInstance().init(Config.getOpenOXdir() + "etc/psoxgui/psoxgui.properties");
                File f = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
                String jarPath = f.getAbsolutePath();
                FileManager fm = new FileManager(jarPath, "web", "miniwebserver.content", FileManager.CharSet.UTF_8);
                wsc.setFileManager(fm);
                wsc.setServletMapping("servlet/webserver/");
                oxsi.setRMIhost(Config.getInstance().getRMIhost());
                // extract language xml file & DTD
                String tmpDir = Config.getTempDir();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpDir + "content.xml"), "UTF-8"));
                bw.write(fm.getFile("/xml/content.xml").getTextData());
                bw.close();
                Stream.writeString(tmpDir + "text.dtd", fm.getFile("/xml/text.dtd").getTextData());
                Config.getInstance().setText(TextLib.loadFile("guitext", tmpDir + "content.xml"));
            }
            MimeType minst = MimeType.getInstance();
            // set caching
            minst.setExpireDate("html",0);
            minst.setExpireDate("htm",0);
            minst.setExpireDate("txt",0);
            minst.setExpireDate("png",60 * 60 * 24);
            minst.setExpireDate("jpg",60 * 60 * 24);
            minst.setExpireDate("css",60 * 60 * 24);
            minst.setExpireDate("js",60 * 60 * 24);
            String tmp = Config.getInstance().getIPaccessFilter();
            if (tmp != null) {
                ipAccessFilter = StringUtil.splitTrim(tmp);
            }
            OXcontext.registerAccessCombinationName(Config.getInstance().getAccessCombination(null));
            wsc.setTemplateManager(new TemplateManager());
        } catch (Exception e) {
            String error = StackTrace.toString(e);
            try {
                Stream.writeString(Config.getTempDir() + "psoxgui_stacktrace.txt", error);
                if (os.toLowerCase().indexOf("windows") >= 0) {
                    System.out.println(error);
                }
            } catch (Exception dummy) {
            }
            initServletError = error;
        } catch (Error err) {
            String error = StackTrace.toString(err);
            try {
                Stream.writeString(Config.getTempDir() + "psoxgui_stacktrace.txt", error);
                if (os.toLowerCase().indexOf("windows") >= 0) {
                    System.out.println(error);
                }
            } catch (Exception dummy) {
            }
            initServletError = error;
        }

    }

    @Override
    public void destroy() {
        WebServerConfig wsc = WebServerConfig.getInstance();
        wsc.cleanup();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ipAccessFilter != null) {
            String ip = request.getRemoteAddr();
            boolean denied = true;
            for (int i = 0; i < ipAccessFilter.length; ++i) {
                if (ip.startsWith(ipAccessFilter[i])) {
                    denied = false;
                    break;
                }
            }
            if (denied) {
                return;
            }
        }
        try {
            if (initServletError != null) {
                String msg = "Error during servlet init phase of Peter's Open-Xchange Server Admin GUI. See stacktrace for details.\n" + initServletError;
                throw new Exception(msg);
            }

            WebServerConfig wsc = WebServerConfig.getInstance();
            String uri = URLDecoder.decode(request.getRequestURI().substring(wsc.getServletMapping().length()), "UTF-8");

            if (wsc.getStartFile() != null && (uri.equals("") || uri.equals("/"))) {
                uri = wsc.getStartFile();
            }

            Content webFile = wsc.getFileManager().getFile(uri);

            MimeType mimeType = webFile.getMimeType();
            response.setContentType(mimeType.getMimeType());
            MimeType minst = MimeType.getInstance();
            Integer expire = minst.getExpireDate(mimeType);
            if(expire != null) {
                setCacheExpireDate(response,expire.intValue());
            }
            if (mimeType.isBinary()) {
                ServletOutputStream sos = response.getOutputStream();
                sos.write(webFile.getBinaryData());
            } else {
                PrintWriter out = response.getWriter();
                String content = webFile.getTextData();
                // invoke template manager
                if (uri.endsWith(".fmt") || uri.endsWith(".ftl") || content.startsWith("<#--")) {
                    TemplateManager tm = wsc.getTemplateManager();
                    tm.process(uri, content, request, response, out);
                } else {
                    out.print(content);
                }
            }
        } catch (FileNotFoundException e) {
            PrintWriter out = response.getWriter();
            ReplaceText[] rt = new ReplaceText[]{
                new ReplaceText("status", "HTTP Return Code 404"),
                new ReplaceText("description", e.getMessage())
            };
            out.print(ReplaceText.replaceText(errorMsg, rt));
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString().replaceAll("\n", "<br>\n");
            ReplaceText[] rt = new ReplaceText[]{
                new ReplaceText("status", "Internal Error: Type Exception"),
                new ReplaceText("description", stacktrace)
            };
            out.print(ReplaceText.replaceText(errorMsg, rt));
        } catch (Error e) {
            PrintWriter out = response.getWriter();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString().replaceAll("\n", "<br>\n");
            ReplaceText[] rt = new ReplaceText[]{
                new ReplaceText("status", "Internal Error: Type Error"),
                new ReplaceText("description", stacktrace)
            };
            out.print(ReplaceText.replaceText(errorMsg, rt));
        }
    }
    private static String initServletError = null;
    private static String[] ipAccessFilter;
    private final static String errorMsg =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
            "<html>" +
            "    <head>" +
            "        <title>Peter's Simple WebServer Error Report</title>" +
            "        <style type=\"text/css\">" +
            "            <!--" +
            "            body { background: #E1E4EB; padding: 0;font-family: Calibri, Arial, Helvetica, sans-serif; }" +
            "            .err {" +
            "                width:42em; border: 1px solid red;margin-top:.5em;margin-bottom:1em;padding:.5em;background-color:Beige;" +
            "            }" +
            "            .errmsg {" +
            "                background-color:BlanchedAlmond;border: 1px solid gray;font-size:smaller;overflow:scroll;margin-top:.3em;padding:.5em" +
            "            }" +
            "            //-->" +
            "        </style>" +
            "    </head>" +
            "    <body>" +
            "        <div class=\"err\">" +
            "            <b style=\"font-size:larger\">Web Server Status Report</b><br />" +
            "            <b><i>$status$</i></b>" +
            "            <div class=\"errmsg\">" +
            "                <code id=\"errorMsg\">$description$</code>" +
            "            </div>" +
            "        </div>" +
            "    </body>" +
            "</html>";
}

    
    
