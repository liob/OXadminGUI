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

import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.OXcontextData;
import at.sciencesoft.oxrmi.OXlogin;
import at.sciencesoft.oxrmi.OXserverInfo;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Login {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map) throws Exception {
        if (isPost) {
            String user = request.getParameter("loginUser");
            String pwd = request.getParameter("loginPwd");
            String ctxlogin = request.getParameter("ctxlogin");
            try {
                if (ctxlogin == null) {
                    if (OXlogin.login2Admin(user, pwd)) {
                        OXserverInfo.getInstance().setOXadmin(user, pwd);
                        SessionHelper.setLoggedIn(request, "loggedIn");
                        SessionHelper.setOXAdmin(request, "isAdmin");
                        response.sendRedirect(Config.getInstance().getURLbase() + "index.html");
                    }
                } else {
                    if (Config.getInstance().allowCTXamdinLogin()) {
                        String split[] = request.getParameter("context").split("\\|");
                        int contextID = Integer.parseInt(split[0]);
                        com.openexchange.admin.rmi.dataobjects.User u = OXlogin.login2User(contextID, user, pwd);
                        if (u != null) {
                            if (u.getId() != 2) {
                                throw new Exception("This user isn't the Context Admin");
                            }
                            SessionHelper.setLoggedIn(request, "loggedIn");
                            Properties prop = OXcontextData.loadOXcontextData(contextID);
                            SessionHelper.setOXworkingContext(request, "workingContext", contextID, split[1], Integer.parseInt((String) prop.getProperty("userID")));
                            response.sendRedirect(Config.getInstance().getURLbase() + "index.html");
                        }
                    }
                }
                if (OXserverInfo.getInstance().getOXadmin() != null && Config.getInstance().allowCTXamdinLogin()) {
                    map.put("contextList", OXcontext.listAll());
                }
                SessionHelper.setString(request, "loginUser", user);
                map.put("loginFailed", "false");
            } catch (Exception e) {
                map.put("error", StackTrace.toString(e));
                SessionHelper.setLastException(request, "lastException", e);
            }
        } else {
            if (OXserverInfo.getInstance().getOXadmin() != null && Config.getInstance().allowCTXamdinLogin()) {
                map.put("contextList", OXcontext.listAll());
            }
        }
        return map;
    }
}
