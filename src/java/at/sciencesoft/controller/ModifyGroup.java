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
import at.sciencesoft.oxrmi.OXgroup;
import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.oxrmi.OXworkingContext;
import at.sciencesoft.oxrmi.Param;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.plugin.GroupIface;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.util.StringUtil;
import at.sciencesoft.webserver.TemplateManager;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ModifyGroup {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXworkingContext ctx = SessionHelper.getOXworkingContext(request, "workingContext");
        int contextID = ctx.getID();
        int groupID = Integer.parseInt(request.getParameter("groupID"));
        Credentials auth = ContextCache.getContextCredentials(contextID);
        Plugin[] plist = null;
        if (ContextCache.isPluginSupport(contextID)) {
            plist = PluginManager.getPulgin(Plugin.PLUGIN.GROUP);
        }
        String tmp = OXcontextData.loadOXcontextData(contextID).getProperty("userEmailDomain");
        String emailDomain = null;
        if (tmp != null) {
            emailDomain = StringUtil.splitTrim(tmp)[0];
            map.put("emailDomain", emailDomain);
        }
         // plugins

        if (plist != null) {
            for (int i = 0; i < plist.length; ++i) {
                for (GroupIface.FIELD field : GroupIface.FIELD.values()) {
                    if (plist[i].getGroupIface().checkGUIflag(field)) {
                        map.put(field.name(), "true");
                    }
                }
            }
        }
        if (!isPost) {
            com.openexchange.admin.rmi.dataobjects.Group grp = OXgroup.getGroup(contextID, auth, groupID);
            if (grp == null) {
                throw new Exception("ModifyGroup.process(): Error getting resource ID '" + groupID + "'");
            }
            map.put("name", grp.getName());
            map.put("displayName", grp.getDisplayname());
            map.put("groupID", grp.getId());

            com.openexchange.admin.rmi.dataobjects.User[] members = OXgroup.getMembers(contextID, auth, groupID);
            com.openexchange.admin.rmi.dataobjects.User[] ulist = OXuser.listAll(contextID, auth);
            if (members == null) {
                map.put("right", ulist);
            } else {
                map.put("left", members);
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < members.length; ++i) {
                    if (i > 0) {
                        buf.append(',');
                    }
                    buf.append(members[i].getId());
                }
                map.put("members", buf.toString());

                HashMap<Integer, com.openexchange.admin.rmi.dataobjects.User> mMap = new HashMap<Integer, com.openexchange.admin.rmi.dataobjects.User>();
                for (int i = 0; i < members.length; ++i) {
                    mMap.put(members[i].getId(), members[i]);
                }
                ArrayList<com.openexchange.admin.rmi.dataobjects.User> al = new ArrayList<com.openexchange.admin.rmi.dataobjects.User>();
                for (int i = 0; i < ulist.length; ++i) {
                    if (mMap.containsKey(ulist[i].getId())) {
                        continue;
                    }
                    al.add(ulist[i]);
                }
                if (al.size() > 0) {
                    map.put("right", al.toArray(new com.openexchange.admin.rmi.dataobjects.User[0]));
                }
            }
            if (plist != null) {
                for (int j = 0; j < plist.length; ++j) {
                    GroupIface gi = plist[j].getGroupIface();
                    gi.loadGUIdata(contextID, groupID, map);
                }
            }
        } else {
            String members = null;
            try {
                String name = request.getParameter("name").trim();
                String displayName = request.getParameter("displayName").trim();
                members = request.getParameter("members");
                // change group
                ResultSet[] rsArray = null;
                ParamMap pmap = new ParamMap();
                ParamMap changes = OXgroup.getChanges(contextID, ContextCache.getContextCredentials(contextID), groupID, name, displayName, members,pmap);          
                pmap.putBoolean(Param.EMAIL_GROUP_ACTIVE,request.getParameter("activeEmailGroup") != null?true:false);
                tmp = request.getParameter("emailGroup");
                if(tmp != null) {
                    tmp = tmp.trim();
                }
                if(emailDomain != null) {
                   tmp+= "@" + emailDomain;
                }
                pmap.putString(Param.EMAIL_GROUP,tmp);
                tmp = request.getParameter("emailAdditional");
                if(tmp != null) {
                    tmp = tmp.trim();
                }
                pmap.putString(Param.EMAIL_GROUP_ADDITIONAL,tmp);
                Exception exception = null;

                if (plist != null) {
                    rsArray = new ResultSet[plist.length];
                    for (int j = 0; j < plist.length; ++j) {
                        GroupIface gi = plist[j].getGroupIface();
                        ResultSet rs = gi.change(false, true, contextID, auth, groupID, changes,pmap, null);
                        if (!rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                        rsArray[j] = rs;
                    }
                }
              

                try {
                    if(changes != null) {
                        OXgroup.change(contextID, ContextCache.getContextCredentials(contextID), groupID, changes);
                    }
                } catch (Exception e) {
                    exception = e;
                }
                if (plist != null) {
                    for (int j = 0; j < plist.length; ++j) {
                        GroupIface gi = plist[j].getGroupIface();
                        ResultSet rs = gi.change(true, exception == null ? true : false, contextID, auth, groupID, changes,pmap, rsArray[j]);
                        if (exception == null && !rs.isSuccess()) {
                            throw new Exception(rs.getErrorMsg());
                        }
                    }
                }
                if (exception != null) {
                    throw exception;
                }
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=group");
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
                com.openexchange.admin.rmi.dataobjects.User[] ulist = OXuser.listAll(contextID, auth);
                if (!members.equals("")) {
                    HashMap<Integer, com.openexchange.admin.rmi.dataobjects.User> uMap = new HashMap<Integer, com.openexchange.admin.rmi.dataobjects.User>();
                    for (int i = 0; i < ulist.length; ++i) {
                        uMap.put(ulist[i].getId(), ulist[i]);
                    }
                    ArrayList<com.openexchange.admin.rmi.dataobjects.User> al = new ArrayList<com.openexchange.admin.rmi.dataobjects.User>();
                    String[] list = members.split(",");
                    HashMap<Integer, String> sMap = new HashMap<Integer, String>();
                    for (int i = 0; i < list.length; ++i) {
                        Integer t = Integer.parseInt(list[i]);
                        sMap.put(t, list[i]);
                        com.openexchange.admin.rmi.dataobjects.User u = uMap.get(t);
                        if (u != null) {
                            al.add(u);
                        }
                    }
                    com.openexchange.admin.rmi.dataobjects.User[] u = al.toArray(new com.openexchange.admin.rmi.dataobjects.User[0]);
                    map.put("left", u);

                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < u.length; ++i) {
                        if (i > 0) {
                            buf.append(',');
                        }
                        buf.append(u[i].getId());
                    }
                    map.put("members", buf.toString());

                    al = new ArrayList<com.openexchange.admin.rmi.dataobjects.User>();
                    for (int i = 0; i < ulist.length; ++i) {
                        if (!sMap.containsKey(ulist[i].getId())) {
                            al.add(ulist[i]);
                        }
                    }
                    map.put("right", al.toArray(new com.openexchange.admin.rmi.dataobjects.User[0]));
                } else {
                    map.put("right", ulist);
                }
            }
        }
        map.put("showUserInGroup", Config.getInstance().getShowUserInGroup());
        return map;
    }
    static private final String[] params = {"name", "displayName", "groupID", "members", "activeEmailGroup", "emailGroup", "emailAdditional"};
}
