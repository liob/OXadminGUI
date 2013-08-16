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

import at.sciencesoft.plugin.Plugin;
import at.sciencesoft.plugin.PluginJava;
import at.sciencesoft.plugin.PluginManager;
import at.sciencesoft.plugin.PluginScript;
import at.sciencesoft.plugin.Script;
import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.webserver.TemplateManager;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *  @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ModifyPlugin {

    static public HashMap process(boolean isPost, HttpServletRequest request, HttpServletResponse response, HashMap map, boolean isCreate) throws Exception {
        String pluginID = request.getParameter("pluginID");
        if (pluginID == null && !isCreate) {
            throw new Exception("ModifyPlugin.process(): Missing parameter 'pluginID'");
        }
        if (isCreate) {
            pluginID = null;
        } else {
            map.put("pluginID", pluginID);
        }
        try {
            if (isPost) {
                String plugin_type = request.getParameter("plugin_type");
                map.put("plugin_type", plugin_type);
                int priority = Integer.parseInt(request.getParameter("priority").trim());
                map.put("priority", "" + priority);
                String name = request.getParameter("name").trim();
                map.put("name", name);
                String enabled = request.getParameter("enabled");
                map.put("enabled", enabled);
                String plugin = request.getParameter("plugin_script");
                map.put("plugin", plugin);
                if (plugin_type.equals("java")) {
                    /*
                    String classPath = request.getParameter("classpath").trim();
                    map.put("classpath", classPath);
                    if (classPath.equals("")) {
                        classPath = null;
                    }
                    */
                    String classPath = null;
                    String clazz = request.getParameter("class").trim();
                    map.put("class", clazz);
                    String jar = request.getParameter("jar");
                    map.put("jar", jar);
                    PluginJava pj = new PluginJava(name, pluginID, Plugin.PLUGIN.valueOf(plugin.toUpperCase()), priority, classPath, jar != null ? true : false, clazz, enabled != null ? true : false);
                    if (pj.isLoadError()) {
                        throw pj.getException();
                    }
                    PluginManager.savePlugin(pj);
                } else {
                    HashMap<Plugin.ACTION, Script> actionMap = new HashMap<Plugin.ACTION, Script>();
                    for (int i = 0; i < action.length; ++i) {
                        String s = "script_" + plugin + '_' + action[i];
                        String mode = s + "_call";
                        String t = request.getParameter(s);
                        if (t != null && !t.trim().equals("")) {
                            map.put(s, t.trim());
                            Script.CALL call = Script.CALL.AFTER;
                            String tmp = request.getParameter(mode);
                            if (tmp != null) {
                                switch (Integer.parseInt(tmp)) {
                                    case 0:
                                        call = Script.CALL.BEFORE;
                                        map.put(mode, 0);
                                        break;
                                    case 1:
                                        call = Script.CALL.AFTER;
                                        map.put(mode, 1);
                                        break;
                                    case 2:
                                        call = Script.CALL.BOTH;
                                        map.put(mode, 2);
                                        break;
                                }
                            }
                            actionMap.put(Plugin.ACTION.valueOf(action[i].toUpperCase()), new Script(t.trim(), call));
                        }
                    }
                    String errorPath = request.getParameter(plugin + "_error");
                    if (errorPath != null) {
                        errorPath = errorPath.trim();
                        map.put(plugin + "_error", errorPath);
                    }
                    String msgPath = request.getParameter(plugin + "_msg");
                    if (msgPath != null) {
                        msgPath = msgPath.trim();
                        map.put(plugin + "_msg", msgPath);
                    }

                    PluginScript ps = new PluginScript(name, pluginID, Plugin.PLUGIN.valueOf(plugin.toUpperCase()), priority, enabled != null ? true : false, actionMap, errorPath, msgPath);
                    if (ps.isLoadError()) {
                        throw ps.getException();
                    }
                    PluginManager.savePlugin(ps);
                }
                response.sendRedirect(Config.getInstance().getURLbase() + "index.html?link=plugin");
                TemplateManager.setRedirectFlag(map);
            } else {
                if (isCreate) {
                    map.put("plugin_type", "java");
                    map.put("enabled", "");
                    map.put("plugin", "bundle");
                } else {
                    Plugin p = PluginManager.get(pluginID);

                    map.put("priority", "" + p.getPriority());
                    map.put("name", p.getName());
                    map.put("enabled", p.isEnabled());
                    String plugin =  p.getPlugin().name().toLowerCase();
                    map.put("plugin",plugin);

                    if(p instanceof PluginJava) {
                        PluginJava pj = (PluginJava)p;
                        map.put("plugin_type", "java");
                        if(pj.isJAR()) {
                              map.put("jar", "on");
                        }
                        map.put("class", pj.getClassName());
                        if(pj.getClassPath() != null) {
                              map.put("classpath",pj.getClassPath());
                        }
                    } else {
                        map.put("plugin_type", "script");
                        PluginScript ps = (PluginScript)p;
                        if(ps.getErrorPath() != null) {
                             map.put(plugin + "_error", ps.getErrorPath());
                        }
                        if(ps.getMsgPath() != null) {
                             map.put(plugin + "_msg", ps.getMsgPath());
                        }
                        HashMap<Plugin.ACTION, Script> actionMap = ps.getActionMap();
                        Iterator<Plugin.ACTION> iter = actionMap.keySet().iterator();
                        while(iter.hasNext()) {
                            Plugin.ACTION action = iter.next();
                            String name = "script_" + plugin + "_" + action.name().toLowerCase();
                            Script script = actionMap.get(action);
                            map.put(name,script.getScript());
                            map.put(name+"_call",script.getCallLevel().ordinal());
                        }
                    }
                }
            }
        } catch (Exception e) {
            map.put("error", StackTrace.toString(e));
            SessionHelper.setLastException(request, "lastException", e);
        }
        return map;
    }
    private static String action[] = {"start", "stop", "create", "delete", "enable", "disable", "change", "change_password"};
}
