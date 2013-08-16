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

import at.sciencesoft.ispconfig3.CustomEmailFilter;
import at.sciencesoft.ispconfig3.EmailFilter;
import at.sciencesoft.ispconfig3.Remote;
import at.sciencesoft.oxrmi.OXuser;
import at.sciencesoft.oxrmi.Param;
import at.sciencesoft.oxrmi.ParamMap;
import at.sciencesoft.plugin.ResultSet;
import at.sciencesoft.plugin.UserIface;
import at.sciencesoft.system.Config;
import com.openexchange.admin.rmi.dataobjects.Credentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class User implements UserIface {

    public ResultSet create(boolean isPost, boolean isSuccess, int contextID, Credentials auth, String loginName, String displayName, String firstName, String lastName, String pwd, String email, ParamMap map, ResultSet rs, com.openexchange.admin.rmi.dataobjects.User user) {
        ISPconfig3Login rlogin = null;
        if (isPost && isSuccess) {
            Rollback rollback = new Rollback();
            try {
                rlogin = new ISPconfig3Login();
                ISPconfig3 isp = ISPconfig3.getInstance();
                Integer quota = map.getInt(Param.MAIL_QUOTA);
                Integer response = map.getInt(Param.AUTORESPONDER);
                boolean responseFlag = false;
                switch (response) {
                    case 0:
                        responseFlag = false;
                        break;
                    case 1:
                        responseFlag = true;
                        break;
                    case 2:
                        // check intervall - also for forward
                        break;
                }
                Integer forward = map.getInt(Param.EMAIL_FORWRAD);
                boolean forwardFlag = false;
                switch (forward) {
                    case 0:
                        forwardFlag = false;
                        break;
                    case 1:
                        forwardFlag = true;
                        break;
                    case 2:
                        // check intervall
                        break;
                }
                // cc "!info@example.tld"
                String forwardList = null;
                CustomEmailFilter customFilter = null;
                if (forwardFlag) {
                    String[] list = map.getStringArray(Param.EMAIL_FORWRAD_LIST);
                    if (list != null) {
                        StringBuffer buf = new StringBuffer();
                        StringBuffer listBuf = new StringBuffer();
                        for (int i = 0; i < list.length; ++i) {
                            buf.append("cc \"!");
                            buf.append(list[i]);
                            buf.append("\"\n");
                            if (i > 0) {
                                listBuf.append(",");
                            }
                            listBuf.append(list[i]);
                        }
                        customFilter = new CustomEmailFilter(CustomEmailFilter.ACTION.UPDATE, FILTER_PROLOG, FILTER_EPILOG, buf.toString());
                        forwardList = listBuf.toString();
                    } else {
                        // user requests forward, but not forward emial(s) available -  reset to off
                        forward = 0;
                        forwardFlag = false;
                    }
                }

                String autoresponderText = map.getString(Param.AUTORESPONDER_TEXT).trim();

                if (autoresponderText.equals("")) {
                    autoresponderText = null;
                    // user requests autoreply, but no text availabe - reset to off
                    if (responseFlag) {
                        responseFlag = false;
                        response = 0;
                    }
                }

                // mail user + email forward
     
                int userID = rlogin.getRemote().addEmailUser(email, pwd, quota == null ? 0 : quota, true, isp.isIMAPenabled(), isp.isPOP3enabled(), responseFlag, autoresponderText, customFilter);
                rollback.add(RollbackEntry.TYPE.EMAIL, userID);
                EmailFilter filter = isp.getDefaultEmailFilter();
                // mail filter
                int mailFilterID = -1;
                if (filter != null) {
                    mailFilterID = rlogin.getRemote().addEmailFilter(userID, true, filter.getRulename(), filter.getSource(), filter.getSearchterm(), filter.getOperation(), filter.getAction(), filter.getIMAPfolderTarget());
                    rollback.add(RollbackEntry.TYPE.EMAIL_FILTER, mailFilterID);
                }
                // email alias
                String[] alias = map.getStringArray(Param.EMAIL_ALIAS);
                StringBuffer buf = new StringBuffer();
                if (alias != null) {
                    for (int i = 0; i < alias.length; ++i) {
                        int aliasID = rlogin.getRemote().addEmailAlias(alias[i], email, true);
                        rollback.add(RollbackEntry.TYPE.EMAIL_ALIAS, aliasID);
                        if (i > 0) {
                            buf.append(',');
                        }
                        buf.append(aliasID + ":" + alias[i]);
                    }
                }
                Properties prop = new Properties();
                prop.put(PROP.mailUserID.name(), "" + userID);
                if (alias != null) {
                    prop.put(PROP.mailAlias.name(), buf.toString());
                }
                prop.put(PROP.mailQuota.name(), "" + (quota == null ? 0 : quota));
                prop.put(PROP.autoresponderType.name(), "" + response);
                if (autoresponderText != null) {
                    prop.put(PROP.autoresponderText.name(), autoresponderText);
                }
                String tmp = map.getString(Param.INTERVALL);
                if (tmp != null) {
                    prop.put(PROP.intervall.name(), tmp);
                }
                prop.put(PROP.mailFilterID.name(), "" + mailFilterID);
                prop.put(PROP.forwardType.name(), "" + forward);
                if (forwardList != null) {
                    prop.put(PROP.forwardList.name(), forwardList);
                }

                rollback.add(getPropertyPath(contextID, user.getId()));
                savePropertyFile(contextID, user.getId(), prop);

                return new ResultSet(true, null);
            } catch (Exception e) {
                try {
                    if (rlogin != null) {
                        rollback.rollback(rlogin);
                    }
                } catch (Exception e2) {
                }
                try {
                    OXuser.delete(contextID, auth, user.getId(), null);
                } catch (Exception e3) {
                }
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.User.create(): " + Remote.extractErrorMsg(e), null, null);
            } finally {

                try {
                    if (rlogin != null) {
                        rlogin.closeRemote();
                    }
                } catch (Exception e) {
                }
            }
        } else {
            return new ResultSet(true);
        }
    }

    public void loadGUIdata(int contextID, int userID, HashMap map) throws Exception {
        Properties prop = loadPropertyFile(contextID, userID);
        String mailquota = prop.getProperty(PROP.mailQuota.name());
        if (mailquota == null || mailquota.equals("0")) {
            mailquota = "";
        }
        map.put("mailQuota", mailquota);
        map.put("autoresponder", prop.getProperty(PROP.autoresponderType.name(), "0"));
        map.put("autoresponderText", prop.getProperty(PROP.autoresponderText.name(), ""));
        map.put("forward", prop.getProperty(PROP.forwardType.name(), "0"));
        map.put("forwardList", prop.getProperty(PROP.forwardList.name(), ""));
        String tmp = prop.getProperty(PROP.intervall.name());
        if (tmp != null) {
            String[] intervall = tmp.split("-");
            String[] left = intervall[0].split("/");
            String[] right = intervall[1].split("/");
            map.put("year_start", left[0]);
            map.put("year_end", right[0]);
            map.put("month_start", left[1]);
            map.put("month_end", right[1]);
            map.put("day_start", left[2]);
            map.put("day_end", right[2]);
        }
    }

    public boolean checkGUIflag(FIELD field) {
        if (field == FIELD.COPY_IMAP_USER) {
            return false;
        }
        return true;
    }

    public void changePassword(int contextID, int userID, String oldPasssword, String newPassword) {
        ISPconfig3Login rlogin = null;
        try {
            new ISPconfig3Login();
            Properties prop = loadPropertyFile(contextID, userID);
            int mailUserID = Integer.parseInt(prop.getProperty(PROP.mailUserID.name()));
            rlogin.getRemote().changeEmailUser(mailUserID, null, newPassword, null, null, null, null, null, null, null);
        } catch (Exception e) {

        }
        finally {
            if(rlogin != null) {
                try {
                    rlogin.closeRemote();
                } catch(Exception e2) {

                }
            }
        }
    }

    public ResultSet change(boolean isPost, boolean isSuccess, int contextID, Credentials auth, int userID, ParamMap changes, ParamMap pluginMap, ResultSet rs) {
        ISPconfig3Login rlogin = null;
        if (!isPost) {
            Rollback rollback = new Rollback();
            try {
                boolean change = false;
                boolean changeCfg = false;
                Properties prop = loadPropertyFile(contextID, userID);
                // check relevant OX parameter
                String email = null;
                String password = null;
                if (changes != null) {
                    email = changes.getString(Param.EMAIL);
                    password = changes.getString(Param.PASSWORD);
                    if (password != null || email != null) {
                        change = true;
                    }
                }

                // check plugin parameter
                Integer mailQuota = null;
                int quota = pluginMap.getInt(Param.MAIL_QUOTA);
                String tmp = prop.getProperty(PROP.mailQuota.name(), "0");

                if (quota != Integer.parseInt(tmp)) {
                    changeCfg = change = true;
                    mailQuota = quota;
                    prop.put(PROP.mailQuota.name(), "" + quota);
                }

                boolean responder = false;
                Boolean responderFlag = null;
                Integer autoresponder = pluginMap.getInt(Param.AUTORESPONDER);
                if (autoresponder == null) {
                    autoresponder = 0;
                }

                switch (autoresponder) {
                    case 0:
                        responder = false;
                        break;
                    case 1:
                        responder = true;
                        break;
                    case 2:
                        // check intervall
                        // reset type
                        break;
                }
                String autoresponderText = pluginMap.getString(Param.AUTORESPONDER_TEXT);
                if (autoresponderText.equals("")) {
                    responder = false;
                    responderFlag = Boolean.FALSE;
                    autoresponder = 0;
                }

                if (autoresponder != Integer.parseInt(prop.getProperty(PROP.autoresponderType.name(), "0"))) {
                    changeCfg = change = true;
                    responderFlag = responder;
                }
                prop.put(PROP.autoresponderType.name(), "" + autoresponder);


                if (prop.getProperty(PROP.autoresponderText.name()) == null) {
                    if (autoresponderText.equals("")) {
                        changeCfg = change = true;
                        prop.put(PROP.autoresponderText.name(), autoresponderText);
                    }
                } else {
                    if (!prop.getProperty(PROP.autoresponderText.name(), "").equals(autoresponderText)) {
                        changeCfg = change = true;
                        prop.put(PROP.autoresponderText.name(), autoresponderText);
                    }
                }


                String intervall = pluginMap.getString(Param.INTERVALL);
                if (prop.getProperty(PROP.intervall.name()) == null) {
                    changeCfg = true;
                    prop.put(PROP.intervall.name(), intervall);
                } else {
                    if (!prop.getProperty(PROP.intervall.name()).equals(intervall)) {
                        changeCfg = true;
                        prop.put(PROP.intervall.name(), intervall);
                    }
                }

                int forwardTyp = pluginMap.getInt(Param.EMAIL_FORWRAD);
                boolean forward = false;

                switch (forwardTyp) {
                    case 0:
                        forward = false;
                        break;
                    case 1:
                        forward = true;
                        break;
                    case 2:
                        // check intervall
                        // reset type
                        break;
                }

                String[] forwardList = pluginMap.getStringArray(Param.EMAIL_FORWRAD_LIST);
                if (forwardList == null) {
                    forward = false;
                    forwardTyp = 0;
                }

                CustomEmailFilter customFilter = null;
                if (forwardTyp != Integer.parseInt(prop.getProperty(PROP.forwardType.name(), "0"))) {
                    changeCfg = true;
                    if (!forward) {
                        customFilter = new CustomEmailFilter(CustomEmailFilter.ACTION.DELETE, null, null, null);
                        change = true;
                    }
                }
                prop.put(PROP.forwardType.name(), "" + forwardTyp);


                String oldFlist = prop.getProperty(PROP.forwardList.name());
                if (forwardList != null) {
                    StringBuffer buf = new StringBuffer();
                    StringBuffer mailDrop = new StringBuffer();
                    for (int i = 0; i < forwardList.length; ++i) {
                        if (i > 0) {
                            buf.append(",");
                        }
                        buf.append(forwardList[i]);
                        mailDrop.append("cc \"!");
                        mailDrop.append(forwardList[i]);
                        mailDrop.append("\"\n");
                    }
                    String cList = buf.toString();
                    if (oldFlist == null) {
                        if (forward) {
                            customFilter = new CustomEmailFilter(CustomEmailFilter.ACTION.UPDATE, FILTER_PROLOG, FILTER_EPILOG, mailDrop.toString());
                            change = true;
                        }
                        changeCfg = true;
                        prop.put(PROP.forwardList.name(), cList);
                    } else {
                        if (!cList.equals(oldFlist)) {
                            if (forward) {
                                customFilter = new CustomEmailFilter(CustomEmailFilter.ACTION.UPDATE, FILTER_PROLOG, FILTER_EPILOG, mailDrop.toString());
                                change = true;
                            }
                            changeCfg = true;
                            prop.put(PROP.forwardList.name(), cList);
                        }
                    }
                } else {
                    if (oldFlist != null) {
                        change = true;
                        prop.remove(PROP.forwardList.name());
                    }
                }

                //
                rlogin = new ISPconfig3Login();
                if (change) {
                    int mailUserID = Integer.parseInt(prop.getProperty(PROP.mailUserID.name()));
                    HashMap<String, String> map = rlogin.getRemote().changeEmailUser(mailUserID, email, password, mailQuota, null, null, null, responderFlag, autoresponderText, customFilter);
                    rollback.add(RollbackEntry.TYPE.EMAIL_CHANGE, map);
                }

                if (changes != null) {
                    String[] alias = changes.getStringArray(Param.EMAIL_ALIAS);
                    // remove e-mail address from alias list
                    if (alias != null) {
                        // load user
                        com.openexchange.admin.rmi.dataobjects.User user = OXuser.getUser(contextID, auth, userID);
                        String primaryEmail = user.getPrimaryEmail();
                        tmp = prop.getProperty(PROP.mailAlias.name());
                        // no existing alias available
                        StringBuffer buf = new StringBuffer();
                        if (tmp == null) {
                            for (int i = 0; i < alias.length; ++i) {
                                // check alias == actual or new e-mail address
                                if (alias[i].equals(primaryEmail) || (email != null && email.equals(alias[i]))) {
                                    continue;
                                }
                                int aliasID = rlogin.getRemote().addEmailAlias(alias[i], primaryEmail, true);
                                rollback.add(RollbackEntry.TYPE.EMAIL_ALIAS, aliasID);
                                if (i > 0) {
                                    buf.append(',');
                                }
                                buf.append(aliasID + ":" + alias[i]);
                            }
                            if (buf.length() > 0) {
                                prop.put(PROP.mailAlias.name(), buf.toString());
                                changeCfg = true;
                            }
                        } else {
                            HashMap<String, String> map = new HashMap<String, String>();
                            for (int i = 0; i < alias.length; ++i) {
                                map.put(alias[i], alias[i]);
                            }
                            map.remove(user.getPrimaryEmail());
                            String[] list = tmp.split(",");
                            for (int i = 0; i < list.length; ++i) {
                                String[] pair = list[i].split(":");
                                if (!map.containsKey(pair[1])) {
                                    rlogin.getRemote().deleteEmailAlias(Integer.parseInt(pair[0]));
                                    rollback.add(RollbackEntry.TYPE.EMAIL_ALIAS_REMOVE, new String[]{pair[1], user.getPrimaryEmail()});
                                } else {
                                    if (buf.length() > 0) {
                                        buf.append(",");
                                    }
                                    buf.append(pair[0] + ":" + pair[1]);
                                    map.remove(pair[1]);
                                }
                            }
                            if (!map.isEmpty()) {
                                Iterator<String> iter = map.keySet().iterator();
                                while (iter.hasNext()) {
                                    tmp = iter.next();
                                    if (buf.length() > 0) {
                                        buf.append(",");
                                    }
                                    int aliasID = rlogin.getRemote().addEmailAlias(tmp, user.getPrimaryEmail(), true);
                                    rollback.add(RollbackEntry.TYPE.EMAIL_ALIAS, aliasID);
                                    buf.append(aliasID + ":" + tmp);
                                }
                            }
                            changeCfg = true;
                            if (buf.length() > 0) {
                                prop.put(PROP.mailAlias.name(), buf.toString());
                            } else {
                                prop.remove(PROP.mailAlias.name());
                            }
                        }
                    }
                }

                if (changeCfg) {
                    rollback.add(getPropertyPath(contextID, userID));
                    savePropertyFile(contextID, userID, prop);
                }

            } catch (Exception e) {
                return new ResultSet(false, "at.sciencesoft.plugin.ispconfig3.User.change(): " + Remote.extractErrorMsg(e), null, null);
            } finally {
                if (rlogin != null) {
                    try {
                        rlogin.closeRemote();
                    } catch (Exception e) {
                    }
                }
            }
        } else {
        }
        return new ResultSet(true);
    }

    public ResultSet delete(boolean isPost, boolean isSuccess, int contextID, Credentials auth, int userID, ResultSet rs) {
        if (isPost && isSuccess) {
            ISPconfig3Login rlogin = null;
            try {


                Properties prop = loadPropertyFile(contextID, userID);
                rlogin = new ISPconfig3Login();
                rlogin.getRemote().deleteEmailUser(Integer.parseInt(prop.getProperty(PROP.mailUserID.name())));
                if (prop.getProperty(PROP.mailFilterID.name()) != null) {
                    rlogin.getRemote().deleteEmailFilter(Integer.parseInt(prop.getProperty(PROP.mailFilterID.name())));
                }
                // delete exsting alias
                String alias = prop.getProperty(PROP.mailAlias.name());
                if (alias != null) {
                    String[] list = alias.split(",");
                    for (int i = 0; i < list.length; ++i) {
                        int pos = list[i].indexOf(":");
                        rlogin.getRemote().deleteEmailAlias(Integer.parseInt(list[i].substring(0, pos)));
                    }
                }

                deletePropertyFile(contextID, userID);

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
        return new ResultSet(true);
    }

    private Properties loadPropertyFile(int contextID, int userID) throws Exception {
        String path = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/user/" + contextID + "/" + userID + ".properties";
        Properties prop = new Properties();
        FileInputStream fi = new FileInputStream(path);
        prop.load(fi);
        fi.close();
        return prop;
    }

    private File getPropertyPath(int contextID, int userID) throws Exception {
        String path = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/user/" + contextID + "/" + userID + ".properties";
        return new File(path);
    }

    private void savePropertyFile(int contextID, int userID, Properties prop) throws Exception {
        String dir = Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/user/" + contextID + "/";
        File f = new File(dir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("at.sciencesoft.plugin.ispconfig3.User.savePropertyFile(): Unable to create directory '" + f.getAbsolutePath() + "'");
            }
        }
        dir += userID + ".properties";

        FileOutputStream fos = new FileOutputStream(dir);
        prop.store(fos, "ISPconfig3<-->OX mapping");
        fos.close();
    }

    private boolean deletePropertyFile(int contextID, int userID) {
        return new File(Config.getInstance().getETCdir() + "plugins/etc/ispconfig3/user/" + contextID + "/" + userID + ".properties").delete();
    }

    public String getVersion() {
        return "0.1";
    }

    private enum PROP {

        mailUserID, mailAlias, mailQuota, autoresponderType, autoresponderText, mailFilterID, intervall, forwardType, forwardList
    }
    private static final String FILTER_PROLOG = "### OX GUI START - Mail forwarding rules - DO EDIT!\n";
    private static final String FILTER_EPILOG = "### OX GUI END - Mail forwarding rules - DO EDIT!\n";
}
