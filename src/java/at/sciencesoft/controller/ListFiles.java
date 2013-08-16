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

import at.sciencesoft.system.Config;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ListFiles {

    public static void process(HttpServletRequest request, HashMap map) {
        try {
            String[] list = Config.getInstance().getSystemFiles();
            String[] logs = Config.getInstance().getLogFiles();
            map.put("propFiles", list);
            map.put("logFiles", logs);
            SessionHelper.setSystemFiles(request, "propFiles", list);
            SessionHelper.setSystemFiles(request, "logFiles", logs);
        } catch (Exception e) {
            map.put("error", StackTrace.toString(e));
            SessionHelper.setLastException(request, "lastException", e);
        }
    }
}
