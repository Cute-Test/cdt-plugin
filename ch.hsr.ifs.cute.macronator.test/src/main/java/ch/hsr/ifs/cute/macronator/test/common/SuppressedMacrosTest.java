package ch.hsr.ifs.cute.macronator.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import ch.hsr.ifs.cute.macronator.common.SuppressedMacros;

public class SuppressedMacrosTest {

	@Test
	public void testShouldAddMacroNameToSuppressedList() throws Exception {
		String macroName = "MACRO";
		Path tempFile = createTempFile();
		SuppressedMacros suppressedMacros = new SuppressedMacros(tempFile);
		suppressedMacros.add(macroName);
		assertTrue(suppressedMacros.isSuppressed(macroName));
	}

	@Test
	public void testShouldNotAddEmptyMacroNameToSuppressedList() throws Exception {
		Path tempFile = createTempFile();
		SuppressedMacros suppressedMacros = new SuppressedMacros(tempFile);
		suppressedMacros.add("");
		assertFalse(suppressedMacros.isSuppressed(""));
	}
	
	@Test
	public void testShouldRemoveMacroNameFromSuppressedList() throws Exception {
		String macroName = "SUPPRESSED_MACRO";
		Path tempFile = createTempFile();
		BufferedWriter writer = Files.newBufferedWriter(tempFile, Charset.forName("UTF-8"));
		writer.append(macroName);
		writer.flush();
		writer.close();
		SuppressedMacros suppressedMacros = new SuppressedMacros(tempFile);
		assertTrue(suppressedMacros.isSuppressed(macroName));
		suppressedMacros.remove(macroName);
		assertFalse(suppressedMacros.isSuppressed(macroName));
	}

	@Test
	public void testShouldLoadSuppressedMacrosFromFile() throws Exception {
		Path tempFile = createTempFile();
		BufferedWriter writer = Files.newBufferedWriter(tempFile, Charset.forName("UTF-8"));
		writer.append("SUPPRESSED_MACRO");
		writer.flush();
		writer.close();
		SuppressedMacros suppressedMacros = new SuppressedMacros(tempFile);
		assertTrue(suppressedMacros.isSuppressed("SUPPRESSED_MACRO"));
	}
	
	@Test
	public void testShouldCreateFileIfNotExisting() throws Exception {
		Path nonExistingFile = FileSystems.getDefault().getPath("nonexisting");
		if (Files.exists(nonExistingFile)) {
			Files.delete(nonExistingFile);
		}
		assertFalse(Files.exists(nonExistingFile));
		SuppressedMacros suppressedMacros = new SuppressedMacros(nonExistingFile);
		suppressedMacros.add("MACRO");
		assertTrue(Files.exists(nonExistingFile));
		Files.delete(nonExistingFile);
	}
	


	private Path createTempFile() throws Exception {
		return Files.createTempFile(null, "testFile");
	}
}
