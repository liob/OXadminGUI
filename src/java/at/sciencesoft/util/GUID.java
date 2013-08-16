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
package at.sciencesoft.util;

import java.rmi.dgc.VMID;

/**
 *
 * @author pezi
 */
public class GUID {

    static public String getUID() {
        char[] tmp = ((new VMID()).toString()).toCharArray();
        int index = 0;
        // filter the formating characters of the VM ID
        for (int i = 0; i < tmp.length; ++i) {
            switch (tmp[i]) {
                case '-':
                case ':':
                    continue;
                default:
                    tmp[index++] = tmp[i];
            }
        }
        return new String(tmp, 0, index);
    }
}
