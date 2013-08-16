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
package at.sciencesoft.controller;

import at.sciencesoft.oxrmi.OXcontextData;
import at.sciencesoft.oxrmi.ContextCache;
import at.sciencesoft.oxrmi.OXgroup;
import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.oxrmi.OXworkingContext;
import at.sciencesoft.oxrmi.Param;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.oxrmi.ReflectionModuleAccess;
import at.sciencesoft.oxrmi.ReflectionModuleAccessMapper;
import at.sciencesoft.oxrmi.UserAction;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.plugin.UserIface;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.util.StringUtil;
import at.sciencesoft.util.Timezone;
import at.sciencesoft.webserver.TemplateManager;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ModifyUser {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXworkingContext ctx = SessionHelper.getOXworkingContext(request, "workingContext");
        final int actualContext = ctx.getID();
        final int contextAdminID = ctx.getContextAdminUserID();
        int userID = Integer.parseInt(request.getParameter("userID"));

        Credentials auth = ContextCache.getContextCredentials(actualContext);
        Properties prop = OXcontextData.loadOXcontextData(actualContext);

        UserModuleAccess uma = OXuser.getModuleAccess(actualContext, auth, userID);
        if (uma == null) {
            throw new Exception("ModifyUser.process(): Error getting getModuleAccess() for user ID '" + userID + "'");
        }
        map.put("moduleAccess", new ReflectionModuleAccessMapper(uma));


        if (!isPost) {
            com.openexchange.admin.rmi.dataobjects.User user = OXuser.getUser(actualContext, auth, userID);
            if (user == null) {
                throw new Exception("ModifyUser.process(): Error getting user ID '" + userID + "'");
            }

            map.put("login", user.getName());
            map.put("displayName", user.getDisplay_name());
            map.put("firstName", user.getGiven_name());
            map.put("lastName", user.getSur_name());
            map.put("userID", user.getId());
            // map.put("pwd", user.getPassword());
            // map.put("pwdRetype", user.getPassword());
            map.put("lang", user.getLanguage());
            map.put("timeZone", "" + Timezone.getTimeZone(user.getTimezone()));
            map.put("company", user.getCompany());

            map.put("middleName", user.getMiddle_name());
            map.put("streetHome", user.getStreet_home());
            map.put("postalCodeHome", user.getPostal_code_home());
            map.put("cityHome", user.getCity_home());
            map.put("stateHome", user.getState_home());
            map.put("streetBusiness", user.getStreet_business());
            map.put("postalCodeBusiness", user.getPostal_code_business());
            map.put("cityBusiness", user.getCity_business());
            map.put("stateBusiness", user.getState_business());
            map.put("phoneBusiness", user.getTelephone_business1());
            map.put("phoneHome", user.getTelephone_home1());
            map.put("faxBusiness", user.getFax_business());
            map.put("mobile", user.getCellular_telephone1());


            String tmp;
            int limit = user.getUploadFileSizeLimit();
            if (limit > 0) {
                tmp = "" + (limit / (1024 * 1024));
            } else {
                tmp = "";
            }
            map.put("uploadSizeLimit", tmp);
            limit = user.getUploadFileSizeLimitPerFile();
            if (limit > 0) {
                tmp = "" + (limit / (1024 * 1024));
            } else {
                tmp = "";
            }
            map.put("uploadSizeLimitPerFile", tmp);

            tmp = user.getDepartment();

            if (prop.getProperty("userDepartment") != null) {
                String[] dlist = Config.getList(prop.getProperty("userDepartment"));
                map.put("departmentList", dlist);
                if (tmp != null) {
                    int i;
                    for (i = 0; i < dlist.length; ++i) {
                        if (dlist[i].equals(tmp)) {
                            map.put("dep_show", "dep_list");
                            map.put("department", tmp);
                            break;
                        }
                    }
                    if (i == dlist.length) {
                        map.put("dep_show", "dep_input");
                        map.put("department_str", tmp);
                    }
                }
            } else {
                map.put("department_str", tmp);
            }

            tmp = user.getImapServerString();
            // int imapPort = user.getImapPort();
            if (prop.getProperty("userIMAPserver") != null) {
                String[] ilist = Config.getList(prop.getProperty("userIMAPserver"));
                map.put("imapList", ilist);

                if (tmp != null) {
                    int i;
                    for (i = 0; i < ilist.length; ++i) {
                        if (ilist[i].equals(tmp)) {
                            map.put("imap_show", "imap_list");
                            map.put("imap", tmp);
                            break;
                        }
                    }
                    if (i == ilist.length) {
                        map.put("imap_show", "imap_input");
                        /*
                        if(imapPort != 143) {
                            tmp+= ":" + imapPort;
                        }
                        */
                        map.put("imap_str", tmp);
                    }
                }
            } else {
                map.put("imap_str", tmp);
            }

            HashSet<String> alias = user.getAliases();
            int index = 0;
            if (alias != null) {
                Iterator<String> iter = alias.iterator();
                StringBuffer buf = new StringBuffer();
                while (iter.hasNext()) {
                    tmp = iter.next();
                    if (tmp.equals(user.getPrimaryEmail())) {
                        continue;
                    }
                    if (index++ > 0) {
                        buf.append(", ");
                    }
                    buf.append(tmp);

                }
                if (buf.length() > 0) {
                    map.put("emailAlias", buf.toString());
                }
            }

            map.put("imapLogin", user.getImapLogin());

            tmp = user.getPrimaryEmail();
            if (prop.getProperty("userEmailDomain") != null) {
                String[] elist = Config.getList(prop.getProperty("userEmailDomain"));
                map.put("emailDomain", elist);
                int pos = tmp.indexOf("@");
                String _user = tmp.substring(0, pos);
                String domain = tmp.substring(pos + 1);
                if (tmp != null) {
                    int i;
                    for (i = 0; i < elist.length; ++i) {
                        if (elist[i].equals(domain)) {
                            map.put("email_show", "domain_list");
                            map.put("domain", domain);
                            map.put("emailUser", _user);
                            break;
                        }
                    }
                    if (i == elist.length) {
                        map.put("email_show", "email_input");
                        map.put("email_str", tmp);
                    }
                }
            } else {
                map.put("email_str", tmp);
            }

            tmp = user.getSmtpServerString();
            if (prop.getProperty("userSMTPserver") != null) {
                String[] slist = Config.getList(prop.getProperty("userSMTPserver"));
                map.put("smtpList", slist);
                if (tmp != null) {
                    int i;
                    for (i = 0; i < slist.length; ++i) {
                        if (slist[i].equals(tmp)) {
                            map.put("smtp_show", "smtp_list");
                            map.put("smtp", tmp);
                            break;
                        }
                    }
                    if (i == slist.length && tmp != null) {
                        map.put("smtp_show", "smtp_input");
                        map.put("smtp_str", tmp);
                    }
                }
            } else {
                map.put("smtp_str", tmp);
            }

            // groups
            com.openexchange.admin.rmi.dataobjects.Group[] groups = OXgroup.listAll(actualContext, ContextCache.getContextCredentials(actualContext));

            if (groups != null && groups.length > 1) {
                HashMap<Integer, com.openexchange.admin.rmi.dataobjects.Group> left = new HashMap<Integer, com.openexchange.admin.rmi.dataobjects.Group>();
                HashMap<Integer, com.openexchange.admin.rmi.dataobjects.Group> right = new HashMap<Integer, com.openexchange.admin.rmi.dataobjects.Group>();
                for (int i = 1; i < groups.length; ++i) {
                    left.put(groups[i].getId(), groups[i]);
                }
                com.openexchange.admin.rmi.dataobjects.Group[] member = OXgroup.listGroupsForUser(actualContext, user.getId(), ContextCache.getContextCredentials(actualContext));
                if (member != null) {
                    for (int i = 1; i < member.length; ++i) {
                        right.put(member[i].getId(), member[i]);
                        left.remove(member[i].getId());
                    }
                }
                if (left.size() > 0) {
                    map.put("left", left.values().toArray(new com.openexchange.admin.rmi.dataobjects.Group[left.size()]));
                }
                if (right.size() > 0) {
                    map.put("right", right.values().toArray(new com.openexchange.admin.rmi.dataobjects.Group[right.size()]));
                }
            }

            // plugins
            if (ContextCache.isPluginSupport(actualContext)) {
                Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.USER);
                if (plist != null) {
                    for (int i = 0; i < plist.length; ++i) {
                        plist[i].getUserIface().loadGUIdata(actualContext, userID, map);
                    }
                }
            }


        } else {
            if (prop.getProperty("userCompany") != null) {
                map.put("company", prop.getProperty("userCompany"));
            }
            if (prop.getProperty("userDepartment") != null) {
                map.put("departmentList", Config.getList(prop.getProperty("userDepartment")));
            }
            if (prop.getProperty("userEmailDomain") != null) {
                map.put("emailDomain", Config.getList(prop.getProperty("userEmailDomain")));
            }
            if (prop.getProperty("userSMTPserver") != null) {
                map.put("smtpList", Config.getList(prop.getProperty("userSMTPserver")));
            }
            if (prop.getProperty("userIMAPserver") != null) {
                map.put("imapList", Config.getList(prop.getProperty("userIMAPserver")));
            }
            try {
                final int _userID = Integer.parseInt(request.getParameter("userID"));
             
                String tmp;
                if (Config.getInstance().isUsernameChangeable()) {
                    tmp = request.getParameter("login").trim();
                } else {
                    tmp = request.getParameter("_login");
                }

                final String login = tmp;
                final String pwd = request.getParameter("pwd").trim();
                String imapLogin = request.getParameter("imapLogin").trim();
                String company = request.getParameter("company").trim();

                String timeZone = Timezone.getTimeZone(Integer.parseInt(request.getParameter("timeZone")));
                tmp = request.getParameter("emailAlias").trim();
                String[] emailAlias = null;
                if (!tmp.equals("")) {
                    emailAlias = StringUtil.splitTrim(tmp);
                }
                String department;
                tmp = request.getParameter("dep_show");
                if (tmp != null && (tmp.equals("dep_list") || tmp.equals(""))) {
                    department = request.getParameter("department");
                } else {
                    department = request.getParameter("department_str");
                }
                String email;
                tmp = request.getParameter("email_show");
                if (tmp != null && (tmp.equals("domain_list") || tmp.equals(""))) {
                    email = request.getParameter("emailUser") + "@" + request.getParameter("domain");
                } else {
                    email = request.getParameter("email_str").trim();
                }

                String smtpServer;
                tmp = request.getParameter("smtp_show");

                if (tmp != null && (tmp.equals("smtp_list") || tmp.equals(""))) {
                    smtpServer = request.getParameter("smtp");
                } else {
                    smtpServer = request.getParameter("smtp_str").trim();
                }
                String imapServer;
                tmp = request.getParameter("imap_show");
                if (tmp != null && (tmp.equals("imap_list") || tmp.equals(""))) {
                    imapServer = request.getParameter("imap");
                } else {
                    imapServer = request.getParameter("imap_str").trim();
                }
                int uploadFileSize = 0;
                int uploadFileSizePerFile = 0;
                int mailQuota = 0;
                tmp = request.getParameter("uploadSizeLimit").trim();
                if (!tmp.equals("")) {
                    uploadFileSize = Integer.parseInt(tmp);
                }
                tmp = request.getParameter("uploadSizeLimitPerFile").trim();
                if (!tmp.equals("")) {
                    uploadFileSizePerFile = Integer.parseInt(tmp);
                }
                tmp = request.getParameter("mailQuota");
                if (tmp != null && !tmp.equals("")) {
                    mailQuota = Integer.parseInt(tmp.trim());
                }

                ParamMap pmap = new ParamMap();
                ParamMap pluginMap = new ParamMap();

                pmap.putString(Param.DISPLAY_NAME, request.getParameter("displayName").trim());
                pmap.putString(Param.FIRST_NAME, request.getParameter("firstName").trim());
                pmap.putString(Param.LAST_NAME, request.getParameter("lastName").trim());
                pmap.putString(Param.LOGIN_NAME, login);
                pmap.putString(Param.PASSWORD, pwd);
                pmap.putString(Param.EMAIL, email);

                pmap.putString(Param.LANGUAGE, request.getParameter("lang"));
                pmap.putString(Param.TIME_ZONE, timeZone);
                pmap.putString(Param.DEPARTMENT, department);
                pmap.putString(Param.COMPANY, company);
                pmap.putStringArray(Param.EMAIL_ALIAS, emailAlias);
                pmap.putString(Param.IMAP_LOGIN, imapLogin);
                pmap.putString(Param.SMTP_SERVER, smtpServer);
                pmap.putString(Param.IMAP_SERVER, imapServer);
                pmap.putInt(Param.UPLOAD_FILE_SIZE, uploadFileSize);
                pmap.putInt(Param.UPLOAD_FILE_SIZE_PER_FILE, uploadFileSizePerFile);
                pmap.putString(Param.MIDDLE_NAME, request.getParameter("middleName").trim());
                pmap.putString(Param.STREET_HOME, request.getParameter("streetHome").trim());
                pmap.putString(Param.POSTAL_CODE_HOME, request.getParameter("postalCodeHome").trim());
                pmap.putString(Param.CITY_HOME, request.getParameter("cityHome").trim());
                pmap.putString(Param.STATE_HOME, request.getParameter("stateHome").trim());
                pmap.putString(Param.STREET_BUSINESS, request.getParameter("streetBusiness").trim());
                pmap.putString(Param.POSTAL_CODE_BUSINESS, request.getParameter("postalCodeBusiness").trim());
                pmap.putString(Param.CITY_BUSINESS, request.getParameter("cityBusiness").trim());
                pmap.putString(Param.STATE_BUSINESS, request.getParameter("stateBusiness").trim());
                pmap.putString(Param.TELEPHONE_BUSINESS, request.getParameter("phoneBusiness").trim());
                pmap.putString(Param.FAX_BUSINESS, request.getParameter("faxBusiness").trim());
                pmap.putString(Param.TELEPHONE_HOME, request.getParameter("phoneHome").trim());
                pmap.putString(Param.MOBILE, request.getParameter("mobile").trim());

                // pluin parameter
                pluginMap.putInt(Param.MAIL_QUOTA, mailQuota);
                tmp = request.getParameter("forward");
                if (tmp != null) {
                    pluginMap.putInt(Param.EMAIL_FORWRAD, Integer.parseInt(tmp));
                    if (!tmp.trim().equals("")) {
                        String[] list = StringUtil.splitTrim(request.getParameter("forwardList"));
                        pluginMap.putStringArray(Param.EMAIL_FORWRAD_LIST, list);
                    }
                }
                tmp = request.getParameter("autoresponder");
                if (tmp != null) {
                    pluginMap.putInt(Param.AUTORESPONDER, Integer.parseInt(tmp));
                    pluginMap.putString(Param.AUTORESPONDER_TEXT, request.getParameter("autoresponderText").trim());
                }
                pluginMap.putString(Param.INTERVALL, request.getParameter("year_start") + "/" + request.getParameter("month_start") + "/" + request.getParameter("day_start") + "-" +
                        request.getParameter("year_end") + "/" + request.getParameter("month_end") + "/" + request.getParameter("day_end"));


                // change user
                Plugin[] plist = null;
                if (ContextCache.isPluginSupport(actualContext)) {
                    plist = PluginManager.getPulgin(Plugin.PLUGIN.USER);
                }
                ResultSet[] rsArray = null;
                ParamMap changes = OXuser.getChanges(actualContext, ContextCache.getContextCredentials(actualContext), _userID, pmap);
                if (plist != null) {
                    rsArray = new ResultSet[plist.length];
                    for (int j = 0; j < plist.length; ++j) {
                        UserIface ui = plist[j].getUserIface();
                        ResultSet rs = ui.change(false, true, actualContext, ContextCache.getContextCredentials(actualContext), _userID, changes, pluginMap, null);
                        if (!rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                        rsArray[j] = rs;
                    }
                }
                Exception exception = null;
                if (changes != null) {
                    try {
                        OXuser.change(actualContext, ContextCache.getContextCredentials(actualContext), _userID, changes, new UserAction() {

                            public void doJob(int contextID) throws Exception {
                                if (_userID == contextAdminID) {
                                    Credentials auth = ContextCache.getContextCredentials(actualContext);
                                    boolean change = false;
                                    if (!pwd.equals("") && !pwd.equals(auth.getPassword())) {
                                        change = true;
                                    }
                                    if (!login.equals(auth.getLogin())) {
                                        change = true;
                                    }
                                    if (change) {
                                        OXcontextData.changeOXadminAccountData(actualContext, login, pwd);
                                        ContextCache.removeContextCredentials(actualContext);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        exception = e;
                    }
                }
                // change module access
                if (exception == null) {
                    try {
                        HashMap<String, String> accessMap = new HashMap<String, String>();
                        ReflectionModuleAccess[] list = OXuser.getModuleAccessMethods();
                        for (int i = 0; i < list.length; ++i) {
                            tmp = request.getParameter(list[i].methodName);
                            if (tmp != null) {
                                accessMap.put(list[i].methodName, request.getParameter(list[i].methodName));
                            }
                        }
                        OXuser.changeModuleAccess(actualContext, auth, userID, accessMap);
                    } catch (Exception e) {
                        exception = e;
                    }
                }
                if (exception == null) {
                    try {
                        tmp = request.getParameter("groups");

                        if (!tmp.equals("*")) {
                            com.openexchange.admin.rmi.dataobjects.Group[] member = OXgroup.listGroupsForUser(actualContext, _userID, ContextCache.getContextCredentials(actualContext));
                            if (member != null) {
                                HashMap<Integer, com.openexchange.admin.rmi.dataobjects.Group> mmap = new HashMap<Integer, com.openexchange.admin.rmi.dataobjects.Group>();
                                for (int i = 1; i < member.length; ++i) {
                                    mmap.put(member[i].getId(), member[i]);
                                }
                                if (!tmp.equals("")) {
                                    String list[] = tmp.split(",");
                                    for (int i = 0; i < list.length; ++i) {
                                        int grpID = Integer.parseInt(list[i]);
                                        if (mmap.containsKey(grpID)) {
                                            mmap.remove(grpID);
                                        } else {
                                            ParamMap grpChanges = new ParamMap();
                                            grpChanges.putStringArray(Param.ADD_USER, new String[]{"" + _userID});
                                            OXgroup.change(actualContext, ContextCache.getContextCredentials(actualContext), grpID, grpChanges);
                                        }
                                    }
                                }
                                // remove
                                if (mmap.size() > 0) {

                                    Iterator<com.openexchange.admin.rmi.dataobjects.Group> iter = mmap.values().iterator();
                                    while (iter.hasNext()) {
                                        ParamMap grpChanges = new ParamMap();
                                        grpChanges.putStringArray(Param.REMOVE_USER, new String[]{"" + _userID});
                                        OXgroup.change(actualContext, ContextCache.getContextCredentials(actualContext), iter.next().getId(), grpChanges);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        exception = e;
                    }
                }
                if (plist != null) {
                    for (int j = 0; j < plist.length; ++j) {
                        UserIface ui = plist[j].getUserIface();
                        ResultSet rs = ui.change(true, exception == null ? true : false, actualContext, ContextCache.getContextCredentials(actualContext), _userID, changes, pluginMap, rsArray[j]);
                        if (exception == null && !rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                    }
                }
                if (exception != null) {
                    throw exception;
                }
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=user");
                TemplateManager.setRedirectFlag(map);
            } catch (Exception e) {
                // fill page with actual values
                map.put("error", StackTrace.toString(e));
                SessionHelper.setLastException(request, "lastException", e);
                for (int i = 0; i < params.length; ++i) {
                    String v = request.getParameter(params[i]);
                    if (v != null && !v.trim().equals("")) {
                        map.put(params[i], v);
                    }
                }
                String tmp;
                if (Config.getInstance().isUsernameChangeable()) {
                    tmp = request.getParameter("login").trim();
                } else {
                    tmp = request.getParameter("_login");
                }
                map.put("login", tmp);

                // fill  access module checkboxes
                ReflectionModuleAccess[] list = OXuser.getModuleAccessMethods();
                for (int i = 0; i < list.length; ++i) {
                    tmp = request.getParameter(list[i].methodName);
                    if (tmp != null) {
                        map.put(list[i].methodName, "on");
                    }
                }

                // groups
                com.openexchange.admin.rmi.dataobjects.Group[] groups = OXgroup.listAll(actualContext, ContextCache.getContextCredentials(actualContext));
                if (groups != null && groups.length > 1) {
                    ArrayList<com.openexchange.admin.rmi.dataobjects.Group> left = new ArrayList<com.openexchange.admin.rmi.dataobjects.Group>();
                    ArrayList<com.openexchange.admin.rmi.dataobjects.Group> right = new ArrayList<com.openexchange.admin.rmi.dataobjects.Group>();

                    tmp = request.getParameter("groups");
                    HashMap<Integer, Integer> selected = new HashMap<Integer, Integer>();
                    if (!tmp.equals("*")) {
                        String[] sel = tmp.split(",");
                        for (int i = 0; i < sel.length; ++i) {
                            int id = Integer.parseInt(sel[i]);
                            selected.put(id, id);
                        }
                    }
                    StringBuffer members = new StringBuffer();
                    for (int i = 1; i < groups.length; ++i) {
                        if (selected.containsKey(groups[i].getId())) {
                            right.add(groups[i]);
                            if (members.length() > 0) {
                                members.append(",");
                            }
                            members.append(groups[i].getId());
                        } else {
                            left.add(groups[i]);
                        }
                    }
                    if (left.size() > 0) {
                        map.put("left", left);
                    }
                    if (right.size() > 0) {
                        map.put("right", right);
                    }
                    map.put("groups", members.toString());
                }


            }

        }
        map.put("timeZones", Timezone.getTimeZones());

        map.put("supportedLang", Config.getInstance().getSupportOXLanguages());
        map.put("loginChangeable", "" + Config.getInstance().isUsernameChangeable());

        map.put("timeZones", Timezone.getTimeZones());
        map.put("supportedLang", Config.getInstance().getSupportOXLanguages());

        map.put("yearIntervall", Config.getInstance().getYearIntervall());
        map.put("monthIntervall", Config.getInstance().getMonthIntervall());
        map.put("dayIntervall", Config.getInstance().getDayIntervall());
        map.put("minPasswordLen", Config.getInstance().getMinPasswordLen());

        // plugins
        if (ContextCache.isPluginSupport(actualContext)) {
            Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.USER);
            if (plist != null) {
                for (int i = 0; i < plist.length; ++i) {
                    for (UserIface.FIELD field : UserIface.FIELD.values()) {
                        if (plist[i].getUserIface().checkGUIflag(field)) {
                            map.put(field.name(), "true");
                        }
                    }
                }
            }
        }

        map.put("moduleAccessMethods", OXuser.getModuleAccessMethods());

        return map;
    }
    static private final String[] params = {"displayName", "firstName", "lastName", "pwd", "pwdRetype",
        "timeZone", "lang", "company", "department", "department_str", "dep_show", "emailUser", "domain", "email_str", "email_show", "userID",
        "emailAlias", "imapLogin", "smtp", "imap", "smtp_show", "imap_show", "imap_str", "smtp_str", "userID", "uploadSizeLimit", "uploadSizeLimitPerFile",
        "mailQuota", "autoresponder", "autoresponderText", "forward", "forwardList", "emailForward", "year_start", "year_end", "month_start", "month_end",
        "day_start", "day_end"
    };
}
