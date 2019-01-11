package ch.hsr.ifs.cute.mockator.infos;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.InfoArgument;
import ch.hsr.ifs.iltis.cpp.core.resources.info.annotations.MessageInfoArgument;


public class MissingMemFunInfo extends MarkerInfo<MissingMemFunInfo> {

    @MessageInfoArgument(0)
    public String testDoubleName;

    @InfoArgument
    public String missingMemFunsForFake;
    @InfoArgument
    public String missingMemFunsForMock;

}
