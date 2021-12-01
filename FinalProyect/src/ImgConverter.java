/*
 	This program converts a jpg image into a grayscale PNG version of the image using threads.
    Copyright (C) 2021	Max Burkle Goya

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;

import java.lang.Math;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Spinner;

public class ImgConverter extends Thread {
	private int width, height, start, end;
	private double brightness;
	BufferedImage img;
	protected Shell shlGreyscaleConverter;
	private Text filePath;
	private Text fileName;
	
	public ImgConverter() {
	}
	
	public ImgConverter(BufferedImage img, double brightness, int width, int height, int start, int end) {
		this.img = img;
		this.brightness = brightness;
		this.width = width;
		this.height = height;
		this.start = start;
		this.end = end;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ImgConverter window = new ImgConverter();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		int index, row, col;

		for (index = start; index < end; index++) {
			row = index / width;
			col = index % width;
			greyPixel(brightness, row, col);
		}
	}
	
	private void greyPixel(double brightness, int row, int col) {
        int rgb = img.getRGB(col, row);  
        
        int red = rgb & 0xFF; 
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb >> 16) & 0xFF;

        float L = (float) (0.3 * (float) red + 0.59 * (float) green + 0.11 * (float) blue);

        int bright = (int) Math.ceil(brightness/100 * 255);
        int color;
        color = bright * (int) L / 255; 				// R color
        color = (color << 8) | bright * (int) L / 255; 	// G color
        color = (color << 8) | bright * (int) L / 255; 	// B color

        img.setRGB(col, row, color);
	}
	
	public static void convertImage(Shell shell, String path, int brightness) throws IOException {

        BufferedImage img = ImageIO.read(new File(path + ".jpg"));
        int height = img.getHeight();
        int width = img.getWidth();
		ImgConverter threads[];
		
		int block = (width * height) / 10;
		threads = new ImgConverter[10];
		
		for (int i = 0; i < threads.length; i++) {
			if (i != threads.length - 1) {
				threads[i] = new ImgConverter(img, brightness, width, height,
									(i * block), ((i + 1) * block));
			} else {
				threads[i] = new ImgConverter(img, brightness, width, height,
									(i * block), (width * height));
			}
		}
		
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
        ImageIO.write(img, "png", new File(path + "_grey.png"));
		MessageDialog.openConfirm(shell,"Completed","File has been succesfully converted.");
    }

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlGreyscaleConverter.open();
		shlGreyscaleConverter.layout();
		while (!shlGreyscaleConverter.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlGreyscaleConverter = new Shell();
		shlGreyscaleConverter.setTouchEnabled(true);
		shlGreyscaleConverter.setToolTipText("");
		shlGreyscaleConverter.setSize(496, 350);
		shlGreyscaleConverter.setText("Image Converter");
		
		Label label = new Label(shlGreyscaleConverter, SWT.NONE);
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		label.setAlignment(SWT.CENTER);
		label.setBounds(408, 206, 15, 15);
		label.setText("%");
		
		filePath = new Text(shlGreyscaleConverter, SWT.BORDER);
		filePath.setBounds(50, 140, 390, 21);
		
		fileName = new Text(shlGreyscaleConverter, SWT.BORDER);
		fileName.setBounds(50, 203, 146, 21);
		
		Spinner spinner = new Spinner(shlGreyscaleConverter, SWT.BORDER);
		spinner.setSelection(100);
		spinner.setBounds(378, 203, 62, 22);
		
		Label filePathLabel = new Label(shlGreyscaleConverter, SWT.NONE);
		filePathLabel.setBounds(50, 119, 321, 15);
		filePathLabel.setText("Image file path: ");
		
		Button btnContinue = new Button(shlGreyscaleConverter, SWT.NONE);
		btnContinue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				int brightness = Integer.valueOf(spinner.getSelection());
				String path = "";
				try {
					path = filePath.getText() + "\\\\" + fileName.getText();
				}
				catch (Exception exc) { 
					MessageDialog.openError(shlGreyscaleConverter,"Error","Error in image file path.");
				}
				
				try {
					convertImage(shlGreyscaleConverter, path, brightness);
				} catch (IOException e1) {
					MessageDialog.openError(shlGreyscaleConverter,"Error","Error in image file path.");
				}
				shlGreyscaleConverter.close();
			}
		});
		btnContinue.setBounds(365, 263, 75, 25);
		btnContinue.setText("Continue");
		
		Label lblGreyscaleImageConverter = new Label(shlGreyscaleConverter, SWT.NONE);
		lblGreyscaleImageConverter.setFont(SWTResourceManager.getFont("Fixedsys", 15, SWT.NORMAL));
		lblGreyscaleImageConverter.setBounds(50, 32, 355, 15);
		lblGreyscaleImageConverter.setText("Greyscale Image converter");
		
		Label lblPleaseTypeIn = new Label(shlGreyscaleConverter, SWT.NONE);
		lblPleaseTypeIn.setBounds(50, 53, 390, 15);
		lblPleaseTypeIn.setText("Please type in the complete path for the desired image to be converted");
		
		Label lblImageFileNamewithouth = new Label(shlGreyscaleConverter, SWT.NONE);
		lblImageFileNamewithouth.setBounds(50, 182, 185, 15);
		lblImageFileNamewithouth.setText("Image file name (withouth .jpg): ");
		
		Label lblBrightnessPercentage = new Label(shlGreyscaleConverter, SWT.NONE);
		lblBrightnessPercentage.setBounds(287, 182, 153, 15);
		lblBrightnessPercentage.setText("Image brightness percentage");
		
		Label lblImageFormatMust = new Label(shlGreyscaleConverter, SWT.NONE);
		lblImageFormatMust.setText("Image format must be .jpg");
		lblImageFormatMust.setBounds(50, 74, 390, 15);

	}
}
