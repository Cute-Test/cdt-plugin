package ch.hsr.ifs.testframework.model

import org.eclipse.osgi.util.NLS
import kotlin.jvm.JvmField


/**
 * @since 3.0
 */
public class Messages : NLS() {

   companion object {
      private const val BUNDLE_NAME = "ch.hsr.ifs.testframework.model.messages"

	   @JvmStatic
      lateinit var ModellBuilder_0: String

      init {
         NLS.initializeMessages(BUNDLE_NAME, Messages::class.java)
      }
   }

}
