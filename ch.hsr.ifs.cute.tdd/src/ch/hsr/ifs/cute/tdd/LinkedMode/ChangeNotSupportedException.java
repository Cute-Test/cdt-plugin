package ch.hsr.ifs.cute.tdd.LinkedMode;

public class ChangeNotSupportedException extends RuntimeException {


	private static final long serialVersionUID = 4404461261482670807L;

	public ChangeNotSupportedException(String message, Exception causingException) {
		super(message, causingException);
	}

}
