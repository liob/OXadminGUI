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
import at.sciencesoft.oxrmi.OXlogin;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.webserver.TemplateManager;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class SetContextAdmin {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        int contextID = Integer.parseInt(request.getParameter("contextID"));
        map.put("contextID", contextID);
        if (!isPost) {
            OXcontext[] olist = OXcontext.list("" + contextID);
            map.put("contextName", olist[0].getName());
        } else {
            String contextName = request.getParameter("contextName");
            String user = request.getParameter("user" + contextID);
            String pwd = request.getParameter("pwd" + contextID);
            map.put("contextName", contextName);

            try {
                if (OXlogin.login2User(contextID, user, pwd) == null) {
                    map.put("loginFailed", "true");
                } else {
                    OXcontextData.saveOXcontextData(contextID, contextName, user, pwd, null);
                    response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=context");
                    TemplateManager.setRedirectFlag(map);
                }
            } catch (Exception e) {
                map.put("error", StackTrace.toString(e));
                SessionHelper.setLastException(request,"lastException", e);
            }
        }

        return map;
    }
}
