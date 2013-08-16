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

import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import java.lang.reflect.Method;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ReflectionModuleAccessMapper {
    public ReflectionModuleAccessMapper(UserModuleAccess moduleAccess) throws Exception  {
        if(clazz == null) {
           clazz  = Class.forName("com.openexchange.admin.rmi.dataobjects.UserModuleAccess");
        }
        this.moduleAccess = moduleAccess;
    }

    public boolean getValue(String methodName)  throws Exception {
        Method method = clazz .getMethod(methodName,(Class[])null);
        return (Boolean)method.invoke(moduleAccess,(Object[])null);
    }

    private UserModuleAccess moduleAccess;
    private static Class clazz;
}
