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

import at.sciencesoft.util.GUID;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public abstract class Plugin {

    public Plugin(String name,String id,PLUGIN  plugin,int priority,boolean enabled) {
        this.name= name;
        this.plugin =  plugin;
        this.priority = priority;
        this.enabled = enabled;
        if(id == null) {
            guid = GUID.getUID();
        } else {
            guid = id;
        }
    }
    

    abstract public UserIface getUserIface();
    abstract public BundleIface getBundleIface();
    abstract public ContextIface getContextIface();
    abstract public GroupIface getGroupIface();
    abstract public ResourceIface getResourceIface();
    abstract public String getType();
    abstract public String getVersion();

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }
    
    public String getID() {
        return guid;
    }

    public void toogle() {
        enabled = !enabled;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public PLUGIN getPlugin() {
        return plugin;
    }

    public boolean isLoadError() {
        return loadError;
    }

    public Exception getException() {
        return exception;
    }



    private String guid;
    private String name;
    private int priority;
    private PLUGIN plugin;

    protected boolean loadError;
    protected Exception exception;

    
    public enum PLUGIN { BUNDLE,CONTEXT,USER,GROUP,RESOURCE }
    public enum ACTION { CREATE,CHANGE,DELETE,ENABLE,DISABLE,START,STOP,CHANGE_PASSWORD }
    private  boolean enabled;

}
