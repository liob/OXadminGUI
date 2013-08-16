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

import at.sciencesoft.system.Config;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public class PluginJava extends Plugin {

    public static void main(String args[]) {
        try {
            Config.getInstance().init("d:/develop/netbeans/OXadminGUI/web/psoxgui.properties");
            Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.USER);

            UserIface ui = plist[0].getUserIface();

            ui.change(true, true, 0, null, 2, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PluginJava(String name, String id, PLUGIN type, int priority, String classPath, boolean isJar, String className, boolean enabled) throws Exception {
        super(name, id, type, priority, enabled);
        this.className = className;
        this.classPath = classPath;
        this.isJar = isJar;
      

        try {

            if (classPath == null) {
                clazz = Class.forName(className).newInstance();
            } else {
                // do not use!!
                clazz = loadClassFromFile(classPath, isJar, className).newInstance();
            }

            switch (type) {
                case BUNDLE:
                    if (!(clazz instanceof BundleIface)) {
                        throw new Exception("Class " + className + " doesn't implements 'BundleIface'!");
                    }
                    version = ((BundleIface) clazz).getVersion();
                    break;
                case CONTEXT:
                    if (!(clazz instanceof ContextIface)) {
                        throw new Exception("Class " + className + " doesn't implements ContextIface'!");
                    }
                    version = ((ContextIface) clazz).getVersion();
                    break;
                case USER:
                    if (!(clazz instanceof UserIface)) {
                        throw new Exception("Class " + className + " doesn't implements 'UserIface'!");
                    }
                    version = ((UserIface) clazz).getVersion();
                    break;
                case GROUP:
                    if (!(clazz instanceof GroupIface)) {
                        throw new Exception("Class " + className + " doesn't implements 'GroupIface'!");
                    }
                    version = ((GroupIface) clazz).getVersion();
                    break;
                case RESOURCE:
                    if (!(clazz instanceof ResourceIface)) {
                        throw new Exception("Class " + className + " doesn't implements 'ResoucreIface'!");
                    }
                    version =  ((ResourceIface) clazz).getVersion();
                    break;
            }
            loadError = false;
        } catch (Exception e) {
            loadError = true;
            exception = e;
            this.disable();
        }

    }

    public String getVersion() {
        return version;
    }

    // do not use!!
    private void addURLtoClassPath(URL u) throws IOException {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].toString().equalsIgnoreCase(u.toString())) {
                return;
            }
        }
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{u});
        } catch (Throwable t) {
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

    // do not use!!
    private Class loadClassFromFile(
            String path,
            boolean isJar,
            String className) throws Exception {
        File f = new File(path);
        addURLtoClassPath(f.toURI().toURL());

        if (isJar) {
            if (!f.exists()) {
                throw new Exception("PluginJava.loadClassFromFile(): Missing file - '" + path + "'");
            }
            if (!f.isFile()) {
                throw new Exception("PluginJava.loadClassFromFile(): Not a file '" + path + "'");
            }

            path = "jar:file://" + path + "!/";
        } else {
            if (!f.exists()) {
                throw new Exception("PluginJava.loadClassFromFile(): Missing directory - '" + path + "'");
            }
            if (!f.isDirectory()) {
                throw new Exception("PluginJava.loadClassFromFile(): Not a directory - '" + path + "'");
            }
            String tmp = f.getAbsolutePath() + File.separatorChar + className.replace('.', File.separatorChar) + ".class";
            if (!new File(tmp).exists()) {
                throw new Exception("PluginJava.loadClassFromFile(): Can not find class - '" + tmp + "'");
            }
            path = "file://" + path;
        }

        File file = new File(path);
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};
        return urls.getClass().getClassLoader().loadClass(className);
    }

    public UserIface getUserIface() {
        return (UserIface) clazz;
    }

    public BundleIface getBundleIface() {
        return (BundleIface) clazz;
    }

    public ContextIface getContextIface() {
        return (ContextIface) clazz;
    }

    public GroupIface getGroupIface() {
        return (GroupIface) clazz;
    }

    public ResourceIface getResourceIface() {
        return (ResourceIface) clazz;
    }

    public String getClassName() {
        return className;
    }

    public String getType() {
        return "JAVA";
    }

    public String getClassPath() {
        return classPath;
    }

    public boolean isJAR() {
        return isJar;
    }
    private String version;
    private String classPath;
    private String className;
    private Object clazz;
    private boolean isJar;
}
