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
import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.oxrmi.OXworkingContext;
import at.sciencesoft.oxrmi.UserAction;
import at.sciencesoft.oxrmi.UserList;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.plugin.UserIface;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class User {

    static public HashMap process(HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {

        OXworkingContext context = SessionHelper.getOXworkingContext(request, "workingContext");
        Credentials auth = ContextCache.getContextCredentials(context.getID());

        String action = request.getParameter("formAction");
        if (action != null) {
            try {
                if (action.equals("deleteUser")) {
                    int end = Integer.parseInt(request.getParameter("userNum"));
                    for (int i = 0; i < end; ++i) {
                        String userID = request.getParameter("u" + i);
                        if (userID != null) {
                            int userIDnum = Integer.parseInt(userID);
                            Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.USER);
                            ResultSet[] rsArray = null;
                            // call plugin pre
                            if (plist != null && ContextCache.isPluginSupport(context.getID())) {
                                rsArray = new ResultSet[plist.length];
                                for (int j = 0; j < plist.length; ++j) {
                                    UserIface ui = plist[j].getUserIface();
                                    ResultSet rs = ui.delete(false, true, context.getID(), auth, userIDnum, null);
                                    if (!rs.isSuccess()) {
                                        throw new Exception(rs.getErrorMsg());
                                    }
                                    rsArray[j] = rs;
                                }
                            }
                            Exception exception = null;
                            try {
                                OXuser.delete(context.getID(), auth, userIDnum, new UserAction() {

                                    public void doJob(int id) throws Exception {
                                    }
                                });
                            } catch (Exception e) {
                                exception = e;
                            }

                            // call plugin post
                            if (plist != null && ContextCache.isPluginSupport(context.getID())) {
                                for (int j = 0; j < plist.length; ++j) {
                                    UserIface ui = plist[j].getUserIface();
                                    ResultSet rs = ui.delete(true, exception == null ? true : false, context.getID(), auth, userIDnum, rsArray[j]);
                                    if (!rs.isSuccess()) {
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
            } catch (Exception e) {
                map.put("error", StackTrace.toString(e));
                SessionHelper.setLastException(request, "lastException", e);
            }
        }


        com.openexchange.admin.rmi.dataobjects.User[] ulist = null;
        final int limit = Config.getInstance().getDisplayUserLimit();
        final Integer[] overTheLimit = new Integer[1];
        HttpSession session = request.getSession(true);
        // special case - resort a search result
        if (request.getParameter("resort") != null && request.getParameter("resort").equals("1") && session.getAttribute("found") != null) {
            ulist = (com.openexchange.admin.rmi.dataobjects.User[]) session.getAttribute("found");
            map.put("listsize", session.getAttribute("listsize"));
            map.put("found", ulist.length);
        } else {
            session.removeAttribute("found");
            // check the display limit
            if (limit > 0 && request.getParameter("init") != null) {
                ulist = OXuser.listAll(context.getID(), auth, new UserList() {
                    public boolean checkLimit(int l) {
                        if (l > limit) {
                            // force serach mode
                            overTheLimit[0] = l;
                            return false;
                        }
                        return true;
                    }
                });
            } else {
                ulist = OXuser.listAll(context.getID(), auth);
            }
        }

        boolean regexpErr = false;
        boolean searchMode = false;
        String search = request.getParameter("search");
        if (search != null) {
            search = search.trim();
            map.put("search", search);
        }
        boolean allFields = request.getParameter("allFields") != null ? true : false;
        if (allFields) {
            map.put("allFields", "1");
        }
        boolean regexp = request.getParameter("regexp") != null ? true : false;
        if (regexp) {
            map.put("regexp", "1");
        }
        if (overTheLimit[0] != null || request.getParameter("searchmode") != null) {
            searchMode = true;
            map.put("searchmode", "1");
        }
        if (request.getParameter("searchbutton") != null && ulist != null) {
            ArrayList<com.openexchange.admin.rmi.dataobjects.User> result = new ArrayList<com.openexchange.admin.rmi.dataobjects.User>();
            Pattern pattern = null;
            if (regexp) {
                map.put("regexp", "1");
                try {
                    pattern = Pattern.compile(search);
                } catch (Exception e) {
                    String list[] = e.toString().split("\n");
                    map.put("regexperr", list[0]);
                    regexpErr = true;
                }
            }
            if (!search.equals("") && !regexpErr) {
                for (int i = 0; i < ulist.length; ++i) {
                    com.openexchange.admin.rmi.dataobjects.User u = ulist[i];
                    StringBuffer buf = new StringBuffer();
                    buf.append(u.getId());
                    buf.append('\n');
                    buf.append(u.getName());
                    buf.append('\n');
                    buf.append(u.getDisplay_name());
                    buf.append('\n');
                    buf.append(u.getGiven_name());
                    buf.append('\n');
                    buf.append(u.getSur_name());
                    buf.append('\n');
                    buf.append(u.getPrimaryEmail());
                    if (allFields) {
                        if (u.getMiddle_name() != null) {
                            buf.append(u.getMiddle_name());
                            buf.append('\n');
                        }
                        if (u.getStreet_home() != null) {
                            buf.append(u.getStreet_home());
                            buf.append('\n');
                        }
                        if (u.getCity_home() != null) {
                            buf.append(u.getCity_home());
                            buf.append('\n');
                        }
                        if (u.getState_home() != null) {
                            buf.append(u.getState_home());
                            buf.append('\n');
                        }
                        if (u.getCompany() != null) {
                            buf.append(u.getCompany());
                            buf.append('\n');
                        }
                        if (u.getDepartment() != null) {
                            buf.append(u.getDepartment());
                            buf.append('\n');
                        }
                        if (u.getPostal_code_home() != null) {
                            buf.append(u.getPostal_code_home());
                            buf.append('\n');
                        }
                        if (u.getStreet_business() != null) {
                            buf.append(u.getStreet_business());
                            buf.append('\n');
                        }
                        if (u.getPostal_code_business() != null) {
                            buf.append(u.getPostal_code_business());
                            buf.append('\n');
                        }
                        if (u.getCity_business() != null) {
                            buf.append(u.getCity_business());
                            buf.append('\n');
                        }
                        if (u.getState_business() != null) {
                            buf.append(u.getState_business());
                            buf.append('\n');
                        }
                        if (u.getTelephone_business1() != null) {
                            buf.append(u.getTelephone_business1());
                            buf.append('\n');
                        }
                        if (u.getTelephone_home1() != null) {
                            buf.append(u.getTelephone_home1());
                            buf.append('\n');
                        }
                        if (u.getCellular_telephone1() != null) {
                            buf.append(u.getCellular_telephone1());
                            buf.append('\n');
                        }
                        if (u.getFax_business() != null) {
                            buf.append(u.getFax_business());
                            buf.append('\n');
                        }
                        HashSet<String> alias = u.getAliases();
                        int index = 0;
                        if (alias != null) {
                            Iterator<String> iter = alias.iterator();
                            while (iter.hasNext()) {
                                String tmp = iter.next();
                                if (tmp.equals(u.getPrimaryEmail())) {
                                    continue;
                                }
                                if (index++ > 0) {
                                    buf.append('\n');
                                }
                                buf.append(tmp);
                            }
                        }
                    }
                    // search operation
                    if (!regexp) {
                        String tmp = buf.toString().toLowerCase();
                        if (tmp.indexOf(search.toLowerCase()) >= 0) {
                            result.add(ulist[i]);
                        }
                    } else {
                        Matcher matcher = pattern.matcher(buf.toString());
                        if (matcher.find()) {
                            result.add(ulist[i]);
                        }
                    }

                }
                if (result.size() > 0) {
                    map.put("listsize", ulist.length);
                    session.setAttribute("listsize", "" + ulist.length);
                    ulist = result.toArray(new com.openexchange.admin.rmi.dataobjects.User[result.size()]);
                    map.put("found", ulist.length);
                    session.setAttribute("found", ulist);
                } else {
                    ulist = null;
                    map.put("noresult", "1");
                }
            }
        } else {
            if (searchMode) {
                int size = -1;
                if (overTheLimit[0] != null) {
                    // in this case ulist == null
                    size = overTheLimit[0];
                } else {
                    size = ulist.length;
                }
                map.put("listsize", size);
                map.put("searchmodehint", "1");
                ulist = null;
            }
        }
        if (regexpErr) {
            ulist = null;
        }

        // sort the list
        String sortOrder = request.getParameter("sortOrder");
        if (sortOrder == null) {
            sortOrder = "0";

        }
        final int sOrder = Integer.parseInt(sortOrder);
        map.put("sortOrder", sOrder);
        map.put("sort", "0");
        if (ulist != null) {
            String sort = request.getParameter("sort");
            if (sort != null && !sort.equals("")) {
                map.put("sort", sort);
                switch (Integer.parseInt(sort)) {
                    // sort ID
                    case 0: {
                        Arrays.sort(ulist, new Comparator<com.openexchange.admin.rmi.dataobjects.User>() {
                            public int compare(com.openexchange.admin.rmi.dataobjects.User a, com.openexchange.admin.rmi.dataobjects.User b) {
                                int r = a.getId() - b.getId();
                                if (sOrder == 1) {
                                    return -r;
                                }
                                return r;
                            }
                        });
                        break;
                    }
                    // sort user name
                    case 1: {
                        final Collator collator = Config.getInstance().getCollator();
                        Arrays.sort(ulist, new Comparator<com.openexchange.admin.rmi.dataobjects.User>() {
                            public int compare(com.openexchange.admin.rmi.dataobjects.User a, com.openexchange.admin.rmi.dataobjects.User b) {
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
                        // sort display name
                        final Collator collator = Config.getInstance().getCollator();
                        Arrays.sort(ulist, new Comparator<com.openexchange.admin.rmi.dataobjects.User>() {
                            public int compare(com.openexchange.admin.rmi.dataobjects.User a, com.openexchange.admin.rmi.dataobjects.User b) {
                                int r = collator.compare(a.getDisplay_name(), b.getDisplay_name());
                                if (sOrder == 1) {
                                    return -r;
                                }
                                return r;
                            }
                        });
                        break;
                    }
                    // sort first name
                    case 3: {
                        final Collator collator = Config.getInstance().getCollator();
                        Arrays.sort(ulist, new Comparator<com.openexchange.admin.rmi.dataobjects.User>() {

                            public int compare(com.openexchange.admin.rmi.dataobjects.User a, com.openexchange.admin.rmi.dataobjects.User b) {
                                int r = collator.compare(a.getGiven_name(), b.getGiven_name());
                                if (sOrder == 1) {
                                    return -r;
                                }
                                return r;
                            }
                        });
                        break;
                    }
                    // sort last name
                    case 4: {
                        final Collator collator = Config.getInstance().getCollator();
                        Arrays.sort(ulist, new Comparator<com.openexchange.admin.rmi.dataobjects.User>() {
                            public int compare(com.openexchange.admin.rmi.dataobjects.User a, com.openexchange.admin.rmi.dataobjects.User b) {
                                int r = collator.compare(a.getSur_name(), b.getSur_name());
                                if (sOrder == 1) {
                                    return -r;
                                }
                                return r;
                            }
                        });
                        break;
                    }
                    // sort email
                    case 5: {
                        final Collator collator = Config.getInstance().getCollator();
                        Arrays.sort(ulist, new Comparator<com.openexchange.admin.rmi.dataobjects.User>() {

                            public int compare(com.openexchange.admin.rmi.dataobjects.User a, com.openexchange.admin.rmi.dataobjects.User b) {
                                int r = collator.compare(a.getPrimaryEmail(), b.getPrimaryEmail());
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

        map.put("userList", ulist);
        map.put("userContextAdminID", context.getContextAdminUserID());
        map.put("workingContext", context);
        return map;
    }
}
