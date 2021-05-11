import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.Math.*;
import java.util.Arrays;

public class ImageDCTDWT3 {
    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgRGB;
	BufferedImage imgDCT;
	BufferedImage imgDWT;
    int[] bytes;
    int width = 512;
	int height = 512;
    int[][][] rgb = new int[height][width][3]; //[{r,g,b}, {r,g,b}...]
    //int[][] quantize;
    double[][][] DCTmatrixFq;
	int[][][] DCT_RGB;
	int[][][] DWT_RGB;
	double[][] rMatrix_DWT;
	double[][] gMatrix_DWT;
	double[][] bMatrix_DWT;
	int[][][] DWTrgbMat;
	
    //get all RGB values from the image
    public void getRGB(String imagePath){
        bytes = new int[512*512*3];
        imgRGB = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        try
		{
			int frameLength = width*height*3;
            File file = new File(imagePath);
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
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					imgRGB.setRGB(x,y,pix);
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
    //encode for DCT
    private void encodeDCT(int m){

        //loop through image to get all r, g, and b values
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){

                Color mycolor = new Color(imgRGB.getRGB(x, y));		
                //get the rgb values for the current pixel
				int r = mycolor.getRed();
				int g = mycolor.getGreen();
				int b = mycolor.getBlue();
                rgb[x][y][0] = r;
                rgb[x][y][1] = g;
                rgb[x][y][2] = b;

                //if(y < 5 && x < 5){
                //    System.out.print("{" + r + ", "+ g + ", " + b + "}, ");
                //}
            }
        }
        /*
        quantize = new int[][]{{16,11,10,16,24,40,51,61}, 
                                {12,12,14,19,26,58,60,55},
                                {14,13,16,24,40,57,69,56},
                                {14,17,22,29,51,87,80,62},
                                {18,22,37,56,68,109,103,77},
                                {24,35,55,64,81,104,113,92},
                                {49,64,78,87,103,121,120,101},
                                {72,92,95,98,112,100,103,99}};
                                */
		/*
		int[][] test_rgb = new int[][]{{178,187,183,175,178,177,150,183}, 
                                {191,174,171,182,176,171,170,188},
                                {199,153,128,177,171,167,173,183},
                                {195,178,158,167,167,165,166,177},
                                {190,186,158,155,159,164,158,178},
                                {194,184,137,148,157,158,150,173},
                                {200,194,148,151,161,155,148,167},
                                {200,195,172,159,159,152,156,154}};
		*/
        int block_size = 8;
        //Discrete Cosine Transform 
        //store r,g,b inside a 512 x 512 matrix
        DCTmatrixFq = new double[height][width][3];
        //for every 8x8 block
        for(int r = 0; r < height; r += block_size){
            for(int c = 0; c < width; c += block_size){
				
				//individual blocks for r, g, b
				double[][] r_block = new double[block_size][block_size];
				double[][] g_block = new double[block_size][block_size];
				double[][] b_block = new double[block_size][block_size];

                //for every 8x8 block
                for(int u = 0; u < block_size; u++){
                    for(int v = 0; v < block_size; v++){

                        //set C(u) and C(v)
                        //C(u), C(v) = 1/sqrt(2) for u,v = 0
                        double Cu = 1;
						double Cv = 1;
                        if(u == 0){
                            Cu = (1/Math.sqrt(2));
                        }
						if(v == 0){
							Cv = (1/Math.sqrt(2));
						}
                        //find F(u,v) for the DCTmatrixFq
                        double Fuv_r = 0.0;
                        double Fuv_g = 0.0;
                        double Fuv_b = 0.0;
                        for (int x = 0; x < block_size; x++){
                            for (int y = 0; y < block_size; y++){
                                Fuv_r += (0.25 * Cu * Cv * rgb[r+x][c+y][0] * Math.cos((2 * x + 1) * u * Math.PI/16.0) * Math.cos((2 * y + 1) * v * Math.PI/16.0));
								Fuv_g += (0.25 * Cu * Cv * rgb[r+x][c+y][1] * Math.cos((2 * x + 1) * u * Math.PI/16.0) * Math.cos((2 * y + 1) * v * Math.PI/16.0));
								Fuv_b += (0.25 * Cu * Cv * rgb[r+x][c+y][2] * Math.cos((2 * x + 1) * u * Math.PI/16.0) * Math.cos((2 * y + 1) * v * Math.PI/16.0));
								//if(r == 0 && c == 0 && u == 0){
									//System.out.println(cucv);
									//System.out.println(rgb[r+x][c+y][0]);
									//System.out.println(rgb[r+x][c+y][1]);
									//System.out.println(rgb[r+x][c+y][2]);
									//System.out.print("{" + Fuv_r + ", "+ Fuv_g + ", " + Fuv_b + "}, ");
								//}
                            }
                        }
                        r_block[u][v] = (int) Fuv_r;
						g_block[u][v] = (int) Fuv_g;
						b_block[u][v] = (int) Fuv_b;
						//double Fq_r = (Fuv_r/(double)quantize[u][v]);
						//double Fq_g = (Fuv_g/(double)quantize[u][v]);
						//double Fq_b = (Fuv_b/(double)quantize[u][v]);
                        //DCTmatrixFq[r+u][c+v][0] = Fuv_r;
						//DCTmatrixFq[r+u][c+v][1] = Fuv_g;
						//DCTmatrixFq[r+u][c+v][2] = Fuv_b;
						
                    }
                }
                //zig zag for keeping first m coefficients
				r_block = zigZag(m, r_block);
				g_block = zigZag(m, g_block);
				b_block = zigZag(m, b_block);

                //fill in the r,g,b values for the DCT matrix
                for(int u = 0; u < block_size; u++){
                    for(int v = 0; v < block_size; v++){
                        DCTmatrixFq[r+u][c+v][0] = (int) r_block[u][v];
						DCTmatrixFq[r+u][c+v][1] = (int) g_block[u][v];
						DCTmatrixFq[r+u][c+v][2] = (int) b_block[u][v];
                    }
                }
            }
        }
		//System.out.println("The DC coefficient is [" + DCTmatrixFq[0][0][0] + ", " + DCTmatrixFq[0][0][1] + ", " + DCTmatrixFq[0][0][1] + "]");
    }
	
