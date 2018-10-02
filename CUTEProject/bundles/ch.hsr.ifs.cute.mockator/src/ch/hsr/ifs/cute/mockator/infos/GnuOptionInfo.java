package ch.hsr.ifs.cute.mockator.infos;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.MessageInfoArgument;


public class GnuOptionInfo extends MarkerInfo<GnuOptionInfo> {

   @MessageInfoArgument(0)
   public String optionName;

   public GnuOptionInfo() {};

   public GnuOptionInfo(String optionName) {
      this.optionName = optionName;
   }

}
