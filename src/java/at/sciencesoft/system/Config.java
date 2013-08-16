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
package at.sciencesoft.system;

import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.util.StringUtil;
import at.sciencesoft.util.text.Text;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.Stack;

/**
 * 
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Config {

    private Config() {
    }

    public static String getTempDir() throws Exception {
        String tmpDir = Config.checkPathLimiter(System.getProperty("java.io.tmpdir"));
        tmpDir += "psoxgui/";
        File f = new File(tmpDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("Config.getTempDir(): Unable to create tmp dir: '" + tmpDir + "'");
            }
        }
        return tmpDir;
    }

    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public static void init() {
    }

    public void init(String path) throws IOException, Exception {
        // Read properties file.
        prop = new Properties();
        prop.load(new FileInputStream(path));
        // check values
        for (int i = 0; i < elements.length; ++i) {
            String tmp = getProperty(elements[i]);
            if (tmp == null) {
                throw new Exception("Config.init(): Missing property " + elements[i]);
            }
        }
        defaultTimezone = getProperty("defaultTimezone");
        defaultLang = getProperty("defaultLang");
        defaultIMAPserver = getProperty("defaultIMAPserver");
        defaultSMTPserver = getProperty("defaultSMTPserver");
        urlBase = getProperty("urlBase");
        ipAccessFilter = getProperty("IPaccessFilter");
        etcDir = checkPathLimiter(getProperty("etcDir"));
        rmiHost = checkPathLimiter(getProperty("rmiHost"));
        String tmp = getProperty("yearIntervall");
        yearIntervall = new int[2];
        if(tmp == null) {
             yearIntervall[0] = 2010;
             yearIntervall[1] = 2012;
        } else {
            String[] list = tmp.split("-");
            yearIntervall[0] = Integer.parseInt(list[0].trim());
            yearIntervall[1] = Integer.parseInt(list[1].trim());
        }
        CTXamdinLogin = Boolean.parseBoolean(getProperty("CTXamdinLogin","false"));

        // collect some enviroment vars
        osName = System.getProperty("os.name");

        if (osName.toLowerCase().indexOf("windows") >= 0) {
            isWindows = true;
        } else {
            isWindows = false;
        }

        tmp = getProperty("defaultOXguiLang");
        if (tmp != null) {
            tmp = tmp.substring(0, 2).toLowerCase();
            defaultOXguiLang = tmp;
        } else {
            defaultOXguiLang = "de";
        }

        isUsernameChangeable = false;
        if (!isWindows) {
            oxUserProp = new Properties();
            oxUserProp.load(new FileInputStream(openxchangeDir + "etc/AdminUser.properties"));
            tmp = oxUserProp.getProperty("USERNAME_CHANGEABLE");
            if (tmp != null && tmp.toLowerCase().equals("true")) {
              isUsernameChangeable = true;
            } 
        }

        tmp = getProperty("disableContextDelete");

        if(tmp != null &&  tmp.toLowerCase().equals("true")) {
            disableContextDelete  = true;
        } else {
            disableContextDelete  = false;
        }

        tmp = getProperty("minPasswordLen");
        if(tmp != null) {
            minPasswordLen = Integer.parseInt(tmp);
        } else {
            minPasswordLen = 5;
        }

        tmp = getProperty("showUserInGroup");
        if(tmp != null) {
            showUserInGroup = Integer.parseInt(tmp) % 3;
        }

        tmp = getProperty("locale");
        if(tmp == null) {
            if(defaultOXguiLang.equals("de")) {
                collator =  Collator.getInstance(Locale.GERMAN);
            } else if(defaultOXguiLang.equals("en")) {
                collator =  Collator.getInstance(Locale.ENGLISH);
            } else if(defaultOXguiLang.equals("it")) {
               collator =  Collator.getInstance(Locale.ITALIAN);
            } else if(defaultOXguiLang.equals("fr")) {
               collator =  Collator.getInstance(Locale.FRENCH);
            } else if(defaultOXguiLang.equals("es")) {
                collator =  Collator.getInstance(new Locale("es"));
            } else if(defaultOXguiLang.equals("nl")) {
                collator =  Collator.getInstance(new Locale("nl"));
            } else {
                // OS/enviroment value
               collator =   Collator.getInstance(new Locale(tmp));
            }
        } else {
            tmp = tmp.toLowerCase();
            if(tmp.equals("de")) {
                collator =  Collator.getInstance(Locale.GERMAN);
            } else if(tmp.equals("en")) {
                collator =  Collator.getInstance(Locale.ENGLISH);
            } else if(tmp.equals("it")) {
               collator =  Collator.getInstance(Locale.ITALIAN);
            } else if(tmp.equals("fr")) {
               collator =  Collator.getInstance(Locale.FRANCE);
            } else {
               collator =  Collator.getInstance(new Locale(tmp));
            }
        }

        tmp = getProperty("userCacheLiveTime");
        if(tmp != null) {
            OXuser.setCacheLiveTime(Integer.parseInt(tmp));
        }

        tmp = getProperty("displayUserLimit");
        if(tmp != null) {
           displayUserLimit = Integer.parseInt(tmp);
        }

        // plugins
        PluginManager.loadAll();

    }

    public int getShowUserInGroup() {
        return showUserInGroup;
    }

    public boolean isDisableContextDelete() {
        return disableContextDelete;
    }

    public boolean allowCTXamdinLogin() {
        return CTXamdinLogin;
    }

    public String getDefaultOXguiLang() {
        return defaultOXguiLang;
    }

    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    public String getRMIhost() {
        return rmiHost;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public String getDefaultIMAPserver() {
        return defaultIMAPserver;
    }

    public String getURLbase() {
        return urlBase;
    }

    public String getDefaultSMTPserver() {
        return defaultSMTPserver;
    }

    public String getETCdir() {
        return etcDir;
    }

    public int[] getYearIntervall() {
        return yearIntervall;
    }

    public int[] getMonthIntervall() {
         if(monthIntervall == null) {
            monthIntervall = new int[12];
            for(int i = 0; i < 12; ++i) {
                monthIntervall[i] = i + 1;
            }
         }
         return monthIntervall;
    }

    public int[] getDayIntervall() {
         if(dayIntervall == null) {
            dayIntervall = new int[31];
            for(int i = 0; i < 31; ++i) {
                dayIntervall[i] = i + 1;
            }
         }
         return dayIntervall;
    }

    private String getProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String getProperty(String key, String defaultValue) {
        String value = prop.getProperty(key, defaultValue);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public ArrayList readPropetyFiles(String startDir) {
        ArrayList<String> al = new ArrayList<String>();
        Stack<File> s = new Stack<File>();
        s.push(new File(startDir));
        while (!s.isEmpty()) {
            File dir = s.pop();
            File[] list = dir.listFiles();
            if (list == null) {
                continue;
            }
            for (int i = 0; i < list.length; ++i) {
                if (list[i].isDirectory()) {
                    s.push(list[i]);
                } else {
                    if (list[i].getAbsolutePath().endsWith(".properties")) {
                        al.add(list[i].getAbsolutePath());
                    }
                }
            }
        }
        return al;
    }

    public String[] getLogFiles() {
        ArrayList<String> al = new ArrayList<String>();
        al.add("/var/log/open-xchange/open-xchange.log.0");
        al.add("/var/log/open-xchange/open-xchange-admin.log.0");
        return al.toArray(new String[0]);
    }

    public String[] getSystemFiles() {
        ArrayList<String> al;
        if (isWindows) {
            al = readPropetyFiles("c:/temp/etc");
        } else {
            al = readPropetyFiles("/opt/open-xchange/etc");
        }
        if (al.isEmpty()) {
            return null;
        }
        String[] list = al.toArray(new String[0]);
        Arrays.sort(list);
        return list;
    }

    public String[] getAccessCombination(String actualAccessCombination) {
        if (moduleAccessRights != null) {
            if (actualAccessCombination != null) {
                if (!rightsMap.containsValue(actualAccessCombination)) {
                    ArrayList<String> al = new ArrayList<String>();
                    al.add(actualAccessCombination);
                    for (int i = 0; i < moduleAccessRights.length; ++i) {
                        al.add(moduleAccessRights[i]);
                    }
                    return al.toArray(new String[0]);
                }
            }
            return moduleAccessRights;
        }
        String rights = getProperty("accessCombination");
        if (rights == null) {
            return null;
        }
        moduleAccessRights = StringUtil.splitTrim(rights);
        rightsMap = new HashMap<String, String>();
        for (int i = 0; i < moduleAccessRights.length; ++i) {
            rightsMap.put(moduleAccessRights[i], moduleAccessRights[i]);
        }
        return moduleAccessRights;
    }

    public int getMinPasswordLen() {
        return minPasswordLen;
    }

    public int getDisplayUserLimit() {
        return displayUserLimit;
    }

    public SupportedOXLang[] getSupportOXLanguages() {
        if (supportedOXLang != null) {
            return supportedOXLang;
        }
        String lang = getProperty("supportedOXLang");
        if (lang == null) {
            return null;
        }
        String[] tmp = lang.split(",");
        supportedOXLang = new SupportedOXLang[tmp.length];
        for (int i = 0; i < tmp.length; ++i) {
            String[] t = tmp[i].split("\\|");
            supportedOXLang[i] = new SupportedOXLang(t[0].trim(), t[1].trim());
        }
        return supportedOXLang;
    }

    public static String[] getList(String s) {
        if (s == null || s.trim().equals("")) {
            return null;
        }
        String[] tmp = s.split(",");
        String[] list = new String[tmp.length];
        for (int i = 0; i < tmp.length; ++i) {
            list[i] = tmp[i];
        }
        return list;
    }

    public void setText(Text text) {
        content = text;
    }

    public Text getText() {
        return content;
    }

    public String getVersion() {
        return "0.1.17";
    }

    public String getBuildDate() {
        return "2012/10/19";
    }

    public String getIPaccessFilter() {
        return ipAccessFilter;
    }

    public boolean isUsernameChangeable() {
        return isUsernameChangeable;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public Collator getCollator() {
        return collator;
    }

    static public String checkPathLimiter(String path) {
        if (path.endsWith("\\") || path.endsWith("/")) {
            return path;
        }
        return path + '/';
    }

    static public String getOpenOXdir() {
        return openxchangeDir;
    }
    private Text content;
    private static Config instance;
    private Properties prop;
    private Properties oxUserProp;
    private String[] moduleAccessRights;
    private HashMap<String, String> rightsMap;
    private SupportedOXLang[] supportedOXLang;
    private int minPasswordLen;
    private String defaultOXguiLang;
    private String defaultTimezone;
    private String defaultLang;
    private String defaultIMAPserver;
    private String defaultSMTPserver;
    private String urlBase;
    private String etcDir;
    private String rmiHost;
    private String ipAccessFilter;
    private String osName;
    private int[] yearIntervall;
    private int[] monthIntervall;
    private int[] dayIntervall;
    private boolean isWindows;
    private boolean isUsernameChangeable;
    private boolean CTXamdinLogin;
    private boolean disableContextDelete;
    private Collator collator;
    private int showUserInGroup;
    private int displayUserLimit;
    private final String[] elements = {"defaultLang", "defaultTimezone", "defaultIMAPserver", "defaultSMTPserver", "urlBase", "etcDir", "rmiHost", "accessCombination"};
    private static final String openxchangeDir = "/opt/open-xchange/";
}
