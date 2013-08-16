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
package at.sciencesoft.plugin;

import at.sciencesoft.system.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
 public class PluginManager {
    
    public static Plugin[] getPulgin(Plugin.PLUGIN plugin) {
        Iterator<String> i = pmap.keySet().iterator();
        ArrayList<Plugin> al = new ArrayList<Plugin>();
        while (i.hasNext()) {
            Plugin p = pmap.get(i.next());
            if (p.isEnabled() && p.getPlugin() == plugin) {
                al.add(p);
            }
        }
        if (al.size() > 0) {
            Plugin[] plist = pmap.values().toArray(new Plugin[0]);
            Arrays.sort(plist, new Comparator<Plugin>() {
                public int compare(Plugin a, Plugin b) {
                    return a.getPriority() - b.getPriority();
                }
            });
        }
        return al.toArray(new Plugin[al.size()]);
    }

    public static void add(Plugin p) {
        pmap.put(p.getID(), p);
    }

    public static void remove(Plugin p) {
        pmap.remove(p.getID());
    }

    public static void remove(String guid) {
        pmap.remove(guid);
    }

    public static Plugin get(String id) {
        return pmap.get(id);
    }

     public static void deletePlugin(String id) throws Exception {
        String path = Config.getInstance().getETCdir() + "plugins/" + id + ".properties";
        new File(path).delete();
        pmap.remove(id);
     }

    public static Plugin loadPlugin(String id) throws Exception {
        if(get(id) != null) {
            return get(id);
        }
        String path = Config.getInstance().getETCdir() + "plugins/" + id + ".properties";
        Properties prop = new Properties();
        synchronized (sync) {
            FileInputStream fi = new FileInputStream(path);
            prop.load(fi);
            fi.close();
            String type = (String) prop.get("type");
            String plugin = (String) prop.get("plugin");
            String name = (String) prop.get("name");
            int priority = Integer.parseInt((String) prop.get("priority"));
            String classPath = (String) prop.get("classpath");
            String className = (String) prop.get("class");
            boolean enabled = true;
            String tmp = (String) prop.get("enabled");
            if (tmp != null) {
                enabled = Boolean.parseBoolean(tmp);
            }
            boolean isJar = false;
            tmp = (String) prop.get("jar");
            if (tmp != null) {
               isJar = Boolean.parseBoolean(tmp);
            }
            if (type.equals("java")) {

                return new PluginJava(name, id, Plugin.PLUGIN.valueOf(plugin), priority, classPath, isJar, className, enabled);
            } else if (type.equals("script")) {
                HashMap<Plugin.ACTION, Script> actionMap = new HashMap<Plugin.ACTION, Script>();
                Plugin.ACTION[] list = Plugin.ACTION.values();
                for(int i = 0; i < list.length; ++i) {
                     tmp  = (String) prop.get(list[i].name());
                     if(tmp != null) {
                         String[] p = tmp.split("\\|");

                         actionMap.put(list[i],new Script(p[1],Script.CALL.valueOf(p[0])));
                     }
                }
                return new PluginScript(name, id, Plugin.PLUGIN.valueOf( plugin), priority, enabled, actionMap, (String) prop.get("error_path"), (String) prop.get("msg_path"));
            }
        }
        throw new Exception("Plugin loadPlugin(): unkown plugin type");
    }

    public static void loadAll() throws Exception {
        String dir = Config.getInstance().getETCdir() + "plugins/";
        File f = new File(dir);
        String[] list = f.list(new FilenameFilter() {
            public boolean accept(File dir,String name) {
                if(name.endsWith(".properties")) {
                    return true;
                }
                return false;
            }
        });
        if(list == null || list.length == 0) {
            return;
        }
        for(int i = 0; i < list.length; ++i) {
            f = new File(list[i]);
            String name = f.getName();
            String id = name.substring(0,name.indexOf('.'));
            pmap.put(id,loadPlugin(id));
        }
       
    }

    public static Plugin[] listAll() {
        if(pmap.size() == 0) {
            return null;
        }
        return pmap.values().toArray(new Plugin[pmap.size()]);

    }

    public static void toogleEnableStatus(String id) throws Exception {
       Plugin p = pmap.get(id);
       p.toogle();
       savePlugin(p);
    }

    public static void savePlugin(Plugin plugin) throws Exception {
        String dir = Config.getInstance().getETCdir() + "plugins/";
        File f = new File(dir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("PluginManager.savePlugin(): Unable to create directory '" + f.getAbsolutePath() + "'");
            }
        }
        dir += plugin.getID() + ".properties";
        Properties prop = new Properties();
        prop.put("name", plugin.getName());
        prop.put("priority", "" + plugin.getPriority());
        prop.put("id", plugin.getID());
        prop.put("enabled", "" + plugin.isEnabled());
        prop.put("plugin", plugin.getPlugin().name());

        if (plugin instanceof PluginJava) {
            PluginJava pj = (PluginJava) plugin;
            prop.put("type", "java");
            prop.put("jar", "" + pj.isJAR());
            String cp = pj.getClassPath();
            if (cp != null) {
                prop.put("classpath", cp);
            }
            prop.put("class", pj.getClassName());
        } else {
            prop.put("type", "script");
            PluginScript ps = (PluginScript) plugin;
            if( ps.getErrorPath() != null) {
                prop.put("error_path", ps.getErrorPath());
            }
            if(ps.getMsgPath() != null) {
                prop.put("msg_path", ps.getMsgPath());
            }
            HashMap<Plugin.ACTION, Script> map = ps.getActionMap();
            Iterator<Plugin.ACTION> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                Plugin.ACTION action = iter.next();
                Script script = map.get(action);
                prop.put(action.name(), script.getCallLevel().name() + "|" + script.getScript());
            }
        }
        synchronized (sync) {
            FileOutputStream fos = new FileOutputStream(dir);
            prop.store(fos, " Plugin : " + plugin.getName());
            fos.close();
        }
        pmap.put(plugin.getID(), plugin);
    }

    private static HashMap<String, Plugin> pmap = new HashMap<String, Plugin>();
    private final static Object sync = new Object();
}
