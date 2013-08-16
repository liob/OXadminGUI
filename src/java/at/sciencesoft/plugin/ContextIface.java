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

package at.sciencesoft.plugin;

import at.sciencesoft.oxrmi.OXcontext;
import at.sciencesoft.oxrmi.ParamMap;


/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public interface ContextIface {
    public ResultSet create(boolean isPost,boolean isSuccess,int contextID, String contextName, String[] mapping, long quota, String accessCombination,String loginName, String displayName,
            String firstName, String lastName,  String pwd, String email, String lang, String timeZone,ParamMap contextInfos,ResultSet rs,OXcontext context);
    public ResultSet change(boolean isPost,boolean isSuccess,int contextID, ParamMap changes,ParamMap contextInfos, boolean downgrade,ResultSet rs);
    public ResultSet delete(boolean isPost,boolean isSuccess,int contextID,ResultSet rs);
    public ResultSet enable(boolean isPost,boolean isSuccess,int contextID,ResultSet rs);
    public ResultSet disable(boolean isPost,boolean isSuccess,int contextID,ResultSet rs);
    public boolean checkGUIflag(FIELD field);
    public String getVersion();
    enum FIELD {DISABLE_EMAIL_DOMAIN,DISABLE_SMTP_SERVER,DISABLE_IMAP_SERVER,JS_CHECK_EMAIL,JS_CHECK_EMAIL_DOMAIN};
}
