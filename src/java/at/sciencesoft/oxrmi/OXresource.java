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

import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import java.rmi.Naming;

/**
 * Helper class for an OX resource.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXresource {

    /**
     * Returns a list of all OX resources for a given context.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @return
     * @throws Exception
     */
    static public Resource[] listAll(int contextID, Credentials auth) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.listAll(): RMIHOST is not set!");
        }
        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        Resource[] rlist = null;
        synchronized (sync) {
            rlist = iface.listAll(ctx, auth);
            if (rlist == null || rlist.length == 0) {
                return null;
            }
            rlist = iface.getData(ctx, rlist, auth);
        }
        return rlist;
    }

    /**
     * Disables an OX resource.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @param resourceID resource ID
     * @throws Exception
     */
    static public void disable(int contextID, Credentials auth, int resourceID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.disable(): RMIHOST is not set!");
        }
        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        Resource res = new Resource(resourceID);
        res.setAvailable(false);
        synchronized (sync) {
            iface.change(ctx, res, auth);
        }
    }

     /**
     * Enables an OX resource.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @param resourceID resource ID
     * @throws Exception
     */
    static public void enable(int contextID, Credentials auth, int resourceID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.disable(): RMIHOST is not set!");
        }
        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        Resource res = new Resource(resourceID);
        res.setAvailable(true);
        synchronized (sync) {
            iface.change(ctx, res, auth);
        }
    }

     /**
     * Deletes an OX resource.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @param resourceID resource ID
     * @throws Exception
     */
    static public void delete(int contextID, Credentials auth, int resourceID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.disable(): RMIHOST is not set!");
        }
        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        Resource res = new Resource(resourceID);
        synchronized (sync) {
            iface.delete(ctx, res, auth);
        }
    }

    /**
     * Returns an OX resource.
     * @param contextID context ID
     * @param auth credentials of the context admin
     * @param resourceID resource ID
     * @return
     * @throws Exception
     */
    static public Resource getResource(int contextID, Credentials auth, int resourceID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.getResoucre(): RMIHOST is not set!");
        }

        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        return iface.getData(ctx, new Resource(resourceID), auth);
    }

    /**
     * Creates an OX resource.
     * @param contextID context ID
     * @param auth  credentials of the context admin
     * @param name  name of the resource
     * @param displayName display name
     * @param description resource description
     * @param email email associated with the resource
     * @throws Exception
     */
    static public Resource create(int contextID, Credentials auth, String name, String displayName, String description, String email) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.create(): RMIHOST is not set!");
        }
        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        Resource res = new Resource();
        res.setName(name);
        res.setDisplayname(displayName);
        res.setDescription(description);
        res.setEmail(email);
        Resource newResource = null;
        synchronized (sync) {
            newResource = newResource = iface.create(ctx, res, auth);
        }
        return newResource;
    }
    
    /**
     * Returns all changes for a given resource regarding a list of parameters.
     * @param contextID contextID context ID
     * @param auth credentials of the context admin
     * @param resourceID resource ID
     * @param name name of the resource
     * @param displayName source description
     * @param description resource description
     * @param email email associated with the resource
     * @return
     * @throws Exception
     */
    static public ParamMap getChanges(int contextID, Credentials auth, int resourceID, String name, String displayName, String description, String email) throws Exception {
        Resource res = getResource(contextID, auth, resourceID);

        ParamMap map = new ParamMap();

        if (!res.getName().equals(name.trim())) {
            map.putString(Param.NAME, name.trim());
        }

        if (!res.getDisplayname().equals(displayName.trim())) {
            map.putString(Param.DISPLAY_NAME, displayName.trim());
        }

        if (!res.getEmail().equals(email.trim())) {
            map.putString(Param.EMAIL, email.trim());
        }

        if (res.getDescription() == null && !description.trim().equals("")) {
            map.putString(Param.DESCRIPTION, description.trim());

        } else {
            if (!res.getDescription().equals(description.trim())) {
                map.putString(Param.DESCRIPTION, description.trim());
            }
        }
        if (map.size() > 0) {
            return map;
        }
        return null;

    }

    /**
     * Changes an OX resource
     * @param contextID context ID
     * @param auth  credentials of the context admin
     * @param resourceID resource ID
     * @param changes changes
     * @throws Exception
     */
    static public void change(int contextID, Credentials auth, int resourceID, ParamMap changes) throws Exception {
        if (changes == null) {
            return;
        }
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXresource.change(): RMIHOST is not set!");
        }
        OXResourceInterface iface = (OXResourceInterface) Naming.lookup(oxsi.getRMIhost() + OXResourceInterface.RMI_NAME);
        Context ctx = new Context(contextID);

       
        Resource newRes = new Resource(resourceID);

        String name = changes.getString(Param.NAME);
        if (name != null) {
            newRes.setName(name);
        }

        String displayName = changes.getString(Param.DISPLAY_NAME);
        if (displayName != null) {
            newRes.setDisplayname(displayName);
        }

        String email = changes.getString(Param.EMAIL);
        if (email != null) {
            newRes.setEmail(email);
        }

        String description = changes.getString(Param.DESCRIPTION);
        if (description != null) {
            newRes.setDescription(description);
        }

        synchronized (sync) {
            iface.change(ctx, newRes, auth);
        }

    }

    // synchronization object
    private static final Object sync = new Object();
}
