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

/**
 * OSGI bundle information class
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXbundleInfo {
    
    public OXbundleInfo(String bundleName, String status) {
        this.bundleName = bundleName;
        this.status = status;
    }

    /**
     * Returns the name of the OSGI bundle.
     * @return OSGI bundle name
     */
    public String getBundleName() {
        return bundleName;
    }

    /**
     * Returns the status of the loaded OSGI bundle - ACTIVE, RESOLVED, ...
     * @return OSGI bundle status
     */
    public String getStatus() {
        return status;
    }

    private String bundleName;
    private String status;
}
