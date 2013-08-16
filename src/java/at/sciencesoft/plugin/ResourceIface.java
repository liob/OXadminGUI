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
import com.openexchange.admin.rmi.dataobjects.Resource;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public interface ResourceIface {
    public ResultSet create(boolean isPost,boolean isSuccess,int contextID, Credentials auth, String name, String displayName, String description, String email,ResultSet rs,Resource resource);
    public ResultSet change(boolean isPost,boolean isSuccess,int contextID, Credentials auth, int resourceID,ParamMap changes,ResultSet rs);
    public ResultSet delete(boolean isPost,boolean isSuccess,int contextID, Credentials auth,int resID,ResultSet rs);
    public ResultSet enable(boolean isPost,boolean isSuccess,int contextID, Credentials auth,int resID,ResultSet rs);
    public ResultSet disable(boolean isPost,boolean isSuccess,int contextID, Credentials auth,int resID,ResultSet rs);
    public String getVersion();
}
