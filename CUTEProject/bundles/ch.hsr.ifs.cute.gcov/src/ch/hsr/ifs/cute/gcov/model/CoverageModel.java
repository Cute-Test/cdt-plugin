/*******************************************************************************
 * Copyright (c) 2007-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class CoverageModel {

   private final Map<IFile, File> fileMap = new TreeMap<>((o1, o2) -> o1.getFullPath().toString().compareTo(o2.getFullPath().toString()));

   public File addFileToModel(IFile file) {
      File f = new File(file);
      fileMap.put(file, f);
      return f;
   }

   public File getModelForFile(IFile file) {
      return fileMap.get(file);
   }

   public File removeFileFromModel(File file) {
      return removeFileFromModel(file.getFile());
   }

   public File removeFileFromModel(IFile file) {
      return fileMap.remove(file);
   }

   public void clearModel() {
      fileMap.clear();
   }

   public Collection<File> getMarkedFiles() {
      return fileMap.values();
   }
}
