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
import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.OXworkingContext;
import at.sciencesoft.oxrmi.UserAction;
import at.sciencesoft.plugin.ContextIface;
import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Context {

    static public HashMap process(HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        try {
            String action = request.getParameter("formAction");
            if (action != null) {
                if (action.equals("changeStatus")) {
                    int end = Integer.parseInt(request.getParameter("contextNum"));
                    for (int i = 0; i < end; ++i) {
                        String contextID = request.getParameter("c" + i);
                        if (contextID != null) {
                            int contextIDNum = Integer.parseInt(contextID.substring(1));
                            Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.CONTEXT);
                            ResultSet[] rsArray = null;
                            // disable context
                            if (contextID.charAt(0) == 'a') {
                                // call plugin pre
                                if (plist != null && ContextCache.isPluginSupport(contextIDNum)) {
                                    rsArray = new ResultSet[plist.length];
                                    for (int j = 0; j < plist.length; ++j) {
                                        ContextIface ci = plist[j].getContextIface();
                                        ResultSet rs = ci.disable(false, true, contextIDNum, null);
                                        if (!rs.isSuccess()) {
                                            throw new Exception(rs.getErrorMsg());
                                        }
                                        rsArray[j] = rs;
                                    }
                                }
                                Exception exception = null;
                                try {
                                    OXcontext.disable(contextIDNum);
                                } catch (Exception e) {
                                    exception = e;
                                }
                                // call plugin post
                                if (plist != null && ContextCache.isPluginSupport(contextIDNum)) {
                                    for (int j = 0; j < plist.length; ++j) {
                                        ContextIface ci = plist[j].getContextIface();
                                        ResultSet rs = ci.disable(true, exception == null ? true : false, contextIDNum, rsArray[j]);
                                        //
                                        if (exception == null && !rs.isSuccess()) {
                                            throw new Exception(rs.getErrorMsg());
                                        }
                                    }
                                }
                                if (exception != null) {
                                    throw exception;
                                }
                                // enable context
                            } else {
                                // call plugin pre
                                if (plist != null && ContextCache.isPluginSupport(contextIDNum)) {
                                    rsArray = new ResultSet[plist.length];
                                    for (int j = 0; j < plist.length; ++j) {
                                        ContextIface ci = plist[j].getContextIface();
                                        ResultSet rs = ci.enable(false, true, contextIDNum, null);
                                        if (!rs.isSuccess()) {
                                            throw new Exception(rs.getErrorMsg());
                                        }
                                        rsArray[j] = rs;
                                    }
                                }
                                Exception exception = null;
                                try {
                                    OXcontext.enable(contextIDNum);
                                } catch (Exception e) {
                                    exception = e;
                                }
                                // call plugin post
                                if (plist != null && ContextCache.isPluginSupport(contextIDNum)) {
                                    for (int j = 0; j < plist.length; ++j) {
                                        ContextIface ci = plist[j].getContextIface();
                                        ResultSet rs = ci.enable(true, exception == null ? true : false, contextIDNum, rsArray[j]);
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
                } else if (action.equals("deleteContext")) {
                    int end = Integer.parseInt(request.getParameter("contextNum"));
                    OXworkingContext wc = SessionHelper.getOXworkingContext(request, "workingContext");
                    for (int i = 0; i < end; ++i) {
                        String contextID = request.getParameter("c" + i);
                        // delete context
                        if (contextID != null) {
                            Plugin[] plist = null;
                            int contextIDNum = Integer.parseInt(contextID.substring(1));
                            if (ContextCache.isPluginSupport(contextIDNum)) {
                                plist = PluginManager.getPulgin(Plugin.PLUGIN.CONTEXT);
                            }
                            ResultSet[] rsArray = null;
                            // call plugin pre
                            if (plist != null) {
                                rsArray = new ResultSet[plist.length];
                                for (int j = 0; j < plist.length; ++j) {
                                    ContextIface ci = plist[j].getContextIface();
                                    ResultSet rs = ci.delete(false, true, contextIDNum, null);
                                    if (!rs.isSuccess()) {
                                        throw new Exception(rs.getErrorMsg());
                                    }
                                    rsArray[j] = rs;
                                }
                            }
                            Exception exception = null;
                            try {
                                OXcontext.delete(contextIDNum, new UserAction() {

                                    public void doJob(int contextID) throws Exception {
                                        OXcontextData.deleteOXcontextData(contextID);
                                        ContextCache.removeContextCredentials(contextID);
                                        ContextCache.removePluginSupportInfo(contextID);
                                    }
                                });
                            } catch (Exception e) {
                                exception = e;
                            }
                            // call plugin post
                            if (plist != null) {
                                for (int j = 0; j < plist.length; ++j) {
                                    ContextIface ci = plist[j].getContextIface();
                                    ResultSet rs = ci.delete(true, exception == null ? true : false, contextIDNum, rsArray[j]);
                                    if (exception == null && !rs.isSuccess()) {
                                        throw new Exception(rs.getErrorMsg());
                                    }
                                }
                            }

                            if (exception != null) {
                                throw exception;
                            }
                            if (wc != null && wc.getID() == contextIDNum) {
                                SessionHelper.resetOXworkingContext(request, "workingContext");
                            }
                        }
                    }
                } else if (action.equals("setContext")) {
                    int id = Integer.parseInt(request.getParameter("contextID"));
                    String name = request.getParameter("context");
                    Properties prop = OXcontextData.loadOXcontextData(id);
                    SessionHelper.setOXworkingContext(request, "workingContext", id, name, Integer.parseInt((String) prop.getProperty("userID")));
                }
            }
            String sortOrder = request.getParameter("sortOrder");
            if (sortOrder == null) {
                sortOrder = "0";

            }
            final int sOrder = Integer.parseInt(sortOrder);
            OXcontext[] list = OXcontext.listAll();
            map.put("sort", "0");
            if (list != null) {
                String sort = request.getParameter("sort");
                if (sort != null  && !sort.equals("")) {
                    map.put("sort", sort);

                    switch (Integer.parseInt(sort)) {
                        case 0: {
                            Arrays.sort(list, new Comparator<OXcontext>() {
                                public int compare(OXcontext a, OXcontext b) {
                                    int r = a.getID() - b.getID();
                                    if (sOrder == 1) {
                                        return -r;
                                    }
                                    return r;
                                }
                            });

                            break;
                        }
                        case 1: {
                            final Collator collator = Config.getInstance().getCollator();
                            Arrays.sort(list, new Comparator<OXcontext>() {
                                public int compare(OXcontext a, OXcontext b) {
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
                            Arrays.sort(list, new Comparator<OXcontext>() {
                                public int compare(OXcontext a, OXcontext b) {
                                    long l = a.getUsedQuota() - b.getUsedQuota();
                                    if (sOrder == 1) {
                                        return (int)-l;
                                    }
                                    return (int)l;
                                }
                            });
                            break;
                        }
                    }
                }
            }
            map.put("sortOrder", sOrder);
            map.put("contextList", list);
            map.put("contextCache", new ContextCache());
            map.put("disableContextDelete", "" + Config.getInstance().isDisableContextDelete());
        } catch (Exception e) {
            map.put("error", StackTrace.toString(e));
            SessionHelper.setLastException(request, "lastException", e);
        }
        return map;
    }
}
