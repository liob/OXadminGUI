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

import at.sciencesoft.util.Crypto;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import java.util.HashMap;
import java.util.Properties;

/**
 * Helper class to cache the credentials of a context admin
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ContextCache {

    public ContextCache() {
    }

    /**
     * Returns the credentials for a given context.
     * @param contextId context ID
     * @return
     * @throws Exception
     */
    public static Credentials getContextCredentials(int contextID) throws Exception {
        Credentials auth = authMap.get(contextID);
        if (auth != null) {
            return auth;
        }
        Properties prop = OXcontextData.loadOXcontextData(contextID);
        OXserverInfo oxsi = OXserverInfo.getInstance();
        auth = new Credentials(prop.getProperty("contextAdmin"), Crypto.decrypt(prop.getProperty("pwd"), oxsi.getOXadmin().getPassword()));
        authMap.put(contextID, auth);
        return auth;
    }

    /**
     * Checks if a context supports plugins. If value is false, no plugin will be called for this context.
     * @param contextID context ID
     * @return
     * @throws Exception
     */
    public static boolean isPluginSupport(int contextID) throws Exception {

        Boolean pluginSupport = pluginMap.get(contextID);
        if (pluginSupport != null) {
            return pluginSupport;
        }

        if (OXcontext.isContextDataAvailable(contextID) == false) {
            return false;
        }
        Properties prop = OXcontextData.loadOXcontextData(contextID);

        if (prop.getProperty("pluginsupport") == null) {
            pluginMap.put(contextID, false);
            return false;
        }
        pluginMap.put(contextID, true);

        return true;

    }

    /**
     * Removes a credential from cache.
     * @param contextID id
     */
    public static void removeContextCredentials(int contextID) {
        authMap.remove(contextID);
    }

    /**
     * Removes the info, if the context supports plugins.
     * @param contextID id
     */
    public static void removePluginSupportInfo(int contextID) {
        pluginMap.remove(contextID);
    }
    private static HashMap<Integer, Credentials> authMap = new HashMap<Integer, Credentials>();
    private static HashMap<Integer, Boolean> pluginMap = new HashMap<Integer, Boolean>();
}
