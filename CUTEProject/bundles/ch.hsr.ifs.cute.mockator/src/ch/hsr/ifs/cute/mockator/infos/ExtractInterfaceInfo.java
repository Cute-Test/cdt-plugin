package ch.hsr.ifs.cute.mockator.infos;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.InfoArgument;


public class ExtractInterfaceInfo extends MarkerInfo<ExtractInterfaceInfo> {

    @InfoArgument
    public String  interfaceName;
    @InfoArgument
    public boolean replaceAllOccurences;

}
