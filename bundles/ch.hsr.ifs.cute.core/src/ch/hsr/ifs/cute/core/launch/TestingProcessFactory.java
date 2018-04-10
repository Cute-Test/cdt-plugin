package ch.hsr.ifs.cute.core.launch;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;


/**
 * Custom testing process factory allows to handle the output stream of the
 * testing process and prevent it from output to Console.
 *
 * adapted for CUTE!!!! stolen from cdt.testsrunner
 */
public class TestingProcessFactory implements IProcessFactory {

   @Override
   @SuppressWarnings("unchecked")
   public IProcess newProcess(ILaunch launch, Process process, String label, @SuppressWarnings("rawtypes") Map attributes) {
      // Mimic the behavior of DSF GDBProcessFactory.
      IProcess proc = null;
      if (attributes != null) {
         Object processTypeCreationAttrValue = attributes.get(org.eclipse.cdt.dsf.gdb.IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR);
         if (IGdbDebugConstants.GDB_PROCESS_CREATION_VALUE.equals(processTypeCreationAttrValue)) { return new GDBProcess(launch, process, label,
               attributes); }
         if (IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE.equals(processTypeCreationAttrValue)) {
            proc = new InferiorRuntimeProcess(launch, process, label, attributes);
         }
         // Probably, it is CDI creating a new inferior process
      } else {
         proc = new RuntimeProcess(launch, process, label, attributes); // for cute this might be an error
      }

      return proc;
   }

}
