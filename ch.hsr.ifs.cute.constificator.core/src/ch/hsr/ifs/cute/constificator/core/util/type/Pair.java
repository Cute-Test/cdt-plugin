package ch.hsr.ifs.cute.constificator.core.util.type;

public class Pair<F,S> {

	private F first;
	private S second;

	public Pair(F first, S second){
		this.first = first;
		this.second = second;
	}

	public F first(){
		return first;
	}

	public S second(){
		return second;
	}

	public void first(F first){
		this.first = first;
	}

	public void second(S second){
		this.second = second;
	}

}
