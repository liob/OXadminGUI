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
import at.sciencesoft.oxrmi.OXgroup;
import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.oxrmi.Param;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.plugin.GroupIface;
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
public class Group implements GroupIface {

    public ResultSet create(boolean isPost, boolean isSuccess, int contextID, Credentials auth, String name, String displayName, String members, ParamMap map, ResultSet rs, com.openexchange.admin.rmi.dataobjects.Group group) {
        ISPconfig3Login rlogin = null;
        if (isPost && isSuccess) {

            Rollback rollback = new Rollback();
            try {
                boolean active = map.getBoolean(Param.NAME.EMAIL_GROUP_ACTIVE);
                String mailForwardAddress = map.getString(Param.NAME.EMAIL_GROUP);

                if (mailForwardAddress != null && mailForwardAddress.charAt(0) != '@') {
                    String additional = map.getString(Param.NAME.EMAIL_GROUP_ADDITIONAL);
                    Properties prop = new Properties();

                    String[] list = members.split(",");
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < list.length; ++i) {
                        if (i > 0) {
                            buf.append(",");
                        }
                        buf.append(OXuser.getUser(contextID, auth, Integer.parseInt(list[i])).getPrimaryEmail());
                    }
                    if (additional != null && !additional.equals("")) {
                        prop.put(PROP.additionalEmails.name(), mailForwardAddress);
                        if (buf.length() > 0) {
                            buf.append(",");
                        }
                        buf.append(additional);
                    }


                    int forwardID = -1;
                    if (buf.length() > 0) {
                        rlogin = new ISPconfig3Login();
                        forwardID = rlogin.getRemote().addEmailForward(mailForwardAddress, buf.toString().split(","), active);
                    } else {
                        active = false;
                    }

                    prop.put(PROP.mailForwardID.name(), "" + forwardID);
                    prop.put(PROP.isActive.name(), "" + active);
                    prop.put(PROP.mailForwardAddress.name(), mailForwardAddress);
                    savePropertyFile(contextID, group.getId(), prop);
                }

            } catch (Exception e) {
                if (rlogin != null) {
                    try {
                        rollback.rollback(rlogin);
                    } catch (Exception e1) {
                    }
                }
                try {
                    OXgroup.delete(contextID, auth, group.getId());
                } catch (Exception e2) {
                }
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Group.create(): " + Remote.extractErrorMsg(e), null, null);
            } finally {
                if (rlogin != null) {
                    try {
                        rlogin.closeRemote();
                    } catch (Exception e3) {
                    }
                }

            }
        }
        return new ResultSet(true);
    }

    public ResultSet change(boolean isPost, boolean isSuccess, int contextID, Credentials auth, int groupID, ParamMap changes, ParamMap pluginMap, ResultSet rs) {
        if (!isPost) {

            ISPconfig3Login rlogin = null;
            try {
                String[] members = null;
                File f = getPropertyPath(contextID, groupID);
                Properties prop = null;
                int forwardID = -1;
                if (f.exists()) {
                    prop = loadPropertyFile(contextID, groupID);
                    forwardID = Integer.parseInt(prop.getProperty(PROP.mailForwardID.name()));
                }
                rlogin = new ISPconfig3Login();


                if (pluginMap.getString(Param.EMAIL_GROUP).charAt(0) == '@') {
                    // delete forward email address?
                    if (f.exists()) {
                        rlogin.getRemote().deleteEmailForward(forwardID);
                        deletePropertyFile(contextID, groupID);
                    }
                } else {
                    boolean change = false;
                    StringBuffer buf = new StringBuffer();
                    if (forwardID == -1) {
                        // create new email forward
                        prop = new Properties();
                        boolean active = pluginMap.getBoolean(Param.NAME.EMAIL_GROUP_ACTIVE);
                        String forward = pluginMap.getString(Param.NAME.EMAIL_GROUP);
                        String[] list = pluginMap.getStringArray(Param.ALL_USER);
                        if (list != null) {
                            for (int i = 0; i < list.length; ++i) {
                                if (i > 0) {
                                    buf.append(",");
                                }
                                buf.append(OXuser.getUser(contextID, auth, Integer.parseInt(list[i])).getPrimaryEmail());
                            }
                        }
                        String additional = pluginMap.getString(Param.NAME.EMAIL_GROUP_ADDITIONAL);

                        if (additional != null && !additional.equals("")) {
                            if (buf.length() > 0) {
                                buf.append(",");
                            }
                            buf.append(additional);
                            prop.put(PROP.additionalEmails.name(), additional);
                        }

                        if (buf.length() > 0) {                
                            forwardID = rlogin.getRemote().addEmailForward(forward, buf.toString().split(","), active);
                        }

                        prop.put(PROP.mailForwardID.name(), "" + forwardID);
                        prop.put(PROP.isActive.name(), "" + active);
                        prop.put(PROP.mailForwardAddress.name(), forward);
                        savePropertyFile(contextID, groupID, prop);
                    } else {

                        // add or remove group members
                        if (changes != null && (changes.getStringArray(Param.ADD_USER) != null || changes.getStringArray(Param.REMOVE_USER) != null)) {
                            change = true;
                        }

                        // check additional email adresses
                        String additional = pluginMap.getString(Param.NAME.EMAIL_GROUP_ADDITIONAL);
                        String actualAdditional = "";
                        if (additional != null && !additional.equals("")) {
                            actualAdditional = additional;
                        }
                        String oldAdditional = prop.getProperty(PROP.additionalEmails.name());
                        if (oldAdditional == null) {
                            oldAdditional = "";
                        }
                        if (!actualAdditional.equals(oldAdditional)) {
                            prop.put(PROP.additionalEmails.name(), additional);
                            change = true;
                        }
                        if (change) {

                            String[] list = pluginMap.getStringArray(Param.ALL_USER);
                            if (list != null) {

                                for (int i = 0; i < list.length; ++i) {
                                    if (i > 0) {
                                        buf.append(",");
                                    }
                                    buf.append(OXuser.getUser(contextID, auth, Integer.parseInt(list[i])).getPrimaryEmail());
                                }
                            }
                            if (!additional.equals("")) {
                                if (buf.length() > 0) {
                                    buf.append(",");
                                }
                                buf.append(additional);
                            }
                            members = buf.toString().split(",");
                        }

                        // change of the group email address?
                        String mailForwardAddress = null;
                        if (!pluginMap.getString(Param.EMAIL_GROUP).equals(prop.getProperty(PROP.mailForwardAddress.name()))) {
                            mailForwardAddress = pluginMap.getString(Param.EMAIL_GROUP);
                            prop.put(PROP.mailForwardAddress.name(), mailForwardAddress);
                            change = true;
                        }

                        // email forwarding is active
                        Boolean active = null;
                        if (pluginMap.getBoolean(Param.EMAIL_GROUP_ACTIVE) != new Boolean(prop.getProperty(PROP.isActive.name()))) {
                            active = pluginMap.getBoolean(Param.EMAIL_GROUP_ACTIVE);
                            prop.put(PROP.isActive.name(), "" + pluginMap.getBoolean(Param.EMAIL_GROUP_ACTIVE));
                            change = true;
                        }

                        // empty forwarding list? - no group members and no additional emails - delete existing email forward
                        if (actualAdditional.equals("") && pluginMap.getStringArray(Param.ALL_USER) == null) {
                            rlogin.getRemote().deleteEmailForward(forwardID);
                            prop.put(PROP.mailForwardID.name(), "" + -1);
                            prop.put(PROP.isActive.name(), "false");
                            change = true;
                        } else {
                            // fire up changes                         
                            rlogin.getRemote().changeEmailForward(forwardID, mailForwardAddress, members, active);
                        }
                        // save changes
                        if (change) {
                            savePropertyFile(contextID, groupID, prop);
                        }
                    }
                }
            } catch (Exception e) {
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Groupt.change(): " + Remote.extractErrorMsg(e), null, null);
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

    public ResultSet delete(boolean isPost, boolean isSuccess, int contextID, Credentials auth, int groupID, ResultSet rs) {
        if (isPost && isSuccess) {
             ISPconfig3Login rlogin = null;
            try {
                // property file available?
                if (!getPropertyPath(contextID, groupID).exists()) {
                    return new ResultSet(true);
                }
                ISPconfig3 isp = ISPconfig3.getInstance();
                
                Properties prop = loadPropertyFile(contextID, groupID);
                int forwardID = Integer.parseInt(prop.getProperty(PROP.mailForwardID.name()));
                if (forwardID != -1) {
                    rlogin = new ISPconfig3Login();
                    rlogin.getRemote().deleteEmailForward(forwardID);
                }
                deletePropertyFile(contextID, groupID);
            } catch (Exception e) {
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.Groupt.delete(): " + Remote.extractErrorMsg(e), null, null);
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

    public void loadGUIdata(int contextID, int groupID, HashMap map) throws Exception {
        File f = getPropertyPath(contextID, groupID);
        if (f.exists()) {
            Properties prop = loadPropertyFile(contextID, groupID);
            if (prop.getProperty(PROP.isActive.name()).equals("true")) {
                map.put("activeEmailGroup", "on");
            }
            if (prop.getProperty(PROP.additionalEmails.name()) != null) {
                map.put("emailAdditional", prop.getProperty(PROP.additionalEmails.name()));
            }
            String tmp = prop.getProperty(PROP.mailForwardAddress.name());
            tmp = tmp.substring(0, tmp.indexOf('@'));
            map.put("emailGroup", tmp);
            if (Integer.parseInt(prop.getProperty(PROP.mailForwardID.name())) == -1) {
                map.put("emptyGroup", "true");
            }
        }
    }

    public boolean checkGUIflag(FIELD field) {
        return true;
    }

    private Properties loadPropertyFile(int contextID, int groupID) throws Exception {
        String path = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/group/" + contextID + "/" + groupID + ".properties";
        Properties prop = new Properties();
        FileInputStream fi = new FileInputStream(path);
        prop.load(fi);
        fi.close();
        return prop;
    }

    private File getPropertyPath(int contextID, int groupID) throws Exception {
        String dir = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/group/" + contextID + "/" + groupID + ".properties";
        return new File(dir);
    }

    private void savePropertyFile(int contextID, int groupID, Properties prop) throws Exception {
        String dir = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/group/" + contextID + "/";
        File f = new File(dir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("at.sciencesoft.plugin.ispconfig3.Group.savePropertyFile(): Unable to create directory '" + f.getAbsolutePath() + "'");
            }
        }
        dir += groupID + ".properties";
        FileOutputStream fos = new FileOutputStream(dir);
        prop.store(fos, "ISPconfig3<-->OX mapping");
        fos.close();
    }

    private boolean deletePropertyFile(int contextID, int groupID) {
        return new File(Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/group/" + contextID + "/" + groupID + ".properties").delete();
    }

    public String getVersion() {
        return "0.1";
    }

    private enum PROP {

        mailForwardAddress, mailForwardID, additionalEmails, isActive
    }
}
