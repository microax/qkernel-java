package com.qkernel.servlet.multipart;

import java.io.*;


public interface FileRenamePolicy {
  
  /**
   * Returns a File object holding a new name for the specified file.
   *
   * @see FilePart#writeTo(File fileOrDirectory)
   */
  public File rename(File f);

}
