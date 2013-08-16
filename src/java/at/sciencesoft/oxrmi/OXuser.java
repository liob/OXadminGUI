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
package at.sciencesoft.oxrmi;


import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

import java.lang.reflect.Method;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Wrapper class for an OX user.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXuser implements Runnable {

    /**
     * Returns a list of all OX users for a given context.
     * @param contextID context ID
     * @param auth
     * @return
     * @throws Exception
     */
    static public User[] listAll(int contextID, Credentials auth) throws Exception {
        // use cache
        if (cacheLiveTime > 0) {
            UserCacheEntry uc = cache.get(contextID);
            if (uc != null) {
                if (System.currentTimeMillis() - uc.getCreationTime() < cacheLiveTime) {
                    return uc.getList();
                }
            }
        }
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.listAll(): RMIHOST is not set!");
        }

        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        User ulist[] = iface.listAll(ctx, auth);
        synchronized (sync) {
            if (ulist == null || ulist.length == 0) {
                return null;
            }
            ulist = iface.getData(ctx, ulist, auth);
            if (cacheLiveTime > 0) {
                cache.put(contextID, new UserCacheEntry(ulist));
            }
        }
        return ulist;
    }

    /**
     * Returns a list of all OX users for a given context.
     * @param contextID context ID
     * @param auth
     * @param list interface to check the number of users
     * @return
     * @throws Exception
     */
    static public User[] listAll(int contextID, Credentials auth, UserList list) throws Exception {
        // use cache?
        if (cacheLiveTime > 0) {
            UserCacheEntry uc = cache.get(contextID);
            if (uc != null) {
                if (System.currentTimeMillis() - uc.getCreationTime() < cacheLiveTime) {
                    User ulist[] = uc.getList();
                    if (!list.checkLimit(ulist.length)) {
                        return null;
                    }
                    return ulist;
                }
            }
        }
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.listAll(): RMIHOST is not set!");
        }

        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        User ulist[] = iface.listAll(ctx, auth);
        if (!list.checkLimit(ulist.length)) {
            return null;
        }
        if (ulist == null || ulist.length == 0) {
            return null;
        }
        synchronized (sync) {
            ulist = iface.getData(ctx, ulist, auth);
            if (cacheLiveTime > 0) {
                cache.put(contextID, new UserCacheEntry(ulist));
            }
        }
        return ulist;
    }

    static public User getUser(int contextID, Credentials auth, int userID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.getUser(): RMIHOST is not set!");
        }

        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        User user;
        synchronized (sync) {
            user = iface.getData(ctx, new User(userID), auth);
        }
        return user;
    }

    static public User[] list(int contextID, Credentials auth, String search) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.list(): RMIHOST is not set!");
        }
        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);

        Context ctx = new Context(contextID);
        User ulist[];
        synchronized (sync) {
            ulist = iface.list(ctx, search, auth);
            if (ulist == null || ulist.length == 0) {
                return null;
            }

            ulist = iface.getData(ctx, ulist, auth);
        }
        return ulist;

    }

    public static ParamMap getChanges(int contextID, Credentials auth, int userID, ParamMap map) throws Exception {
        ParamMap changes = new ParamMap();

        String displayName = map.getString(Param.DISPLAY_NAME);
        String firstName = map.getString(Param.FIRST_NAME);
        String lastName = map.getString(Param.LAST_NAME);
        String loginName = map.getString(Param.LOGIN_NAME);
        String pwd = map.getString(Param.PASSWORD);
        String email = map.getString(Param.EMAIL);
        String lang = map.getString(Param.LANGUAGE);
        String timeZone = map.getString(Param.TIME_ZONE);
        String department = map.getString(Param.DEPARTMENT);
        String company = map.getString(Param.COMPANY);
        String[] emailAlias = map.getStringArray(Param.EMAIL_ALIAS);
        String imapLogin = map.getString(Param.IMAP_LOGIN);
        String smtpServer = map.getString(Param.SMTP_SERVER);
        String imapServer = map.getString(Param.IMAP_SERVER);
        Integer iv = map.getInt(Param.UPLOAD_FILE_SIZE);
        int uploadFileSize = iv == null ? 0 : iv;
        iv = map.getInt(Param.UPLOAD_FILE_SIZE_PER_FILE);
        int uploadFileSizePerFile = iv == null ? 0 : iv;
        String middleName = map.getString(Param.MIDDLE_NAME);
        String streetHome = map.getString(Param.STREET_HOME);
        String postalCodeHome = map.getString(Param.POSTAL_CODE_HOME);
        String cityHome = map.getString(Param.CITY_HOME);
        String stateHome = map.getString(Param.STATE_HOME);
        String streetBusiness = map.getString(Param.STREET_BUSINESS);
        String postalCodeBusiness = map.getString(Param.POSTAL_CODE_BUSINESS);
        String cityBusiness = map.getString(Param.CITY_BUSINESS);
        String stateBusiness = map.getString(Param.STATE_BUSINESS);
        String phoneBusiness = map.getString(Param.TELEPHONE_BUSINESS);
        String faxBusiness = map.getString(Param.FAX_BUSINESS);
        String phoneHome = map.getString(Param.TELEPHONE_HOME);
        String mobile = map.getString(Param.MOBILE);


        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.getChangeMap(): RMIHOST is not set!");
        }
        User user = getUser(contextID, auth, userID);

        // displayed name
        if (displayName != null && !user.getDisplay_name().equals(displayName.trim())) {
            changes.putString(Param.DISPLAY_NAME, displayName.trim());
        }

        // first name
        if (firstName != null && !user.getGiven_name().equals(firstName.trim())) {
            changes.putString(Param.FIRST_NAME, firstName.trim());
        }
        // last name
        if (lastName != null && !user.getSur_name().equals(lastName.trim())) {
            changes.putString(Param.LAST_NAME, lastName.trim());
        }
        // login name
        if (loginName != null && !user.getName().equals(loginName.trim())) {
            changes.putString(Param.LOGIN_NAME, loginName.trim());
        }
        // password
        if (pwd != null && !pwd.trim().equals("")) {
            changes.putString(Param.PASSWORD, pwd.trim());
        }
        // email
        boolean changeEmail = false;
        if (email != null && !user.getPrimaryEmail().equals(email.trim())) {
            changeEmail = true;
            changes.putString(Param.EMAIL, email.trim());
        }
        // language
        if (lang != null && (user.getLanguage() == null || !user.getLanguage().equals(lang.trim()))) {
            changes.putString(Param.LANGUAGE, lang.trim());
        }

        // timezone
        if (timeZone != null && (user.getTimezone() == null || !user.getTimezone().equals(timeZone.trim()))) {
            changes.putString(Param.TIME_ZONE, timeZone.trim());
        }
        // department

        if (department != null && (user.getDepartment() == null || !user.getDepartment().equals(department.trim()))) {
            changes.putString(Param.DEPARTMENT, department.trim());
        }
        // company
        if (company != null && (user.getCompany() == null || !user.getCompany().equals(company.trim()))) {
            changes.putString(Param.COMPANY, company.trim());
        }

        if (middleName != null && (user.getMiddle_name() == null || !user.getMiddle_name().equals(middleName.trim()))) {
            changes.putString(Param.MIDDLE_NAME, middleName.trim());
        }

        if (streetHome != null && (user.getStreet_home() == null || !user.getStreet_home().equals(streetHome.trim()))) {
            changes.putString(Param.STREET_HOME, streetHome.trim());
        }

        if (postalCodeHome != null && (user.getPostal_code_home() == null || !user.getPostal_code_home().equals(postalCodeHome.trim()))) {
            changes.putString(Param.POSTAL_CODE_HOME, postalCodeHome.trim());
        }

        if (cityHome != null && (user.getCity_home() == null || !user.getCity_home().equals(cityHome.trim()))) {
            changes.putString(Param.CITY_HOME, cityHome.trim());
        }

        if (stateHome != null && (user.getState_home() == null || !user.getState_home().equals(stateHome.trim()))) {
            changes.putString(Param.STATE_HOME, stateHome.trim());
        }

        if (streetBusiness != null && (user.getStreet_business() == null || !user.getStreet_business().equals(streetBusiness.trim()))) {
            changes.putString(Param.STREET_BUSINESS, streetBusiness.trim());
        }
        if (postalCodeBusiness != null && (user.getPostal_code_business() == null || !user.getPostal_code_business().equals(postalCodeBusiness.trim()))) {
            changes.putString(Param.POSTAL_CODE_BUSINESS, postalCodeBusiness.trim());
        }
        if (cityBusiness != null && (user.getCity_business() == null || !user.getCity_business().equals(cityBusiness.trim()))) {
            changes.putString(Param.CITY_BUSINESS, cityBusiness.trim());
        }
        if (stateBusiness != null && (user.getState_business() == null || !user.getState_business().equals(stateBusiness.trim()))) {
            changes.putString(Param.STATE_BUSINESS, stateBusiness.trim());
        }
        if (phoneBusiness != null && (user.getTelephone_business1() == null || !user.getTelephone_business1().equals(phoneBusiness.trim()))) {
            changes.putString(Param.TELEPHONE_BUSINESS, phoneBusiness.trim());
        }
        if (phoneHome != null && (user.getTelephone_home1() == null || !user.getTelephone_home1().equals(phoneHome.trim()))) {
            changes.putString(Param.TELEPHONE_HOME, phoneHome.trim());
        }
        if (faxBusiness != null && (user.getFax_business() == null || !user.getFax_business().equals(faxBusiness.trim()))) {
            changes.putString(Param.FAX_BUSINESS, faxBusiness.trim());
        }
        if (mobile != null && (user.getCellular_telephone1() == null || !user.getCellular_telephone1().equals(mobile.trim()))) {
            changes.putString(Param.MOBILE, mobile.trim());
        }

        // email alias
        boolean change = false;
        HashSet<String> hset = new HashSet<String>();
        if (emailAlias != null) {
            for (int i = 0; i < emailAlias.length; ++i) {
                hset.add(emailAlias[i].trim());
            }
        }
        // prevent to remove primary e-mail address
        if (changeEmail) {
            hset.remove(user.getPrimaryEmail());
            hset.add(email.trim());
        } else {
            hset.add(user.getPrimaryEmail());
        }

        HashSet<String> oldHset = user.getAliases();
        int oldSize = 0;
        if (oldHset != null) {
            oldSize = oldHset.size();
        }
        if (hset.size() != oldSize) {
            change = true;
        } else {
            for (Iterator<String> iter = hset.iterator(); iter.hasNext();) {
                if (!oldHset.contains(iter.next())) {
                    change = true;
                    break;
                }
            }
        }

        if (change) {
            changes.putStringArray(Param.EMAIL_ALIAS, hset.toArray(new String[hset.size()]));
        }


        // imap login
        if (imapLogin != null && (user.getImapLogin() == null || !user.getImapLogin().equals(imapLogin.trim()))) {
            changes.putString(Param.IMAP_LOGIN, imapLogin.trim());
        }
        // imap server
        if (imapServer != null && (user.getImapServerString() == null || !user.getImapServerString().equals(imapServer.trim()))) {
            changes.putString(Param.IMAP_SERVER, imapServer.trim());
        }
        // smtp server
        if (smtpServer != null && (user.getSmtpServerString() == null || !user.getSmtpServerString().equals(smtpServer.trim()))) {
            changes.putString(Param.SMTP_SERVER, smtpServer.trim());
        }

        if (uploadFileSize == 0) {
            uploadFileSize = -1;
        }
        if (uploadFileSize != user.getUploadFileSizeLimit()) {

            changes.putInt(Param.UPLOAD_FILE_SIZE, uploadFileSize);
        }

        // map to - 1, -1 == unlimited
        if (uploadFileSizePerFile == 0) {
            uploadFileSizePerFile = -1;
        }
        if (uploadFileSizePerFile != user.getUploadFileSizeLimitPerFile()) {
            changes.putInt(Param.UPLOAD_FILE_SIZE_PER_FILE, uploadFileSizePerFile);
        }

        if (changes.size() > 0) {
            return changes;
        }

        return null;
    }

    static public void change(int contextID, Credentials auth, int userID, ParamMap changes, UserAction action) throws Exception {
        if (changes == null) {
            return;
        }
        String displayName = changes.getString(Param.DISPLAY_NAME);
        String firstName = changes.getString(Param.FIRST_NAME);
        String lastName = changes.getString(Param.LAST_NAME);
        String loginName = changes.getString(Param.LOGIN_NAME);
        String pwd = changes.getString(Param.PASSWORD);
        String email = changes.getString(Param.EMAIL);
        String lang = changes.getString(Param.LANGUAGE);
        String timeZone = changes.getString(Param.TIME_ZONE);
        String department = changes.getString(Param.DEPARTMENT);
        String company = changes.getString(Param.COMPANY);
        String[] emailAlias = changes.getStringArray(Param.EMAIL_ALIAS);
        String imapLogin = changes.getString(Param.IMAP_LOGIN);
        String smtpServer = changes.getString(Param.SMTP_SERVER);
        String imapServer = changes.getString(Param.IMAP_SERVER);
        Integer iv = changes.getInt(Param.UPLOAD_FILE_SIZE);
        int uploadFileSize = iv != null ? iv : 0;
        iv = changes.getInt(Param.UPLOAD_FILE_SIZE_PER_FILE);
        int uploadFileSizePerFile = iv != null ? iv : 0;
        String middleName = changes.getString(Param.MIDDLE_NAME);
        String streetHome = changes.getString(Param.STREET_HOME);
        String postalCodeHome = changes.getString(Param.POSTAL_CODE_HOME);
        String cityHome = changes.getString(Param.CITY_HOME);
        String stateHome = changes.getString(Param.STATE_HOME);
        String streetBusiness = changes.getString(Param.STREET_BUSINESS);
        String postalCodeBusiness = changes.getString(Param.POSTAL_CODE_BUSINESS);
        String cityBusiness = changes.getString(Param.CITY_BUSINESS);
        String stateBusiness = changes.getString(Param.STATE_BUSINESS);
        String phoneBusiness = changes.getString(Param.TELEPHONE_BUSINESS);
        String faxBusiness = changes.getString(Param.FAX_BUSINESS);
        String phoneHome = changes.getString(Param.TELEPHONE_HOME);
        String mobile = changes.getString(Param.MOBILE);



        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.changeUser(): RMIHOST is not set!");
        }
        Context ctx = new Context(contextID);
        User user = getUser(contextID, auth, userID);
        User newUser = new User(userID);
        boolean change = false;
        // displayed name
        if (displayName != null && !user.getDisplay_name().equals(displayName.trim())) {
            change = true;
            newUser.setDisplay_name(displayName.trim());
        }
        // first name
        if (firstName != null && !user.getGiven_name().equals(firstName.trim())) {
            change = true;
            newUser.setGiven_name(firstName.trim());
        }
        // last name
        if (lastName != null && !user.getSur_name().equals(lastName.trim())) {
            change = true;
            newUser.setSur_name(lastName.trim());
        }
        // login name
        if (loginName != null && !user.getName().equals(loginName.trim())) {
            change = true;
            newUser.setName(loginName.trim());
        }
        // password
        if (pwd != null && !pwd.trim().equals("")) {
            change = true;
            newUser.setPassword(pwd.trim());
        }

        // email
        if (email != null && !user.getPrimaryEmail().equals(email.trim())) {
            change = true;
            String tmp = email.trim();
            newUser.setPrimaryEmail(tmp);
            newUser.setEmail1(tmp);
            newUser.setDefaultSenderAddress(tmp);
            newUser.setMailenabled(true);
        }
        // language
        if (lang != null && (user.getLanguage() == null || !user.getLanguage().equals(lang.trim()))) {
            change = true;
            newUser.setLanguage(lang.trim());
        }
        // timezone
        if (timeZone != null && (user.getTimezone() == null || !user.getTimezone().equals(timeZone.trim()))) {
            change = true;
            newUser.setTimezone(timeZone.trim());
        }
        // department
        if (department != null && (user.getDepartment() == null || !user.getDepartment().equals(department.trim()))) {
            change = true;
            newUser.setDepartment(department.trim());
        }
        // company
        if (company != null && (user.getCompany() == null || !user.getCompany().equals(company.trim()))) {
            change = true;
            newUser.setCompany(company.trim());
        }

        if (middleName != null && (user.getMiddle_name() == null || !user.getMiddle_name().equals(middleName.trim()))) {
            change = true;
            newUser.setMiddle_name(middleName.trim());
        }

        if (streetHome != null && (user.getStreet_home() == null || !user.getStreet_home().equals(streetHome.trim()))) {
            change = true;
            newUser.setStreet_home(streetHome.trim());
        }

        if (postalCodeHome != null && (user.getPostal_code_home() == null || !user.getPostal_code_home().equals(postalCodeHome.trim()))) {
            change = true;
            newUser.setPostal_code_home(postalCodeHome.trim());
        }

        if (cityHome != null && (user.getCity_home() == null || !user.getCity_home().equals(cityHome.trim()))) {
            change = true;
            newUser.setCity_home(cityHome.trim());
        }

        if (stateHome != null && (user.getState_home() == null || !user.getState_home().equals(stateHome.trim()))) {
            change = true;
            newUser.setState_home(stateHome.trim());
        }

        if (streetBusiness != null && (user.getStreet_business() == null || !user.getStreet_business().equals(streetBusiness.trim()))) {
            change = true;
            newUser.setStreet_business(streetBusiness.trim());
        }

        if (postalCodeBusiness != null && (user.getPostal_code_business() == null || !user.getPostal_code_business().equals(postalCodeBusiness.trim()))) {
            change = true;
            newUser.setPostal_code_business(postalCodeBusiness.trim());
        }


        if (cityBusiness != null && (user.getCity_business() == null || !user.getCity_business().equals(cityBusiness.trim()))) {
            change = true;
            newUser.setCity_business(cityBusiness.trim());
        }
        if (stateBusiness != null && (user.getState_business() == null || !user.getState_business().equals(stateBusiness.trim()))) {
            change = true;
            newUser.setState_business(stateBusiness.trim());
        }
        if (phoneBusiness != null && (user.getTelephone_business1() == null || !user.getTelephone_business1().equals(phoneBusiness.trim()))) {
            change = true;
            newUser.setTelephone_business1(phoneBusiness.trim());
        }
        if (phoneHome != null && (user.getTelephone_home1() == null || !user.getTelephone_home1().equals(phoneHome.trim()))) {
            change = true;
            newUser.setTelephone_home1(phoneHome.trim());
        }
        if (faxBusiness != null && (user.getFax_business() == null || !user.getFax_business().equals(faxBusiness.trim()))) {
            change = true;
            newUser.setFax_business(faxBusiness.trim());
        }
        if (mobile != null && (user.getCellular_telephone1() == null || !user.getCellular_telephone1().equals(mobile.trim()))) {
            change = true;
            newUser.setCellular_telephone1(mobile.trim());
        }

        // email alias      
        if (emailAlias != null) {
            HashSet<String> hset = new HashSet<String>();
            change = true;
            for (int i = 0; i < emailAlias.length; ++i) {
                hset.add(emailAlias[i].trim());
            }
            newUser.setAliases(hset);
        }
        // imap login
        if (imapLogin != null && (user.getImapLogin() == null || !user.getImapLogin().equals(imapLogin.trim()))) {
            change = true;
            newUser.setImapLogin(imapLogin.trim());
        }
        // imap server
        if (imapServer != null && (user.getImapServerString() == null || !user.getImapServerString().equals(imapServer.trim()))) {
            change = true;
            newUser.setImapServer(imapServer.trim());
        }
        // smtp server
        if (smtpServer != null && (user.getSmtpServerString() == null || !user.getSmtpServerString().equals(smtpServer.trim()))) {
            change = true;
            newUser.setSmtpServer(smtpServer.trim());
        }

        if (uploadFileSize == 0) {
            uploadFileSize = -1;
        }
        if (uploadFileSize != user.getUploadFileSizeLimit()) {
            change = true;
            newUser.setUploadFileSizeLimit(uploadFileSize * 1024 * 1024);
        }

        // map to - 1, -1 == unlimited
        if (uploadFileSizePerFile == 0) {
            uploadFileSizePerFile = -1;
        }
        if (uploadFileSizePerFile != user.getUploadFileSizeLimitPerFile()) {
            change = true;
            newUser.setUploadFileSizeLimitPerFile(uploadFileSizePerFile * 1024 * 1024);
        }

        if (change) {
            OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
            synchronized (sync) {
                iface.change(ctx, newUser, auth);
                if (cacheLiveTime > 0) {
                    UserCacheEntry uc = cache.get(contextID);
                    if (uc != null) {
                        if (System.currentTimeMillis() - uc.getCreationTime() < cacheLiveTime) {
                            User[] list = uc.getList();
                            for (int i = 0; i < list.length; ++i) {
                                if (list[i].getId() == userID) {
                                    list[i] = getUser(contextID, auth, userID);
                                    break;
                                }
                            }
                        }
                    }
                }
                if (action != null) {
                    action.doJob(contextID);
                }

            }
        }
    }

    static public User create(int contextID, Credentials auth, String loginName, String displayName, String firstName, String lastName, String pwd, String email, ParamMap map, UserAction action) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXuser.createUser(): RMIHOST is not set!");
        }
        if (map == null) {
            map = new ParamMap();
        }
        String lang = map.getString(Param.LANGUAGE);
        String timeZone = map.getString(Param.TIME_ZONE);
        String department = map.getString(Param.DEPARTMENT);
        String company = map.getString(Param.COMPANY);
        String[] emailAlias = map.getStringArray(Param.EMAIL_ALIAS);
        String imapLogin = map.getString(Param.IMAP_LOGIN);
        String smtpServer = map.getString(Param.SMTP_SERVER);
        String imapServer = map.getString(Param.IMAP_SERVER);
        Integer iv = map.getInt(Param.UPLOAD_FILE_SIZE);
        int uploadFileSize = iv != null ? iv : 0;
        iv = map.getInt(Param.UPLOAD_FILE_SIZE_PER_FILE);
        int uploadFileSizePerFile = iv != null ? iv : 0;

        String middleName = map.getString(Param.MIDDLE_NAME);
        String streetHome = map.getString(Param.STREET_HOME);
        String postalCodeHome = map.getString(Param.POSTAL_CODE_HOME);
        String cityHome = map.getString(Param.CITY_HOME);
        String stateHome = map.getString(Param.STATE_HOME);
        String streetBusiness = map.getString(Param.STREET_BUSINESS);
        String postalCodeBusiness = map.getString(Param.POSTAL_CODE_BUSINESS);
        String cityBusiness = map.getString(Param.CITY_BUSINESS);
        String stateBusiness = map.getString(Param.STATE_BUSINESS);
        String phoneBusiness = map.getString(Param.TELEPHONE_BUSINESS);
        String faxBusiness = map.getString(Param.FAX_BUSINESS);
        String phoneHome = map.getString(Param.TELEPHONE_HOME);
        String mobile = map.getString(Param.MOBILE);

        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);

        User user = new User();
        // displayed name
        if (displayName == null || displayName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): displayName is empty!");
        }
        user.setDisplay_name(displayName.trim());
        // first name
        if (firstName == null || firstName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): firstName is empty!");
        }
        user.setGiven_name(firstName.trim());
        // last name
        if (lastName == null || lastName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): lastName is empty!");
        }
        // login name
        user.setSur_name(lastName.trim());
        if (loginName == null || loginName.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): loginName is empty!");
        }
        user.setName(loginName.trim());
        // password
        if (pwd == null || pwd.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): pwd is empty!");
        }
        user.setPassword(pwd.trim());
        // email
        if (email == null || email.trim().equals("")) {
            throw new Exception("OXcontext.createContext(): email is empty!");
        }
        user.setMailenabled(true);
        user.setPrimaryEmail(email);
        user.setDefaultSenderAddress(email);
        user.setEmail1(email);
        // language
        if (lang != null) {
            user.setLanguage(lang);
        }
        // timezone
        if (timeZone != null) {
            user.setTimezone(timeZone);
        }
        // department
        if (department != null) {
            user.setDepartment(department);
        }
        // company
        if (company != null) {
            user.setCompany(company);
        }
        // email alias
        if (emailAlias != null) {
            HashSet<String> alias = new HashSet<String>();
            for (int i = 0; i < emailAlias.length; ++i) {
                alias.add(emailAlias[i]);
            }
            user.setAliases(alias);
        }
        // imap login
        if (imapLogin != null) {
            user.setImapLogin(imapLogin);
        }
        // imap server
        if (imapServer != null) {
            user.setImapServer(imapServer);
        }
        // uploadSizeLimit
        if (uploadFileSize > 0) {
            user.setUploadFileSizeLimit(uploadFileSize * 1024 * 1024);
        }
        //second name
        if (middleName != null) {
            user.setMiddle_name(middleName);
        }
        if (streetHome != null) {
            user.setStreet_home(streetHome);
        }
        if (postalCodeHome != null) {
            user.setPostal_code_home(postalCodeHome);
        }
        if (cityHome != null) {
            user.setCity_home(cityHome);
        }
        if (stateHome != null) {
            user.setState_home(stateHome);
        }
        if (streetBusiness != null) {
            user.setStreet_business(streetBusiness);
        }
        if (postalCodeBusiness != null) {
            user.setPostal_code_business(postalCodeBusiness);
        }
        if (cityBusiness != null) {
            user.setCity_business(cityBusiness);
        }
        if (stateBusiness != null) {
            user.setState_business(stateBusiness);
        }
        if (phoneBusiness != null) {
            user.setTelephone_business1(phoneBusiness);
        }
        if (phoneHome != null) {
            user.setTelephone_home1(phoneHome);
        }
        if (faxBusiness != null) {
            user.setFax_business(faxBusiness);
        }
        if (mobile != null) {
            user.setCellular_telephone1(mobile);
        }
        // uploadSizeLimitPerFile
        if (uploadFileSizePerFile > 0) {
            user.setUploadFileSizeLimitPerFile(uploadFileSizePerFile * 1024 * 1024);
        }
        // smtp server
        if (smtpServer != null) {
            user.setSmtpServer(smtpServer);
        }

        if (action != null) {
            action.doJob(contextID);
        }

        String accessCombination;
        if (map.getString(Param.ACCESS_COMBINATION) != null) {
            accessCombination = map.getString(Param.ACCESS_COMBINATION);
        } else {
            accessCombination = OXcontext.getAccessCombinationName(ctx);
        }
        User newUser;
        synchronized (sync) {
            newUser = iface.create(ctx, user, accessCombination, auth);
            if (cacheLiveTime > 0) {
                UserCacheEntry uc = cache.get(contextID);
                if (uc != null) {
                    if (System.currentTimeMillis() - uc.getCreationTime() < cacheLiveTime) {
                        User[] oldList = uc.getList();
                        User[] list = new User[oldList.length + 1];
                        int i;
                        for (i = 0; i < oldList.length; ++i) {
                            list[i] = oldList[i];
                        }
                        list[i] = newUser;
                        uc.setList(list);
                    }
                }
            }
        }
        return newUser;
    }

    public static void delete(int contextID, Credentials auth, int userID, UserAction action) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.delete(): RMIHOST is not set!");
        }
        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        User user = new User(userID);

        synchronized (sync) {
            iface.delete(ctx, user, auth);
            if (cacheLiveTime > 0) {
                UserCacheEntry uc = cache.get(contextID);
                if (uc != null) {
                    if (System.currentTimeMillis() - uc.getCreationTime() < cacheLiveTime) {
                        User[] oldList = uc.getList();
                        User[] list = new User[oldList.length - 1];
                        int index = 0;
                        for (int i = 0; i < oldList.length; ++i) {
                            if (oldList[i].getId() == userID) {
                                continue;
                            }
                            list[index++] = oldList[i];
                        }
                        uc.setList(list);
                    } else {
                        // invalidate cache
                        cache.remove(contextID);
                    }
                }
            }
            if (action != null) {
                action.doJob(userID);
            }
        }
    }

    public static UserModuleAccess getModuleAccess(int contextID, Credentials auth, int userID) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.getModuleAccess(): RMIHOST is not set!");
        }
        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        User user = new User(userID);
        return iface.getModuleAccess(ctx, user, auth);
    }

    public static void changeModuleAccess(int contextID, Credentials auth, int userID, UserModuleAccess moduleAccess) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.getModuleAccess(): RMIHOST is not set!");
        }
        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        User user = new User(userID);
        iface.changeModuleAccess(ctx, user, moduleAccess, auth);

    }

    public static void changeModuleAccess(int contextID, Credentials auth, int userID, HashMap<String, String> map) throws Exception {
        OXserverInfo oxsi = OXserverInfo.getInstance();
        if (oxsi.getRMIhost() == null) {
            throw new Exception("OXcontext.getModuleAccess(): RMIHOST is not set!");
        }
        OXUserInterface iface = (OXUserInterface) Naming.lookup(oxsi.getRMIhost() + OXUserInterface.RMI_NAME);
        Context ctx = new Context(contextID);
        User user = new User(userID);
        UserModuleAccess moduleAccess = iface.getModuleAccess(ctx, user, auth);
        if (_UserModuleAccess == null) {
            _UserModuleAccess = Class.forName("com.openexchange.admin.rmi.dataobjects.UserModuleAccess");
        }
        boolean change = false;
        for (int i = 0; i < reflectionModuleAccess.length; ++i) {
            String methodName = reflectionModuleAccess[i].getMethodName();
            boolean value = false;
            if (map.get(reflectionModuleAccess[i].methodName) != null) {
                value = true;
            }
            Method method = _UserModuleAccess.getMethod(methodName, (Class[]) null);
            if ((Boolean) method.invoke(moduleAccess, (Object[]) null) != value) {
                method = _UserModuleAccess.getMethod("set" + reflectionModuleAccess[i].getDisplayName(), boolean.class);
                method.invoke(moduleAccess, value);
                change = true;
            }
        }
        if (change) {
            iface.changeModuleAccess(ctx, user, moduleAccess, auth);
        }
    }

    public static ReflectionModuleAccess[] getModuleAccessMethods() {
        if (reflectionModuleAccess == null) {
            ArrayList<ReflectionModuleAccess> list = new ArrayList<ReflectionModuleAccess>();

            try {
                if (_UserModuleAccess == null) {
                    _UserModuleAccess = Class.forName("com.openexchange.admin.rmi.dataobjects.UserModuleAccess");
                }
                Method m[] = _UserModuleAccess.getDeclaredMethods();
                for (int i = 0; i < m.length; i++) {
                    if(m[i].getReturnType() != boolean.class) {
                            continue;
                        }
                    String tmp = m[i].getName();
                    if (tmp.startsWith("get")) {

                        list.add(new ReflectionModuleAccess(tmp.substring(3), tmp));
                    }
                    if (tmp.startsWith("is")) {
                        list.add(new ReflectionModuleAccess(tmp.substring(2), tmp));
                    }
                }
            } catch (Throwable e) {
                System.err.println(e);
            }
            reflectionModuleAccess = list.toArray(new ReflectionModuleAccess[list.size()]);
        }
        return reflectionModuleAccess;
    }

    public static void setCacheLiveTime(int sec) {
        cacheLiveTime = sec * 1000;
        new Thread(new OXuser()).start();
    }

    /**
     * Cleaning thread for the user cache
     */
    public void run() {
        do {
            try {
                // 5 min
                Thread.sleep(5 * 60 * 1000);
            } catch (Exception e) {
            }
            if (!cache.isEmpty()) {
                synchronized (sync) {
                    Integer[] keys = cache.keySet().toArray(new Integer[cache.size()]);
                    for (int i = 0; i < keys.length; ++i) {
                        UserCacheEntry u = cache.get(keys[i]);
                        if (System.currentTimeMillis() - u.getCreationTime() > cacheLiveTime) {
                            cache.remove(keys[i]);
                        }
                    }
                }
            }
        } while (true);
    }
    private static final Object sync = new Object();
    private static Class _UserModuleAccess;
    private static ReflectionModuleAccess[] reflectionModuleAccess;
    private static HashMap<Integer, UserCacheEntry> cache = new HashMap<Integer, UserCacheEntry>();
    private static int cacheLiveTime;
}