	private double[][] zigZag(int m, double[][] block){
		/*
		System.out.println("Matrix before: ");
		for(int a = 0; a < 8; a++){
			for(int b = 0; b < 8; b++){
				System.out.print(matrix[a][b][rgb] + " ");
			}
			System.out.println();
		}
		*/
		int row = 0;
		int col = 0;
		int numCoeff = 1;

		//start with the top left triangle of the matrix
		//if you've already reached the total coefficients (n/4096, set equal to 1)
        //[__  __  __  __ ]
        //[__  __  __  __ ]
        //[__  __  __  __ ]
        //[__  __  __  __ ]
		if(numCoeff > m){
			block[row][col] = 0; 
			numCoeff++;
		} else {
			numCoeff++;
		}
        //[_X_  __  __  __ ]
        //[__  __  __  __ ]
        //[__  __  __  __ ]
        //[__  __  __  __ ]
        //numCoeff: 2 (of 4)
        //row : 0
        //col : 0

		while(true) {
			col++; //+1 column
            //loop through top half of block
            //[_X_  __  __  __ ]
            //[__  __  __  __ ]
            //[__  __  __  __ ]
            //[__  __  __  __ ]
            //numCoeff: 2 (of 4)
            //row : 0
            //col : 1

			if(numCoeff > m){
				block[row][col] = 0; 
				numCoeff++;
			}else{
				numCoeff++;
			}
            //[_X_  _X_  __  __ ]
            //[__  __  __  __ ]
            //[__  __  __  __ ]
            //[__  __  __  __ ]
            //numCoeff: 3 (of 4)
            //row : 0
            //col : 1
            //loop down left diagonal (till first column)
			while(col != 0) {
				row++;
				col--;
                //[_X_  _X_  __  __ ]
                //[__  __  __  __ ]
                //[__  __  __  __ ]
                //[__  __  __  __ ]
                //numCoeff: 2 (of 4)
                //row : 1
                //col : 0

				if(numCoeff > m){
					block[row][col] = 0; 
					numCoeff++;
				}else{
					numCoeff++;
				}
                //[_X_  _X_  __  __ ]
                //[_X_  __  __  __ ]
                //[__  __  __  __ ]
                //[__  __  __  __ ]
                //numCoeff: 4 (of 4)
                //row : 1
                //col : 0
			}
			row++;
            //[_X_  _X_  __  __ ]
            //[_X_  __  __  __ ]
            //[__  __  __  __ ]
            //[__  __  __  __ ]
            //numCoeff: 4 (of 4)
            //row : 2
            //col : 0

            //make sure it's not out of bounds
			if(row >= block.length) {
				row--;
				break;
			}
            //[_X_  _X_  __  __ ]
            //[_X_  __  __  __ ]
            //[_X_  __  __  __ ]
            //[__  __  __  __ ]
            //numCoeff: 4 (of 4)
            //row : 2
            //col : 0
			if(numCoeff > m){
				block[row][col] = 0; 
				numCoeff++;
			}else{
				numCoeff++;
			}
            //[_X_  _X_  __  __ ]
            //[_X_  __  __  __ ]
            //[_X_  __  __  __ ]
            //[__  __  __  __ ]
            //numCoeff: 5 (of 4)
            //row : 2
            //col : 0

            //circle back to first row diagonal
			while(row != 0) {
				row--;
				col++;
				if(numCoeff > m){
					block[row][col] = 0; 
					numCoeff++;
				}else{
					numCoeff++;
				}
                //[_X_  _X_  _0_  __ ]
                //[_X_  _0_  __  __ ]
                //[_X_  __  __  __ ]
                //[__  __  __  __ ]
                //numCoeff: 6 (of 4)
                //row : 0
                //col : 2
			}
		}

		//for lower triangle of matrix
		while(true) {
			col++;
			if(numCoeff > m){
				block[row][col]=0; 
				numCoeff++;
			}else{
				numCoeff++;
			}

			while(col != block.length-1)
			{
				col++;
				row--;

				if(numCoeff > m){
					block[row][col] = 0; 
					numCoeff++;
				}else{
					numCoeff++;
				}
			}
			row++;
			if(row >= block.length)
			{
				row--;
				break;
			}

			if(numCoeff > m){
				block[row][col]=0; 
				numCoeff++;
			}else{
				numCoeff++;
			}

			while(row != block.length-1)
			{
				row++;
				col--;
				if(numCoeff > m){
					block[row][col]= 0; 
					numCoeff++;
				}else{
					numCoeff++;
				}
			}
		}
		/*
		System.out.println("Matrix after: (m = " + m + "): ");
		for(int a = 0; a < 8; a++){
			for(int b = 0; b < 8; b++){
				System.out.print(matrix[a][b][rgb] + " ");
			}
			System.out.println();
		}
		*/
		return block;
	}
	
