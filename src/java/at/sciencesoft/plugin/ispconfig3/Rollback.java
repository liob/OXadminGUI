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

import at.sciencesoft.ispconfig3.Remote;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Rollback {

    public Rollback() {
        rollback = new ArrayList<RollbackEntry>();
    }

    public void add(RollbackEntry.TYPE type, int id) {
        rollback.add(new RollbackEntry(type, id));
    }

    public void add(File file) {
        rollback.add(new RollbackEntry(file));
    }

    public void add(RollbackEntry.TYPE type, HashMap<String, String> map) {
        rollback.add(new RollbackEntry(type, map));
    }

    public void add(RollbackEntry.TYPE type, String[] list) {
        rollback.add(new RollbackEntry(type, list));
    }

    public String rollback(ISPconfig3Login rlogin) {
        StringBuffer buf = new StringBuffer();
        try {
            if (rlogin == null) {
               rlogin = new ISPconfig3Login();
            }
            for (int i = rollback.size() - 1; i >= 0; --i) {
                RollbackEntry re = rollback.get(i);
                buf.append("Step " + i + "/" + rollback.size() + ": ");
                switch (re.getType()) {
                    case FILE: {                  
                        if (re.getFile().delete()) {
                            buf.append(" OK ");
                        } else {
                            buf.append(" FAILED ");
                        }
                        buf.append("- delete FILE: " + re.getFile().getAbsolutePath());
                        buf.append("\n");
                        break;
                    }
                    case EMAIL_ALIAS: {
                         try {
                            rlogin.getRemote().deleteEmailAlias(re.getID());
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- delete EMAIL_ALIAS: " + re.getID());
                         break;
                    }
                    case EMAIL: {
                         try {
                            rlogin.getRemote().deleteEmailUser(re.getID());
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- delete EMAIL: " + re.getID());
                         break;
                    }
                    case EMAIL_SPAMFILTER: {
                         try {
                            rlogin.getRemote().deleteEmailSpamFilter(re.getID());
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- delete EMAIL_SPAMFILTER: " + re.getID());
                         break;
                    }
                    case EMAIL_FORWARD: {
                         try {
                            rlogin.getRemote().deleteEmailForward(re.getID());
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- delete EMAIL_FORWARD: " + re.getID());
                         break;
                    }
                    case EMAIL_DOMAIN: {
                         try {
                            rlogin.getRemote().deleteEmailDomain(re.getID());
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- delete EMAIL_DOMAIN: " + re.getID());
                         break;
                    }
                    case EMAIL_FILTER: {
                         try {
                            rlogin.getRemote().deleteEmailFilter(re.getID());
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- delete EMIAL_FILTER: " + re.getID());
                         break;
                    }
                    case EMAIL_CHANGE: {
                         try {
                            rlogin.getRemote().changeEmailUser(i,rlogin.getRemote().getEmailUserAsMap(re.getID()));
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- revert EMAIL_CHANGE: " + re.getID());
                         break;
                    }
                    case EMAIL_ALIAS_REMOVE: {
                        String list[] = re.getList();
                        try {
                            rlogin.getRemote().addEmailAlias(list[0],list[1],true);
                            buf.append(" OK ");
                         } catch(Exception e) {
                             buf.append(" FAILED ");
                         }
                         buf.append("- revert EMAIL_ALIAS_REMOVE - add EMAIL_ALIAS: " + list[0] + "->" + list[1]);
                         break;
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (rlogin != null) {
                    rlogin.closeRemote();
                }
            } catch (Exception e) {
            }
        }
        return buf.toString();

    }
    private ArrayList<RollbackEntry> rollback;
}
