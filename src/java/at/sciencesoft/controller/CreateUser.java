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
import com.openexchange.admin.rmi.dataobjects.User;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class CreateUser {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXworkingContext ctx = SessionHelper.getOXworkingContext(request, "workingContext");
        int actualContext = ctx.getID();
        Properties prop = OXcontextData.loadOXcontextData(actualContext);

        Plugin[] plist = null;
        if (ContextCache.isPluginSupport(actualContext)) {
            plist = PluginManager.getPulgin(Plugin.PLUGIN.USER);
        }


        if (!isPost) {

            String lang = prop.getProperty("userLang");
            if (lang == null) {
                lang = Config.getInstance().getDefaultLang();
            }

            if (lang != null) {
                map.put("lang", lang);

            }
            String tz = prop.getProperty("userTimeZone");
            if (tz != null) {
                map.put("timeZone", "" + tz);
            } else {
                String tzone = Config.getInstance().getDefaultTimezone();
                if (tzone != null) {
                    int v = Timezone.getTimeZone(tzone);
                    if (v != -1) {
                        map.put("timeZone", "" + v);
                    }
                }
            }
            map.put("forward", "" + 0);
            map.put("autoresponder", "" + 0);
            Calendar cal = Calendar.getInstance();
            map.put("year_start", "" + cal.get(Calendar.YEAR));
            map.put("year_end", "" + cal.get(Calendar.YEAR));
            map.put("month_start", "" + (cal.get(Calendar.MONTH) + 1));
            map.put("month_end", "" + (cal.get(Calendar.MONTH) + 1));
            map.put("day_start", "" + cal.get(Calendar.DAY_OF_MONTH));
            map.put("day_end", "" + cal.get(Calendar.DAY_OF_MONTH));
            
            // groups
            com.openexchange.admin.rmi.dataobjects.Group[] groups = OXgroup.listAll(actualContext, ContextCache.getContextCredentials(actualContext));
            if (groups != null && groups.length > 1) {
                com.openexchange.admin.rmi.dataobjects.Group[] grp = new com.openexchange.admin.rmi.dataobjects.Group[groups.length - 1];
                int index = 0;
                for (int i = 1; i < groups.length; ++i) {
                    grp[index++] = groups[i];
                }
                map.put("left", grp);
            }


        } else {
            try {
                String displayName = request.getParameter("displayName").trim();
                String firstName = request.getParameter("firstName").trim();
                String lastName = request.getParameter("lastName").trim();
                String login = request.getParameter("login").trim();
                String pwd = request.getParameter("pwd").trim();
                String imapLogin = request.getParameter("imapLogin").trim();
                String company = request.getParameter("company").trim();
                String lang = request.getParameter("lang");
                String timeZone = Timezone.getTimeZone(Integer.parseInt(request.getParameter("timeZone")));
                String middleName = request.getParameter("middleName").trim();
                String streetHome = request.getParameter("streetHome").trim();
                String postalCodeHome = request.getParameter("postalCodeHome").trim();
                String cityHome = request.getParameter("cityHome").trim();
                String stateHome = request.getParameter("stateHome").trim();

                String streetBusiness = request.getParameter("streetBusiness").trim();
                String postalCodeBusiness = request.getParameter("postalCodeBusiness").trim();
                String cityBusiness = request.getParameter("cityBusiness").trim();
                String stateBusiness = request.getParameter("stateBusiness").trim();
                String phoneBusiness = request.getParameter("phoneBusiness").trim();
                String faxBusiness = request.getParameter("faxBusiness").trim();
                String phoneHome = request.getParameter("phoneHome").trim();
                String mobile = request.getParameter("mobile").trim();

                String tmp = request.getParameter("emailAlias").trim();
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
                pmap.putString(Param.LANGUAGE, lang);
                pmap.putString(Param.TIME_ZONE, timeZone);
                if (!department.equals("")) {
                    pmap.putString(Param.DEPARTMENT, department);
                }
                if (!company.equals("")) {
                    pmap.putString(Param.COMPANY, company);
                }
                if (emailAlias != null) {
                    pmap.putStringArray(Param.EMAIL_ALIAS, emailAlias);
                }
                if (!imapLogin.equals("")) {
                    pmap.putString(Param.IMAP_LOGIN, imapLogin);
                }

                if (!smtpServer.equals("")) {
                    pmap.putString(Param.SMTP_SERVER, smtpServer);
                }
                if (!imapServer.equals("")) {
                    pmap.putString(Param.IMAP_SERVER, imapServer);
                }

                if (!middleName.equals("")) {
                    pmap.putString(Param.MIDDLE_NAME, middleName);
                }
                if (!streetHome.equals("")) {
                    pmap.putString(Param.STREET_HOME, streetHome);
                }
                if (!postalCodeHome.equals("")) {
                    pmap.putString(Param.POSTAL_CODE_HOME, postalCodeHome);
                }
                if (!cityHome.equals("")) {
                    pmap.putString(Param.CITY_HOME, cityHome);
                }
                if (!stateHome.equals("")) {
                    pmap.putString(Param.STATE_HOME, stateHome);
                }
                if (!streetBusiness.equals("")) {
                    pmap.putString(Param.STREET_BUSINESS, streetBusiness);
                }
                if (!postalCodeBusiness.equals("")) {
                    pmap.putString(Param.POSTAL_CODE_BUSINESS, postalCodeBusiness);
                }
                if (!cityBusiness.equals("")) {
                     pmap.putString(Param.CITY_BUSINESS, cityBusiness);
                }
                if (!stateBusiness.equals("")) {
                    pmap.putString(Param.STATE_BUSINESS, stateBusiness);
                }
                if (!phoneBusiness.equals("")) {
                    pmap.putString(Param.TELEPHONE_BUSINESS, phoneBusiness);
                }
                if (!faxBusiness.equals("")) {
                    pmap.putString(Param.FAX_BUSINESS, faxBusiness);
                }
                if (!phoneHome.equals("")) {
                     pmap.putString(Param.TELEPHONE_HOME, phoneHome);
                }
                if (!mobile.equals("")) {
                     pmap.putString(Param.MOBILE, mobile);
                }

                if (uploadFileSize > 0) {
                    pmap.putInt(Param.UPLOAD_FILE_SIZE, uploadFileSize);
                }

                if (uploadFileSizePerFile > 0) {
                    pmap.putInt(Param.UPLOAD_FILE_SIZE_PER_FILE, uploadFileSizePerFile);
                }

                pmap.putInt(Param.MAIL_QUOTA, mailQuota);
                tmp = request.getParameter("forward");
                if (tmp != null) {
                    pmap.putInt(Param.EMAIL_FORWRAD, Integer.parseInt(tmp));
                    if (!tmp.trim().equals("")) {
                        String[] list = StringUtil.splitTrim(request.getParameter("forwardList"));
                        pmap.putStringArray(Param.EMAIL_FORWRAD_LIST, list);
                    }
                }
                tmp = request.getParameter("autoresponder");
                if (tmp != null) {
                    pmap.putInt(Param.AUTORESPONDER, Integer.parseInt(tmp));
                    pmap.putString(Param.AUTORESPONDER_TEXT, request.getParameter("autoresponderText").trim());
                }
                if (request.getParameter("year_start") != null) {
                    pmap.putString(Param.INTERVALL, request.getParameter("year_start") + "/" + request.getParameter("month_start") + "/" + request.getParameter("day_start") + "-" +
                            request.getParameter("year_end") + "/" + request.getParameter("month_end") + "/" + request.getParameter("day_end"));
                }

                ResultSet[] rsArray = null;
                if (plist != null) {
                    rsArray = new ResultSet[plist.length];
                    for (int i = 0; i < plist.length; ++i) {
                        UserIface gi = plist[i].getUserIface();
                        ResultSet rs = gi.create(false, true, actualContext, ContextCache.getContextCredentials(actualContext), login, displayName, firstName, lastName, pwd, email, pmap, null, null);
                        if (!rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                        rsArray[i] = rs;
                    }
                }
                Exception exception = null;
                User user = null;
                try {
                    user = OXuser.create(actualContext, ContextCache.getContextCredentials(actualContext), login, displayName, firstName, lastName, pwd, email, pmap, null);
                } catch (Exception e) {
                    exception = e;
                }

                // groups
                if (exception == null) {
                    try {
                        tmp = request.getParameter("groups");
                        if (!tmp.equals("")) {
                            String[] groups = tmp.split(",");
                            ParamMap changes = new ParamMap();
                            changes.putStringArray(Param.ADD_USER, new String[]{"" + user.getId()});
                            for (int i = 0; i < groups.length; ++i) {
                                OXgroup.change(actualContext, ContextCache.getContextCredentials(actualContext), Integer.parseInt(groups[i]), changes);
                            }
                        }
                    } catch (Exception e) {
                        exception = e;
                    }
                }

                if (plist != null) {
                    for (int i = 0; i < plist.length; ++i) {
                        UserIface gi = plist[i].getUserIface();
                        ResultSet rs = gi.create(true, exception == null ? true : false, actualContext, ContextCache.getContextCredentials(actualContext), login, displayName, firstName, lastName, pwd, email, pmap, rsArray[i], user);
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
                map.put("error", StackTrace.toString(e));
                SessionHelper.setLastException(request, "lastException", e);
                for (int i = 0; i < params.length; ++i) {
                    String v = request.getParameter(params[i]);
                    if (v != null && !v.trim().equals("")) {
                        map.put(params[i], v);
                    }
                }

                // groups
                com.openexchange.admin.rmi.dataobjects.Group[] groups = OXgroup.listAll(actualContext, ContextCache.getContextCredentials(actualContext));
                if (groups != null && groups.length > 1) {
                    ArrayList<com.openexchange.admin.rmi.dataobjects.Group> left = new ArrayList<com.openexchange.admin.rmi.dataobjects.Group>();
                    ArrayList<com.openexchange.admin.rmi.dataobjects.Group> right = new ArrayList<com.openexchange.admin.rmi.dataobjects.Group>();

                    String tmp = request.getParameter("groups");
                    HashMap<Integer, Integer> selected = new HashMap<Integer, Integer>();
                    if (!tmp.equals("")) {
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

        if (prop.getProperty("uploadSizeLimit") != null) {
            map.put("uploadSizeLimit", prop.getProperty("uploadSizeLimit"));
        }

        if (prop.getProperty("uploadSizeLimitPerFile") != null) {
            map.put("uploadSizeLimitPerFile", prop.getProperty("uploadSizeLimitPerFile"));
        }

        if (prop.getProperty("mailQuota") != null) {
            map.put("mailQuota", prop.getProperty("mailQuota"));
        }

        map.put("timeZones", Timezone.getTimeZones());
        map.put("supportedLang", Config.getInstance().getSupportOXLanguages());

        map.put("yearIntervall", Config.getInstance().getYearIntervall());
        map.put("monthIntervall", Config.getInstance().getMonthIntervall());
        map.put("dayIntervall", Config.getInstance().getDayIntervall());
        map.put("minPasswordLen", Config.getInstance().getMinPasswordLen());
        map.put("showUserInGroup", Config.getInstance().getShowUserInGroup());

        if (plist != null) {
            for (int i = 0; i < plist.length; ++i) {
                for (UserIface.FIELD field : UserIface.FIELD.values()) {
                    if (plist[i].getUserIface().checkGUIflag(field)) {
                        map.put(field.name(), "true");
                    }
                }
            }
        }


        return map;
    }
    static private final String[] params = {"login", "displayName", "firstName", "lastName", "pwd", "pwdRetype",
        "timeZone", "lang", "company", "department", "department_str", "dep_show", "emailUser", "domain", "email_str", "email_show",
        "emailAlias", "imapLogin", "smtp", "imap", "smtp_show", "imap_show", "imap_str", "smtp_str", "uploadSizeLimit", "uploadSizeLimitPerFile",
        "mailQuota", "autoresponder", "autoresponderText", "forward", "forwardList", "emailForward", "year_start", "year_end", "month_start", "month_end",
        "day_start", "day_end", "middleName", "streetHome", "postalCodeHome", "cityHome", "stateHome", "streetBusiness", "postalCodeBusiness", "cityBusiness",
        "stateBusiness", "phoneBusiness", "faxBusiness", "phoneHome", "mobile"
    };
}
