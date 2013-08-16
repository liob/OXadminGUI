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

import at.sciencesoft.system.Config;
import java.rmi.Naming;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Wrapper ans helper class for an OX context.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXcontext {

    /**
     * Constructor
     * @param context OX context
     */
    public OXcontext(Context context) {
        this.context = context;
    }

    /**
     * Returns the OX context ID.
     * @return OX context ID
     */
    public int getID() {
        return context.getId();
    }

    /**
     * Returns the name of the context.
     * @return OX context name
     */
    public String getName() {
        return context.getName();
    }

    /**
     * Returns the file quota of the context.
     * @return file quota of the context, returns -1 if this value isn' available
     */
    public long getMaxQuota() {
        Long l = context.getMaxQuota();
        // this value can be NULL!
        if (l == null) {
            return -1;
        }
        return l;
    }

    /**
     * Returns the used file quota of the context.
     * @return used file quota of the context, returns -1 if this value isn' available
     */
    public long getUsedQuota() {
        Long l = context.getUsedQuota();
        if (l == null) {
            return -1;
        }
        return l;
    }

    /**
     * Returns the mappings of a OX context.
     * @return mappings in form of a comma separated list
     */
    public String getLoginMappings() {
        HashSet<String> hs = context.getLoginMappings();
        if (hs == null) {
            return "";
        }
        Iterator<String> iter = hs.iterator();
        StringBuffer buf = new StringBuffer();
        while (iter.hasNext()) {
            buf.append(iter.next());
            if (iter.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    /**
     * Returns the mappings of an OX context without the context name.
     * @return mappings in form of a comma separated list
     */
    public String getLoginMappingsWithoutContextName() {
        HashSet<String> hs = context.getLoginMappings();
        if (hs == null) {
            return "";
        }
        Iterator<String> iter = hs.iterator();
        StringBuffer buf = new StringBuffer();
        while (iter.hasNext()) {
            String name = iter.next();
            if (name.equals(context.getName())) {
                continue;
            }
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(name);

        }
        return buf.toString();
    }

    /**
     * Returns the name of a predefined user access definition.
     * @return name of the access definition, returns 'UNKOWN' if this additional info isn't availabe.
     * @throws Exception
     */
    public String getAccessCombinationName() throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.getAccessCombinationName(): RMIHOST is not set!");
        }
        Credentials auth = oxsi.getOXadmin();

        if (auth == null) {
            throw new Exception("OXcontext.getAccessCombinationName(): OXadmin is not set!");
        }
        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        String tmp = iface.getAccessCombinationName(this.getOXContext(), auth);
        if (tmp == null) {
            tmp = "UNKNOWN";
        }
        return tmp;
    }

    /**
     * Returns the name of a predefined user access definition for a give OX context.
     * @param OX context ID
     * @return name of the access definition, returns 'UNKOWN' if this additional info isn't availabe.
     * @throws Exception
     */
    public static String getAccessCombinationName(Context ctx) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext. getAccessCombinationName(): RMIHOST is not set!");
        }
        Credentials auth = oxsi.getOXadmin();

        if (auth == null) {
            throw new Exception("OXcontext. getAccessCombinationName(): OXadmin is not set!");
        }
        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        String tmp = iface.getAccessCombinationName(ctx, auth);
        if (tmp == null) {
            tmp = "UNKNOWN";
        }
        return tmp;
    }

    /**
     * Returns the mappings of a OX context
     * @return OX context list
     */
    public String[] getLoginMappingsList() {
        HashSet<String> hs = context.getLoginMappings();
        if (hs == null) {
            return null;
        }
        Iterator<String> iter = hs.iterator();
        ArrayList<String> al = new ArrayList<String>();
        while (iter.hasNext()) {
            al.add(iter.next());
        }
        return al.toArray(new String[0]);
    }

    /**
     * Returns if the OX context is enabled.
     * @return
     */
    public boolean isEnabled() {
        return context.isEnabled();
    }

    /**
     * Returns if the OX context is disabled.
     * @return
     */
    public Context getOXContext() {
        return context;
    }

    /**
     * Checks if property file, which is associated with the context, is available.
     * @return
     */
    public boolean isContextDataAvailable() {
        return new File(Config.getInstance().getETCdir() + "context/" + getID() + ".properties").exists();
    }

    public static boolean isContextDataAvailable(int ctxID) {
        return new File(Config.getInstance().getETCdir() + "context/" + ctxID + ".properties").exists();
    }

    /**
     * Returns the next free context ID.
     * @return
     * @throws Exception
     */
    public static OXcontextIDinfo getNextFreeID() throws Exception {
        OXcontext[] list = listAll();
        if (list == null) {
            // start with context ID 1
            return new OXcontextIDinfo(1, null);
        }
        int[] iarray = new int[list.length];
        for (int i = 0; i < list.length; ++i) {
            iarray[i] = list[i].getID();
        }
        // traverse the list of IDs to find the first free ID
        Arrays.sort(iarray);
        int left = 1;
        int right = 0;
        int freeID = -1;
        for (int i = 0; i < list.length; ++i) {
            right = list[i].getID();
            if (right - left > 1) {
                if (i == 0) {
                    freeID = 1;
                } else {
                    freeID = left + 1;
                }
                break;
            }
            left = right;
        }
        if (freeID == -1) {
            freeID = right + 1;
        }
        return new OXcontextIDinfo(freeID, iarray);

    }

    /**
     * Returns a list of OX contexts considering a search pattern
     * @param searchPattern
     * @return OX context list
     * @throws Exception
     */
    public static OXcontext[] list(String searchPattern) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.list(): RMIHOST is not set!");
        }

        Credentials auth = oxsi.getOXadmin();

        if (auth == null) {
            throw new Exception("OXcontext.list(): OXadmin is not set!");
        }

        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        Context[] clist;
        synchronized (sync) {
            clist = iface.list(searchPattern, oxsi.getOXadmin());
            if (clist == null || clist.length == 0) {
                return null;
            }
            clist = iface.getData(clist, auth);
        }
        OXcontext[] list = new OXcontext[clist.length];
        for (int i = 0; i < clist.length; ++i) {
            list[i] = new OXcontext(clist[i]);
        }
        return list;
    }

    /**
     * Returns the OX context for a given ID.
     * @param id context ID
     * @return
     * @throws Exception
     */
    public static OXcontext getContext(int id) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.getContext(): RMIHOST is not set!");
        }

        Credentials auth = oxsi.getOXadmin();

        if (auth == null) {
            throw new Exception("OXcontext.getContext(): OXadmin is not set!");
        }

        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        Context ctx = null;
        synchronized (sync) {
            ctx = iface.getData(new Context(id), auth);
        }
        if (ctx != null) {
            return new OXcontext(ctx);
        }
        return null;
    }

    /**
     * Returns a list of OX contexts.
     * @return
     * @throws Exception
     */
    public static OXcontext[] listAll() throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.listAll(): RMIHOST is not set!");
        }

        Credentials auth = oxsi.getOXadmin();

        if (auth == null) {
            throw new Exception("OXcontext.listAll(): OXadmin is not set!");
        }

        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);

        Context[] clist;
        synchronized (sync) {
            clist = iface.listAll(oxsi.getOXadmin());

            if (clist == null || clist.length == 0) {
                return null;
            }

            clist = iface.getData(clist, auth);
        }
        OXcontext[] list = new OXcontext[clist.length];

        for (int i = 0; i < clist.length; ++i) {
            list[i] = new OXcontext(clist[i]);
        }
        return list;
    }

    /**
     * Deletes an OX context
     * @param contextID context ID
     * @param action additional action within the synchronized delete operation
     * @throws Exception
     */
    public static void delete(int contextID, UserAction action) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.delete(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.delete(): OXadmin is not set!");
        }
        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        Context context = new Context(contextID);
        synchronized (sync) {
            iface.delete(context, oxsi.getOXadmin());
            if (action != null) {
                action.doJob(contextID);
            }
        }
    }

    /**
     * Disables an OX context.
     * @param contextID OX context ID
     * @throws Exception
     */
    public static void disable(int contextID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.disable(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.disable(): OXadmin is not set!");
        }

        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        Context context = new Context(contextID);
        synchronized (sync) {
            iface.disable(context, oxsi.getOXadmin());
        }
    }

    /**
     * Enables an OX context.
     * @param contextID OX context ID
     * @throws Exception
     */
    public static void enable(int contextID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.enable(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.enable(): OXadmin is not set!");
        }

        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        Context context = new Context(contextID);
        synchronized (sync) {
            iface.enable(context, oxsi.getOXadmin());
        }
    }

    /**
     * Downgrades the rights of an OX context. Remove DB objects with higher rights.
     * @param contextID OX context ID
     * @throws Exception
     */
    public static void downgrade(int contextID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.downgrade(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.downgrade(): OXadmin is not set!");
        }
        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);
        Context context = new Context(contextID);
        synchronized (sync) {
            iface.downgrade(context, oxsi.getOXadmin());
        }
    }

    /**
     * Returns the changes of an OX context.
     * @param contextID context ID
     * @param contextName OX context name
     * @param mapping context mapping
     * @param quota file quota
     * @param accessCombination name of the right access mask
     * @return
     * @throws Exception
     */
    public static ParamMap getChanges(int contextID, String contextName, String[] mapping, long quota, String accessCombination) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.changeContext(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.changeContext(): OXadmin is not set!");
        }

        OXcontext oldContext = OXcontext.getContext(contextID);

        ParamMap map = new ParamMap();

        if (contextName != null && !contextName.trim().equals(oldContext.getName())) {
            map.putString(Param.NAME, contextName.trim());
        }

        if (quota > 0 && quota != oldContext.getMaxQuota()) {
            map.putInt(Param.FILE_QUOTA, (int) quota);
        }

        if (mapping != null) {
            HashSet<String> hset = new HashSet<String>();
            for (int i = 0; i < mapping.length; ++i) {
                hset.add(mapping[i].trim());
            }
            // prevent remove actual context name from mapping
            hset.add(contextName);

            HashSet<String> oldHset = oldContext.getOXContext().getLoginMappings();

            if (oldHset == null) {
                map.putStringArray(Param.MAPPING, hset.toArray(new String[hset.size()]));
            } else {
                if (oldHset.size() != hset.size()) {
                     map.putStringArray(Param.MAPPING, hset.toArray(new String[hset.size()]));
                } else {
                    for (Iterator<String> iter = hset.iterator(); iter.hasNext();) {
                        if (!oldHset.contains(iter.next())) {
                            map.putStringArray(Param.MAPPING, hset.toArray(new String[hset.size()]));
                            break;
                        }
                    }
                }
            }
        }
       
        String oldAccessCombination = oldContext.getAccessCombinationName();
        if (!oldAccessCombination.equals(accessCombination)) {
            map.putString(Param.ACCESS_COMBINATION, accessCombination);
        }

        if (map.size() > 0) {
            return map;
        }
        return null;
    }

    /**
     * Changes an OX context.
     * @param contextID context ID
     * @param ParamMap change map
     * @param downgrade if the new <i>accessCombination</i> implies less user rights, the user can fireup a downgrade of the underlying DB.
     * @param action user action within the synchronized change operation
     * @throws Exception
     */
    public static void change(final int contextID, ParamMap changes, boolean downgrade, UserAction action) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.changeContext(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.changeContext(): OXadmin is not set!");
        }
        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);

        Context context = null;

        if (changes != null) {
            context = new Context(contextID);
            String tmp = changes.getString(Param.NAME);
            if (tmp != null) {
                context.setName(tmp);
            }

            Integer quota = changes.getInt(Param.FILE_QUOTA);
            if (quota != null) {
                context.setMaxQuota((long) quota);
            }

            String[] mapping = changes.getStringArray(Param.MAPPING);
            if (mapping != null) {
                HashSet<String> hset = new HashSet<String>();
                for (int i = 0; i < mapping.length; ++i) {
                    hset.add(mapping[i].trim());
                }
                context.setLoginMappings(hset);
            }
        }

        if (changes != null) {
            synchronized (sync) {
                iface.change(context, oxsi.getOXadmin());
                if (action != null) {
                    action.doJob(contextID);
                }
            }
        } else {
            action.doJob(contextID);
        }

        // downgrade DB?
        if (changes != null) {
            String accessCombination = changes.getString(Param.ACCESS_COMBINATION);
            if (accessCombination != null) {
                iface.changeModuleAccess(context, accessCombination, oxsi.getOXadmin());
                if (downgrade) {
                    downgrade(contextID);
                }
            }
        }
    }

    /**
     * Registers all known user access definitions.
     * See: /opt/open-xchange/etc/admindaemon/ModuleAccessDefinitions.properties
     * @param list access definitions
     */
    public static void registerAccessCombinationName(String[] list) {
        accessRightMap = new HashMap<String, Integer>();
        int ordinal = list.length;
        for (int i = 0; i < list.length; ++i) {
            accessRightMap.put(list[i], ordinal--);
        }
    }

    /**
     * Returns the ordinal number of an  access definition
     * @param name name of hte access definition
     * @return
     * @throws Exception
     * @deprecated The intention was to use ordinals to indicate if a downgrade is possible. But this
     * method is too uncertain.
     */
    public static int getAccessCombinationNameOrdinal(String name) throws Exception {
        Integer ordinal = accessRightMap.get(name);
        if (ordinal == null) {
            throw new Exception("OXcontext.getAccessCombinationNameOrdinal(): Unkown AccessCombinationName " + name);
        }
        return ordinal;
    }

    /**
     * Creates an OX context.
     * @param contextID OX context ID
     * @param contextName OX context name
     * @param mapping context mapping
     * @param quota file quota
     * @param accessCombination name of the right access mask
     * @param loginName login of the CTX admin
     * @param displayName display name of the CTX admin
     * @param firstName first name of the CTX admin
     * @param lastName last name of the CTX admin
     * @param pwd  password of the CTX admin
     * @param email email of the CTX admin
     * @param lang language of the CTX admin
     * @param timeZone time zone of the CTX admin
     * @param action user action within the synchronized create operation
     * @throws Exception
     */
    public static OXcontext create(int contextID, String contextName, String[] mapping, long quota, String accessCombination, String loginName, String displayName,
            String firstName, String lastName, String pwd, String email, String lang, String timeZone, ParamMap contextInfo, UserAction action) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.createContext(): RMIHOST is not set!");
        }
        if (oxsi.getOXadmin() == null) {
            throw new Exception("OXcontext.createContext(): OXadmin is not set!");
        }
        OXContextInterface iface = (OXContextInterface) Naming.lookup(oxsi.getRMIhost() + OXContextInterface.RMI_NAME);

        if (contextID <= 0) {
            throw new Exception("OXcontext.createContext(): contextID must be greater than zero!");
        }

        Context context = new Context(contextID);
        context.setName(contextName);

        if (quota > 0) {
            context.setMaxQuota(quota);
        }

        if (mapping != null) {
            HashSet<String> hset = new HashSet<String>();
            for (int i = 0; i < mapping.length; ++i) {
                hset.add(mapping[i].trim());
            }
            context.setLoginMappings(hset);
        } else {
            context.setLoginMappings(new HashSet<String>());
        }

        User user = new User();
        // displayed name
        if (displayName == null || displayName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): displayName is empty!");
        }
        user.setDisplay_name(displayName.trim());
        // first name
        if (firstName == null || firstName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): firstName is empty!");
        }
        user.setGiven_name(firstName.trim());
        // last name
        if (lastName == null || lastName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): lastName is empty!");
        }
        user.setSur_name(lastName.trim());
        if (loginName == null || loginName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): loginName is empty!");
        }
        user.setName(loginName.trim());
        if (pwd == null || pwd.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): pwd is empty!");
        }
        user.setPassword(pwd.trim());
        // email
        if (email == null || email.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): email is empty!");
        }
        user.setMailenabled(true);
        user.setPrimaryEmail(email);
        user.setEmail1(email);
        if (lang != null) {
            user.setLanguage(lang);
        }
        if (timeZone != null) {
            user.setTimezone(timeZone);
        }
        if (contextInfo != null) {
            String imapServer = contextInfo.getString(Param.IMAP_SERVER);
            if (imapServer != null && !imapServer.equals("")) {
                user.setImapServer(imapServer);
            }
            String smtpServer = contextInfo.getString(Param.SMTP_SERVER);
            if (smtpServer != null && !smtpServer.equals("")) {
                user.setSmtpServer(smtpServer);
            }
            // TODO: set appopriate IMAP login
            user.setImapLogin(email);
        }


        Context newContext = null;

        synchronized (sync) {
            newContext = iface.create(context, user, accessCombination, oxsi.getOXadmin());
            if (action != null) {
                action.doJob(contextID);
            }
        }
        return new OXcontext(newContext);
    }
    private Context context;
    private static HashMap<String, Integer> accessRightMap;
    private static final Object sync = new Object();
}
