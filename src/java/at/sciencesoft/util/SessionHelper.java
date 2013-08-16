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
package at.sciencesoft.util;

import at.sciencesoft.oxrmi.OXworkingContext;
import at.sciencesoft.system.Config;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class SessionHelper {

    public static String setLang(HttpServletRequest request,String langStrName) {
        HttpSession sess = request.getSession();
        String lang = request.getParameter(langStrName);
        if (lang != null) {
            sess.setAttribute(langStrName, lang);
        } else {
            lang = (String) sess.getAttribute(langStrName);
            if (lang == null) {
                lang = Config.getInstance().getDefaultOXguiLang();
                sess.setAttribute(langStrName, lang);

            }
        }
        return lang;
    }

    public static boolean isLoggedIn(HttpServletRequest request,String loginStrName) {
        HttpSession sess = request.getSession();
        String tmp = (String) sess.getAttribute(loginStrName);
        if (tmp != null && tmp.equals("true")) {
            return true;
        }
        return false;
    }

    public static void setLoggedIn(HttpServletRequest request,String loginStrName) {
        HttpSession sess = request.getSession();
        sess.setAttribute(loginStrName, "true");
    }

    public static void setString(HttpServletRequest request,String attrib,String value) {
        HttpSession sess = request.getSession();
        sess.setAttribute(attrib,value);
    }

    public static String getString(HttpServletRequest request,String attrib) {
        HttpSession sess = request.getSession();
        return (String)sess.getAttribute(attrib);
    }

    public static boolean isOXAdmin(HttpServletRequest request,String admin) {
        HttpSession sess = request.getSession();
        String tmp = (String) sess.getAttribute(admin);
        if (tmp != null && tmp.equals("true")) {
            return true;
        }
        return false;
    }

     public static void setOXAdmin(HttpServletRequest request,String admin) {
        HttpSession sess = request.getSession();
        sess.setAttribute(admin, "true");
    }

    public static void setOXworkingContext(HttpServletRequest request,String workingContextStrName, int i, String name, int userID) {
        HttpSession sess = request.getSession();
        sess.setAttribute(workingContextStrName, new OXworkingContext(i, name, userID));
    }

    public static void setLastException(HttpServletRequest request,String lastExceptionStrName, Exception e) {
        HttpSession sess = request.getSession();
        sess.setAttribute(lastExceptionStrName, e);
    }

    public static void setSystemFiles(HttpServletRequest request,String sysFileListStrName, String[] list) {
        HttpSession sess = request.getSession();
        sess.setAttribute(sysFileListStrName, list);
    }

     public static void setLastMessage(HttpServletRequest request,String msgStrName, String msg) {
        HttpSession sess = request.getSession();
        sess.setAttribute(msgStrName, msg);
    }

    public static Exception getLastException(HttpServletRequest request,String lastExceptionStrName) {
        HttpSession sess = request.getSession();
        return (Exception) sess.getAttribute(lastExceptionStrName);
    }

    public static String[] getSystemFiles(HttpServletRequest request,String sysFileListStrName) {
        HttpSession sess = request.getSession();
        return (String[])sess.getAttribute(sysFileListStrName);
    }

     public static String getLastMessage(HttpServletRequest request,String msgStrName) {
        HttpSession sess = request.getSession();
        return (String) sess.getAttribute(msgStrName);
    }

    public static void resetOXworkingContext(HttpServletRequest request,String workingContextStrName) {
        HttpSession sess = request.getSession();
        sess.removeAttribute(workingContextStrName);
    }

    public static OXworkingContext getOXworkingContext(HttpServletRequest request,String workingContextStrName) {
        HttpSession sess = request.getSession();
        return (OXworkingContext) sess.getAttribute(workingContextStrName);

    }

    public static void invalidateSession(HttpServletRequest request) {
        HttpSession sess = request.getSession(false);
        if (sess != null) {
            // sess.invalidate() throws an error;
            // see https://bugs.open-xchange.com/show_bug.cgi?id=14466

            Enumeration e = sess.getAttributeNames();
            ArrayList<String> al = new ArrayList<String>();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                al.add(name);
            }
            for (int i = 0; i < al.size(); ++i) {
                sess.removeAttribute(al.get(i));
            }

        }
    }

    public static void invalidateSession(HttpServletRequest request, String[] doNotRemove) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (doNotRemove != null) {
            for (int i = 0; i < doNotRemove.length; ++i) {
                map.put(doNotRemove[i], doNotRemove[i]);
            }
        }

        HttpSession sess = request.getSession(false);
        if (sess != null) {
            // sess.invalidate() throws an error;
            // see https://bugs.open-xchange.com/show_bug.cgi?id=14466

            Enumeration e = sess.getAttributeNames();
            ArrayList<String> al = new ArrayList<String>();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                al.add(name);
            }
            for (int i = 0; i < al.size(); ++i) {
                if(map.containsKey(al.get(i))) {
                    continue;
                }
                sess.removeAttribute(al.get(i));
            }

        }
    }
}
