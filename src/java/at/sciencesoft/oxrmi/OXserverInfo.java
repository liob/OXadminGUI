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

import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * Class for some OX server infos.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXserverInfo {

    private OXserverInfo() {
    }

    /**
     * Returns the RMI host.
     * @return
     */
    public String getRMIhost() {
        return RMI_HOSTNAME;
    }

    /**
     * Sets the RMI host.
     * @param rmiHost
     */
    public void setRMIhost(String rmiHost) {
        if(!rmiHost.endsWith("/")) {
            rmiHost+= "/";
        }
        RMI_HOSTNAME = rmiHost;
    }

    /**
     * Sets the OX admin.
     * @param oxAdmin OX administrator
     * @param pwd password
     * @throws Exception
     */
    public void setOXadmin(String oxAdmin,String pwd) throws Exception {
         if(oxAdmin == null || pwd == null || oxAdmin.equals("") || pwd.equals("")) {
             throw new Exception("OXserverInfo.setOXadmin(): admin or pwd is empty!");
         }
         oxAdminAuth = new Credentials();
         oxAdminAuth.setLogin(oxAdmin);
         oxAdminAuth.setPassword(pwd);
    }

    /**
     * Returns the credentials of the OX administrator.
     * @return
     */
    public  Credentials getOXadmin() {
        return oxAdminAuth;
    }

    /**
     * Returns an instance of this class.
     * @return
     */
    public static synchronized OXserverInfo getInstance() {
        if(instance == null) {
            instance = new OXserverInfo();
        }
        return instance;
    }

    private String RMI_HOSTNAME;
    private Credentials oxAdminAuth;
    private static OXserverInfo instance;
}
