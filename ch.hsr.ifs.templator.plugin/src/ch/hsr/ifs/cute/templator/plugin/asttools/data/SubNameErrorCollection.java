package ch.hsr.ifs.cute.templator.plugin.asttools.data;

import org.eclipse.cdt.core.dom.ast.IASTName;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;

import java.util.ArrayList;
import java.util.List;

public class SubNameErrorCollection {

	public class SubNameError {
		private IASTName name;
		private TemplatorException exception;

		public SubNameError(IASTName name, TemplatorException exception) {
			super();
			this.name = name;
			this.exception = exception;
		}

		public IASTName getName() {
			return name;
		}

		public TemplatorException getException() {
			return exception;
		}
	}

	private List<SubNameError> deductionErrors = new ArrayList<>();
	private List<SubNameError> resolvingErrors = new ArrayList<>();

	public void addDeductionError(IASTName name, TemplatorException e) {
		deductionErrors.add(new SubNameError(name, e));
	}

	public void addResolvingError(IASTName name, TemplatorException e) {
		resolvingErrors.add(new SubNameError(name, e));
	}

	public List<SubNameError> getDeductionErrors() {
		return deductionErrors;
	}

	public List<SubNameError> getResolvingErrors() {
		return resolvingErrors;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		printErrorList("Deduction", stringBuilder, deductionErrors);
		printErrorList("Resolving", stringBuilder, resolvingErrors);

		return stringBuilder.toString();
	}

	private void printErrorList(String title, StringBuilder stringBuilder, List<SubNameError> errors) {
		stringBuilder.append("------------" + title + " Errors------------\n");
		for (SubNameError error : errors) {
			stringBuilder.append(error.getName() + " : ");
			stringBuilder.append(error.getException().getMessage() + "\n");
		}
	}
}