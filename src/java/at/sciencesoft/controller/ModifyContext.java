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

import at.sciencesoft.oxrmi.ContextCache;
import at.sciencesoft.oxrmi.OXcontextData;
import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.OXcontextIDinfo;
import at.sciencesoft.oxrmi.OXworkingContext;
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
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ModifyContext {

    static public HashMap process(boolean isPost, final HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXcontextIDinfo idInfo = OXcontext.getNextFreeID();
        map.put("usedIDList", idInfo.getUsedID());
        map.put("timeZones", Timezone.getTimeZones());
        map.put("supportedLang", Config.getInstance().getSupportOXLanguages());
        String accessCombination = null;
        int contextID = Integer.parseInt(request.getParameter("contextID"));

        Plugin[] plist = null;
        if (ContextCache.isPluginSupport(contextID)) {
            plist = PluginManager.getPulgin(Plugin.PLUGIN.CONTEXT);
        }

        if (!isPost) {
            OXcontext context = OXcontext.getContext(contextID);
            if (context == null) {
                throw new Exception("ModifyContext.process(): Error getting context ID '" + contextID + "'");
            }
            map.put("contextID", contextID);
            map.put("contextName", context.getName());
            map.put("quota", "" + context.getMaxQuota());
            map.put("usedQuota", "" + context.getUsedQuota());
            accessCombination = context.getAccessCombinationName();
            map.put("accessCombination", accessCombination);

            map.put("mapping", context.getLoginMappingsWithoutContextName());
            Properties prop = OXcontextData.loadOXcontextData(context.getID());
            String[] userTemplate = OXcontextData.getContextAttributes();
            for (int i = 0; i < userTemplate.length; ++i) {
                String tmp = prop.getProperty(userTemplate[i]);
                if (tmp != null) {
                    tmp = tmp.trim();
                    if (!tmp.equals("")) {
                        map.put(userTemplate[i], tmp);
                    }
                }
            }
            if (plist != null) {
                for (int i = 0; i < plist.length; ++i) {
                    ContextIface ci = plist[i].getContextIface();
                    for (ContextIface.FIELD field : ContextIface.FIELD.values()) {
                        if (plist[i].getContextIface().checkGUIflag(field)) {
                            map.put(field.name(), "true");
                        }
                    }
                }
            }
        } else {
            try {

                final String contextName = request.getParameter("contextName");
                String[] mapping = StringUtil.splitTrim(request.getParameter("mapping"));;
                long quota = Long.parseLong(request.getParameter("quota").trim());
                accessCombination = request.getParameter("accessCombination");
                String downgrade = request.getParameter("downgrade");

               
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

                ResultSet[] rsArray = null;
                boolean _downgrade = downgrade != null ? true : false;

                ParamMap changes = OXcontext.getChanges(contextID, contextName, mapping, quota, accessCombination);
                if (plist != null) {
                    rsArray = new ResultSet[plist.length];
                    for (int i = 0; i < plist.length; ++i) {
                        ContextIface ci = plist[i].getContextIface();
                        ResultSet rs = ci.change(false, true, contextID, changes, contextInfo, _downgrade, null);
                        if (!rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                        rsArray[i] = rs;
                    }
                }
                Exception exception = null;
                try {
                    OXcontext.change(contextID, changes, _downgrade, new UserAction() {

                        public void doJob(int contextID) throws Exception {
                            ContextCache.removePluginSupportInfo(contextID);
                            OXcontextData.changeOXcontextData(contextID, userMap);
                            OXworkingContext ctx = SessionHelper.getOXworkingContext(request, "workingContext");
                            if (ctx != null) {
                                if (!ctx.getName().equals(contextName)) {
                                    ctx.setName(contextName);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    exception = e;
                }
                if (plist != null) {
                    for (int i = 0; i < plist.length; ++i) {
                        ContextIface ci = plist[i].getContextIface();
                        ResultSet rs = ci.change(true, exception == null ? true : false, contextID, changes, contextInfo, _downgrade, rsArray[i]);
                        if (exception == null && !rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                    }
                }
                if (exception != null) {
                    throw exception;
                }
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=context");
                TemplateManager.setRedirectFlag(map);

            } catch (Exception e) {
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
                map.put("error", StackTrace.toString(e));
                SessionHelper.setLastException(request, "lastException", e);
            }

        }
        map.put("accessCombinationList", Config.getInstance().getAccessCombination(accessCombination));
        return map;
    }
    static private final String[] params = {"contextID", "contextName", "mapping", "quota", "accessCombination", "usedQuota", "mailQuota", "downgrade", "pluginsupport"};
}
