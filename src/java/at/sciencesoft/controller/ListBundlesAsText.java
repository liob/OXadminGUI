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

import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.webserver.TemplateHandler;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ListBundlesAsText implements TemplateHandler {

    public void init() throws Exception {
    }

    public HashMap process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String msg = SessionHelper.getLastMessage(request,"lastOSGIbundles");
        HashMap map = new HashMap();
        map.put("bundles",  msg);
        return map;
    }
}
