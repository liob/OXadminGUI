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

import at.sciencesoft.oxrmi.ParamMap;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import java.util.HashMap;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class UserImpl implements UserIface {

    public ResultSet create(boolean isPost, boolean isSuccess,int contextID, Credentials auth, String loginName, String displayName, String firstName, String lastName, String pwd, String email,ParamMap map, ResultSet rs,User user) {
        return new ResultSet(true);
    }

    public ResultSet change(boolean isPost,boolean isSuccess, int contextID, Credentials auth, int userID, ParamMap changes,ParamMap pluginMap,ResultSet rs) {
        return new ResultSet(true);
    }

    public ResultSet delete(boolean isPost,boolean isSuccess, int contextID, Credentials auth, int userID, ResultSet rs) {
        return new ResultSet(true);
    }

    public void changePassword(int contextID,int userID,String oldPasssword,String newPassword) {

    }

    public void loadGUIdata(int contextID,int userID,HashMap map) {

    }

    public boolean checkGUIflag(FIELD field) {
         return false;
    }
    
    public String getVersion() {
         return "0.1";
    }
}
