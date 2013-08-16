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

import at.sciencesoft.oxrmi.Param;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.util.ExecuteScript;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class UserScriptImpl implements UserIface {

    public UserScriptImpl(HashMap<Plugin.ACTION, Script> actionMap, String errorPath, String msgPath) {
        this.actionMap = actionMap;
        this.errorPath = errorPath;
        this.msgPath = msgPath;
    }

    public ResultSet create(boolean isPost, boolean isSuccess, int contextID, Credentials auth, String loginName, String displayName, String firstName, String lastName, String pwd, String email, ParamMap map, ResultSet rs, User user) {
        Script script = actionMap.get(Plugin.ACTION.CREATE);
        boolean call = false;
        if (script != null) {
            if (!isPost) {
                if ((script.getCallLevel() == Script.CALL.BEFORE || script.getCallLevel() == Script.CALL.BOTH)) {
                    call = true;
                }
            } else {
                if ((script.getCallLevel() == Script.CALL.AFTER || script.getCallLevel() == Script.CALL.BOTH)) {
                    call = true;
                }
            }
        }

        if (call) {
            ArrayList<String> al = new ArrayList<String>();
            al.add(script.getScript());
            al.add(Plugin.PLUGIN.USER.name());
            al.add(Plugin.ACTION.CREATE.name());
            al.add("" + (isPost ? 1 : 0));
            al.add("" + contextID);

            al.add("\"" + ExecuteScript.escapeCharacter(loginName) + "\"");
            al.add("\"" + ExecuteScript.escapeCharacter(displayName) + "\"");
            al.add("\"" + ExecuteScript.escapeCharacter(firstName) + "\"");
            al.add("\"" + ExecuteScript.escapeCharacter(lastName) + "\"");
            al.add("\"" + ExecuteScript.escapeCharacter(pwd) + "\"");
            al.add("\"" + ExecuteScript.escapeCharacter(email) + "\"");

            if (map.size() > 0) {

                Iterator<Param> iter = map.getIterator();
                while (iter.hasNext()) {
                    Param p = iter.next();
                    al.add(p.name());
                    al.add("\"" + ExecuteScript.escapeCharacter(map.toString(p)) + "\"");
                }
            }
            return ExecuteScript.call(al.toArray(new String[0]), errorPath, msgPath);
        }
        return new ResultSet(true);
    }

    public void changePassword(int contextID, int userID, String oldPassword, String newPassword) {
        ArrayList<String> al = new ArrayList<String>();
        Script script = actionMap.get(Plugin.ACTION.CHANGE_PASSWORD);
        if(script != null) {
            al.add(script.getScript());
            al.add(Plugin.PLUGIN.USER.name());
            al.add(Plugin.ACTION.CHANGE_PASSWORD.name());
            al.add("" + contextID);
            al.add("" + userID);
            al.add("\"" + ExecuteScript.escapeCharacter(oldPassword) + "\"");
            al.add("\"" + ExecuteScript.escapeCharacter(newPassword) + "\"");
            ExecuteScript.call(al.toArray(new String[0]), errorPath, msgPath);
        }
    }

    public ResultSet change(boolean isPost, boolean isSuccess, int contextID, Credentials auth, int userID, ParamMap changes, ParamMap pluginMap, ResultSet rs) {
        if (changes == null) {
            return new ResultSet(true);
        }
        Script script = actionMap.get(Plugin.ACTION.CHANGE);
        boolean call = false;
        if (script != null) {
            if (!isPost) {
                if ((script.getCallLevel() == Script.CALL.BEFORE || script.getCallLevel() == Script.CALL.BOTH)) {
                    call = true;
                }
            } else {
                if ((script.getCallLevel() == Script.CALL.AFTER || script.getCallLevel() == Script.CALL.BOTH)) {
                    call = true;
                }
            }
        }

        if (call) {
            ArrayList<String> al = new ArrayList<String>();
            al.add(script.getScript());
            al.add(Plugin.PLUGIN.USER.name());
            al.add(Plugin.ACTION.CHANGE.name());
            al.add("" + (isPost ? 1 : 0));
            al.add("" + contextID);
            al.add("" + userID);
            Iterator<Param> iter = changes.getIterator();
            while (iter.hasNext()) {
                Param p = iter.next();
                al.add(p.name());
                al.add("\"" + ExecuteScript.escapeCharacter(changes.toString(p)) + "\"");
            }

            return ExecuteScript.call(al.toArray(new String[0]), errorPath, msgPath);
        }
        return new ResultSet(true);

    }

    public ResultSet delete(boolean isPost, boolean isSuccess, int contextID, Credentials auth, int userID, ResultSet rs) {
        Script script = actionMap.get(Plugin.ACTION.DELETE);
        boolean call = false;
        if (script != null) {
            if (!isPost) {
                if ((script.getCallLevel() == Script.CALL.BEFORE || script.getCallLevel() == Script.CALL.BOTH)) {
                    call = true;
                }
            } else {
                if ((script.getCallLevel() == Script.CALL.AFTER || script.getCallLevel() == Script.CALL.BOTH)) {
                    call = true;
                }
            }
        }

        if (call) {
            ArrayList<String> al = new ArrayList<String>();
            al.add(script.getScript());
            al.add(Plugin.PLUGIN.USER.name());
            al.add(Plugin.ACTION.DELETE.name());
            al.add("" + (isPost ? 1 : 0));
            al.add("" + contextID);
            al.add("" + userID);
            return ExecuteScript.call(al.toArray(new String[0]), errorPath, msgPath);
        }
        return new ResultSet(true);
    }

    public void loadGUIdata(int contextID, int userID, HashMap map) {
    }

    public boolean checkGUIflag(FIELD field) {
        return false;
    }

    public String getVersion() {
        return "0.1";
    }
    private HashMap<Plugin.ACTION, Script> actionMap;
    private String errorPath;
    private String msgPath;
}
