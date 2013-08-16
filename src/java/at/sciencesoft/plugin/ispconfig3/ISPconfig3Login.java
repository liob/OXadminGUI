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

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ISPconfig3Login {

    public ISPconfig3Login()  throws Exception{
        ispConfig3 = ISPconfig3.getInstance();

    }

    public Remote getRemote() throws Exception {
        if (remote == null) {
            remote = ispConfig3.getRemoteIface();
            remote.login();
        }
        return remote;
    }
    
    public void closeRemote() throws Exception {
        if (remote != null) {
            remote.logout();
            remote = null;
        }
    }

    private ISPconfig3 ispConfig3;
    private Remote remote;
}
