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

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ParamMap {

    public ParamMap() {
         map = new HashMap<Param, Object>();
    }

    public int size() {
        return map.size();
    }

    public void putString(Param param, String str) throws Exception {
        if (param.getType() != Param.TYPE.STRING) {
            throw new Exception("ParamMap.putString(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        map.put(param, str);
    }

    public void putStringArray(Param param, String[] list) throws Exception {
        if (param.getType() != Param.TYPE.STRINGLIST) {
            throw new Exception("ParamMap.putStringList(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        map.put(param, list);
    }

    public void putInt(Param param, int i) throws Exception {
        if (param.getType() != Param.TYPE.INT) {
            throw new Exception("ParamMap.putInt(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        map.put(param, i);
    }

     public void putBoolean(Param param, boolean b) throws Exception {
        if (param.getType() != Param.TYPE.BOOLEAN) {
            throw new Exception("ParamMap.putBoolean(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        map.put(param, b);
    }

    public String getString(Param param) throws Exception {
        if (param.getType() != Param.TYPE.STRING) {
            throw new Exception("ParamMap.getString(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        return (String) map.get(param);
    }

    public String[] getStringArray(Param param) throws Exception {
        if (param.getType() != Param.TYPE.STRINGLIST) {
            throw new Exception("ParamMap.getStringList(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        return (String[]) map.get(param);
    }

    public Integer getInt(Param param) throws Exception {
        if (param.getType() != Param.TYPE.INT) {
            throw new Exception("ParamMap.getInt(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        return (Integer) map.get(param);
    }

    public Boolean getBoolean(Param param) throws Exception {
        if (param.getType() != Param.TYPE.BOOLEAN) {
            throw new Exception("ParamMap.getBoolean(): wrong type - " + param.name() + " (" + param.getType() + ")");
        }
        return (Boolean) map.get(param);
    }

    public String toString(Param param) {
        if (param.getType() == Param.TYPE.STRING) {
            return (String) map.get(param);
        }
        if (param.getType() == Param.TYPE.INT) {
            return "" + (Integer) map.get(param);
        }
        if (param.getType() == Param.TYPE.STRINGLIST) {
            String[] list = (String[]) map.get(param);
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < list.length; ++i) {
                if(i > 0) {
                    buf.append(',');
                }
                buf.append(list[i]);
            }
            return buf.toString();
        }
        return null;
    }

    public Iterator<Param> getIterator() {
        return map.keySet().iterator();
    }
    private HashMap<Param, Object> map;
}
