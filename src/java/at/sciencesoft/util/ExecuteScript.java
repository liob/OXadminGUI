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

import at.sciencesoft.plugin.ResultSet;
import java.io.File;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ExecuteScript {

    static public String escapeCharacter(String s) {
        return s.replaceAll("\"", "\\\"");
    }

    static public ResultSet call(String commands[], String errorPath, String msgPath) {
        try {


            Process p = Runtime.getRuntime().exec(commands);
            int result = p.waitFor();
            boolean success = result == 0 ? true : false;
            String errorMsg = "Unknown script error";
            StringBuffer buf = new StringBuffer();
            String msg = null;
            if (!success) {
                if (errorPath != null) {
                    buf.append("Script: ");
                    for (int i = 0; i < commands.length; ++i) {
                        if (i > 0) {
                            buf.append(' ');
                        }
                        buf.append(commands[i]);
                    }
                    buf.append('\n');
                    File f = new File(errorPath);
                    if (f.exists()) {
                        errorMsg = new String(Stream.readCharStream(errorPath));
                    }
                    buf.append(errorMsg);
                }
            } else {
                if (msgPath != null) {
                    File f = new File(msgPath);
                    if (f.exists()) {
                        msg = new String(Stream.readCharStream(msgPath));
                    }
                }

            }
            return new ResultSet(success, buf.toString(), msg, null);

        } catch (Exception e) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < commands.length; ++i) {
                if (i > 0) {
                    buf.append(' ');
                }
                buf.append(commands[i]);
            }
            buf.append('\n');
            buf.append(StackTrace.toString(e));
            return new ResultSet(false, buf.toString(), null, null);
        }
    }
}
