/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model

import java.util.regex.Matcher
import java.util.regex.Pattern


private const val REG_EXP = "((.*)(\t)(.*)(\t)(.*)(\t)(.*)(\t))"


private fun unquote(text: String) = 
   text.replace(Regex("\\\\t"), "\t")
       .replace(Regex("\\\\n"), "\n")
       .replace(Regex("\\\\r"), "\r")
       .replace(Regex("\\\\\\\\"), "\\\\")

private fun unquoteMsg(text: String) = text.replace(Regex("\\\\\\\\"), "\\\\")

/**
 * @author Emanuel Graf
 *
 */
class TestFailure(message: String) : TestResult(message) {

   public var expected: String? = null
   public var was: String? = null
   private var middle: String? = null

   init {
      val pattern = Pattern.compile(REG_EXP)
      val matcher = pattern.matcher(message)
      if (matcher.find()) {
         this.message = unquoteMsg(matcher.group(2))
         expected = unquote(matcher.group(4))
         middle = unquote(matcher.group(6))
         was = unquote(matcher.group(8))
      } else {
         this.message = message
      }
   }

   override val msg: String
      get() {
         val strBuild = StringBuilder(message)
         if (expected != null && was != null) {
            strBuild.append(' ')
                    .append(expected)
                    .append(' ')
                    .append(middle)
                    .append(' ')
                    .append(was)
         }
         return strBuild.toString()
      }

}
