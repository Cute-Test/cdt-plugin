package ch.hsr.ifs.cute.macronator.common;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.codan.core.cxx.Activator;

public class SuppressedMacros {

	private Path file;
	private Set<String> suppressedMacros;

	public SuppressedMacros(Path file) {
		this.file = file;
		this.suppressedMacros = new HashSet<String>();
	}

	public void add(String macroName) {
		if (macroName.length() > 0) {
			suppressedMacros.add(macroName);
		}
		persistSuppressedMacros();

	}

	public void remove(String macroName) {
		suppressedMacros.remove(macroName);
		persistSuppressedMacros();
	}

	public boolean isSuppressed(String macroName) {
		readSuppressedMacros();
		return suppressedMacros.contains(macroName);
	}

	private void readSuppressedMacros() {
		BufferedReader reader = null;
		try {
			if (!Files.exists(file)) {
				file = Files.createFile(file);
			}
			reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
			String line = null;
			suppressedMacros = new HashSet<String>();
			while ((line = reader.readLine()) != null) {
				suppressedMacros.add(line);
			}
		} catch (IOException e) {
			Activator.log("Error reading suppressed macros", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void persistSuppressedMacros() {
		BufferedWriter writer = null;
		try {
			writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
			for (String macro : suppressedMacros) {
				writer.write(macro + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			Activator.log("Error persisting suppressed macros", e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
