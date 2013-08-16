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

import at.sciencesoft.oxrmi.OXbundleInfo;
import at.sciencesoft.oxrmi.OXbundles;
import at.sciencesoft.util.SessionHelper;
import at.sciencesoft.util.StackTrace;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ListBundles {
    public static void process( HttpServletRequest request,HashMap map) {
        try {
            OXbundleInfo[] list = new OXbundles().getBundleList();
            if(list != null) {
                int size = list.length / 2;
                if(list.length %2 != 0) {
                    ++size;
                }
                String[][] values = new String[size][];
                int i;
                for(i= 0; i < size; ++i) {
                    String[] v = new String[4];
                    values[i] = v;
                    v[0] = list[i].getBundleName();
                    v[1] = list[i].getStatus();
                }
                int index = size;
                for(i = 0; i < list.length - size; ++i) {
                    String[] v =  values[i];
                    v[2] = list[index].getBundleName();
                    v[3] = list[index].getStatus();
                    ++index;
                }
                if(list.length  %2 != 0) {
                    String[] v =  values[i];
                    v[2] = "&nbsp;";
                    v[3] = "&nbsp;";
                }
                 map.put("bundleList",values);
                 StringBuffer text = new StringBuffer();
                 int max = 0;
                 for(i = 0; i < list.length; ++i) {
                       max = Math.max(list[i].getBundleName().length(),max);
                 }
                 for(i = 0; i < list.length; ++i) {
                       String tmp = list[i].getBundleName();
                       text.append(tmp);
                       int end =  max - tmp.length() + 4;
                       for(int j = 0; j < end; ++j) {
                           text.append(' ');
                       }
                       text.append(list[i].getStatus());
                       text.append("\r\n");
                 }

                 SessionHelper.setLastMessage(request,"lastOSGIbundles",text.toString());
            }
           
        } catch(Exception e) {
             map.put("error", StackTrace.toString(e));
             SessionHelper.setLastException(request,"lastException", e);
        }
    }
}
