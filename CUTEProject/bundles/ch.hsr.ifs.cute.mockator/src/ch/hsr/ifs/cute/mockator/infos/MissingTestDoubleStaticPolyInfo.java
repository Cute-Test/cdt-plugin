package ch.hsr.ifs.cute.mockator.infos;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.MessageInfoArgument;


public class MissingTestDoubleStaticPolyInfo extends MarkerInfo<MissingTestDoubleStaticPolyInfo> {

   @MessageInfoArgument(0)
   public String seamName;

   public MissingTestDoubleStaticPolyInfo() {}

   public MissingTestDoubleStaticPolyInfo(String seamName) {
      this.seamName = seamName;
   }

}
