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
import at.sciencesoft.oxrmi.ContextCache;
import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.OXgroup;
import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.oxrmi.Param;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.plugin.ContextIface;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.system.Config;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class Context implements ContextIface {

    /**
     * 
     * @param isPost
     * @param isSuccess
     * @param contextID
     * @param contextName
     * @param mapping
     * @param quota
     * @param accessCombination
     * @param loginName
     * @param displayName
     * @param firstName
     * @param lastName
     * @param pwd
     * @param email
     * @param lang
     * @param timeZone
     * @param contextInfos
     * @param rs
     * @param context
     * @return
     */
    public ResultSet create(boolean isPost, boolean isSuccess, int contextID, String contextName, String[] mapping, long quota, String accessCombination, String loginName, String displayName,
            String firstName, String lastName, String pwd, String email, String lang, String timeZone, ParamMap contextInfos, ResultSet rs, OXcontext context) {
        if (!isPost) {
            String[] emailDomain = null;
            try {
                emailDomain = contextInfos.getStringArray(Param.EMAIL_DOMAIN);
            } catch (Exception e) {
            }
            if (emailDomain == null) {
                return new ResultSet(true, "at.sciencesoft.plugin.ispconfig3.Context.create(): Missing email domain!", null, null);
            }
            if (emailDomain.length != 1) {
                return new ResultSet(true, "at.sciencesoft.plugin.ispconfig3.Context.create(): Only one email domain supported!", null, null);
            }
            ISPconfig3Login rlogin = null;
            Rollback rollback = new Rollback();
            try {

                rlogin = new ISPconfig3Login();
                ISPconfig3 isp = ISPconfig3.getInstance();
                ;
                int domainID = rlogin.getRemote().addEmailDomain(emailDomain[0], true);
                rollback.add(RollbackEntry.TYPE.EMAIL_DOMAIN, domainID);
                int domainSpamFilterID = -1;
                if (isp.getDomainSPAMpolicyID() != -1) {

                    String domain = "@" + emailDomain[0];
                    domainSpamFilterID = rlogin.getRemote().addEmailSpamFilter(isp.getDomainSPAMpolicyID(), 5, domain, domain);
                    rollback.add(RollbackEntry.TYPE.EMAIL_SPAMFILTER, domainSpamFilterID);
                }
                String dir = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/context/";
                File f = new File(dir);
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        throw new Exception("at.sciencesoft.plugin.ispconfig3.Context.create(): Unable to create directory '" + f.getAbsolutePath() + "'");
                    }
                }
                dir += contextID + ".properties";
                f = new File(dir);
                rollback.add(f);
                Properties prop = new Properties();
                prop.put("emailDomain", emailDomain[0]);
                prop.put("emailDomainID", "" + domainID);
                prop.put("domainSpamFilterID", "" + domainSpamFilterID);
                FileOutputStream fos = new FileOutputStream(dir);
                prop.store(fos, "ISPconfig3<-->OX mapping");
                fos.close();

                // create mail account for the ctxAdmin

                int userID = rlogin.getRemote().addEmailUser(email, pwd, 0, true, isp.isIMAPenabled(), isp.isPOP3enabled(), false, null, null);
                rollback.add(RollbackEntry.TYPE.EMAIL, userID);
                dir = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/user/" + contextID + "/";
                f = new File(dir);
                rollback.add(f);
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        throw new Exception("at.sciencesoft.plugin.ispconfig3.User.create(): Unable to create directory '" + f.getAbsolutePath() + "'");
                    }
                }
                dir += "2.properties";
                f = new File(dir);
                prop = new Properties();
                prop.put("mailUserID", "" + userID);
                fos = new FileOutputStream(dir);
                prop.store(fos, "ISPconfig3<-->OX mapping");
                fos.close();

            } catch (Exception e) {
                if (rlogin != null) {
                    try {
                        rollback.rollback(rlogin);
                    } catch (Exception e1) {
                    }
                }
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Context.create(): " + Remote.extractErrorMsg(e), null, null);
            } finally {
                if (rlogin != null) {
                    try {
                        rlogin.closeRemote();
                    } catch (Exception e2) {
                    }
                }
            }
            HashMap map = new HashMap();
            map.put("rollback", rollback);
            return new ResultSet(true, map);
        } else {
            if (!isSuccess) {
                Rollback rollback = (Rollback) rs.getMap().get("rollback");
                rollback.rollback(null);
            }
            return new ResultSet(true);
        }
    }

    public ResultSet change(boolean isPost, boolean isSuccess, int contextID, ParamMap changes, ParamMap contextInfos, boolean downgrade, ResultSet rs) {
        if (!isPost) {
        }
        return new ResultSet(true);
    }

    public ResultSet delete(boolean isPost, boolean isSuccess, int contextID, ResultSet rs) {
        if (!isPost) {
            try {
                // get user list before deleting the context
                Credentials cred = ContextCache.getContextCredentials(contextID);
                com.openexchange.admin.rmi.dataobjects.User[] user = OXuser.listAll(contextID, cred);
                com.openexchange.admin.rmi.dataobjects.Group[] group = OXgroup.listAll(contextID, cred);
                HashMap map = new HashMap();
                map.put("user", user);
                map.put("group", group);
                return new ResultSet(true, map);
            } catch (Exception e) {
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Context.delete(): " + Remote.extractErrorMsg(e), null, null);
            }
        } else {
            if (isSuccess) {
                ISPconfig3Login rlogin = null;
                try {

                    Properties prop = getPropertyFile(contextID);

                    int emailDomainID = Integer.parseInt(prop.getProperty("emailDomainID"));
                    int domainSpamFilterID = Integer.parseInt(prop.getProperty("domainSpamFilterID"));
                    com.openexchange.admin.rmi.dataobjects.User[] user = (com.openexchange.admin.rmi.dataobjects.User[]) rs.getMap().get("user");

                    User u = new User();
                    for (int i = 0; i < user.length; ++i) {
                        u.delete(true, true, contextID, null, user[i].getId(), null);
                    }

                    com.openexchange.admin.rmi.dataobjects.Group[] group = (com.openexchange.admin.rmi.dataobjects.Group[]) rs.getMap().get("group");
                    if (group != null) {
                        Group g = new Group();
                        for (int i = 0; i < group.length; ++i) {
                            g.delete(true, true, contextID, null, group[i].getId(), null);
                        }
                    }


                    rlogin = new ISPconfig3Login();
                    rlogin.getRemote().deleteEmailDomain(emailDomainID);
                    if (domainSpamFilterID != -1) {
                        rlogin.getRemote().deleteEmailSpamFilter(domainSpamFilterID);
                    }
                    // delete context property file
                    deletePropertyFile(contextID);
                    // delete ctxAdmin property file

                } catch (Exception e) {
                    return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Context.delete(): " + Remote.extractErrorMsg(e), null, null);
                } finally {
                    if (rlogin != null) {
                        try {
                            rlogin.closeRemote();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        return new ResultSet(true);
    }

    public ResultSet enable(boolean isPost, boolean isSuccess, int contextID, ResultSet rs) {
        if (isPost && isSuccess) {
            ISPconfig3Login rlogin = null;
            try {
                rlogin = new ISPconfig3Login();
                int emailDomainID = Integer.parseInt(getPropertyFile(contextID).getProperty(PROP.emailDomainID.name()));
                rlogin.getRemote().changeEmailDomain(emailDomainID, null, new Boolean(true));
            } catch (Exception e) {
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Context.enbale(): " + Remote.extractErrorMsg(e), null, null);
            } finally {
                if (rlogin != null) {
                    try {
                        rlogin.closeRemote();
                        ;
                    } catch (Exception e) {
                    }
                }
            }
        }
        return new ResultSet(true);
    }

    public ResultSet disable(boolean isPost, boolean isSuccess, int contextID, ResultSet rs) {
        if (isPost && isSuccess) {
            ISPconfig3Login rlogin = null;
            try {
                rlogin = new ISPconfig3Login();
                int emailDomainID = Integer.parseInt(getPropertyFile(contextID).getProperty("emailDomainID"));
                rlogin.getRemote().changeEmailDomain(emailDomainID, null, new Boolean(false));
            } catch (Exception e) {
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Context.disable(): " + Remote.extractErrorMsg(e), null, null);
            } finally {
                if (rlogin != null) {
                    try {
                        rlogin.closeRemote();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return new ResultSet(true);
    }

    private Properties getPropertyFile(int contextID) throws Exception {
        String path = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/context/" + contextID + ".properties";
        Properties prop = new Properties();
        FileInputStream fi = new FileInputStream(path);
        prop.load(fi);
        fi.close();
        return prop;
    }

    private boolean deletePropertyFile(int contextID) throws Exception {
        String path = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/context/" + contextID + ".properties";
        return new File(path).delete();
    }

    public boolean checkGUIflag(FIELD field) {
        return true;
    }

    public String getVersion() {
        return "0.1";
    }

    private enum PROP {

        emailDomainID
    }
}
