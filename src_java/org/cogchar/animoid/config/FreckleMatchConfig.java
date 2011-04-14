/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.config;

import java.io.Serializable;

/**
 *
 * @author Stu Baurmann
 */
public class FreckleMatchConfig implements Serializable {
	// IF all existing freckle-faces match with a score less than this threshold,
	// AND the enrollment image is of sufficiently good quality, then it can be
	// enrolled as a new face.  If null, then enrollment is disabled.
	private	Double			matchScorePreventEnrollThresh;

	private	Double			matchScoreAcceptThresh;
	private	Double			matchScoreExpandThresh;
	private	Integer			maxProfileWidth;

	public Double getMatchScoreAcceptThresh() {
		return matchScoreAcceptThresh;
	}

	public void setMatchScoreAcceptThresh(Double thresh) {
		this.matchScoreAcceptThresh = thresh;
	}

	public Double getMatchScoreExpandThresh() {
		return matchScoreExpandThresh;
	}

	public void setMatchScoreExpandThresh(Double thresh) {
		this.matchScoreExpandThresh = thresh;
	}

	public Double getMatchScorePreventEnrollThresh() {
		return matchScorePreventEnrollThresh;
	}

	public void setMatchScorePreventEnrollThresh(Double thresh) {
		this.matchScorePreventEnrollThresh = thresh;
	}

	public Integer getMaxProfileWidth() {
		return maxProfileWidth;
	}

	public void setMaxProfileWidth(Integer width) {
		this.maxProfileWidth = width;
	}


	@Override public String toString() {
		return "FreckleMatchConfig["
			+ "\npreventEnrollThresh=" + matchScorePreventEnrollThresh
			+ "\nmatchScoreAcceptThresh=" + matchScoreAcceptThresh
			+ "\nmatchScoreExpandThresh=" + matchScoreExpandThresh
			+ "\nmaxProfileWidth=" + maxProfileWidth
			+ "]";
	}
}
