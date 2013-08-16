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

package at.sciencesoft.controller;

import at.sciencesoft.osgi.OXAdminGuiServletServiceRegistry;
import at.sciencesoft.system.Config;
//import com.openexchange.session.Session;
//import com.openexchange.sessiond.SessiondService;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Information {
      static public HashMap process(HashMap map,HttpServletRequest request) throws Exception {
        map.put("version",Config.getInstance().getVersion());
        map.put("buildDate",Config.getInstance().getBuildDate());
        map.put("osArch", System.getProperty("os.arch"));
        map.put("osName", System.getProperty("os.name"));
        map.put("javaVersion", System.getProperty("java.version"));
        map.put("javaVendor", System.getProperty("java.vendor"));

        /*
        String id = request.getParameter("id");
        String info = "???";
        if(id != null) {
            Session ses = OXAdminGuiServletServiceRegistry.getServiceRegistry().getService(SessiondService.class).getSession(id);
            if(ses != null) {
               info = ses.getLoginName();
            } else {
               info = "wrong ID";
            }
        }
         map.put("online",  info );
        */

        
        return map;
      }
}
