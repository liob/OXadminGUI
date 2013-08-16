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

import java.util.HashMap;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class ResultSet {
   public ResultSet(boolean success,String errorMsg,String msg,HashMap map) {
       this.success = success;
       this.map = map;
       this.errorMsg = errorMsg;
       this.msg = msg;
   }

   public ResultSet(boolean success,HashMap map) {
       this.success = success;
       this.map = map;
       this.errorMsg = null;
       this.msg = null;
   }

   public ResultSet(boolean success) {
       this.success = success;
   }

   public boolean isSuccess() {
       return success;
   }

   public HashMap getMap() {
       return map;
   }

   public String getErrorMsg() {
       return errorMsg;
   }

   public String getErrorMsgHTML() {
       return errorMsg.replaceAll("\n", "<br />");
   }

   public String getMsg() {
       return msg;
   }

   public String getMsgHTML() {
       return msg.replaceAll("\n", "<br />");
   }


   private boolean  success;
   private String errorMsg;
   private String msg;
   private HashMap map;
}
