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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class StackTrace {

    public static String toString(Exception e) {
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    public static String toHTML(Exception e) {
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        String html = w.toString();
        html = html.replaceAll("\n", "<br >");
        return html;
    }

    public static String toString(Error e) {
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    public static String toHTML(Error e) {
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        String html = w.toString();
        html = html.replaceAll("\n", "<br >");
        return html;
    }
}