	//decode for DCT
    private void decodeDCT(int m){

		DCT_RGB = new int[height][width][3];
		//double quantmat[][] = {{16,11,10,16,24,40,51,61}, {12,12,14,19,26,58,60,55},{14,13,16,24,40,57,69,56},{14,17,22,29,51,87,80,62},{18,22,37,56,68,109,103,77},{24,35,55,64,81,104,113,92},{49,64,78,87,103,121,120,101},{72,92,95,98,112,100,103,99}};
		//int Q[][] = {{0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0}};
		
		//keep only m coefficients and zero-out the rest
		//zigZag(DCTmatrixFq, m, 1);
		//zigZag(DCTmatrixFq, m, 2);
		
		//for(int t = 0; t < 8; t++){
		//	for(int g = 0; g < 8; g++){
		//		System.out.print(DCTmatrixFq[t][g][0] + " ");
		//	}
        //    System.out.println();
		//}
		//Inverse DCT 
        int block_size = 8;
		for(int r = 0; r < height; r += block_size)
		{
			for(int c = 0; c < width; c += block_size)
			{
				for (int x = 0; x < block_size; x++)
				{
					for (int y = 0; y < block_size; y++)
					{
						double inv_Fuv_r = 0.0;
						double inv_Fuv_g = 0.0;
						double inv_Fuv_b = 0.0;

						for (int u = 0; u < block_size; u++)
						{
							for (int v = 0; v < block_size; v++)
							{
								double Cu = 1;
								double Cv = 1;
                        		if(u == 0){
                            		Cu = (1/Math.sqrt(2));
                        		}
								if(v == 0){
                            		Cv = (1/Math.sqrt(2));
                        		}
								inv_Fuv_r += (0.25 * Cu * Cv * DCTmatrixFq[r+u][c+v][0] * Math.cos((2 * x + 1) * u * Math.PI/16.0) * Math.cos((2 * y + 1) * v * Math.PI/16.0));
								inv_Fuv_g += (0.25 * Cu * Cv * DCTmatrixFq[r+u][c+v][1] * Math.cos((2 * x + 1) * u *Math.PI/16.0) * Math.cos((2 * y + 1) * v * Math.PI/16.0));
								inv_Fuv_b += (0.25 * Cu * Cv * DCTmatrixFq[r+u][c+v][2] * Math.cos((2 * x + 1) * u *Math.PI/16.0) * Math.cos((2 * y + 1) * v * Math.PI/16.0));
							}
						}
						DCT_RGB[r + x][c + y][0] = (int)(inv_Fuv_r);
						DCT_RGB[r + x][c + y][1] = (int)(inv_Fuv_g);
						DCT_RGB[r + x][c + y][2] = (int)(inv_Fuv_b);
					}
				}
			}
		}
		//for(int t = 0; t < 8; t++){
		//	for(int g = 0; g < 8; g++){
		//		System.out.print(DCT_RGB[t][g][0] + " ");
		//	}
        //    System.out.println();
		//}
		//cut off values at 0 and 255 
		for(int x = 0; x < width; x++)
			{	
			for(int y = 0; y < height; y++)
			{	
				//r,g,b values
				for(int g = 0; g < 3; g++){
					if (DCT_RGB[x][y][g] > 255){
						DCT_RGB[x][y][g] = 255;
					}
					if(DCT_RGB[x][y][g] < 0){
						DCT_RGB[x][y][g] = 0;
					}
				}
			}
		}
	}

