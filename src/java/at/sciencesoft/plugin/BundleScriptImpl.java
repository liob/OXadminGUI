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

import at.sciencesoft.util.ExecuteScript;
import java.util.HashMap;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class BundleScriptImpl implements BundleIface {
    public BundleScriptImpl(HashMap<Plugin.ACTION, Script> actionMap,String errorPath) {
        this.actionMap = actionMap;
        this.errorPath = errorPath;
    }
    public ResultSet startBundle() {
        Script script = actionMap.get(Plugin.ACTION.START);
        if(script != null) {
            return ExecuteScript.call(new String[] { script.getScript(),Plugin.PLUGIN.BUNDLE.name(), Plugin.ACTION.START.name() }, errorPath, null);
        }
        return new ResultSet(true);

    }
    public ResultSet stopBundle() {
        Script script = actionMap.get(Plugin.ACTION.STOP);
        if(script != null) {
            return ExecuteScript.call(new String[] { script.getScript(), Plugin.PLUGIN.BUNDLE.name(), Plugin.ACTION.STOP.name()}, errorPath, null);
        }
        return new ResultSet(true);

    }
    public String getVersion() {
        return "0.1";
    }
    private HashMap<Plugin.ACTION, Script> actionMap;
    private String errorPath;
}
