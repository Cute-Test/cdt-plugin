/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;


/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */
public class NullStrategy extends AddPushbackStatementStrategy {

   public NullStrategy(IDocument doc) {
      super(doc, null, null);
   }

   @Override
   public MultiTextEdit getEdit() {
      return new MultiTextEdit();
   }

   @Override
   public String createPushBackContent() {
      return "";
   }

}
