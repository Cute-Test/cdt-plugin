package ch.hsr.ifs.cute.mockator.infos;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.InfoArgument;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.MessageInfoArgument;

import ch.hsr.ifs.cute.mockator.testdouble.creation.subtype.ArgumentPassByStrategy;


public class CreateTestDoubleSubTypeInfo extends MarkerInfo<CreateTestDoubleSubTypeInfo> {

   @MessageInfoArgument(0)
   public String nameOfMissingInstance;
   @InfoArgument
   public String parentClassName;
   @InfoArgument
   public String targetIncludePath;
   @InfoArgument
   public ArgumentPassByStrategy passByStrategy;

}
