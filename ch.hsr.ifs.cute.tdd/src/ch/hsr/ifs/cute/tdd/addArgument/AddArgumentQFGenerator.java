/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.addArgument;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import ch.hsr.ifs.cute.tdd.TDDPlugin;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddErrorIdCollection;

/**
 * Generates one or more "Add argument" quick fixes for a codan marker. WrongArgumentChecker generates the markers that can be processed here.
 */
@SuppressWarnings("restriction")
public class AddArgumentQFGenerator implements IMarkerResolutionGenerator {

	private static IMarkerResolution[] MARKER_RESOLUTION_TYPE = new IMarkerResolution[] {};
	public static final String MSG_QUICK_FIX_ACTION_ADD = Messages.AddArgumentQFGenerator_0;
	public static final String MSG_QUICK_FIX_ACTION_REMOVE = Messages.AddArgumentQFGenerator_1;
	public static final String MSG_QUICK_FIX_ARGUMENT = Messages.AddArgumentQFGenerator_2;
	public static final String ASSIGN_SEPARATOR = "==";
	public static final String CANDIDATE_SEPARATOR = ":candidate ";
	public static final String PARAMETERS = "targetParameters" + ASSIGN_SEPARATOR;
	public static final String ADD_ARGUMENTS = "addArguments" + ASSIGN_SEPARATOR;
	public static final String REMOVE_ARGUMENTS = "removeArguments" + ASSIGN_SEPARATOR;
	public static final String SEPARATOR = "°";
	public static final int REQUIRED_MARKER_ARGUMENTS = 4;
	private static final String IMG_OBJS_CORRECTION_REMOVE_PATH = "obj16/remove_correction.gif";

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		List<IMarkerResolution> result = new ArrayList<IMarkerResolution>();
		if (isResolutionPossible(marker)) {
			CodanArguments ca = new CodanArguments(marker);
			String[] candidates = getCandidates(ca);
			String funcName = ca.getName();
			for (int i = 0; i < candidates.length; i++) {
				AddArgumentQuickFix quickFix = getQuickFix(funcName, candidates[i], i);
				if (quickFix != null && quickFix.isApplicable(marker)) {
					result.add(quickFix);
				}
			}
		}
		return result.toArray(MARKER_RESOLUTION_TYPE);
	}

	private boolean isResolutionPossible(IMarker marker) {
		String pid = getProblemID(marker);
		if (pid != null
				&& !((pid.equals(TddErrorIdCollection.ERR_ID_InvalidArguments_HSR)) || pid.equals(TddErrorIdCollection.ERR_ID_InvalidArguments_FREE_HSR))) {
			return false;
		}
		return getProblemArguments(marker).length >= REQUIRED_MARKER_ARGUMENTS && new CodanArguments(marker).getCandidateNr() >= 0;
	}

	private AddArgumentQuickFix getQuickFix(String function, String candidate, int candidateNr) {
		String message = getMessage(function, candidate);
		if (!message.isEmpty()) {
			Image image;
			if (message.startsWith(MSG_QUICK_FIX_ACTION_ADD)) {
				image = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CORRECTION_ADD);
			} else {
				image = TDDPlugin.getImageDescriptor(IMG_OBJS_CORRECTION_REMOVE_PATH).createImage();
			}
			return new AddArgumentQuickFix(message, candidateNr, image);
		}
		return null;
	}

	private String getMessage(String function, String candidate) {
		String[] args = candidate.split(SEPARATOR);
		if (args.length < 2 || !(args[0].startsWith(ADD_ARGUMENTS) || (args[0].startsWith(REMOVE_ARGUMENTS))) || !args[1].startsWith(PARAMETERS)) {
			return "";
		}
		String action = null;
		if (args[0].startsWith(ADD_ARGUMENTS)) {
			action = MSG_QUICK_FIX_ARGUMENT.replace(Messages.AddArgumentQFGenerator_10, MSG_QUICK_FIX_ACTION_ADD);
		} else if (args[0].startsWith(REMOVE_ARGUMENTS)) {
			action = MSG_QUICK_FIX_ARGUMENT.replace("&0", MSG_QUICK_FIX_ACTION_REMOVE);
		}
		String toBeAddedArguments = "";
		String[] toBeAddedArgumentsline = args[0].split(ASSIGN_SEPARATOR);
		if (toBeAddedArgumentsline.length > 1) {
			toBeAddedArguments = toBeAddedArgumentsline[1];
		}
		String[] targetParameterline = args[1].split(ASSIGN_SEPARATOR);
		String targetParameter = "";
		if (targetParameterline.length > 1) {
			targetParameter = targetParameterline[1];
		}
		return action.replace("&1", toBeAddedArguments).replace("&2", function + "(" + targetParameter + ")");
	}

	private String[] getCandidates(CodanArguments ca) {
		return ca.getCandidates().split(CANDIDATE_SEPARATOR);
	}

	public String[] getProblemArguments(IMarker marker) {
		return CodanProblemMarker.getProblemArguments(marker);
	}

	public String getProblemID(IMarker marker) {
		String problemId = CodanProblemMarker.getProblemId(marker);
		if (problemId == null) {
			return "";
		}
		return problemId;
	}

}
