package org.ginkgo.gcov.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class DemangleHelper {
	public static final String FILT_COMMAND = "c++filt -n ";
	public static final String MAIN = "main";

	private static final Map<String, String> parameterMap = new HashMap<String, String>() {
		{
			put("v", ""); // void
			put("b", "bool");
			put("c", "char");
			put("a", "signed char");
			put("h", "unsigned char");
			put("s", "short");
			put("t", "unsigned short");
			put("i", "int");
			put("j", "unsigned int");
			put("l", "long");
			put("m", "unsigned long");
			put("x", "long long");
			put("y", "unsigned long long");
			put("w", "wchar_t");
			put("f", "float");
			put("d", "double");
			put("e", "long double");
			put("z", "...");
			put("K", "const");
			put("V", "volatile");
			// U8__vectori, 5__m64 etc.
		}
	};

	private static final Map<String, String> pointerMap = new HashMap<String, String>() {
		{
			put("P", "*");
			put("R", "&");
			put("M", ""); // TODO implement Pointer to member
			put("F", ""); // TODO implement Pointer to Function
			put("A", "[]"); // size needs to can be specified. // TODO implement
		}
	};

	// K = const; V=volatile; VK= const volatile; (N=Nested name)

	public static String demangleName(String functionName) {
		Runtime runtime = Runtime.getRuntime();
		Process process;
		StringBuffer demangledName = new StringBuffer();
		try {
			process = runtime.exec(FILT_COMMAND + functionName);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				demangledName.append(line);
			}
			if (MAIN.equals(functionName))
				demangledName.append("()");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return demangledName.toString();
	}

	public static String demangle(File file) {
		StringBuffer demangledName = new StringBuffer();
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process;
			String command = "sh -c 'gcov -f -b " + file.getName() + " | "
					+ "c++filt -n'";

			// String[] command2 = new String[] { "sh", "-c",
			// "'gcov -f -b " + file.getName() + " | " + "c++filt -n'" };
			// String command2 = "sh - c ls";
			System.out.println(file.getAbsolutePath());
			System.out.println(file.getCanonicalPath());
			System.out.println(file.getParent());
			System.out.println(file.getPath());
			process = runtime.exec(command, null, file.getParentFile());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				demangledName.append(line);
			}
			if (demangledName.length() < 2) {
				br = new BufferedReader(new InputStreamReader(process
						.getErrorStream()));
				while ((line = br.readLine()) != null) {
					demangledName.append(line);
				}
			}
			// if (MAIN.equals(functionName))
			// demangledName.append("()");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return demangledName.toString();
	}

	public static String demangle(String mangled) {
		StringBuffer demangledFunction = new StringBuffer();
		int index = 0;
		boolean nested = false;
		boolean isNumber = false;
		StringBuffer number = null;
		// If the string does not start with _Z it is not a mangled name.
		if (mangled.length() > 2 && '_' == mangled.charAt(index++)
				&& 'Z' == mangled.charAt(index++)) {
			while (index < mangled.length()) {
				boolean hasIndexBeenModified = false;
				char c = mangled.charAt(index);
				switch (c) {
				case '_':
				case 'Z':
					break;
				case 'N':
					nested = true;
					break;
				case 'E':
					if (nested)
						nested = false;
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					if (!isNumber) {
						number = new StringBuffer();
						isNumber = true;
					}
					number.append(c);
					break;
				default:
					if (isNumber) {
						int length = Integer.parseInt(number.toString());
						demangledFunction.append(mangled.substring(index,
								index += length));
						isNumber = false;
						hasIndexBeenModified = true;
						if (nested && mangled.charAt(index) != 'E')
							demangledFunction.append("::");
					} else if (!nested) {
						demangledFunction.append(demangleParameter(index,
								mangled));
						index = mangled.length();
					}
				}
				if (!hasIndexBeenModified)
					index++;
			}
		} else {
			demangledFunction.append(mangled);
			if ("main".equals(demangledFunction.toString()))
				demangledFunction.append("()");
		}
		return demangledFunction.toString();
	}

	public static String demangleParameter(int index, String mangled) {
		StringBuffer parameter = new StringBuffer();
		boolean parameterStarted = false;
		boolean isArray = false;
		String pointer = "";
		String isConst = "";
		String isVolatile = "";
		String complex = "";
		String imaginary = "";
		StringBuffer array = new StringBuffer();
		StringBuffer arrayLength = new StringBuffer();
		while (index < mangled.length()) {
			switch (mangled.charAt(index)) {
			case 'P':
				pointer += "*";
				break;
			case 'R':
				pointer += "&";
				break;
			case 'K':
				isConst = " const";
				break;
			case 'V':
				isVolatile = " volatile";
				break;
			case 'C':
				complex = "complex ";
				break;
			case 'A':
				isArray = true;
				array.append("[");
				break;
			case 'G':
				imaginary = "imaginary ";
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if (isArray) {
					arrayLength.append(mangled.charAt(index));
				}
				break;
			case '_':
				if (isArray) {
					isArray = false;
					array.append(arrayLength);
					array.append("]");
					arrayLength = new StringBuffer();
				}
				break;
			default:
				String token = parameterMap.get(mangled.substring(index,
						index + 1));
				if (token != null) {
					if (!parameterStarted) {
						parameter.append("(");
						parameterStarted = true;
					} else {
						parameter.append(", ");
					}
					parameter.append(token);
					parameter.append(complex);
					parameter.append(imaginary);
					parameter.append(isConst);
					parameter.append(isVolatile);
					if (!"".equals(array.toString())) {
						parameter.append(" ");
						if (!"".equals(pointer)) {
							pointer = "<" + pointer + "> ";
						}
					}
					parameter.append(pointer);
					parameter.append(array);
					pointer = "";
					isConst = "";
					isVolatile = "";
					complex = "";
					imaginary = "";
					array = new StringBuffer();
				}
			}
			index++;
		}
		parameter.append(")");
		return parameter.toString();
	}
}
