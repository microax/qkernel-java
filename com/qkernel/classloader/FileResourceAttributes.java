package com.qkernel.classloader;

//import org.apache.naming.resources.ResourceAttributes;
import java.io.File;
import java.util.Date;

    /**
     * This specialized resource attribute implementation does some lazy
     * reading (to speed up simple checks, like checking the last modified
     * date).
     */
public class FileResourceAttributes extends ResourceAttributes {


        // -------------------------------------------------------- Constructor


        public FileResourceAttributes(File file) {
            this.file = file;
        }

        // --------------------------------------------------- Member Variables


        protected File file;


        protected boolean accessed = false;


        // ----------------------------------------- ResourceAttributes Methods


        /**
         * Is collection.
         */
        public boolean isCollection() {
            if (!accessed) {
                collection = file.isDirectory();
                accessed = true;
            }
            return super.isCollection();
        }


        /**
         * Get content length.
         *
         * @return content length value
         */
        public long getContentLength() {
            if (contentLength != -1L)
                return contentLength;
            contentLength = file.length();
            return contentLength;
        }


        /**
         * Get creation time.
         *
         * @return creation time value
         */
        public long getCreation() {
            if (creation != -1L)
                return creation;
            creation = file.lastModified();
            return creation;
        }


        /**
         * Get creation date.
         *
         * @return Creation date value
         */
        public Date getCreationDate() {
            if (creation == -1L) {
                creation = file.lastModified();
            }
            return super.getCreationDate();
        }


        /**
         * Get last modified time.
         *
         * @return lastModified time value
         */
        public long getLastModified() {
            if (lastModified != -1L)
                return lastModified;
            lastModified = file.lastModified();
            return lastModified;
        }


        /**
         * Get lastModified date.
         *
         * @return LastModified date value
         */
        public Date getLastModifiedDate() {
            if (lastModified == -1L) {
                lastModified = file.lastModified();
            }
            return super.getLastModifiedDate();
        }


        /**
         * Get name.
         *
         * @return Name value
         */
        public String getName() {
            if (name == null)
                name = file.getName();
            return name;
        }


        /**
         * Get resource type.
         *
         * @return String resource type
         */
        public String getResourceType() {
            if (!accessed) {
                collection = file.isDirectory();
                accessed = true;
            }
            return super.getResourceType();
        }


    }
