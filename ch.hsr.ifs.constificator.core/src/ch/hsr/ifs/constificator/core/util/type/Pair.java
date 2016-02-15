package ch.hsr.ifs.constificator.core.util.type;

public class Pair<F,S> {

	private F m_first;
	private S m_second;

	public Pair(F first, S second){
		m_first = first;
		m_second = second;
	}

	public F first(){
		return m_first;
	}

	public S second(){
		return m_second;
	}

	public void first(F first){
		m_first = first;
	}

	public void second(S second){
		m_second = second;
	}

}
