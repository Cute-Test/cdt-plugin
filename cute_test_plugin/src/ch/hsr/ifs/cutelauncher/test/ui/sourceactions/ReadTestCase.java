package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cutelauncher.test.TestPlugin;

class ReadTestCase{//TODO checking for null values
	public ArrayList<String> testname=new ArrayList<String>();
	public ArrayList<Integer> cursorpos=new ArrayList<Integer>();
	public ArrayList<String> test=new ArrayList<String>();
	public ArrayList<String> expected=new ArrayList<String>();
	
	enum state{TEST, SAVETEST, EXPECTED, SAVEEXPECTED, CURSOR};
	state m;
		
	public ReadTestCase(String file){
		StringBuilder builder=new StringBuilder();

		String newline= System.getProperty("line.separator"); 
		try{
		String testnametmp=null;
		BufferedReader br=readTest(file);
		while(br.ready()){
			String str=br.readLine();
			if(str.startsWith("//test")){
				m=state.SAVEEXPECTED;
				testnametmp=str.substring(6);
			}
			if(str.startsWith("//expected")){
				m=state.SAVETEST;continue;
			}
			if(str.startsWith("//")&& m!=state.SAVEEXPECTED)continue;
			switch(m){
			case TEST:
				builder.append(str+newline);
				break;
			case SAVETEST:
				testname.add(testnametmp);
				test.add(builder.toString());
				builder=new StringBuilder();
				m=state.EXPECTED;
			case EXPECTED:
				builder.append(str+newline);
				break;
			case SAVEEXPECTED:
				if(builder.length()>0){
					expected.add(builder.toString());
					builder=new StringBuilder();
				}
				m=state.TEST;
				break;
			}
		}
		}catch(IOException ioe){ioe.printStackTrace();}
		//handle the last expected
		if(builder.length()>0){
			expected.add(builder.toString());
			builder=new StringBuilder();
		}
		
		parseForCursorPos();
	}
	public static BufferedReader readTest(String file) throws IOException{
		Bundle bundle1 = TestPlugin.getDefault().getBundle();
		Path path1 = new Path(file);
		URL url1=FileLocator.toFileURL(FileLocator.find(bundle1, path1, null));
		//BufferedInputStream bis=new BufferedInputStream(url1.openStream());
		BufferedReader br=new BufferedReader(new InputStreamReader(url1.openStream()));
		return br;
	}
	public void parseForCursorPos(){
		for(String str:test){
			cursorpos.add(str.indexOf("^"));
		}
		for(int i=0;i<test.size();i++){//remove caret from test
			String str=test.get(i);
			test.remove(i);
			test.add(i, str.replaceAll("[\\^]",""));
		}
		
	}
}