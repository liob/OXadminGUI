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
package at.sciencesoft.webserver;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class WebServerConfig {

    private WebServerConfig() {
    }

    static public synchronized WebServerConfig getInstance() {
        if (instance == null) {
            instance = new WebServerConfig();
        }
        return instance;
    }

    public void setFileManager(FileManager fManager) {
        fileManager = fManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void cleanup() {
        if(fileManager != null) {
            fileManager.cleanup();
        }
    }

    public void setServletMapping(String mapping) {
        if (!mapping.endsWith("/")) {
            servletMapping = mapping + "/";
        } else {
            servletMapping = mapping;
        }
    }

    public String getServletMapping() {
        return servletMapping;
    }

    public void setStartFile(String startFile) {
        this.startFile = startFile;
    }

    public String getStartFile() {
        return startFile;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }
    
    
    private static WebServerConfig instance;
    private FileManager fileManager;
    private TemplateManager templateManager;
    private String servletMapping;
    private String startFile;
}
