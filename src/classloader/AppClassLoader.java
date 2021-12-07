package com.qkernel.classloader;

import java.io.File;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.net.URLStreamHandler;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

import com.qkernel.Daemon;

import javax.naming.directory.DirContext;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;
@SuppressWarnings( "unchecked" )

/*
import com.core.loader.naming.ResourceAttributes;
import com.core.loader.naming.FileResourceAttributes;
import com.core.loader.util.Extension;
*/


/**
 * Specialized application class loader.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Local repositories are searched in
 * the order they are added via the initial constructor and/or any subsequent
 * calls to <code>addRepository()</code> or <code>addJar()</code>.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - No check for sealing violations or
 * security is made unless a security manager is present.
 * <p>
 * <strong>FIXME</strong> - Implement findResources.
 *
 */

public class AppClassLoader
    extends URLClassLoader
{
protected class PrivilegedFindResource
        implements PrivilegedAction {

        private String name;
        private String path;

        PrivilegedFindResource(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public Object run() {
            return findResourceInternal(name, path);
        }

    }


    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new ClassLoader
     */
    public AppClassLoader() {


        super(new URL[0]);

        this.parent = getParent();
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

    }

    /**
     * Construct a new ClassLoader with a base directory
     */
    public AppClassLoader(String baseDir) {

        this();
        setDocBase( baseDir );
    }
    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public AppClassLoader(DirContext resources) {

        super(new URL[0]);
        this.resources = resources;
        this.parent = getParent();
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

    }


    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public AppClassLoader(ClassLoader parent, DirContext resources) {

        super(new URL[0], parent);
        this.resources = resources;
        this.parent = getParent();
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

    }


    // ----------------------------------------------------- Instance Variables


    private Daemon daemon = null;


    /**
     * Associated directory context giving access to the resources in this
     * webapp.
     */
    protected DirContext resources = null;


    /**
     * The set of optional packages (formerly standard extensions) that
     * are available in the repositories associated with this class loader.
     * Each object in this list is of type
     * <code>org.apache.catalina.loader.Extension</code>.
     */
    protected ArrayList available = new ArrayList();


    /**
     * The cache of ResourceEntry for classes and resources we have loaded,
     * keyed by resource name.
     */
    protected HashMap resourceEntries = new HashMap();


    /**
     * The list of not found resources.
     */
    protected HashMap notFoundResources = new HashMap();


    /**
     * The debugging detail level of this component.
     */
    protected int debug = 0;


    /**
     * Should this class loader delegate to the parent class loader
     * <strong>before</strong> searching its own repositories (i.e. the
     * usual Java2 delegation model)?  If set to <code>false</code>,
     * this class loader will search its own repositories first, and
     * delegate to the parent only if the class or resource is not
     * found locally.
     */
    protected boolean delegate = false;


    /**
     * The list of local repositories, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected String[] repositories = new String[0];


    /**
     * Repositories translated as path in the work directory (for Jasper
     * originally), but which is used to generate fake URLs should getURLs be
     * called.
     */
    protected File[] files = new File[0];


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
    */
    protected JarFile[] jarFiles = new JarFile[0];


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected File[] jarRealFiles = new File[0];


    /**
     * The path which will be monitored for added Jar files.
     */
    protected String jarPath = null;


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected String[] jarNames = new String[0];


    /**
     * The list of JARs last modified dates, in the order they should be
     * searched for locally loaded classes or resources.
     */
    protected long[] lastModifiedDates = new long[0];


    /**
     * The list of resources which should be checked when checking for
     * modifications.
     */
    protected String[] paths = new String[0];


    /**
     * The set of optional packages (formerly standard extensions) that
     * are required in the repositories associated with this class loader.
     * Each object in this list is of type
     * <code>org.apache.catalina.loader.Extension</code>.
     */
    protected ArrayList required = new ArrayList();


    /**
     * A list of read File and Jndi Permission's required if this loader
     * is for a web application context.
     */
    private ArrayList permissionList = new ArrayList();


    /**
     * The PermissionCollection for each CodeSource for a web
     * application context.
     */
    private HashMap loaderPC = new HashMap();


    /**
     * Instance of the SecurityManager installed.
     */
    private SecurityManager securityManager = null;


    /**
     * The parent class loader.
     */
    private ClassLoader parent = null;


    /**
     * The system class loader.
     */
    private ClassLoader system = null;


    /**
     * Has this component been started?
     */
    protected boolean started = false;


    // ------------------------------------------------------------- Properties


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the "delegate first" flag for this class loader.
     */
    public boolean getDelegate() {

        return (this.delegate);

    }


    /**
     * Set the "delegate first" flag for this class loader.
     *
     * @param delegate The new "delegate first" flag
     */
    public void setDelegate(boolean delegate) {

        this.delegate = delegate;

    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for the file directory path.
     *
     * @param path file directory path
     */
    public void setPermissions(String path) {
        if( securityManager != null ) {
            permissionList.add(new FilePermission(path + "-","read"));
        }
    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for URL.
     *
     * @param url URL for a file or directory on local system
     */
    public void setPermissions(URL url) {
        setPermissions(url.toString());
    }


    /**
     * Return the JAR path.
     */
    public String getJarPath() {

        return this.jarPath;

    }


    /**
     * Change the Jar path.
     */
    public void setJarPath(String jarPath) {

        this.jarPath = jarPath;

    }


    // ------------------------------------------------------- Reloader Methods


    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a
     *  directory pathname, a JAR file pathname, or a ZIP file pathname
     *
     * @exception IllegalArgumentException if the specified repository is
     *  invalid or does not exist
     */
    public void addRepository(String repository) {

        String normalized = normalize( repository );
        File classRepository = new File(getDocBase() + normalized );
        addRepository( normalized, classRepository );

    }


    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a
     *  directory pathname, a JAR file pathname, or a ZIP file pathname
     *
     * @exception IllegalArgumentException if the specified repository is
     *  invalid or does not exist
     */
    synchronized void addRepository(String repository, File file) {

        // Note : There should be only one (of course), but I think we should
        // keep this a bit generic

        if (repository == null)
            return;

        if (debug >= 1)
            log("addRepository(" + repository + ")");

        int i;

        // Add this repository to our internal list
        String[] result = new String[repositories.length + 1];
        for (i = 0; i < repositories.length; i++) {
            result[i] = repositories[i];
        }
        result[repositories.length] = repository;
        repositories = result;

        // Add the file to the list
        File[] result2 = new File[files.length + 1];
        for (i = 0; i < files.length; i++) {
            result2[i] = files[i];
        }
        result2[files.length] = file;
        files = result2;

    }


    synchronized void addJar(String jar, JarFile jarFile, File file)
        throws IOException {

        if (jar == null)
            return;
        if (jarFile == null)
            return;
        if (file == null)
            return;

        if (debug >= 1)
            log("addJar(" + jar + ")");

        int i;

        if ((jarPath != null) && (jar.startsWith(jarPath))) {

            String jarName = jar.substring(jarPath.length());
            while (jarName.startsWith("/"))
                jarName = jarName.substring(1);

            String[] result = new String[jarNames.length + 1];
            for (i = 0; i < jarNames.length; i++) {
                result[i] = jarNames[i];
            }
            result[jarNames.length] = jarName;
            jarNames = result;

        }

        JarFile[] result2 = new JarFile[jarFiles.length + 1];
        for (i = 0; i < jarFiles.length; i++) {
            result2[i] = jarFiles[i];
        }
        result2[jarFiles.length] = jarFile;
        jarFiles = result2;

        try {

            // Register the JAR for tracking

            long lastModified =
                ((ResourceAttributes) resources.getAttributes(jar))
                .getLastModified();

            String[] result = new String[paths.length + 1];
            for (i = 0; i < paths.length; i++) {
                result[i] = paths[i];
            }
            result[paths.length] = jar;
            paths = result;

            long[] result3 = new long[lastModifiedDates.length + 1];
            for (i = 0; i < lastModifiedDates.length; i++) {
                result3[i] = lastModifiedDates[i];
            }
            result3[lastModifiedDates.length] = lastModified;
            lastModifiedDates = result3;

        } catch (NamingException e) {
        }

        // Add the file to the list
        File[] result4 = new File[jarRealFiles.length + 1];
        for (i = 0; i < jarRealFiles.length; i++) {
            result4[i] = jarRealFiles[i];
        }
        result4[jarRealFiles.length] = file;
        jarRealFiles = result4;

        // Load manifest
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            Iterator extensions = Extension.getAvailable(manifest).iterator();
            while (extensions.hasNext()) {
                available.add(extensions.next());
            }
            extensions = Extension.getRequired(manifest).iterator();
            while (extensions.hasNext()) {
                required.add(extensions.next());
            }
        }

    }


    /**
     * Return a list of "optional packages" (formerly "standard extensions")
     * that have been declared to be available in the repositories associated
     * with this class loader, plus any parent class loader implemented with
     * the same class.
     */
    public Extension[] findAvailable() {

        // Initialize the results with our local available extensions
        ArrayList results = new ArrayList();
        Iterator available = this.available.iterator();
        while (available.hasNext())
            results.add(available.next());

        // Trace our parentage tree and add declared extensions when possible
        ClassLoader loader = this;
        while (true) {
            loader = loader.getParent();
            if (loader == null)
                break;
            if (!(loader instanceof AppClassLoader))
                continue;
            Extension extensions[] =
                ((AppClassLoader) loader).findAvailable();
            for (int i = 0; i < extensions.length; i++)
                results.add(extensions[i]);
        }

        // Return the results as an array
        Extension extensions[] = new Extension[results.size()];
        return ((Extension[]) results.toArray(extensions));

    }


    /**
     * Return a String array of the current repositories for this class
     * loader.  If there are no repositories, a zero-length array is
     * returned.
     */
    public String[] findRepositories() {

        return (repositories);

    }


    /**
     * Return a list of "optional packages" (formerly "standard extensions")
     * that have been declared to be required in the repositories associated
     * with this class loader, plus any parent class loader implemented with
     * the same class.
     */
    public Extension[] findRequired() {

        // Initialize the results with our local required extensions
        ArrayList results = new ArrayList();
        Iterator required = this.required.iterator();
        while (required.hasNext())
            results.add(required.next());

        // Trace our parentage tree and add declared extensions when possible
        ClassLoader loader = this;
        while (true) {
            loader = loader.getParent();
            if (loader == null)
                break;
            if (!(loader instanceof AppClassLoader))
                continue;
            Extension extensions[] =
                ((AppClassLoader) loader).findRequired();
            for (int i = 0; i < extensions.length; i++)
                results.add(extensions[i]);
        }

        // Return the results as an array
        Extension extensions[] = new Extension[results.size()];
        return ((Extension[]) results.toArray(extensions));

    }


    /**
     * Have one or more classes or resources been modified so that a reload
     * is appropriate?
     */
    public boolean modified() {

        if (debug >= 2)
            log("modified()");

        // Checking for modified loaded resources
        int length = paths.length;

        // A rare race condition can occur in the updates of the two arrays
        // It's totally ok if the latest class added is not checked (it will
        // be checked the next time
        int length2 = lastModifiedDates.length;
        if (length > length2)
            length = length2;

        for (int i = 0; i < length; i++) {
            try {
                long lastModified =
                    //((ResourceAttributes) resources.getAttributes(paths[i]))
                    ((ResourceAttributes) getAttributes(paths[i]))
                    .getLastModified();
                if (lastModified != lastModifiedDates[i]) {
                    log(paths[i]+ " was modified from last date: "
                        + new java.util.Date(lastModifiedDates[i]) +" and reloaded");
                    return (true);
                }
            } catch (NamingException e) {
                log("    Resource '" + paths[i] + "' is missing");
                return (true);
            }
        }

        length = jarNames.length;

        // Check if JARs have been added or removed
        if (getJarPath() != null) {

            try {
                NamingEnumeration enm = resources.listBindings(getJarPath());
                int i = 0;
                while (enm.hasMoreElements() && (i < length)) {
                    NameClassPair ncPair = (NameClassPair) enm.nextElement();
                    String name = ncPair.getName();
                    // Ignore non JARs present in the lib folder
                    if (!name.endsWith(".jar"))
                        continue;
                    if (!name.equals(jarNames[i])) {
                        // Missing JAR
                        log("    Additional JARs have been added : '"
                            + name + "'");
                        return (true);
                    }
                    i++;
                }
                if (enm.hasMoreElements()) {
                    while (enm.hasMoreElements()) {
                        NameClassPair ncPair =
                            (NameClassPair) enm.nextElement();
                        String name = ncPair.getName();
                        // Additional non-JAR files are allowed
                        if (name.endsWith(".jar")) {
                            // There was more JARs
                            log("    Additional JARs have been added");
                            return (true);
                        }
                    }
                } else if (i < jarNames.length) {
                    // There was less JARs
                    log("    Additional JARs have been added");
                    return (true);
                }
            } catch (NamingException e) {
                if (debug > 2)
                    log("    Failed tracking modifications of '"
                        + getJarPath() + "'");
            } catch (ClassCastException e) {
                log("    Failed tracking modifications of '"
                    + getJarPath() + "' : " + e.getMessage());
            }

        }

        // No classes have been modified
        return (false);

    }


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("AppClassLoader\r\n");
        sb.append("  available:\r\n");
        Iterator available = this.available.iterator();
        while (available.hasNext()) {
            sb.append("    ");
            sb.append(available.next().toString());
            sb.append("\r\n");
        }
        sb.append("  delegate: ");
        sb.append(delegate);
        sb.append("\r\n");
        sb.append("  repositories:\r\n");
        for (int i = 0; i < repositories.length; i++) {
            sb.append("    ");
            sb.append(repositories[i]);
            sb.append("\r\n");
        }
        sb.append("  required:\r\n");
        Iterator required = this.required.iterator();
        while (required.hasNext()) {
            sb.append("    ");
            sb.append(required.next().toString());
            sb.append("\r\n");
        }
        if (getParent() != null) {
            sb.append("----------> Parent Classloader:\r\n");
            sb.append(getParent().toString());
            sb.append("\r\n");
        }
        return (sb.toString());

    }


    // ---------------------------------------------------- ClassLoader Methods


    /**
     * Find the specified class in our local repositories, if possible.  If
     * not found, throw <code>ClassNotFoundException</code>.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class findClass(String name) throws ClassNotFoundException {

        if (debug >= 3)
            log("    findClass(" + name + ")");

        // (1) Permission to define this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    if (debug >= 4)
                        log("      securityManager.checkPackageDefinition");
                    securityManager.checkPackageDefinition(name.substring(0,i));
                } catch (Exception se) {
                    if (debug >= 4)
                        log("      -->Exception-->ClassNotFoundException", se);
                    throw new ClassNotFoundException(name);
                }
            }
        }

        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class clazz = null;
        try {
            if (debug >= 4)
                log("      findClassInternal(" + name + ")");
            try {
                clazz = findClassInternal(name);
            } catch(AccessControlException ace) {
                ace.printStackTrace();
                throw new ClassNotFoundException(name);
            } catch (RuntimeException e) {
                if (debug >= 4)
                    log("      -->RuntimeException Rethrown", e);
                throw e;
            }
            if (clazz == null) {
                try {
                    clazz = super.findClass(name);
                } catch(AccessControlException ace) {
                    throw new ClassNotFoundException(name);
                } catch (RuntimeException e) {
                    if (debug >= 4)
                        log("      -->RuntimeException Rethrown", e);
                    throw e;
                }
            }
            if (clazz == null) {
                if (debug >= 3)
                    log("    --> Returning ClassNotFoundException");
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            if (debug >= 3)
                log("    --> Passing on ClassNotFoundException", e);
            throw e;
        }

        // Return the class we have located
        if (debug >= 4)
            log("      Returning class " + clazz);
        if ((debug >= 4) && (clazz != null))
            log("      Loaded by " + clazz.getClassLoader());
        return (clazz);

    }


    /**
     * Find the specified resource in our local repository, and return a
     * <code>URL</code> refering to it, or <code>null</code> if this resource
     * cannot be found.
     *
     * @param name Name of the resource to be found
     */
    public URL findResource(final String name) {

        if (debug >= 3)
            log("    findResource(" + name + ")");

        URL url = null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry == null) {
            if (securityManager != null) {
                PrivilegedAction dp =
                    new PrivilegedFindResource(name, name);
                entry = (ResourceEntry)AccessController.doPrivileged(dp);
            } else {
                entry = findResourceInternal(name, name);
            }
        }
        if (entry != null) {
            url = entry.source;
        }

        if (debug >= 3) {
            if (url != null)
                log("    --> Returning '" + url.toString() + "'");
            else
                log("    --> Resource not found, returning null");
        }
        return (url);

    }


    /**
     * Return an enumeration of <code>URLs</code> representing all of the
     * resources with the given name.  If no resources with this name are
     * found, return an empty enumeration.
     *
     * @param name Name of the resources to be found
     *
     * @exception IOException if an input/output error occurs
     */
    public Enumeration findResources(String name) throws IOException {

        if (debug >= 3)
            log("    findResources(" + name + ")");

        Vector result = new Vector();

        int jarFilesLength = jarFiles.length;
        int repositoriesLength = repositories.length;

        int i;

        // Looking at the repositories
        for (i = 0; i < repositoriesLength; i++) {
            try {
                String fullPath = repositories[i] + name;
                //resources.lookup(fullPath);
                lookupResource(fullPath);
                // Note : Not getting an exception here means the resource was
                // found
                try {
                    result.addElement(new File(files[i], name).toURL());
                } catch (MalformedURLException e) {
                    // Ignore
                }
            } catch (NamingException e) {
            }
        }

        // Looking at the JAR files
        for (i = 0; i < jarFilesLength; i++) {
            JarEntry jarEntry = jarFiles[i].getJarEntry(name);
            if (jarEntry != null) {
                try {
                    String jarFakeUrl = jarRealFiles[i].toURL().toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + name;
                    result.addElement(new URL(jarFakeUrl));
                } catch (MalformedURLException e) {
                    // Ignore
                }
            }
        }

        // Adding the results of a call to the superclass
        Enumeration otherResourcePaths = super.findResources(name);

        while (otherResourcePaths.hasMoreElements()) {
            result.addElement(otherResourcePaths.nextElement());
        }

        return result.elements();

    }


    /**
     * Find the resource with the given name.  A resource is some data
     * (images, audio, text, etc.) that can be accessed by class code in a
     * way that is independent of the location of the code.  The name of a
     * resource is a "/"-separated path name that identifies the resource.
     * If the resource cannot be found, return <code>null</code>.
     * <p>
     * This method searches according to the following algorithm, returning
     * as soon as it finds the appropriate URL.  If the resource cannot be
     * found, returns <code>null</code>.
     * <ul>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findResource()</code> to find this resource in our
     *     locally defined repositories.</li>
     * <li>Call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * </ul>
     *
     * @param name Name of the resource to return a URL for
     */
    public URL getResource(String name) {

        if (debug >= 2)
            log("getResource(" + name + ")");
        URL url = null;

        // (1) Delegate to parent if requested
        if (delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (debug >= 2)
                    log("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        // (2) Search local repositories
        if (debug >= 3)
            log("  Searching local repositories");
        url = findResource(name);
        if (url != null) {
            if (debug >= 2)
                log("  --> Returning '" + url.toString() + "'");
            return (url);
        }

        // (3) Delegate to parent unconditionally if not already attempted
        if( !delegate ) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (debug >= 2)
                    log("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        // (4) Resource was not found
        if (debug >= 2)
            log("  --> Resource not found, returning null");
        return (null);

    }


    /**
     * Find the resource with the given name, and return an input stream
     * that can be used for reading it.  The search order is as described
     * for <code>getResource()</code>, after checking to see if the resource
     * data has been previously cached.  If the resource cannot be found,
     * return <code>null</code>.
     *
     * @param name Name of the resource to return an input stream for
     */
    public InputStream getResourceAsStream(String name) {

        if (debug >= 2)
            log("getResourceAsStream(" + name + ")");
        InputStream stream = null;

        // (0) Check for a cached copy of this resource
        stream = findLoadedResource(name);
        if (stream != null) {
            if (debug >= 2)
                log("  --> Returning stream from cache");
            return (stream);
        }

        // (1) Delegate to parent if requested
        if (delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                // FIXME - cache???
                if (debug >= 2)
                    log("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (2) Search local repositories
        if (debug >= 3)
            log("  Searching local repositories");
        URL url = findResource(name);
        if (url != null) {
            // FIXME - cache???
            if (debug >= 2)
                log("  --> Returning stream from local");
            stream = findLoadedResource(name);
            if (stream != null)
                return (stream);
        }

        // (3) Delegate to parent unconditionally
        if (!delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                // FIXME - cache???
                if (debug >= 2)
                    log("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (4) Resource was not found
        if (debug >= 2)
            log("  --> Resource not found, returning null");
        return (null);

    }


    /**
     * Load the class with the specified name.  This method searches for
     * classes in the same manner as <code>loadClass(String, boolean)</code>
     * with <code>false</code> as the second argument.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class loadClass(String name) throws ClassNotFoundException {

       return (loadClass(name, false));

    }


    /**
     * Load the class with the specified name, searching using the following
     * algorithm until it finds and returns the class.  If the class cannot
     * be found, returns <code>ClassNotFoundException</code>.
     * <ul>
     * <li>Call <code>findLoadedClass(String)</code> to check if the
     *     class has already been loaded.  If it has, the same
     *     <code>Class</code> object is returned.</li>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>loadClass()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findClass()</code> to find this class in our locally
     *     defined repositories.</li>
     * <li>Call the <code>loadClass()</code> method of our parent
     *     class loader, if any.</li>
     * </ul>
     * If the class was found using the above steps, and the
     * <code>resolve</code> flag is <code>true</code>, this method will then
     * call <code>resolveClass(Class)</code> on the resulting Class object.
     *
     * @param name Name of the class to be loaded
     * @param resolve If <code>true</code> then resolve the class
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

        if (debug >= 2)
            log("loadClass(" + name + ", " + resolve + ")");
        Class clazz = null;

        // Don't load classes if class loader is stopped
        if (!started) {
            log("Lifecycle error : CL stopped");
            throw new ClassNotFoundException(name);
        }

        // (0) Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            if (debug >= 3)
                log("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (debug >= 3)
                log("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // If a system class, use system class loader
        if( name.startsWith("java.") ) {
            ClassLoader loader = system;
            clazz = loader.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
            throw new ClassNotFoundException(name);
        }

        // (.5) Permission to access this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0,i));
                } catch (SecurityException se) {
                    String error = "Security Violation, attempt to use " +
                        "Restricted Class: " + name;

		    daemon.event_log.SendMessage(error);

		    //                    System.out.println(error);
                    se.printStackTrace();
                    log(error);
                    throw new ClassNotFoundException(error);
                }
            }
        }

        // (1) Delegate to our parent if requested
        if (delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (debug >= 3)
                        log("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        // (2) Search local repositories
        if (debug >= 3)
            log("  Searching local repositories");
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (debug >= 3)
                    log("  Loading class from local repository");
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }

        // (3) Delegate to parent unconditionally
        if (!delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (debug >= 3)
                        log("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        // This class was not found
        throw new ClassNotFoundException(name);

    }


    /**
     * Get the Permissions for a CodeSource.  If this instance
     * of AppClassLoader is for a web application context,
     * add read FilePermission or JndiPermissions for the base
     * directory (if unpacked),
     * the context URL, and jar file resources.
     *
     * @param CodeSource where the code was loaded from
     * @return PermissionCollection for CodeSource
     */
    protected PermissionCollection getPermissions(CodeSource codeSource) {

        String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc;
        if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null) {
            pc = super.getPermissions(codeSource);
            if (pc != null) {
                Iterator perms = permissionList.iterator();
                while (perms.hasNext()) {
                    Permission p = (Permission)perms.next();
                    pc.add(p);
                }
                loaderPC.put(codeUrl,pc);
            }
        }
        return (pc);

    }


    /**
     * Returns the search path of URLs for loading classes and resources.
     * This includes the original list of URLs specified to the constructor,
     * along with any URLs subsequently appended by the addURL() method.
     * @return the search path of URLs for loading classes and resources.
     */
    public URL[] getURLs() {

        URL[] external = super.getURLs();

        int filesLength = files.length;
        int jarFilesLength = jarRealFiles.length;
        int length = filesLength + jarFilesLength + external.length;
        int i;

        try {

            URL[] urls = new URL[length];
            for (i = 0; i < length; i++) {
                if (i < filesLength) {
                    urls[i] = files[i].toURL();
                } else if (i < filesLength + jarFilesLength) {
                    urls[i] = jarRealFiles[i - filesLength].toURL();
                } else {
                    urls[i] = external[i - filesLength - jarFilesLength];
                }
            }

            return urls;

        } catch (MalformedURLException e) {
            return (new URL[0]);
        }

    }



    /**
     * Start the class loader.
     *
     */
    public void start(Daemon d)
    {
	daemon  = d;
        started = true;
	//        debug = 10;
    }


    /**
     * Stop the class loader.
     *
     */
    public void stop(){

        started = false;

        int length = jarFiles.length;
        for (int i = 0; i < length; i++) {
            try {
                jarFiles[i].close();
                jarFiles[i] = null;
            } catch (IOException e) {
                // Ignore
            }
        }

        notFoundResources.clear();
        resourceEntries.clear();
        repositories = new String[0];
        files = new File[0];
        jarFiles = new JarFile[0];
        jarRealFiles = new File[0];
        jarPath = null;
        jarNames = new String[0];
        lastModifiedDates = new long[0];
        paths = new String[0];

        required.clear();
        permissionList.clear();
        loaderPC.clear();

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Find specified class in local repositories.
     *
     * @return the loaded class, or null if the class isn't found
     */
    protected Class findClassInternal(String name)
        throws ClassNotFoundException {

        if (!validate(name))
            throw new ClassNotFoundException(name);

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + ".class";

        ResourceEntry entry = null;

        if (securityManager != null) {
            PrivilegedAction dp =
                new PrivilegedFindResource(name, classPath);
            entry = (ResourceEntry)AccessController.doPrivileged(dp);
        } else {
            entry = findResourceInternal(name, classPath);
        }

        if (entry == null)
            throw new ClassNotFoundException(name);

        Class clazz = entry.loadedClass;
        if (clazz != null)
            return clazz;

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);

        Package pkg = null;

        if (packageName != null) {

            pkg = getPackage(packageName);

            // Define the package (if null)
            if (pkg == null) {
                if (entry.manifest == null) {
                    definePackage(packageName, null, null, null, null, null,
                                  null, null);
                } else {
                    definePackage(packageName, entry.manifest, entry.source);
                }
            }

        }

        // Create the code source object
        CodeSource codeSource =
            new CodeSource(entry.source, entry.certificates);

        if (securityManager != null) {

            // Checking sealing
            if (pkg != null) {
                boolean sealCheck = true;
                if (pkg.isSealed()) {
                    sealCheck = pkg.isSealed(entry.source);
                } else {
                    sealCheck = (entry.manifest == null)
                        || !isPackageSealed(packageName, entry.manifest);
                }
                if (!sealCheck)
                    throw new SecurityException
                        ("Sealing violation loading " + name + " : Package "
                         + packageName + " is sealed.");
            }

        }

        synchronized(entry) {
            // Since all threads use the same ResourceEntry instance, it is
            // the one which will contain the class
            if (entry.loadedClass == null) {
                clazz = defineClass(name, entry.binaryContent, 0,
                                    entry.binaryContent.length, codeSource);
                entry.loadedClass = clazz;
            } else {
                clazz = entry.loadedClass;
            }
        }


        return clazz;

    }


    /**
     * Find specified resource in local repositories.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(String name, String path) {

        if (!started) {
            log("Lifecycle error : CL stopped");
            return null;
        }

        if ((name == null) || (path == null))
            return null;

        if (notFoundResources.containsKey(name))
            return null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null)
            return entry;

        int contentLength = -1;
        InputStream binaryStream = null;

        int jarFilesLength = jarFiles.length;
        int repositoriesLength = repositories.length;

        int i;

        //Resource resource = null;

        for (i = 0; (entry == null) && (i < repositoriesLength); i++) {
            try {

                String fullPath = repositories[i] + normalize(path);

                //resource = (Resource) resources.lookup(fullPath);
                lookupResource(fullPath);
                // Note : Not getting an exception here means the resource was
                // found

                entry = new ResourceEntry();
                try {
                    entry.source = new File(files[i], path).toURL();
                } catch (MalformedURLException e) {
                    return null;
                }
                ResourceAttributes attributes =
                    //(ResourceAttributes) resources.getAttributes(fullPath);
                    (ResourceAttributes) getAttributes(fullPath);
                contentLength = (int) attributes.getContentLength();
                entry.lastModified = attributes.getLastModified();
                //try {
                //    binaryStream = resource.streamContent();
                //} catch (IOException e) {
                //    return null;
                //}

                try {
                    binaryStream = entry.source.openStream();
                } catch (IOException e) {
                    return null;
                }


                // Register the full path for modification checking
                synchronized (paths) {

                    int j;

                    long[] result2 = new long[lastModifiedDates.length + 1];
                    for (j = 0; j < lastModifiedDates.length; j++) {
                        result2[j] = lastModifiedDates[j];
                    }
                    result2[lastModifiedDates.length] = entry.lastModified;
                    lastModifiedDates = result2;

                    String[] result = new String[paths.length + 1];
                    for (j = 0; j < paths.length; j++) {
                        result[j] = paths[j];
                    }
                    result[paths.length] = fullPath;
                    paths = result;

                }

            } catch (NamingException e) {
            }
        }

        JarEntry jarEntry = null;

        for (i = 0; (entry == null) && (i < jarFilesLength); i++) {

            jarEntry = jarFiles[i].getJarEntry(path);

            if (jarEntry != null) {

                entry = new ResourceEntry();
                try {
                    String jarFakeUrl = jarRealFiles[i].toURL().toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + path;
                    entry.source = new URL(jarFakeUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
                entry.certificates = jarEntry.getCertificates();
                contentLength = (int) jarEntry.getSize();
                try {
                    entry.manifest = jarFiles[i].getManifest();
                    binaryStream = jarFiles[i].getInputStream(jarEntry);
                } catch (IOException e) {
                    return null;
                }
            }

        }

        if (entry == null) {
            synchronized (notFoundResources) {
                notFoundResources.put(name, name);
            }
            return null;
        }

        byte[] binaryContent = new byte[contentLength];

        try {
            int pos = 0;
            while (true) {
                int n = binaryStream.read(binaryContent, pos,
                                          binaryContent.length - pos);
                if (n <= 0)
                    break;
                pos += n;
            }
            binaryStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        entry.binaryContent = binaryContent;

        // Add the entry in the local resource repository
        synchronized (this) {
            // Ensures that all the threads which may be in a race to load
            // a particular class all end up with the same ResourceEntry
            // instance
            ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
            if (entry2 == null) {
                resourceEntries.put(name, entry);
            } else {
                entry = entry2;
            }
        }

        return entry;

    }


    /**
     * Returns true if the specified package name is sealed according to the
     * given manifest.
     */
    protected boolean isPackageSealed(String name, Manifest man) {

        String path = name + "/";
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);

    }


    /**
     * Finds the resource with the given name if it has previously been
     * loaded and cached by this class loader, and return an input stream
     * to the resource data.  If this resource has not been cached, return
     * <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected InputStream findLoadedResource(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            if (entry.binaryContent != null)
                return new ByteArrayInputStream(entry.binaryContent);
        }
        return (null);

    }


    /**
     * Finds the class with the given name if it has previously been
     * loaded and cached by this class loader, and return the Class object.
     * If this class has not been cached, return <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected Class findLoadedClass0(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            return entry.loadedClass;
        }
        return (null);  // FIXME - findLoadedResource()

    }


    /**
     * Refresh the system policy file, to pick up eventual changes.
     */
    protected void refreshPolicy() {

        try {
            // The policy file may have been modified to adjust
            // permissions, so we're reloading it when loading or
            // reloading a Context
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch (AccessControlException e) {
            // Some policy files may restrict this, even for the core,
            // so this exception is ignored
        }

    }


    /**
     * Validate a classname. As per SRV.9.7.2, we must restict loading of
     * classes from J2SE (java.*) and classes of the servlet API
     * (javax.servlet.*). That should enhance robustness and prevent a number
     * of user error (where an older version of servlet.jar would be present
     * in /WEB-INF/lib).
     *
     * @param name class name
     * @return true if the name is valid
     */
    protected boolean validate(String name) {

        if (name == null)
            return false;
        if (name.startsWith("java."))
            return false;

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);
        else
            return true;

        if (packageName.equals("javax.servlet"))
            return false;
        if (packageName.equals("javax.servlet.http"))
            return false;
        if (packageName.equals("javax.servlet.jsp"))
            return false;
        if (packageName.equals("javax.servlet.jsp.tagext"))
            return false;

        return true;

    }


    /**
     * Log a debugging output message.
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	daemon.event_log.SendMessage(message);

//        System.out.println("AppClassLoader: " + message);

    }


    /**
     * Log a debugging output message with an exception.
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    private void log(String message, Throwable throwable) {

	daemon.event_log.SendMessage(message);

	/*
        System.out.println("AppClassLoader: " + message);
        throwable.printStackTrace(System.out);
	*/
    }


    // ------------------------------------------------------ Protected Classes


    /**
     * Resource entry.
     */
    protected static class ResourceEntry {


        /**
         * The "last modified" time of the origin file at the time this class
         * was loaded, in milliseconds since the epoch.
         */
        long lastModified;


        /**
         * Binary content of the resource.
         */
        byte[] binaryContent;


        /**
         * Loaded class.
         */
        Class loadedClass;


        /**
         * URL source from where the object was loaded.
         */
        URL source;


        /**
         * Manifest (if the resource was loaded from a JAR).
         */
        Manifest manifest = null;


        /**
         * Certificates (if the resource was loaded from a JAR).
         */
        Certificate[] certificates = null;


    }
    //----------------------------------------------------------------------
    /**
     * The document base path.
     */
    protected String docBase = null;

    /**
     * The document base directory.
     */
    protected File base = null;


    /**
     * Absolute normalized filename of the base.
     */
    protected String absoluteBase = null;


    /**
     * Case sensitivity.
     */
    protected boolean caseSensitive = true;


     /**
     * Set the document root.
     *
     * @param docBase The new document root
     *
     * @exception IllegalArgumentException if the specified value is not
     *  supported by this implementation
     * @exception IllegalArgumentException if this would create a
     *  malformed URL
     */
    public void setDocBase(String docBase) {

	// Validate the format of the proposed document root
	if (docBase == null)
	    throw new IllegalArgumentException
		( "Resource is null");

	// Calculate a File object referencing this document base directory
	File base = new File(docBase);

	// Validate that the document base is an existing directory
	if (!base.exists() || !base.isDirectory() || !base.canRead())
	    throw new IllegalArgumentException
		("Resource is not an existing directory " + docBase);
	this.base = base;
        this.absoluteBase = normalize(base.getAbsolutePath());
        this.docBase = base.getAbsolutePath();

    }


    /**
     * Return the document root for this component.
     */
    public String getDocBase() {
	return (this.docBase);
    }

    /**
     * Set case sensitivity.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    /**
     * Is case sensitive ?
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public Object lookupResource(String name)
        throws NamingException {
        Object result = null;
        String path = getDocBase();

        File file = file( name);

        if (file == null)
            throw new NamingException
                ("Resource notFound" + name);

        return file;

        /*if (file.isDirectory()) {
            FileDirContext tempContext = new FileDirContext(env);
            tempContext.setDocBase(file.getPath());
            result = tempContext;
        } else {
            result = new FileResource(file);
        }

        return result;*/

    }

    /**
     * Retrieves selected attributes associated with a named object.
     * See the class description regarding attribute models, attribute type
     * names, and operational attributes.
     *
     * @return the requested attributes; never null
     * @param name the name of the object from which to retrieve attributes
     * @param attrIds the identifiers of the attributes to retrieve. null
     * indicates that all attributes should be retrieved; an empty array
     * indicates that none should be retrieved
     * @exception NamingException if a naming exception is encountered
     */
    public javax.naming.directory.Attributes getAttributes(String name)
        throws NamingException {

        // Building attribute list
        File file = file(name);

        if (file == null)
            throw new NamingException
                ("Resource not found " + name);

        return new FileResourceAttributes(file);

    }


    /**
     * Return a File object representing the specified normalized
     * context-relative path if it exists and is readable.  Otherwise,
     * return <code>null</code>.
     *
     * @param name Normalized context-relative path (with leading '/')
     */
    protected File file(String name) {
	if( File.separatorChar == '\\' )
            name = name.replace('/',File.separatorChar);

        File file = new File(base, name);
        if (file.exists() && file.canRead()) {
            // Windows only check
            if ((caseSensitive) && (File.separatorChar  == '\\')) {
                String fileAbsPath = file.getAbsolutePath();
                if (fileAbsPath.endsWith("."))
                    fileAbsPath = fileAbsPath + "/";
                String absPath = normalize(fileAbsPath);
                String canPath = null;
                try {
                    canPath = file.getCanonicalPath();
                    if (canPath != null)
                        canPath = normalize(canPath);
                } catch (IOException e) {
                }
                if ((absoluteBase.length() < absPath.length())
                    && (absoluteBase.length() < canPath.length())) {
                    absPath = absPath.substring(absoluteBase.length());
                    if ((canPath == null) || (absPath == null))
                        return null;
                    if (absPath.equals(""))
                        absPath = "/";
                    canPath = canPath.substring(absoluteBase.length());
                    if (canPath.equals(""))
                        canPath = "/";
                    if (!canPath.equals(absPath))
                        return null;
                }
            }
        } else {
            return null;
        }
        return file;

    }

/**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    protected String normalize(String path) {

	String normalized = path;

	// Normalize the slashes and add leading slash if necessary
	if (normalized.indexOf('\\') >= 0)
	    normalized = normalized.replace('\\', '/');
        boolean absolute = ( normalized.length() > 1 && (normalized.charAt(1) == ':'))?
          true: false;
	if (!normalized.startsWith("/") && !absolute)
	    normalized = "/" + normalized;

	// Resolve occurrences of "//" in the normalized path
	while (true) {
	    int index = normalized.indexOf("//");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 1);
	}

	// Resolve occurrences of "/./" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/./");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 2);
	}

	// Resolve occurrences of "/../" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/../");
	    if (index < 0)
		break;
	    if (index == 0)
		return (null);	// Trying to go outside our context
	    int index2 = normalized.lastIndexOf('/', index - 1);
	    normalized = normalized.substring(0, index2) +
		normalized.substring(index + 3);
	}

	// Return the normalized path that we have completed
	return (normalized);

    }



}

