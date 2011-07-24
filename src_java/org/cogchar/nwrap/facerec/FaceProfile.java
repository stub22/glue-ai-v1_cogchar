/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.nwrap.facerec;

import org.cogchar.vision.OpenCVImage;
import java.util.Collection;

/**
 *
 * @author Stu Baurmann
 */
public class FaceProfile {
	private	Collection<OpenCVImage>		myOCVImages;
	private	Long						myFIR_NativePointer;

	public FaceProfile(Collection<OpenCVImage> images) {
		myOCVImages = images;
	}
	public long[] getNativeImagePointerArray() {
		long imagePtrs[] = new long[myOCVImages.size()];
		int idx = 0;
		for (OpenCVImage ocvi: myOCVImages) {
			imagePtrs[idx++] = ocvi.raw();
		}
		return imagePtrs;
	}
	public void setFIR_NativePointer(Long fir_np) {
		myFIR_NativePointer = fir_np;
	}
	public Long getFIR_NativePointer() {
		return myFIR_NativePointer;
	}
}
