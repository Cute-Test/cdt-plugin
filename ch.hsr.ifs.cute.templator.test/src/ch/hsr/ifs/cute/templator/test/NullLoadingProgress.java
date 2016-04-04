package ch.hsr.ifs.cute.templator.test;

import ch.hsr.ifs.cute.templator.plugin.util.ILoadingProgress;

/** For tests only. */
public class NullLoadingProgress implements ILoadingProgress {

    @Override
    public void setProgress(double d) {
    }

    @Override
    public void setStatus(String statusText) {
    }

}
