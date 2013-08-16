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
import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.OXcontextIDinfo;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.oxrmi.UserAction;
import at.sciencesoft.plugin.ContextIface;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.util.StringUtil;
import at.sciencesoft.util.Timezone;
import at.sciencesoft.webserver.TemplateManager;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class CreateContext {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXcontextIDinfo idInfo = OXcontext.getNextFreeID();
        map.put("usedIDList", idInfo.getUsedID());
        if (!isPost) {
            map.put("contextID", "" + idInfo.getNextFreeID());
            String lang = Config.getInstance().getDefaultLang();
            if (lang != null) {
                map.put("lang", lang);
                map.put("userLang", lang);
            }
            String tzone = Config.getInstance().getDefaultTimezone();
            if (tzone != null) {
                int v = Timezone.getTimeZone(tzone);
                if (v != -1) {
                    map.put("timeZone", "" + v);
                    map.put("userTimeZone", "" + v);
                }
            }
            String imap = Config.getInstance().getDefaultIMAPserver();
            if (imap != null) {
                map.put("userIMAPserver", imap);
            }
            String smtp = Config.getInstance().getDefaultSMTPserver();
            if (smtp != null) {
                map.put("userSMTPserver", smtp);
            }

        } else {
            try {
                int contextID = Integer.parseInt(request.getParameter("contextID").trim());
                final String contextName = request.getParameter("contextName").trim();

                String mapping = request.getParameter("mapping").trim();
                String[] loginMapping = null;
                if (mapping != null && mapping.length() > 0) {
                    loginMapping = StringUtil.splitTrim(mapping);
                }
                long quota = Long.parseLong(request.getParameter("quota"));
                String accessCombination = request.getParameter("accessCombination").trim();
                String displayName = request.getParameter("displayName").trim();
                String firstName = request.getParameter("firstName").trim();
                String lastName = request.getParameter("lastName").trim();
                final String loginName = request.getParameter("loginName").trim();
                final String pwd = request.getParameter("pwd").trim();
                String email = request.getParameter("email").trim();
                String lang = request.getParameter("lang").trim();
                String timeZone = Timezone.getTimeZone(Integer.parseInt(request.getParameter("timeZone").trim()));
                boolean pluginsupport = request.getParameter("pluginsupport") != null ? true : false;

                // save context data
                final HashMap<String, String> userMap = new HashMap<String, String>();
                String[] userTemplate = OXcontextData.getContextAttributes();
                for (int i = 0; i < userTemplate.length; ++i) {
                    String tmp = request.getParameter(userTemplate[i]);
                    if (tmp != null) {
                        tmp = tmp.trim();
                        if (!tmp.equals("")) {
                            userMap.put(userTemplate[i], tmp);
                        }
                    }
                }
                ParamMap contextInfo = OXcontextData.getContextAttributes(request);

                // create context
                Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.CONTEXT);
                ResultSet[] rsArray = null;
                if (plist != null && pluginsupport) {
                    rsArray = new ResultSet[plist.length];
                    for (int i = 0; i < plist.length; ++i) {
                        ContextIface ci = plist[i].getContextIface();
                        ResultSet rs = ci.create(false, true, contextID, contextName, loginMapping, quota, accessCombination, loginName, displayName, firstName, lastName, pwd, email, lang, timeZone, contextInfo, null, null);
                        if (!rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                        rsArray[i] = rs;
                    }
                }
                Exception exception = null;
                OXcontext ctx = null;
                try {
                    ctx = OXcontext.create(contextID, contextName, loginMapping, quota, accessCombination, loginName, displayName,
                            firstName, lastName, pwd, email, lang, timeZone, contextInfo,
                            new UserAction() {

                                public void doJob(int contextID) throws Exception {
                                    OXcontextData.saveOXcontextData(contextID, contextName, loginName, pwd, userMap);
                                }
                            });
                } catch (Exception e) {
                    exception = e;
                }
                if (plist != null && pluginsupport) {
                    for (int i = 0; i < plist.length; ++i) {
                        ContextIface ci = plist[i].getContextIface();
                        ResultSet rs = ci.create(true, exception == null ? true : false, contextID, contextName, loginMapping, quota, accessCombination, loginName, displayName, firstName, lastName, pwd, email, lang, timeZone, contextInfo, rsArray[i], ctx);
                        if (exception == null && !rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                    }
                }
                if (exception != null) {
                     throw new Exception(exception);
                }
                
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=context");
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
                String[] userTemplate = OXcontextData.getContextAttributes();
                for (int i = 0; i < userTemplate.length; ++i) {
                    String v = request.getParameter(userTemplate[i]);
                    if (v != null && !v.trim().equals("")) {
                        map.put(userTemplate[i], v);
                    }
                }
            }
        }
        map.put("timeZones", Timezone.getTimeZones());
        map.put("accessCombinationList", Config.getInstance().getAccessCombination(null));
        map.put("supportedLang", Config.getInstance().getSupportOXLanguages());

        return map;
    }
    static private final String[] params = {"contextID", "contextName", "mapping", "quota", "displayName", "accessCombination",
        "firstName", "lastName", "loginName", "pwd", "pwdRetype", "email", "timeZone", "lang",
        "mailQuota", "uploadSizeLimit", "uploadSizeLimitPerFile", "pluginsupport"
    };
}
