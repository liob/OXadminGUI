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

import at.sciencesoft.oxrmi.OXserverInfo;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.text.Group;
import at.sciencesoft.webserver.TemplateHandler;
import at.sciencesoft.webserver.TemplateManager;
import at.sciencesoft.webserver.WebServerConfig;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Main implements TemplateHandler {

    public void init() throws Exception {
    }

    public HashMap process(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap map = new HashMap();
        TemplateManager tm = WebServerConfig.getInstance().getTemplateManager();
        boolean isPost = request.getMethod().equals("POST");
        String link = request.getParameter("link");

        // invalidate existing session after a servlet container restart!
        if (SessionHelper.isLoggedIn(request, "loggedIn") && OXserverInfo.getInstance().getOXadmin() == null) {
            SessionHelper.invalidateSession(request, new String[]{"oxguilang"});
        }

        if (!SessionHelper.isLoggedIn(request, "loggedIn")) {
            if (!(link != null && (link.equals("login") || link.equals("info")))) {
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=login");
            }
        }

        boolean isAdmin = false;

        if (SessionHelper.isOXAdmin(request, "isAdmin")) {
            map.put("isAdmin", "true");
            isAdmin = true;
        }

        if (link != null) {
            if (isAdmin && link.equals("context")) {
                tm.preloadTemplate("/context.html");
                map.put("path", "/context.html");
                Context.process(request, response, map);
            } else if (link.equals("login")) {
                tm.preloadTemplate("/login.html");
                map.put("path", "/login.html");
                Login.process(isPost, request, response, map);
            } else if (link.equals("logout")) {
                tm.preloadTemplate("/logout.html");
                Logout.process(request);
                map.put("path", "/logout.html");
            } else if (isAdmin && link.equals("setcontextadmin")) {
                tm.preloadTemplate("/setcontextadmin.html");
                map.put("path", "/setcontextadmin.html");
                SetContextAdmin.process(isPost, request, response, map);
            } else if (isAdmin && link.equals("createcontext")) {
                tm.preloadTemplate("/createcontext.html");
                map.put("path", "/createcontext.html");
                CreateContext.process(isPost, request, response, map);
            } else if (isAdmin && link.equals("modifycontext")) {
                tm.preloadTemplate("/modifycontext.html");
                map.put("path", "/modifycontext.html");
                ModifyContext.process(isPost, request, response, map);
            } else if (link.equals("user")) {
                tm.preloadTemplate("/user.html");
                map.put("path", "/user.html");
                map.put("selMenu", "user");
                User.process(request, response, map);
            } else if (link.equals("createuser")) {
                tm.preloadTemplate("/createuser.html");
                map.put("path", "/createuser.html");
                CreateUser.process(isPost, request, response, map);
            } else if (link.equals("modifyuser")) {
                tm.preloadTemplate("/modifyuser.html");
                map.put("path", "/modifyuser.html");
                ModifyUser.process(isPost, request, response, map);

            } else if (link.equals("info")) {
                tm.preloadTemplate("/info.html");
                map.put("path", "/info.html");
                Information.process(map, request);
                map.put("selMenu", "info");
            } else if (link.equals("group")) {
                tm.preloadTemplate("/group.html");
                map.put("path", "/group.html");
                map.put("selMenu", "group");
                at.sciencesoft.controller.Group.process(request, response, map);
            } else if (link.equals("creategroup")) {
                tm.preloadTemplate("/creategroup.html");
                map.put("path", "/creategroup.html");
                CreateGroup.process(isPost, request, response, map);
            } else if (link.equals("modifygroup")) {
                tm.preloadTemplate("/modifygroup.html");
                map.put("path", "/modifygroup.html");
                ModifyGroup.process(isPost, request, response, map);
            } else if (link.equals("resource")) {
                tm.preloadTemplate("/resource.html");
                map.put("path", "/resource.html");
                map.put("selMenu", "resource");
                Resource.process(request, response, map);
            } else if (link.equals("createresource")) {
                tm.preloadTemplate("/createresource.html");
                map.put("path", "/createresource.html");
                CreateResource.process(isPost, request, response, map);
            } else if (link.equals("modifyresource")) {
                tm.preloadTemplate("/modifyresource.html");
                map.put("path", "/modifyresource.html");
                ModifyResource.process(isPost, request, response, map);
            } else if (link.equals("welcome")) {
                tm.preloadTemplate("/welcome.html");
                map.put("path", "/welcome.html");
            } else if (isAdmin && link.equals("plugin")) {
                tm.preloadTemplate("/plugin.html");
                map.put("path", "/plugin.html");
                map.put("selMenu", "plugin");
                Plugin.process(request, response, map);
            } else if (isAdmin && link.equals("modifyplugin")) {
                tm.preloadTemplate("/modifyplugin.html");
                map.put("path", "/modifyplugin.html");
                map.put("selMenu", "plugin");
                ModifyPlugin.process(isPost, request, response, map, request.getParameter("pluginID") == null ? true : false);
            } else if (link.equals("bundles")) {
                tm.preloadTemplate("/bundles.html");
                map.put("path", "/bundles.html");
                map.put("selMenu", "info");
                ListBundles.process(request, map);
            } else if (link.equals("files")) {
                tm.preloadTemplate("/files.html");
                map.put("path", "/files.html");
                map.put("selMenu", "info");
                ListFiles.process(request, map);
            } else if (link.equals("pluginhelp")) {
                tm.preloadTemplate("/pluginhelp.html");
                map.put("path", "/pluginhelp.html");
            } else {
                tm.preloadTemplate("/welcome.html");
                map.put("path", "/welcome.html");
            }
        } else {
            tm.preloadTemplate("/welcome.html");
            map.put("path", "/welcome.html");
        }
        Group group = Config.getInstance().getText().getGroup("general");
        String lang = SessionHelper.setLang(request, "oxguilang");
        group.setLang(lang);

        map.put("msg", group);
        map.put("oxguilang", lang);
        map.put("workingContext", SessionHelper.getOXworkingContext(request, "workingContext"));
        if (SessionHelper.isLoggedIn(request, "loggedIn")) {
            map.put("loggedIn", "true");
            map.put("admin",SessionHelper.getString(request,"loginUser"));
        }


        return map;
    }
}
