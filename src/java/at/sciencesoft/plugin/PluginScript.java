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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class PluginScript extends Plugin {

    public PluginScript(String name, String id, PLUGIN plugin, int priority, boolean enabled, HashMap<Plugin.ACTION, Script> actionMap, String errorPath, String msgPath) throws Exception {
        super(name, id, plugin, priority, enabled);

        try {
            Iterator<Plugin.ACTION> iter = actionMap.keySet().iterator();
            while(iter.hasNext()) {
                Plugin.ACTION a = iter.next();
                Script s = actionMap.get(a);
                File f = new File(s.getScript());
                if(!f.exists()) {
                    throw new Exception("Plugin.Plugin(): Missing " + a.name() + " script - '" + f.getAbsolutePath() + "'");
                }
            }
            switch (plugin) {
                case BUNDLE:
                    bundle = new BundleScriptImpl(actionMap, errorPath);
                    version = bundle.getVersion();
                    break;
                case GROUP:
                    group = new GroupScriptImpl(actionMap, errorPath, msgPath);
                    version = group.getVersion();
                    break;
                case RESOURCE:
                    resource = new ResourceScriptImpl(actionMap, errorPath, msgPath);
                    version = resource.getVersion();
                    break;
                case USER:
                    user = new UserScriptImpl(actionMap, errorPath, msgPath);
                    version = user.getVersion();
                    break;
                case CONTEXT:
                    context = new ContextScriptImpl(actionMap, errorPath, msgPath);
                    version = context.getVersion();
                    break;
            }
            this.actionMap = actionMap;
            this.errorPath = errorPath;
            this.msgPath = msgPath;

        } catch (Exception e) {
            loadError = true;
            exception = e;
            this.disable();
        }
    }

    public String getErrorPath() {
        return errorPath;
    }

    public String getMsgPath() {
        return msgPath;
    }

    public BundleIface getBundleIface() {
        return bundle;
    }

    public GroupIface getGroupIface() {
        return group;
    }

    public UserIface getUserIface() {
        return user;
    }

    public ContextIface getContextIface() {
        return context;
    }

    public ResourceIface getResourceIface() {
        return resource;
    }

    public String getType() {
        return "SCRIPT";
    }

    public String getVersion() {
        return version;
    }

    public HashMap<Plugin.ACTION, Script> getActionMap() {
        return actionMap;
    }

    private String version;
    private GroupScriptImpl group;
    private BundleScriptImpl bundle;
    private ResourceScriptImpl resource;
    private UserScriptImpl user;
    private ContextScriptImpl context;
    private HashMap<Plugin.ACTION, Script> actionMap;
    private String errorPath;
    private String msgPath;
}
