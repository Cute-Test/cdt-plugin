/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.testframework.event.ConsoleEventParser;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.tests.mock.FileInputTextConsole;
import junit.framework.TestCase;


/**
 * @author Emanuel Graf IFS
 *
 */
public abstract class ConsoleTest extends TestCase {

   private ConsoleEventParser consoleEventParser;
   protected String           filePathRoot;

   protected FileInputTextConsole   tc;
   protected ConsolePatternListener cpl;

   @Override
   protected void setUp() throws Exception {
      useCUTE();
      prepareTest();
   }

   @Override
   protected void tearDown() throws Exception {
      tc.removePatternMatchListener(cpl);
      tc.end();
   }

   private void useCUTE() {
      consoleEventParser = new CuteConsoleEventParser();
      filePathRoot = "testDefs/cute/";
   }

   private void prepareTest() {
      tc = getConsole();
      cpl = new ConsolePatternListener(consoleEventParser);
      addTestEventHandler(cpl);
      tc.addPatternMatchListener(cpl);
   }

   protected void emulateTestRun() throws IOException, InterruptedException {
      final Semaphore semaphore = new Semaphore(0);
      tc.addPatternMatchListener(new IPatternMatchListener() {

         @Override
         public void matchFound(PatternMatchEvent event) {
            semaphore.release();
         }

         @Override
         public void disconnect() {}

         @Override
         public void connect(TextConsole console) {}

         @Override
         public String getPattern() {
            return ".+";
         }

         @Override
         public String getLineQualifier() {
            return null;
         }

         @Override
         public int getCompilerFlags() {
            return 0;
         }
      });
      tc.startTest();
      semaphore.acquire(); //wait until MatchJob is actually running (meaning that cute test-result-pattern-match-listener has at least started working).
      //joins all console pattern-match-jobs belonging to the "tc" console
      Job.getJobManager().join(tc, new NullProgressMonitor());
   }

   protected FileInputTextConsole getConsole() {
      return new FileInputTextConsole(fullFilePath());
   }

   protected abstract String getInputFilePath();

   protected abstract void addTestEventHandler(ConsolePatternListener lis);

   protected String firstConsoleLine() throws CoreException, IOException {
      Bundle bundle = TestframeworkTestPlugin.getDefault().getBundle();
      Path path = new Path(fullFilePath());
      BufferedReader br = null;
      try {
         String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
         br = new BufferedReader(new FileReader(file2));
         return br.readLine();
      } finally {
         if (br != null) {
            br.close();
         }
      }
   }

   private String fullFilePath() {
      return filePathRoot + getInputFilePath();
   }
}
