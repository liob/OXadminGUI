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

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class RollbackEntry {
    public RollbackEntry(TYPE type,int id) {
        this.type = type;
        this.id = id;
    }

    public RollbackEntry(TYPE type,String[] list) {
        this.type = type;
        this.list = list;
    }

    public RollbackEntry(File file) {
        this.type = TYPE.FILE;
        this.file = file;
    }

    public RollbackEntry(TYPE type,HashMap<String, String> map) {
        this.type = type;
        this.map = map;
    }

    public TYPE getType() {
        return type;
    }

    public int getID() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public String[] getList() {
        return list;
    }

    private TYPE type;
    private int id;
    private File file;
    private HashMap<String, String> map;
    private String[] list;

    public enum TYPE { EMAIL_DOMAIN,EMAIL,EMAIL_ALIAS,EMAIL_ALIAS_REMOVE,EMAIL_SPAMFILTER,EMAIL_FILTER,FILE,EMAIL_CHANGE,EMAIL_FORWARD };
}
