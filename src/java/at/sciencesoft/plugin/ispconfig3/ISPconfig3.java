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
package at.sciencesoft.plugin.ispconfig3;

import at.sciencesoft.ispconfig3.EmailFilter;
import at.sciencesoft.ispconfig3.Remote;
import at.sciencesoft.system.Config;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ISPconfig3 {

    private ISPconfig3() {
    }

    public static synchronized ISPconfig3 getInstance() throws Exception {
        if (instance == null) {
            instance = new ISPconfig3();
            String path = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/ispconfig3.properties";
            Properties prop = new Properties();
            FileInputStream fi = new FileInputStream(path);
            prop.load(fi);
            fi.close();
            instance.remote = new Remote(prop.getProperty("soapLocation"), prop.getProperty("soapURI"), prop.getProperty("remoteUser"), prop.getProperty("remoteUserPassword"),
                    Integer.parseInt(prop.getProperty("serverID")), Integer.parseInt(prop.getProperty("clientID")));
            String tmp = prop.getProperty("domainSpamFilterID");
            if(tmp != null) {
                instance.domainSPAMpolicyID = Integer.parseInt(tmp);
            }
            tmp = prop.getProperty("mailFilter");
            // SPAM,SUBJECT,***SPAM***,CONTAINS,MOVE,Spam
            //  0      1        2         3       4   5 
            String[] list = tmp.split(",");
            String imapFolder = null;
            if(list.length == 6) {
                imapFolder = list[5];
            }
            instance.dummy = new EmailFilter(-1,-1,true,list[0], EmailFilter.SOURCE.valueOf(list[1]), list[2], EmailFilter.OPERATION.valueOf(list[3]), EmailFilter.ACTION.valueOf(list[4]), imapFolder);
        }
        return instance;
    }

    public Remote getRemoteIface() {
        return remote;
    }



    public boolean isIMAPenabled() {
        return isIMAPenabled;
    }

    public boolean isPOP3enabled() {
        return isPOP3enabled;
    }

    public int getDomainSPAMpolicyID() {
        return domainSPAMpolicyID;
    }

    public EmailFilter getDefaultEmailFilter() {
        return dummy;
    }
   
    private Remote remote = null;
    private boolean isIMAPenabled = true;
    private boolean isPOP3enabled = true;
    private int domainSPAMpolicyID = -1;
    private EmailFilter dummy;
    private static ISPconfig3 instance;
}
