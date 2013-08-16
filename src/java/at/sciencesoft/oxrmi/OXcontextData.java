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
package at.sciencesoft.oxrmi;

import at.sciencesoft.system.Config;
import at.sciencesoft.util.Crypto;
import at.sciencesoft.util.StringUtil;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to manage additional OX contextID informations in form of a property files. <br>
 * File name: <i>contextID.properties</i>
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXcontextData {

    /**
     * Deletes the property file
     * @param contextID OX contextID
     */
    public static void deleteOXcontextData(int contextID) {
        String path = Config.getInstance().getETCdir() + "context/" + contextID + ".properties";
        synchronized (sync) {
            new File(path).delete();
            dataCache.remove(contextID);
        }
    }

    /**
     * Saves the contextID data.
     * @param contextID OX contextID
     * @param contextName OX contextID name
     * @param contextAdmin OX contextID admin login
     * @param pwd password of the OX contextID admin
     * @param map additional informations
     * @return
     * @throws Exception
     */
    public static int saveOXcontextData(int contextID, String contextName, String contextAdmin, String pwd, HashMap<String, String> map) throws Exception {
        String dir = Config.getInstance().getETCdir() + "context/";
        File f = new File(dir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("OXcontext.saveOXadminAccountData(): Unable to create directory '" + f.getAbsolutePath() + "'");
            }
        }
        dir += contextID + ".properties";
        int userID;
        synchronized (sync) {
            f = new File(dir);
            Properties prop = new Properties();
            if (f.exists()) {
                FileInputStream fi = new FileInputStream(dir);
                prop.load(fi);
                fi.close();
            }

            User[] user = OXuser.list(contextID, new Credentials(contextAdmin, pwd), contextAdmin);
            if (user == null || user.length > 1) {
                throw new Exception("OXadminAccountData.saveOXadminAccountData(): Error getting OXadmin user ID");
            }
            userID = user[0].getId();
            prop.put("userID", "" + userID);
            prop.put("contextAdmin", contextAdmin);
            OXserverInfo oxsi = OXserverInfo.getInstance();
            prop.put("pwd", Crypto.encrypt(pwd, oxsi.getOXadmin().getPassword()));
            if (map != null) {
                prop.putAll(map);
            }
            FileOutputStream fos = new FileOutputStream(dir);
            prop.store(fos, " data for context ID: " + contextID);
            fos.close();
            dataCache.put(contextID, prop);
        }
        return userID;
    }

    /**
     * Changes the context admin and the password.
     * @param contextID contextID ID
     * @param contextAdmin contextID administrator
     * @param pwd password
     * @throws Exception
     */
    public static void changeOXadminAccountData(int contextID, String contextAdmin, String pwd) throws Exception {
        String path = Config.getInstance().getETCdir() + "context/" + contextID + ".properties";
        Properties prop = new Properties();
        synchronized (sync) {
            FileInputStream fi = new FileInputStream(path);
            prop.load(fi);
            fi.close();

            boolean change = false;
            if (contextAdmin != null && !contextAdmin.equals("")) {
                prop.put("contextAdmin", contextAdmin);
                change = true;
            }
            if (pwd != null && !pwd.equals("")) {
                OXserverInfo oxsi = OXserverInfo.getInstance();
                prop.put("pwd", Crypto.encrypt(pwd, oxsi.getOXadmin().getPassword()));
                change = true;
            }
            if (change) {
                FileOutputStream fos = new FileOutputStream(path);
                prop.store(fos, " data for context ID: " + contextID);
                fos.close();
                dataCache.put(contextID, prop);
            }
        }
    }

    /**
     * Changes context attributes.
     * @param contextID conetxtID
     * @param contextData additional context data
     * @throws Exception
     */
    public static void changeOXcontextData(int contextID, HashMap<String, String> contextData) throws Exception {
        int workingContextID = contextID;
        String path = Config.getInstance().getETCdir() + "context/" + contextID + ".properties";
        Properties prop = new Properties();
        synchronized (sync) {
            FileInputStream fi = new FileInputStream(path);
            prop.load(fi);
            fi.close();
            boolean change = false;
            Iterator<String> iter = contextData.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = (String) prop.get(key);
                if (value == null) {
                    change = true;
                    prop.put(key, contextData.get(key));
                } else {
                    if (!value.equals(contextData.get(key))) {
                        change = true;
                        prop.put(key, contextData.get(key));
                    }
                }
            }
            for (int i = 0; i < contextAttributes.length; ++i) {
                if (contextData.get(contextAttributes[i]) == null && prop.get(contextAttributes[i]) != null) {
                    change = true;
                    prop.remove(contextAttributes[i]);
                }
            }

            if (change) {
                FileOutputStream fos = new FileOutputStream(path);
                prop.store(fos, " data for context ID: " + workingContextID);
                fos.close();
                dataCache.put(contextID, prop);
            }
        }
    }

    /**
     * Load the property file, which contains the context data.
     * @param contextID contetxID
     * @return
     * @throws Exception
     */
    public static Properties loadOXcontextData(int contextID) throws Exception {
        if (dataCache.containsKey(contextID)) {
            synchronized (sync) {
                return dataCache.get(contextID);
            }
        }
        String path = Config.getInstance().getETCdir() + "context/" + contextID + ".properties";
        Properties prop = new Properties();
        synchronized (sync) {
            FileInputStream fi = new FileInputStream(path);
            prop.load(fi);
            fi.close();
            dataCache.put(contextID, prop);
        }

        return prop;
    }

    /**
     * Returns a list of context attributes.
     * @return
     */
    public static String[] getContextAttributes() {
        return contextAttributes;
    }

    /**
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static ParamMap getContextAttributes(HttpServletRequest request) throws Exception {
        ParamMap map = new ParamMap();
        map.putString(Param.LANGUAGE, request.getParameter("userLang").trim());
        map.putStringArray(Param.EMAIL_DOMAIN, StringUtil.splitTrim(request.getParameter("userEmailDomain")));
        map.putString(Param.IMAP_SERVER, request.getParameter("userIMAPserver").trim());
        map.putString(Param.SMTP_SERVER, request.getParameter("userSMTPserver").trim());
        map.putString(Param.COMPANY, request.getParameter("userCompany").trim());
        map.putString(Param.DEPARTMENT, request.getParameter("userDepartment").trim());
        map.putString(Param.TIME_ZONE, request.getParameter("userTimeZone").trim());
        String tmp = request.getParameter("mailQuota").trim();
        if (!tmp.equals("")) {
            map.putInt(Param.MAIL_QUOTA, Integer.parseInt(tmp));
        }
        tmp = request.getParameter("uploadSizeLimit").trim();
        if (!tmp.equals("")) {
            map.putInt(Param.UPLOAD_FILE_SIZE, Integer.parseInt(tmp));
        }
        tmp = request.getParameter("uploadSizeLimitPerFile").trim();
        if (!tmp.equals("")) {
            map.putInt(Param.UPLOAD_FILE_SIZE_PER_FILE, Integer.parseInt(tmp));
        }
        return map;
    }

    private final static Object sync = new Object();
    private final static HashMap<Integer, Properties> dataCache = new HashMap<Integer, Properties>();
    private final static String[] contextAttributes = {
        "userLang", "userEmailDomain", "userIMAPserver", "userSMTPserver",
        "userCompany", "userDepartment", "userTimeZone", "mailQuota",
        "uploadSizeLimit", "uploadSizeLimitPerFile", "pluginsupport"};
}
