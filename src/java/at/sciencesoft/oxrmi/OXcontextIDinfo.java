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
 * Helper class to store the context IDs, which are in use, and the next free context ID.
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class OXcontextIDinfo {

    public OXcontextIDinfo(int nextFreeID, int[] usedID) {
        this.nextFreeID = nextFreeID;
        this.usedID = usedID;
    }

    /**
     * Returns the next free context ID.
     * @return
     */
    public int getNextFreeID() {
        return nextFreeID;
    }

    /**
     * Returns the context IDs, which are in use.
     * @return
     */
    public int[] getUsedID() {
        return usedID;
    }

    private int[] usedID;
    private int nextFreeID;
}
