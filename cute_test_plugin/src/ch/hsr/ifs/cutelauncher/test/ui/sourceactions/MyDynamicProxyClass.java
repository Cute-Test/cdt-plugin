package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class MyDynamicProxyClass implements java.lang.reflect.InvocationHandler
{
  Object obj;
  public MyDynamicProxyClass(Object obj)
  { this.obj = obj; }
  
  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
  {
    try {
    	//Beware, it is obj, not via proxy
    	//invoke(proxy,args) results in recursive loop
    	System.out.println(m);
    	return m.invoke(obj, args);
    	
    	// do something
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (Exception e) {
      throw e;
    }
    // return something
    
  }
  
  static public Object newInstance(Object obj, Class[] interfaces)
  {
    return java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                                    interfaces,
                                                    new MyDynamicProxyClass(obj));
  }
}
//@see http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-proxy.html?page=1
//Explore the Dynamic Proxy API