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


/**
 * @author Emanuel Graf IFS
 *
 */
public class Function {

   private final String     name;
   private final int        called;
   private final int        execBlocks;
   private final List<Line> lines = new ArrayList<>();
   private File             file;

   public Function(String name, int called, int execBlocks) {
      super();
      this.name = name;
      this.called = called;
      this.execBlocks = execBlocks;
   }

   public File getFile() {
      return file;
   }

   public void setFile(File file) {
      this.file = file;
   }

   public String getName() {
      return name;
   }

   public int getCalled() {
      return called;
   }

   public int getExecBlocks() {
      return execBlocks;
   }

   public List<Line> getLines() {
      return lines;
   }

   public void addLine(Line l) {
      lines.add(l);
      l.setFunction(this);
   }

   @Override
   public String toString() {
      return name;
   }
}
