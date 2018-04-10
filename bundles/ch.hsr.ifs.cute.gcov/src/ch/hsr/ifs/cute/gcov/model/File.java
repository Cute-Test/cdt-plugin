/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;


/**
 * @author Emanuel Graf IFS
 *
 */
public class File {

   private final IFile          file;
   private final List<Function> functions = new ArrayList<>();

   public File(IFile file) {
      super();
      this.file = file;
   }

   public IFile getFile() {
      return file;
   }

   public String getFileName() {
      return file.getName();
   }

   public void addFunction(Function f) {
      functions.add(f);
      f.setFile(this);
   }

   public List<Function> getFunctions() {
      return functions;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((file == null) ? 0 : file.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      File other = (File) obj;
      if (file == null) {
         if (other.file != null) return false;
      } else if (!file.equals(other.file)) return false;
      return true;
   }
}