	private double[] filter(double[] arr){
		int len = arr.length;
		while(len > 0){
			arr = highLowPass(arr, len);
			len /= 2;
		}
		return arr;
	}

	private double[] highLowPass(double[] arr, int len){
		//create a new copy of the array to filter
		double[] filteredArray = Arrays.copyOf(arr, arr.length);
		int halflen = len/2;
		for(int i = 0; i < halflen; i++){
			//low pass filter
			filteredArray[i] = (arr[i * 2] + arr[i * 2 + 1]) / 2;

			//high pass filter
			filteredArray[halflen + i] = (arr[i * 2] - arr[i * 2 + 1]) / 2;
		}
		return filteredArray;
	}

	private double[][] transpose(double[][] original) {
		double[][] transposedMatrix = new double[height][width];
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				//flip the rows and columns
				transposedMatrix[r][c] = original[c][r]; 
			}
		}
		return transposedMatrix;
	}
	
	private void encodeDWT(int n){
		//RGB values are already inside the rgb matrix
		//for this calculation, I decided to break it into three channels
		rMatrix_DWT = new double[height][width];
		gMatrix_DWT = new double[height][width];
		bMatrix_DWT = new double[height][width];
		
		//System.out.println("The rgb is:");
		//for(int i = 0; i < 8; i++){
		//	for(int j = 0; j < 8; j++){
		//		System.out.print(rgb[i][j][0] + " ");
		//	}
		//	System.out.println();
		//}
		//split into r,g, and b channels
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				rMatrix_DWT[i][j] = rgb[i][j][0];
				gMatrix_DWT[i][j] = rgb[i][j][1];
				bMatrix_DWT[i][j] = rgb[i][j][2];
			}
		}
		//System.out.println("the wdith of rMatrix_DWT is " + rMatrix_DWT.length);
		//System.out.println("the height of rMatrix_DWT is " + rMatrix_DWT[0].length);
		//System.out.println("The rMatrix_DWT before filter  is:");
		//for(int i = 0; i < 8; i++){
		//	for(int j = 0; j < 8; j++){
		//		System.out.print(rMatrix_DWT[i][j] + " ");
		//	}
		//	System.out.println();
		//}

		//decompose by rows
		for(int x = 0; x < width; x++){
			rMatrix_DWT[x] = filter(rMatrix_DWT[x]);
			gMatrix_DWT[x] = filter(gMatrix_DWT[x]);
			bMatrix_DWT[x] = filter(bMatrix_DWT[x]);
		}
		
		//System.out.println("The rMatrix AFTER filter is:");
		//for(int i = 0; i < 8; i++){
		//	for(int j = 0; j < 8; j++){
		//		System.out.print(rMatrix_DWT[i][j] + " ");
		//	}
		//	System.out.println();
		//}

		//transpose, and do the same for all columns
		rMatrix_DWT = transpose(rMatrix_DWT);
		gMatrix_DWT = transpose(gMatrix_DWT);
		bMatrix_DWT = transpose(bMatrix_DWT);

		//filter columns
		for(int y=0; y < height; y++) {
			rMatrix_DWT[y] = filter(rMatrix_DWT[y]);
			gMatrix_DWT[y] = filter(gMatrix_DWT[y]);
			bMatrix_DWT[y] = filter(bMatrix_DWT[y]);
		}		
		//flip back
		rMatrix_DWT = transpose(rMatrix_DWT);
		gMatrix_DWT = transpose(gMatrix_DWT);
		bMatrix_DWT = transpose(bMatrix_DWT);

		//zigzag with n
		rMatrix_DWT = zigZag(n, rMatrix_DWT);
		gMatrix_DWT = zigZag(n, gMatrix_DWT);
		bMatrix_DWT = zigZag(n, bMatrix_DWT);
	}

	private double[] filter_comp(double[] arr) {
		int len = 1;
		while (len <= arr.length) {
			arr = lowHighPassComp(arr, len);
			len = len*2;
		}
		return arr;
	}

	private double[] lowHighPassComp(double[] array, int len) {
		double[] filteredArrayComp = Arrays.copyOf(array, array.length);
		int halflen = len/2;
		for(int i = 0; i < halflen; i++) {

			filteredArrayComp[i * 2] = array[i] + array[halflen + i];

			filteredArrayComp[i * 2 + 1] = array[i] - array[halflen + i];
		}
		return filteredArrayComp;
	}

	private void decodeDWT(){

		//for returning rgb values
		DWTrgbMat = new int[height][width][3];
		
		//decompose by cols
		rMatrix_DWT = transpose(rMatrix_DWT);
		gMatrix_DWT = transpose(gMatrix_DWT);
		bMatrix_DWT = transpose(bMatrix_DWT);

		for(int c=0; c < height; c++) {
			rMatrix_DWT[c] = filter_comp(rMatrix_DWT[c]);
			gMatrix_DWT[c] = filter_comp(gMatrix_DWT[c]);
			bMatrix_DWT[c] = filter_comp(bMatrix_DWT[c]);
		}	
		rMatrix_DWT = transpose(rMatrix_DWT);
		gMatrix_DWT = transpose(gMatrix_DWT);
		bMatrix_DWT = transpose(bMatrix_DWT);

		//rows
		for(int r=0; r < width; r++){
			rMatrix_DWT[r] = filter_comp(rMatrix_DWT[r]);
			gMatrix_DWT[r] = filter_comp(gMatrix_DWT[r]);
			bMatrix_DWT[r] = filter_comp(bMatrix_DWT[r]);
		}

		//limit to < 255 and > 0, add to resulting matrix of RGB
		for(int i=0; i < height; i++){
			for(int j=0; j < width; j++){
				//round the numbers
				DWTrgbMat[i][j][0] = (int) Math.round(rMatrix_DWT[i][j]);
				DWTrgbMat[i][j][1] = (int) Math.round(gMatrix_DWT[i][j]);
				DWTrgbMat[i][j][2] = (int) Math.round(bMatrix_DWT[i][j]);

				//cut off ranges
				for(int rgb = 0; rgb < 3; rgb++){
					if(DWTrgbMat[i][j][rgb] < 0){
						DWTrgbMat[i][j][rgb] = 0;
					}
					if(DWTrgbMat[i][j][rgb] > 255){
						DWTrgbMat[i][j][rgb] = 255;
					}
				}
			}
		}		
	}
	
	private void createDWTImage(){
		//set new RGB buffered image
		//DWTrgbMat = new int[height][width][3];
		imgDWT = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int r = DWTrgbMat[x][y][0];
				int g = DWTrgbMat[x][y][1];
				int b = DWTrgbMat[x][y][2];
				Color color = new Color(r, g, b);
				imgDWT.setRGB(x,y,color.getRGB());
			}
		}
		//create a new rgb image to return 
	}
	private void showDWTImage(){
		// Use label to display the image
		frame = new JFrame("DWT Image");
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgDWT));

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

	private void createDCTImage(){
		//set new RGB buffered image
		imgDCT = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int r = DCT_RGB[x][y][0];
				int g = DCT_RGB[x][y][1];
				int b = DCT_RGB[x][y][2];
				Color color = new Color(r, g, b);
				imgDCT.setRGB(x,y,color.getRGB());
			}
		}
		//create a new rgb image to return 
	}
	private void showDCTImage(){
		// Use label to display the image
		frame = new JFrame("DCT Image");
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgDCT));

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

        ImageDCTDWT3 image = new ImageDCTDWT3();
        System.out.println("The file is: " + args[0]);
        image.getRGB(args[0]); 

		int n = Integer.parseInt(args[1]);
		System.out.println("n is " + n);
		int m = (int) Math.ceil(n/4096.0);
		System.out.println("m is " + m);

        //step 1: DCT encoding 
        //       fill in the rgb matrix 
        //       do DCT Transform
        //       quantization
        image.encodeDCT(m);
		image.decodeDCT(m);
		image.createDCTImage();
		image.showDCTImage();
		//show image
		image.encodeDWT(n);
		image.decodeDWT();
		image.createDWTImage();
		image.showDWTImage();

        //image.showIms(args, "DCT Conversion");
        //image.showIms(args, "DWT Conversion");
    }
}
