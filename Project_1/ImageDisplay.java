
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

 
public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	int width = 352;
	int height = 288;
	double[][][] rgbDub = new double[width][height][3];
	int[][][] rgb = new int[width][height][3];
	double[][][] yuv = new double[width][height][3];

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

	//this function extracts the RGB values from the buffered image and converts them to YUV
	private void convertToYUV(){

		double[][] yuvConversion = new double[][]{
			{0.299, 0.587, 0.114},
			{0.596, -0.274, -0.322},
			{0.211, -0.523, 0.312}
		};

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				Color mycolor = new Color(imgOne.getRGB(x, y));
				//convert the r,g,b to y,u,v values
				int r = mycolor.getRed();
				int g = mycolor.getGreen();
				int b = mycolor.getBlue();

				//matrix transformation
				double y_val = yuvConversion[0][0]*r + yuvConversion[0][1]*g + yuvConversion[0][2]*b;
				double u_val = yuvConversion[1][0]*r + yuvConversion[1][1]*g + yuvConversion[1][2]*b;
				double v_val = yuvConversion[2][0]*r + yuvConversion[2][1]*g + yuvConversion[2][2]*b;
				
				//store y in: yuv[x][y][0]
				yuv[x][y][0] = y_val;
				//store u in: yuv[x][y][1]
				yuv[x][y][1] = u_val;
				//store v in: yuv[x][y][2]
				yuv[x][y][2] = v_val;
			}
		}
		/* for printing */
		/*
		Color mycolor1 = new Color(imgOne.getRGB(0, 0)); //width, height
		Color mycolor2 = new Color(imgOne.getRGB(1, 0)); //width, height
		Color mycolor3 = new Color(imgOne.getRGB(2, 0)); //width, height
		System.out.println("First three RGB in row 0");
		System.out.println(mycolor1.getRed() + ", " + mycolor1.getGreen() + ", " + mycolor1.getBlue());
		System.out.println(mycolor2.getRed() + ", " + mycolor2.getGreen() + ", " + mycolor2.getBlue());
		System.out.println(mycolor3.getRed() + ", " + mycolor3.getGreen() + ", " + mycolor3.getBlue());

		Color mycolo1 = new Color(imgOne.getRGB(0, 1)); //width, height
		Color mycolo2 = new Color(imgOne.getRGB(1, 1)); //width, height
		Color mycolo3 = new Color(imgOne.getRGB(2, 1)); //width, height
		System.out.println("First three RGB in row 0");
		System.out.println(mycolo1.getRed() + ", " + mycolo1.getGreen() + ", " + mycolo1.getBlue());
		System.out.println(mycolo2.getRed() + ", " + mycolo2.getGreen() + ", " + mycolo2.getBlue());
		System.out.println(mycolo3.getRed() + ", " + mycolo3.getGreen() + ", " + mycolo3.getBlue());

		System.out.println("First three YUV in row 0"); //height stays 0
		System.out.println(yuv[0][0][0] + ", " + yuv[0][0][1] + ", " + yuv[0][0][2]);
		System.out.println(yuv[1][0][0] + ", " + yuv[1][0][1] + ", " + yuv[1][0][2]);
		System.out.println(yuv[2][0][0] + ", " + yuv[2][0][1] + ", " + yuv[2][0][2]);

		System.out.println("First three RGB in column 0");
		Color mycolor4 = new Color(imgOne.getRGB(0, 0)); //width, height
		Color mycolor5 = new Color(imgOne.getRGB(0, 1)); //width, height
		Color mycolor6 = new Color(imgOne.getRGB(0, 2)); //width, height
		System.out.println(mycolor4.getRed() + ", " + mycolor4.getGreen() + ", " + mycolor4.getBlue());
		System.out.println(mycolor5.getRed() + ", " + mycolor5.getGreen() + ", " + mycolor5.getBlue());
		System.out.println(mycolor6.getRed() + ", " + mycolor6.getGreen() + ", " + mycolor6.getBlue());

		System.out.println("First three YUV in column 0"); //width stays 0
		System.out.println(yuv[0][0][0] + ", " + yuv[0][0][1] + ", " + yuv[0][0][2]);
		System.out.println(yuv[0][1][0] + ", " + yuv[0][1][1] + ", " + yuv[0][1][2]);
		System.out.println(yuv[0][2][0] + ", " + yuv[0][2][1] + ", " + yuv[0][2][2]);
		*/
		
	}

	private void subsampleYUV(String[] args){
		/* for printing */
		/*
		System.out.println();
		for(int y = 0; y < 1; y++){
			for(int x = 0; x < width; x++){
				System.out.print(yuv[x][y][2] + " ");
			}
			System.out.println();
		}
		System.out.println();
		*/
		//args[1] = Y
		//args[2] = U
		//args[3] = V
		int ySub = Integer.parseInt(args[1]);
		int uSub = Integer.parseInt(args[2]);
		int vSub = Integer.parseInt(args[3]);

		//tracks the running average needed to fill in the blanks
		//average = (runningAvg + (currPos - y_sub))/2
		//or if 

		for(int y = 0; y < height; y++){
			upSampleY(ySub, y);
			upSampleU(uSub, y);
			upSampleV(vSub, y);
		}
		/* for printing */
		/*
		for(int y = 0; y < 2; y++){
			for(int x = 0; x < width; x++){
				System.out.print("(" + yuv[x][y][0] + ", " + yuv[x][y][1] + ", " + + yuv[x][y][2] + ") ");
			}
			System.out.println();
		}
		*/
	}
	private void upSampleY(int ySub, int y){
		int leftKeep = 0;
		for(int x = 0; x < width; x++){
			//replace with average  
			if(x % ySub == 0){
				leftKeep = x;
			} else {
				//if there is left and a right, take the average
				if( (leftKeep + ySub) < width){
					yuv[x][y][0] = (yuv[leftKeep][y][0] + yuv[leftKeep + ySub][y][0]) / 2;
				} else {
					//otherwise, fill in with the leftmost value
					yuv[x][y][0] = yuv[leftKeep][y][0];
				}
			}
		}
	}
	private void upSampleU(int uSub, int y){
		int leftKeep = 0;
		for(int x = 0; x < width; x++){
			//replace with average  
			if(x % uSub == 0){
				leftKeep = x;
			} else {
				//if there is left and a right, take the average
				if( (leftKeep + uSub) < width){
					yuv[x][y][1] = (yuv[leftKeep][y][1] + yuv[leftKeep + uSub][y][1]) / 2;
				} else {
					//otherwise, fill in with the leftmost value
					yuv[x][y][1] = yuv[leftKeep][y][1];
				}
			}
		}
	}
	private void upSampleV(int vSub, int y){
		int leftKeep = 0;
		for(int x = 0; x < width; x++){
			//replace with average  
			if(x % vSub == 0){
				leftKeep = x;
			} else {
				//if there is left and a right, take the average
				if( (leftKeep + vSub) < width){
					yuv[x][y][2] = (yuv[leftKeep][y][2] + yuv[leftKeep + vSub][y][2]) / 2;
				} else {
					//otherwise, fill in with the leftmost value
					yuv[x][y][2] = yuv[leftKeep][y][2];
				}
			}
		}
	}
	private void convertToRGB(){

		double[][] rgbConversion = new double[][]{
			{1.000, 0.956, 0.621},
			{1.000, -0.272, -0.647},
			{1.000, -1.106, 1.703}
		};

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				//convert the r,g,b to y,u,v values
				double y_val = yuv[x][y][0];
				double u_val = yuv[x][y][1];
				double v_val = yuv[x][y][2];

				//matrix transformation and clamp r,g,b to [0, 255]
				double r = rgbConversion[0][0]*y_val + rgbConversion[0][1]*u_val + rgbConversion[0][2]*v_val;
				double g = rgbConversion[1][0]*y_val + rgbConversion[1][1]*u_val + rgbConversion[1][2]*v_val;
				double b = rgbConversion[2][0]*y_val + rgbConversion[2][1]*u_val + rgbConversion[2][2]*v_val;

				//clamp r,g,b to [0, 255]

				//store r in: rgb[x][y][0]
				rgbDub[x][y][0] = r;
				//store u in: yuv[x][y][1]
				rgbDub[x][y][1] = g;
				//store v in: yuv[x][y][2]
				rgbDub[x][y][2] = b;
			}
		}
	}

	private void quantization(String Q){

		int Qarg = Integer.parseInt(Q);
		if(Qarg == 256){
			quantize256();
			return;
		}

		int[] steps = calculateSteps(Qarg); 

		quantizeY(steps);
		quantizeU(steps);
		quantizeV(steps);

		/* for printing */
		/*
		System.out.println();
		System.out.println("First 3 RGB double in row 0"); //height stays 0
		for(int i = 0; i < 3; i++){
			System.out.println(rgbDub[i][0][0] + ", " + rgbDub[i][0][1] + ", " + rgbDub[i][0][2]);
		}
		System.out.println();
		System.out.println("First 3 RGB in row 0"); //height stays 0
		for(int i = 0; i < 3; i++){
			System.out.println(rgb[i][0][0] + ", " + rgb[i][0][1] + ", " + rgb[i][0][2]);
		}
		*/
		
	}
	private void quantize256(){
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				//clamp between 0 and 255
				rgb[x][y][0] = (int) Math.min(Math.max(Math.round(rgbDub[x][y][0]), 0), 255);
				rgb[x][y][1] = (int) Math.min(Math.max(Math.round(rgbDub[x][y][1]), 0), 255);
				rgb[x][y][2] = (int) Math.min(Math.max(Math.round(rgbDub[x][y][2]), 0), 255);
			}
		}
		/*
		System.out.println();
		System.out.println("First 3 RGB double in row 0"); //height stays 0
		for(int i = 0; i < 3; i++){
			System.out.println(rgbDub[i][0][0] + ", " + rgbDub[i][0][1] + ", " + rgbDub[i][0][2]);
		}
		System.out.println();
		System.out.println("First 3 RGB in row 0"); //height stays 0
		for(int i = 0; i < 3; i++){
			System.out.println(rgb[i][1][0] + ", " + rgb[i][1][1] + ", " + rgb[i][1][2]);
		}
		*/
		
	}
	private int[] calculateSteps(int Qarg){
		int[] steps = new int[Qarg];

		//1.28, 2.56, 3.84
		double size = 0;
		size = (double)256/Qarg;
		/*
		if((256/Qarg) % 2 == 0){
			size = (double)256/Qarg;
		}
		*/
		/*
		else {
			size = (double)255/Qarg;
		}
		*/
		//0 counts as 1 so add 1
		double total = size; //+32
		/*System.out.println("step size is: " + total);*/
		for(int i = 1; i < steps.length; i++){
			steps[i] = (int) Math.round(total);
			total += size;
		}
		/* for printing */
		/*
		for(int i = 0; i < steps.length; i++){
			System.out.println(steps[i]);
		}
		*/
		
		return steps;
	}
	private void quantizeY(int[] steps){		
		//change rgbDub to integer matrix rgb
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				double min = Math.abs(rgbDub[x][y][0] - steps[0]);
				int minLoc = 0;
				//find the min difference
				for(int j = 0; j < steps.length; j++){
					if(Math.abs(rgbDub[x][y][0] - steps[j]) <= min){
						min = Math.abs(rgbDub[x][y][0] - steps[j]);
						minLoc = j;
					}
				}
				rgb[x][y][0] = steps[minLoc];
			}
		}
	}
	private void quantizeU(int[] steps){		
		//change rgbDub to integer matrix rgb
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				double min = Math.abs(rgbDub[x][y][1] - steps[0]);
				int minLoc = 0;
				//find the min difference
				for(int j = 0; j < steps.length; j++){
					if(Math.abs(rgbDub[x][y][1] - steps[j]) <= min){
						min = Math.abs(rgbDub[x][y][1] - steps[j]);
						minLoc = j;
					}
				}
				rgb[x][y][1] = steps[minLoc];
			}
		}
	}
	private void quantizeV(int[] steps){		
		//change rgbDub to integer matrix rgb
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				double min = Math.abs(rgbDub[x][y][2] - steps[0]);
				int minLoc = 0;
				//find the min difference
				for(int j = 0; j < steps.length; j++){
					if(Math.abs(rgbDub[x][y][2] - steps[j]) <= min){
						min = Math.abs(rgbDub[x][y][2] - steps[j]);
						minLoc = j;
					}
				}
				rgb[x][y][2] = steps[minLoc];
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

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
		ren.convertToYUV();
		ren.subsampleYUV(args);
		ren.convertToRGB();
		ren.quantization(args[4]);

		ren.createNewImage();
		ren.showNewImage();
	}

}
