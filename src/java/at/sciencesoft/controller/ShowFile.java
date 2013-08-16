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
import at.sciencesoft.util.StackTrace;
import at.sciencesoft.util.Stream;
import at.sciencesoft.webserver.TemplateHandler;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ShowFile implements TemplateHandler {

    public void init() throws Exception {
    }

    public HashMap process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String file = "Error!";
        String[] list;
        try {
            String index = request.getParameter("index");
            switch (index.charAt(0)) {
                case 'p':
                    list = SessionHelper.getSystemFiles(request, "propFiles");
                    file = new String(Stream.readCharStream(list[Integer.parseInt(index.substring(1))]));
                    break;
                 case 'l':
                    list = SessionHelper.getSystemFiles(request, "logFiles");
                    file = new String(Stream.readCharStream(list[Integer.parseInt(index.substring(1))]));
                    break;
            }
        } catch (Exception e) {
            file = StackTrace.toString(e);
        }
        HashMap map = new HashMap();
        map.put("fileContent", file);
        return map;
    }
}
