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
package at.sciencesoft.oxrmi;

import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * Helper class for an OX group.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXgroup {

    /**
     * Returns a list of all OX groups for a given context.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @return
     * @throws Exception
     */
    public static Group[] listAll(int contextID, Credentials auth) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.listAll(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);


        Group glist[];
        synchronized (sync) {
            glist = iface.listAll(ctx, auth);
            if (glist == null || glist.length == 0) {
                return null;
            }
            glist = iface.getData(ctx, glist, auth);
        }
        return glist;
    }

    /**
     * Deletes an OX group.
     * @param contextID contextID
     * @param auth credentials of the context admin
     * @param groupID group id
     * @throws Exception
     */
    public static void delete(int contextID, Credentials auth, int groupID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.delete(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        Group group = new Group(groupID);
        synchronized (sync) {
            iface.delete(ctx, group, auth);
        }

    }

    /**
     * Returns the members of an OX group.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @param groupID group id
     * @return
     * @throws Exception
     */
    public static User[] getMembers(int contextID, Credentials auth, int groupID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.getMembers(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        User[] user;
        synchronized (sync) {
            user = iface.getMembers(ctx, new Group(groupID), auth);
            if (user != null && user.length == 0) {
                user = null;
            }
            if (user != null) {
                OXUserInterface iface2 = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
                user = iface2.getData(ctx, user, auth);

            }
        }
        return user;
    }

    /**
     * Return an OX group.
     * @param contextID
     * @param auth credentials of the context admin
     * @param groupID
     * @return
     * @throws Exception
     */
    public static Group getGroup(int contextID, Credentials auth, int groupID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.getGroup(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        Group grp = new Group(groupID);
        synchronized (sync) {
            grp = iface.getData(ctx, grp, auth);
        }
        return grp;

    }

    /**
     * Returns all changes for a given group regarding a list of parameters.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @param groupID group ID
     * @param name name of the group
     * @param displayName display name
     * @param members  comma separated OX user ID list
     * @return returns NULL for no changes
     * @throws Exception
     */
    public static ParamMap getChanges(int contextID, Credentials auth, int groupID, String name, String displayName, String members, ParamMap paramMap) throws Exception {
        ParamMap changes = new ParamMap();

        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.getChangeMap(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        Group group = getGroup(contextID, auth, groupID);
        if (!name.trim().equals(group.getName())) {
            changes.putString(Param.NAME, name.trim());
        }

        if (!displayName.trim().equals(group.getDisplayname())) {
            changes.putString(Param.DISPLAY_NAME, displayName.trim());
        }

        User[] user = iface.getMembers(ctx, group, auth);

        ArrayList<String> addUser = new ArrayList<String>();
        ArrayList<String> removeUser = new ArrayList<String>();

        HashMap<Integer, String> map2 = new HashMap<Integer, String>();
        if (user == null || user.length == 0) {
            if (members != null && !members.equals("")) {
                String[] list = members.split(",");
                for (int i = 0; i < list.length; ++i) {
                    addUser.add(list[i]);
                    map2.put(Integer.parseInt(list[i]), list[i]);
                }

            }
        } else {
            if (members != null && !members.equals("")) {
                // split comma seperated list
                String[] list = members.split(",");
                HashMap<Integer, String> map = new HashMap<Integer, String>();

                for (int i = 0; i < list.length; ++i) {
                    map.put(new Integer(list[i]), list[i]);
                    map2.put(new Integer(list[i]), list[i]);
                }
                // find all users, which must be removed
                for (int i = 0; i < user.length; ++i) {
                    if (!map.containsKey(user[i].getId())) {
                        removeUser.add("" + user[i].getId());
                        map2.remove(user[i].getId());
                    } else {
                        map.remove(user[i].getId());
                    }
                }
                //  user
                if (!map.isEmpty()) {
                    Iterator<Integer> iter = map.keySet().iterator();
                    while (iter.hasNext()) {
                        int t = iter.next();
                        addUser.add("" + t);
                        map2.put(t, "" + t);
                    }
                }

            } else {
                // remove all members
                for (int i = 0; i < user.length; ++i) {
                    removeUser.add("" + user[i].getId());
                }
            }
        }

        if (addUser.size() > 0) {
            changes.putStringArray(Param.ADD_USER, addUser.toArray(new String[addUser.size()]));
        }
        if (removeUser.size() > 0) {
            changes.putStringArray(Param.REMOVE_USER, removeUser.toArray(new String[removeUser.size()]));
        }


        if (paramMap != null && map2.size() > 0) {
            paramMap.putStringArray(Param.ALL_USER, map2.values().toArray(new String[map2.size()]));
        }

        if (changes.size() > 0) {
            return changes;
        }

        return null;
    }

    /**
     * Changes an existing OX group.
     * @param contextID
     * @param auth credentials of the context admin
     * @param groupID group ID
     * @param changes
     * @throws Exception
     */
    public static void change(int contextID, Credentials auth, int groupID, ParamMap changes) throws Exception {
        if (changes == null) {
            return;
        }
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.change(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        Group group = getGroup(contextID, auth, groupID);
        Group newGroup = new Group(groupID);

        String name = changes.getString(Param.NAME);
        if (name != null) {
            newGroup.setName(name);
        }

        String displayName = changes.getString(Param.DISPLAY_NAME);
        if (displayName != null) {
            newGroup.setDisplayname(displayName);
        }

        synchronized (sync) {
            iface.change(ctx, newGroup, auth);

            String[] user = changes.getStringArray(Param.ADD_USER);
            User[] oxuser;
            if (user != null) {
                oxuser = new User[user.length];
                for (int i = 0; i < user.length; ++i) {
                    oxuser[i] = new User(Integer.parseInt(user[i]));
                }
                iface.addMember(ctx, group, oxuser, auth);

            }

            user = changes.getStringArray(Param.REMOVE_USER);
            if (user != null) {
                oxuser = new User[user.length];
                for (int i = 0; i < user.length; ++i) {
                    oxuser[i] = new User(Integer.parseInt(user[i]));
                }
                iface.removeMember(ctx, group, oxuser, auth);

            }
        }

    }

    /**
     * Creates an OX group.
     * @param contextID contextID
     * @param auth credentials of the context admin
     * @param name group name
     * @param displayName display name
     * @param members comma separated OX user ID list
     * @throws Exception
     */
    public static Group create(int contextID, Credentials auth, String name, String displayName, String members) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.delete(): RMIHOST is not set!");
        }

        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        Group group = new Group();

        group.setName(name);
        group.setDisplayname(displayName);

        User[] userList = null;

        if (members != null && !members.equals("")) {
            String[] list = members.split(",");
            userList = new User[list.length];
            for (int i = 0; i < list.length; ++i) {
                userList[i] = new User(Integer.parseInt(list[i]));
            }
        }
        Group newGroup = null;
        synchronized (sync) {
            newGroup = iface.create(ctx, group, auth);
            if (userList != null) {
                iface.addMember(ctx, newGroup, userList, auth);
            }

        }
        return newGroup;
    }

    /**
     * Returns the group membership of an user.
     * @param ctx
     * @param userID
     * @param auth
     * @return
     */
    public static Group[] listGroupsForUser(int contextID,
            int userID,
            Credentials auth) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXgroup.listGroupsForUser(): RMIHOST is not set!");
        }
        OXGroupInterface iface = (OXGroupInterface) Naming.lookup(oxsi.getRMIhost() + OXGroupInterface.RMI_NAME);
        return iface.listGroupsForUser(new Context(contextID), new User(userID), auth);
    }
    // synchronization object
    private static final Object sync = new Object();
}
