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
import at.sciencesoft.oxrmi.OXresource;
import at.sciencesoft.oxrmi.OXworkingContext;

import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResourceIface;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.webserver.TemplateManager;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ModifyResource {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXworkingContext ctx = SessionHelper.getOXworkingContext(request, "workingContext");
        int actualContext = ctx.getID();
        Properties prop = OXcontextData.loadOXcontextData(actualContext);
        int resourceID = Integer.parseInt(request.getParameter("resourceID"));
        Credentials auth = ContextCache.getContextCredentials(actualContext);
        if (!isPost) {
            com.openexchange.admin.rmi.dataobjects.Resource res = OXresource.getResource(actualContext, auth, resourceID);
            if (res == null) {
                throw new Exception("ModifyResource.process(): Error getting resource ID '" + resourceID + "'");
            }
            map.put("name", res.getName());
            map.put("displayName", res.getDisplayname());
            map.put("description", res.getDescription());
            map.put("resourceID", "" + resourceID);

            String tmp = res.getEmail();
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

        } else {
            try {
                String name = request.getParameter("name").trim();
                String displayName = request.getParameter("displayName").trim();
                String description = request.getParameter("description").trim();
                String tmp;
                String email;
                tmp = request.getParameter("email_show");
                if (tmp != null && (tmp.equals("domain_list") || tmp.equals(""))) {
                    email = request.getParameter("emailUser") + "@" + request.getParameter("domain");
                } else {
                    email = request.getParameter("email_str").trim();
                }

                // change resource
                Plugin[] plist = null;
                if (ContextCache.isPluginSupport(actualContext)) {
                    plist = PluginManager.getPulgin(Plugin.PLUGIN.RESOURCE);
                }
                ResultSet[] rsArray = null;
                ParamMap changes = OXresource.getChanges(actualContext, ContextCache.getContextCredentials(actualContext), resourceID, name, displayName, description, email);
                if (plist != null) {
                    rsArray = new ResultSet[plist.length];
                    for (int j = 0; j < plist.length; ++j) {
                        ResourceIface ri = plist[j].getResourceIface();
                        ResultSet rs = ri.change(false, true, actualContext, ContextCache.getContextCredentials(actualContext), resourceID, changes, null);
                        if (!rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                        rsArray[j] = rs;
                    }
                }
                Exception exception = null;
                try {
                    OXresource.change(actualContext, ContextCache.getContextCredentials(actualContext), resourceID, changes);
                } catch (Exception e) {
                    exception = e;
                }
                if (plist != null) {
                    for (int j = 0; j < plist.length; ++j) {
                        ResourceIface ri = plist[j].getResourceIface();
                        ResultSet rs = ri.change(true,exception== null?true:false,actualContext,ContextCache.getContextCredentials(actualContext), resourceID, changes, rsArray[j]);
                        if (exception==null && !rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                    }
                }
                if(exception != null) {
                    throw exception;
                }
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=resource");
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
            }
        }
        if (prop.getProperty("userEmailDomain") != null) {
            map.put("emailDomain", Config.getList(prop.getProperty("userEmailDomain")));
        }
        return map;
    }
    static private final String[] params = {"name", "displayName", "emailUser", "domain", "email_str", "email_show", "resourceID", "description"};
}
