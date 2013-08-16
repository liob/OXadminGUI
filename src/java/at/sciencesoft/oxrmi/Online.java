/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.sciencesoft.oxrmi;

import com.openexchange.sessiond.impl.SessionControl;
import com.openexchange.sessiond.impl.SessionHandler;
import java.util.List;

/**
 *
 * @author pezi
 */
public class Online {
    public static String test() {
        List<SessionControl> l = SessionHandler.getSessions();
        if(l == null)  {
            return "";
        }
        String t = "";
        for(int i = 0; i < l.size(); ++i) {
            t+= l.get(i).getSession().getSessionID() + "<br />";
        }
        return t;
    }
}
