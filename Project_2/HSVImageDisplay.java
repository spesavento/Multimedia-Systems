import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class HSVImageDisplay {
    
	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	int width = 512;
	int height = 512;
	//double[][][] rgbDub = new double[width][height][3];
	int[][][] rgb = new int[width][height][3];
	double[][][] hsv = new double[width][height][3];

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

    public void showIms(String[] args){

		// Read a parameter from command line
		String param1 = args[1];
		System.out.println("The second parameter was: " + param1);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

    //convert from RGB to the HSV color space
    private void rgbToHSV(){
        //loop through image to get all r, g, and b values
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){

                Color mycolor = new Color(imgOne.getRGB(x, y));		
                //get the rgb values for the current pixel
				int r = mycolor.getRed();
				int g = mycolor.getGreen();
				int b = mycolor.getBlue();
                rgb[x][y][0] = r;
                rgb[x][y][1] = g;
                rgb[x][y][2] = b;

                //divide by 255
                double r_conv = r / 255.0;
                double g_conv = g / 255.0;
                double b_conv = b / 255.0;

                //get the overall max and min of RGB
                double maxRGB = Math.max(Math.max(g_conv, b_conv), r_conv);
                double minRGB = Math.min(Math.min(g_conv, b_conv), r_conv);
                double h = -999;
                double s = -999;

                double minMaxDiff = maxRGB-minRGB;
                /* Convert H */
                /*
                if(maxRGB == r_conv){
                    h = (60 * ((g_conv - b_conv) / minMaxDiff) + 0);
                } else if(maxRGB == g_conv) {
                    h = (60 * ((b_conv - r_conv) / minMaxDiff) + 2);
                } else if(maxRGB == b_conv){
                    h = (60 * ((r_conv - g_conv) / minMaxDiff) + 4);
                } else if(maxRGB == minRGB){
                    h = 0;
                }
                */
                int r_shift = 360;
				int g_shift = 120;
				int b_shift = 240;
                if(maxRGB == r_conv){
                    h = (60 * ((g_conv - b_conv) / minMaxDiff) + r_shift) % 360;
                } else if(maxRGB == g_conv) {
                    h = (60 * ((b_conv - r_conv) / minMaxDiff) + g_shift) % 360;
                } else if(maxRGB == b_conv){
                    h = (60 * ((r_conv - g_conv) / minMaxDiff) + b_shift) % 360;
                } else if(maxRGB == minRGB){
                    h = 0;
                }
                if(h > 359){
					System.out.println(h);
				}

                /* Convert S */
                if(maxRGB != 0){
                    s = (minMaxDiff / maxRGB) * 100;
                } else {
                    s = 0;
                }
                /* Convert V */
                double v = maxRGB * 100; 

                hsv[x][y][0] = h;
                hsv[x][y][1] = s;
                hsv[x][y][2] = v;
                //testing:
                //https://www.rapidtables.com/convert/color/rgb-to-hsv.html
                /*
				if(x > 150 && x < 200  && y < 3){
                    System.out.println("rgb (" + r + " " + g + " " + b + ")");
                    System.out.println("hsv (" + h + " " + s + " " + v + ")");
                }
				*/
            }
        }
    }
    private void thresholdHSV(String low, String high){
        //All the pixels falling between these two hue thresholds will be displayed in the original color 
        //in the output image, whereas all the other pixels outside the threshold will be displayed in gray.
        int lowerBound = Integer.parseInt(low);
        int upperBound = Integer.parseInt(high);
        //System.out.println(lowerBound);
        //System.out.println(upperBound);

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                //if out of range hue (else keep value)
				/*
                if(x < 3 && y < 3){
                    System.out.println(hsv[x][y][0]);
                }
				*/
                if(hsv[x][y][0] < lowerBound || hsv[x][y][0] > upperBound){
                    //double greyscaleVal = 0.5 * (Math.max(Math.max(rgb[x][y][0],rgb[x][y][1]) ,rgb[x][y][2]) + Math.min(Math.min(rgb[x][y][0],rgb[x][y][1]), rgb[x][y][2]));
                    //double greyscaleVal = (rgb[x][y][0] + rgb[x][y][1] + rgb[x][y][2])/3;
                    //double greyscaleVal = 0.2989 * rgb[x][y][0] + 0.5870 * rgb[x][y][1] + 0.1140 * rgb[x][y][2];
                    
					double greyscaleVal = (Math.max(Math.max(rgb[x][y][0],rgb[x][y][1]),rgb[x][y][2]));
                    //take the highest?

                    //replace with grey
                    rgb[x][y][0] = (int) Math.round(greyscaleVal);
                    rgb[x][y][1] = (int) Math.round(greyscaleVal);
                    rgb[x][y][2] = (int) Math.round(greyscaleVal);
                }
            }
        }
    }

    private void createNewImage(){
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++)
			{
				int r = rgb[x][y][0];
				int g = rgb[x][y][1];
				int b = rgb[x][y][2];
				Color color = new Color(r, g, b);
				//int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				imgTwo.setRGB(x,y,color.getRGB());
			}
		}
	}
	public void showNewImage(){
		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgTwo));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}
    public static void main(String[] args){
        HSVImageDisplay ren = new HSVImageDisplay();
        ren.showIms(args);
        ren.rgbToHSV();
        ren.thresholdHSV(args[1], args[2]);
        ren.createNewImage();
		ren.showNewImage();
    }
}
