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

import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Plugin {

    static public HashMap process(HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        try {
            String action = request.getParameter("formAction");
            if (action != null) {
                
                if (action.equals("deletePlugin")) {
                    int end = Integer.parseInt(request.getParameter("pluginNum"));
                    for(int i = 0; i < end; ++i) {
                        String tmp = request.getParameter("p"+i);
                        if(tmp != null) {
                            PluginManager.deletePlugin(tmp.substring(1));
                        }
                    }

                } else if (action.equals("changeStatus")) {
                    int end = Integer.parseInt(request.getParameter("pluginNum"));
                    for(int i = 0; i < end; ++i) {
                        String tmp = request.getParameter("p"+i);
                        if(tmp != null) {
                            PluginManager.toogleEnableStatus(tmp.substring(1));
                        }
                    }
                }
            }
            map.put("pluginList", PluginManager.listAll());
        } catch (Exception e) {
            map.put("error", StackTrace.toString(e));
            SessionHelper.setLastException(request, "lastException", e);
        }
        return map;
    }
}
