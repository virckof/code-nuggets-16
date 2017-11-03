package edu.ca.ualberta.code.nuggets.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class to scale thumbnail of code authors extracted from github
 */
public class ImageTransformations {

	public static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);

		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, 0, 0, width, height);
		gc.dispose();

		image.dispose();

		return scaled;
	}
}
