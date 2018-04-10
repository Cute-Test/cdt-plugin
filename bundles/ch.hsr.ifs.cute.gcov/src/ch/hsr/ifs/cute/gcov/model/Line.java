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
import java.util.Collections;
import java.util.List;


/**
 * @author Emanuel Graf IFS
 *
 */
public class Line {

   private Function       function;
   private final int      nr;
   private CoverageStatus status;
   private List<Branch>   branches;

   public Line(int nr, CoverageStatus status) {
      super();
      this.nr = nr;
      this.status = status;
   }

   public void addBranch(Branch b) {
      if (branches == null) {
         branches = new ArrayList<>();
      }
      branches.add(b);
      if (status == CoverageStatus.Covered && b.getStatus() == CoverageStatus.Uncovered) {
         status = CoverageStatus.PartiallyCovered;
      }
   }

   public Function getFunction() {
      return function;
   }

   public void setFunction(Function function) {
      this.function = function;
   }

   public int getNr() {
      return nr;
   }

   public CoverageStatus getStatus() {
      return status;
   }

   public List<Branch> getBranches() {
      if (branches == null) { return Collections.emptyList(); }
      return branches;
   }

   @Override
   public String toString() {
      return nr + " " + status;
   }

}
