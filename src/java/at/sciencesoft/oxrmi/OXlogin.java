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

import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import java.rmi.Naming;

/**
 * Class for login as OX admin and as OX user.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXlogin {

    /**
     * Login as OX administrator.
     * @return true for valid credentials
     * @throws Exception
     */
    static public boolean login2Admin() throws Exception {
        try {
            OXserverInfo oxsi = OXserverInfo.getInstance();
            if (oxsi.getRMIhost() == null) {
                throw new Exception("OXLogin.login2Admin()): RMIHOST is not set!");
            }
            OXLoginInterface iface = (OXLoginInterface) Naming.lookup(oxsi.getRMIhost() + OXLoginInterface.RMI_NAME);
            iface.login(oxsi.getOXadmin());
            return true;
        } catch (InvalidCredentialsException e) {
            return false;
        }
    }

    /**
     * Login as OX administrator.
     * @param admin admin login
     * @param pwd password
     * @return true for valid credentials
     * @throws Exception
     */
    static public boolean login2Admin(String admin, String pwd) throws Exception {
        try {
            OXserverInfo oxsi = OXserverInfo.getInstance();
            if (oxsi.getRMIhost() == null) {
                throw new Exception("OXLogin.login2Admin()): RMIHOST is not set!");
            }
            OXLoginInterface iface = (OXLoginInterface) Naming.lookup(oxsi.getRMIhost() + OXLoginInterface.RMI_NAME);
            Credentials auth = new Credentials();
            auth.setLogin(admin);
            auth.setPassword(pwd);
            iface.login(auth);
            return true;
        } catch (InvalidCredentialsException e) {
            return false;
        }
    }

    /**
     * Login as OX user.
     * @param contextID context ID
     * @param user OX user
     * @param pwd password
     * @return true for valid credentials
     * @throws Exception
     */
    static public User login2User(int contextID, String user, String pwd) throws Exception {
        try {
            OXserverInfo oxsi = OXserverInfo.getInstance();
            if (oxsi.getRMIhost() == null) {
                throw new Exception("OXLogin.login2User(): RMIHOST is not set!");
            }
            OXLoginInterface iface = (OXLoginInterface) Naming.lookup(oxsi.getRMIhost() + OXLoginInterface.RMI_NAME);
            Context ctx = new Context(contextID);
            Credentials auth = new Credentials();
            auth.setLogin(user);
            auth.setPassword(pwd);
            return iface.login2User(ctx, auth);
        } catch (InvalidCredentialsException e) {
            return null;
        }
    }

    public static void main(String args[]) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        oxsi.setRMIhost("rmi://localhost:1099/");
        login2Admin("oxadminmaster","terbium"); 
        
    }
    
}
