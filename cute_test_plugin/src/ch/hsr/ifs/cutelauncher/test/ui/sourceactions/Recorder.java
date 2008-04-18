package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import java.util.HashSet;

public class Recorder {
	static HashSet<String> hs;
	
	public static void store(HashSet hh){
		if(hs == null){
			hs=new HashSet();
		}
		
		hs.addAll(hh);

	}
	
	public static void printUniqueCall(){
		if(hs!=null){
		for(String i:hs){
			System.out.println(i);
		}System.out.println("###########");
		}else System.out.println("nothing");
			
	}
}
