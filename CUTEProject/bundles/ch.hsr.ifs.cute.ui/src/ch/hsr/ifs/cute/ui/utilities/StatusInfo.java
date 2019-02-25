/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.utilities;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;


/**
 * A settable IStatus Can be an error, warning, info or ok. For error, info and warning states, a message describes the problem
 *
 * @since 4.0
 */
public class StatusInfo implements IStatus {

    public static final IStatus OK_STATUS = new StatusInfo();

    private String fStatusMessage;
    private int    fSeverity;

    public StatusInfo() {
        this(OK, null);
    }

    public StatusInfo(int severity, String message) {
        fStatusMessage = message;
        fSeverity = severity;
    }

    @Override
    public IStatus[] getChildren() {
        return new IStatus[0];
    }

    @Override
    public int getCode() {
        return fSeverity;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public String getMessage() {
        return fStatusMessage;
    }

    @Override
    public String getPlugin() {
        return CUIPlugin.PLUGIN_ID;
    }

    @Override
    public int getSeverity() {
        return fSeverity;
    }

    public boolean isError() {
        return fSeverity == IStatus.ERROR;
    }

    public boolean isInfo() {
        return fSeverity == IStatus.INFO;
    }

    @Override
    public boolean isMultiStatus() {
        return false;
    }

    @Override
    public boolean isOK() {
        return fSeverity == IStatus.OK;
    }

    public boolean isWarning() {
        return fSeverity == IStatus.WARNING;
    }

    @Override
    public boolean matches(int severityMask) {
        return (fSeverity & severityMask) != 0;
    }

    public void setError(String errorMessage) {
        fStatusMessage = errorMessage;
        fSeverity = IStatus.ERROR;
    }

    public void setInfo(String infoMessage) {
        fStatusMessage = infoMessage;
        fSeverity = IStatus.INFO;
    }

    public void setOK() {
        fStatusMessage = null;
        fSeverity = IStatus.OK;
    }

    public void setWarning(String warningMessage) {
        fStatusMessage = warningMessage;
        fSeverity = IStatus.WARNING;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("StatusInfo ");
        if (fSeverity == OK) {
            buf.append("OK");
        } else if (fSeverity == ERROR) {
            buf.append("ERROR");
        } else if (fSeverity == WARNING) {
            buf.append("WARNING");
        } else if (fSeverity == INFO) {
            buf.append("INFO");
        } else {
            buf.append("severity=");
            buf.append(fSeverity);
        }
        buf.append(": ");
        buf.append(fStatusMessage);
        return buf.toString();
    }
}
