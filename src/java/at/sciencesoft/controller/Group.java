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
import at.sciencesoft.oxrmi.OXgroup;
import at.sciencesoft.oxrmi.OXworkingContext;
import at.sciencesoft.plugin.GroupIface;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Group {

    static public HashMap process(HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        OXworkingContext context = SessionHelper.getOXworkingContext(request, "workingContext");
        Credentials auth = ContextCache.getContextCredentials(context.getID());
        try {
            String action = request.getParameter("formAction");
            if (action != null) {
                if (action.equals("deleteGroup")) {
                    int end = Integer.parseInt(request.getParameter("groupNum"));
                    for (int i = 0; i < end; ++i) {
                        String groupID = request.getParameter("g" + i);
                        if (groupID != null) {
                            int groupIDnum = Integer.parseInt(groupID);

                            // delete group
                            Plugin[] plist = null;
                            if (ContextCache.isPluginSupport(context.getID())) {
                                plist = PluginManager.getPulgin(Plugin.PLUGIN.GROUP);
                            }
                            ResultSet[] rsArray = null;
                            if (plist != null) {
                                rsArray = new ResultSet[plist.length];
                                for (int j = 0; j < plist.length; ++j) {
                                    GroupIface gi = plist[j].getGroupIface();
                                    ResultSet rs = gi.delete(false, true, context.getID(), auth, groupIDnum, null);
                                    if (!rs.isSuccess()) {
                                        throw new Exception(rs.getErrorMsg());
                                    }
                                    rsArray[j] = rs;
                                }
                            }
                            Exception exception = null;
                            try {
                                OXgroup.delete(context.getID(), auth, groupIDnum);
                            } catch (Exception e) {
                                exception = e;
                            }
                            if (plist != null) {
                                for (int j = 0; j < plist.length; ++j) {
                                    GroupIface gi = plist[j].getGroupIface();
                                    ResultSet rs = gi.delete(true, exception == null ? true : false, context.getID(), auth, groupIDnum, rsArray[j]);
                                    if (exception == null && !rs.isSuccess()) {
                                        throw new Exception(rs.getErrorMsg());
                                    }
                                }
                            }
                            if (exception != null) {
                                throw exception;
                            }

                        }
                    }
                }
            }
            com.openexchange.admin.rmi.dataobjects.Group[] list = OXgroup.listAll(context.getID(), auth);
            String sortOrder = request.getParameter("sortOrder");
            if (sortOrder == null) {
                sortOrder = "0";

            }
            final int sOrder = Integer.parseInt(sortOrder);
            map.put("sortOrder", sOrder);
            map.put("sort", "0");
            if (list != null) {
                String sort = request.getParameter("sort");
                  if (sort != null  && !sort.equals("")) {
                    map.put("sort", sort);
                    switch (Integer.parseInt(sort)) {
                        case 0: {
                            Arrays.sort(list, new Comparator<com.openexchange.admin.rmi.dataobjects.Group>() {
                                public int compare(com.openexchange.admin.rmi.dataobjects.Group a, com.openexchange.admin.rmi.dataobjects.Group b) {
                                    int r = a.getId() - b.getId();
                                    if (sOrder == 1) {
                                        return -r;
                                    }
                                    return r;
                                }
                            });


                            break;
                        }
                        // user name
                        case 1: {
                            final Collator collator = Config.getInstance().getCollator();
                            Arrays.sort(list, new Comparator<com.openexchange.admin.rmi.dataobjects.Group>() {

                                public int compare(com.openexchange.admin.rmi.dataobjects.Group a, com.openexchange.admin.rmi.dataobjects.Group b) {
                                    int r = collator.compare(a.getName(), b.getName());
                                    if (sOrder == 1) {
                                        return -r;
                                    }
                                    return r;
                                }
                            });
                            break;
                        }
                        case 2: {
                            final Collator collator = Config.getInstance().getCollator();
                            Arrays.sort(list, new Comparator<com.openexchange.admin.rmi.dataobjects.Group>() {

                                public int compare(com.openexchange.admin.rmi.dataobjects.Group a, com.openexchange.admin.rmi.dataobjects.Group b) {
                                    int r = collator.compare(a.getDisplayname(), b.getDisplayname());
                                    if (sOrder == 1) {
                                        return -r;
                                    }
                                    return r;
                                }
                            });
                            break;
                        }
                    }
                }
            }
            map.put("groupList", list);
        } catch (Exception e) {
            map.put("error", StackTrace.toString(e));
            SessionHelper.setLastException(request, "lastException", e);
        }
        return map;
    }
}
