package org.document.scanner;




import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class Scanner {
	int originalWidth = 900;
	int originalHeight = 1200;
	double reduceRatio =10;
	static InputStream inputFile = null;
	static FileOutputStream  outFile = null;
	
	static Logger logger = Logger.getLogger(Scanner.class.getName());
	public void loadOpenCV() throws IOException{
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public String[] scanCords(ScanFile sf) throws Exception{
		String[] arrayOfCordinates= new String[8];
		  loadOpenCV();
		  String fileName = sf.getFileName()+"."+sf.getFileType();
		
		
		  
		  Mat matBytes = new Mat(1, sf.getFile_byte_pre_scan().length, CvType.CV_8S);
		  matBytes.put(0, 0, sf.getFile_byte_pre_scan());
		   matBytes = Imgcodecs.imdecode(matBytes, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		   double imageSize = (Math.round(sf.getFile_byte_pre_scan().length*100/1024/1024)/100.0d) ;
			
			originalWidth = sf.getOriWidth();
			originalHeight = sf.getOriHeight();
			
			double megaPixel =  (sf.getOriWidth() * sf.getOriHeight()) / 1000000;
			
			
			int reduceRatioSizeLandscape = 100;
			 int reduceRatioSizePotarait = 75;
			 if (megaPixel > 5.0){
				  reduceRatioSizeLandscape = 200;
				  reduceRatioSizePotarait = 150;
			 }
			 
			   if (megaPixel > 7.0){
				    reduceRatioSizeLandscape = 400;
					  reduceRatioSizePotarait = 300;
			   }
			   if (originalWidth > originalHeight){
				  reduceRatio = originalWidth / reduceRatioSizeLandscape;
			  }else{
				  reduceRatio = originalWidth / reduceRatioSizePotarait;
			  }
			   
			   int w  =(int) (originalWidth/reduceRatio); 
			   int h =  (int) (originalHeight/reduceRatio);
			   Mat reducedSize =new Mat();
			   Imgproc.resize( matBytes,reducedSize, new Size(w,h));
			  
			/*   BufferedImage imgFinal = ImageIO.read(new ByteArrayInputStream(sf.getFile_byte_pre_scan()));
			   Image scaledImg =  imgFinal.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH);
				BufferedImage thumbnail = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				thumbnail.createGraphics().drawImage(scaledImg,0,0,null);
				 ByteArrayOutputStream baos = new ByteArrayOutputStream();
				 baos.flush();
					ImageIO.write( thumbnail, "png", baos );
					baos.flush();
					byte[] imageInByte = baos.toByteArray();
					baos.close();
			   
					
					Mat reducedSize = Imgcodecs.imdecode(matBytes, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE); */
				
					  Mat blur = new Mat();
			   Imgproc.GaussianBlur(reducedSize, blur, new Size(5.0,5.0), 0.0);
			if (sf.isGenAllFiles())Imgcodecs.imwrite(sf.getFilePath()+"/"+"Gb_"+fileName, blur);
			
			Mat eddgedImage = new Mat();
			
			Imgproc.Canny(blur, eddgedImage, 75, 200);
			if (sf.isGenAllFiles())Imgcodecs.imwrite(sf.getFilePath()+"/"+"Ed_"+fileName, eddgedImage);
			
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(eddgedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			Collections.reverse(contours);
		    Mat drawCounters = reducedSize;
		    Imgproc.drawContours(drawCounters, contours, -1, new Scalar(0, 255, 0),2);
		    
		    if (sf.isGenAllFiles())Imgcodecs.imwrite(sf.getFilePath()+"/"+"Wo_"+fileName, drawCounters);
			    MatOfPoint temp_contour = contours.get(0); 
			    MatOfPoint2f approxCurve = new MatOfPoint2f();
			    MatOfPoint largest_contour = contours.get(0);
			    List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
			    MatOfPoint2f bigestCurve=null;
			    boolean found = false;
			    MatOfPoint2f approxCurve_temp=null;
			    for (int idx = 0; idx < contours.size(); idx++) {
			        temp_contour = contours.get(idx);
			            MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
			             approxCurve_temp = new MatOfPoint2f();
			            Imgproc.approxPolyDP(new_mat, approxCurve_temp,  0.02 * Imgproc.arcLength(new_mat, true), true);
			            
			            if (approxCurve_temp.total() == 4){
			           
			            if (bigestCurve == null ){
			            	bigestCurve=approxCurve_temp;
			            }

			            if (Imgproc.contourArea(approxCurve_temp) > Imgproc.contourArea(bigestCurve))
			            bigestCurve =approxCurve_temp;
			            logger.log(Level.FINE, "Size found with 4 courners"+String.valueOf(Imgproc.contourArea(bigestCurve)));
			            }
			    }
			    double bigestCounter= (reducedSize.width() *reducedSize.height() *20)/100 ;
			    if (Imgproc.contourArea(bigestCurve) > bigestCounter){
			    	found=true;
			    	 approxCurve=bigestCurve;
		                largest_contour = temp_contour;
		                largest_contours.add(largest_contour);
			    }
			    if (!found){
			    	sf.setScanStaus(false);
			    	return null;
			    }
			    Mat largestCunter = reducedSize;
			    Imgproc.drawContours(largestCunter, largest_contours, -1, new Scalar(0, 255, 0),2);
			    
			    double[] p1 = approxCurve.get(0,0);
			    double[] p2 = approxCurve.get(1,0);
			    double[] p3 = approxCurve.get(2,0);
			    double[] p4 = approxCurve.get(3,0);
			    
			    if (p2[0] < (originalWidth/reduceRatio)/2 && p2[1] < (originalHeight/reduceRatio)/2){
			    	 double[] temp_double;
					    temp_double = approxCurve.get(1,0); 
					    arrayOfCordinates[0]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[1]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(2,0); 
					    arrayOfCordinates[2]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[3]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(3,0); 
					    arrayOfCordinates[4]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[5]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(0,0); 
					    arrayOfCordinates[6]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[7]=String.valueOf(temp_double[1] *reduceRatio );
			    }
			    else  if (p3[0] < (originalWidth/reduceRatio)/2 && p3[1] < (originalHeight/reduceRatio)/2){
			    	 double[] temp_double;
					    temp_double = approxCurve.get(2,0); 
					    arrayOfCordinates[0]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[1]=String.valueOf(temp_double[1] *reduceRatio);
					    temp_double = approxCurve.get(3,0); 
					    arrayOfCordinates[2]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[3]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(0,0); 
					    arrayOfCordinates[4]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[5]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(1,0); 
					    arrayOfCordinates[6]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[7]=String.valueOf(temp_double[1] *reduceRatio );
			    }
			    else{
			    	 double[] temp_double;
					    temp_double = approxCurve.get(0,0); 
					    arrayOfCordinates[0]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[1]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(1,0); 
					    arrayOfCordinates[2]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[3]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(2,0); 
					    arrayOfCordinates[4]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[5]=String.valueOf(temp_double[1] *reduceRatio );
					    temp_double = approxCurve.get(3,0); 
					    arrayOfCordinates[6]=String.valueOf(temp_double[0] *reduceRatio );
					    arrayOfCordinates[7]=String.valueOf(temp_double[1] *reduceRatio );
					  
			    }

				sf.setFile_byte_pre_scan(null);
				sf.setScanStaus(true);

			    
			    double newHeight=Double.valueOf(arrayOfCordinates[3]) - Double.valueOf(arrayOfCordinates[1]);
			    double newWidth=Double.valueOf(arrayOfCordinates[6]) - Double.valueOf(arrayOfCordinates[0]);
			    
			    if (newHeight < 0.0 || newWidth < 0.0 ){
			    	sf.setScanStaus(false);
			    }
			    
		return arrayOfCordinates;
		
	}


public ScanFile scan(ScanFile sf,String fileName, ScanType scanType) throws Exception{
	loadOpenCV();
	  Mat originalColorImage =null;
	  Mat gray =null;
	  
	  Mat encoded = new Mat(1, sf.getFile_byte_pre_scan().length, CvType.CV_8S);
	  encoded.put(0, 0, sf.getFile_byte_pre_scan());
	   originalColorImage = Imgcodecs.imdecode(encoded, Imgcodecs.CV_LOAD_IMAGE_COLOR);
	   if (sf.isGenAllFiles())Imgcodecs.imwrite("Or_"+fileName, originalColorImage);
	   gray = Imgcodecs.imdecode(encoded, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
	   
	   
	   double imageSize = (Math.round(sf.getFile_byte_pre_scan().length*100)/100.0d) /1024/1024;
		originalWidth = originalColorImage.width();
		originalHeight = originalColorImage.height();

		Imgproc.GaussianBlur(originalColorImage, gray, new Size(5.0,5.0), 0.0);
		
		
		
		Mat eddgedImage = new Mat();
		Imgproc.Canny(gray, eddgedImage, 75, 200);
		if (sf.isGenAllFiles())Imgcodecs.imwrite("eddgedImage_"+fileName, eddgedImage);
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(eddgedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Collections.reverse(contours);
		
		    MatOfPoint temp_contour = contours.get(0); 
		    MatOfPoint2f approxCurve = new MatOfPoint2f();
		    MatOfPoint largest_contour = contours.get(0);
		    List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
		    
		    boolean found = false;
		    for (int idx = 0; idx < contours.size(); idx++) {
		        temp_contour = contours.get(idx);
		            MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
		            MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
		            Imgproc.approxPolyDP(new_mat, approxCurve_temp,  0.02 * Imgproc.arcLength(new_mat, true), true);
		            if (approxCurve_temp.total() == 4)
		            if (approxCurve_temp.total() == 4 && Imgproc.contourArea(temp_contour) > 250000.0) {
		            	found=true;
		                approxCurve=approxCurve_temp;
		                largest_contour = temp_contour;
		                largest_contours.add(largest_contour);
		                break;
		            }
		    }
		    if (!found){
		    	sf.setScanStaus(false);
		    	return sf;
		    }
		    Mat drawCounters = new Mat();
		    Imgproc.drawContours(drawCounters, largest_contours, -1, new Scalar(0, 255, 0),2);
		    
		    if (sf.isGenAllFiles())Imgcodecs.imwrite("withOutline_"+fileName, drawCounters);
		    double newHeight=Double.valueOf(sf.getArrayOfCordinates()[3]) - Double.valueOf(sf.getArrayOfCordinates()[1]);
		    double newWidth=Double.valueOf(sf.getArrayOfCordinates()[6]) - Double.valueOf(sf.getArrayOfCordinates()[0]);
		    double keepMargin = 5.00;
		    double[] temp_double;
		    temp_double = approxCurve.get(0,0);       
		    Point p1 = new Point(temp_double[0]- keepMargin, temp_double[1] - keepMargin);
		    temp_double = approxCurve.get(1,0);       
		    Point p2 = new Point(temp_double[0]- keepMargin, temp_double[1]+ keepMargin);
		    temp_double = approxCurve.get(2,0);       
		    Point p3 = new Point(temp_double[0]+ keepMargin, temp_double[1]+ keepMargin);
		    temp_double = approxCurve.get(3,0);       
		    Point p4 = new Point(temp_double[0]+ keepMargin, temp_double[1]- keepMargin);
		    List<Point> source = new ArrayList<Point>();
		    source.add(p1);
		    source.add(p2);
		    source.add(p3);
		    source.add(p4);
		    Mat startM = Converters.vector_Point2f_to_Mat(source);
		    Mat result=warp(originalColorImage,startM,newHeight,newWidth);

		  
		    Imgproc.resize(result, result, new Size(originalWidth,originalHeight));
		    Mat brightColorImage=new Mat();
		    result.convertTo(brightColorImage, -1,1.17,5);
		    if (sf.isGenAllFiles()) Imgcodecs.imwrite("Co_"+fileName, brightColorImage);
		    
		    
		    Imgproc.cvtColor(result, gray, Imgproc.COLOR_RGB2GRAY);
		    Mat brightGray = new Mat();
		    gray.convertTo(brightGray, -1,1.17,5);
		    if (sf.isGenAllFiles())Imgcodecs.imwrite("Gr_"+fileName, brightGray);
		    
		    Mat blackAndWhiteImage=new Mat();
		   Imgproc.adaptiveThreshold(gray, blackAndWhiteImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 4);
			if (sf.isGenAllFiles()) Imgcodecs.imwrite("Ba_"+fileName, blackAndWhiteImage);
		    if (scanType.equals(ScanType.GRAY)){
		    	 File fi = new File("Gr_"+fileName);
				   sf.setFile_byte_post_scan(Files.readAllBytes(fi.toPath()));
				   sf.setScanStaus(true);
		    }else if (scanType.equals(ScanType.COLOR)){
		    	 File fi = new File("Co_"+fileName);
				   sf.setFile_byte_post_scan(Files.readAllBytes(fi.toPath()));
				   sf.setScanStaus(true);
		    }else{
		    	  File fi = new File("Ba_"+fileName);
		    	   sf.setFile_byte_post_scan( Files.readAllBytes(fi.toPath()));
		    	   sf.setScanStaus(true);
		    }
		    sf.setScanStaus(true);
			sf.setFile_byte_pre_scan(null);
	return sf;
	
}

public ScanFile cropImage3(String[] arrayOfCordinates,ScanFile sf,String fileName, ScanType scanType) throws Exception{
	loadOpenCV();
	  Mat originalImage=null;
	  Mat encoded = new Mat(1, sf.getFile_byte_pre_scan().length, CvType.CV_8S);
	  encoded.put(0, 0, sf.getFile_byte_pre_scan());
	  originalImage = Imgcodecs.imdecode(encoded, Imgcodecs.CV_LOAD_IMAGE_COLOR);
	   if (sf.isGenAllFiles())Imgcodecs.imwrite("Or_"+fileName, originalImage);
	  
	  
		
			
	   double imageSize = (Math.round(sf.getFile_byte_pre_scan().length*100/1024/1024)/100.0d) ;
			originalWidth = originalImage.width();
			originalHeight = originalImage.height();

			double newHeight=Double.valueOf(sf.getArrayOfCordinates()[3]) - Double.valueOf(sf.getArrayOfCordinates()[1]);
			double newWidth=Double.valueOf(sf.getArrayOfCordinates()[6]) - Double.valueOf(sf.getArrayOfCordinates()[0]);
			    Point p1 = new Point(Double.valueOf(arrayOfCordinates[0]), Double.valueOf(arrayOfCordinates[1]));
			    
			    
			    Point p2 = new Point(Double.valueOf(arrayOfCordinates[2]), Double.valueOf(arrayOfCordinates[1]));
			  
	
			
			    
			    Point p3 = new Point(Double.valueOf(arrayOfCordinates[0]), Double.valueOf(arrayOfCordinates[3]));
			 
			    Point p4 = new Point(Double.valueOf(arrayOfCordinates[2]), Double.valueOf(arrayOfCordinates[3]));
			    List<Point> source = new ArrayList<Point>();
			    source.add(p1);
			    source.add(p3);
			    source.add(p4);
			    source.add(p2);
			   
			   
			    Mat startM = Converters.vector_Point2f_to_Mat(source);
			    Mat color=warp(originalImage,startM,newHeight,newWidth);
			    
			    Mat gray = new Mat();
				  Imgproc.cvtColor(color, gray, Imgproc.COLOR_RGB2GRAY);
				  Mat brightGray = new Mat();
				  color.convertTo(brightGray, -1,1.17,5);
				 Imgcodecs.imwrite("Gr_"+fileName, brightGray);
			
				    Mat brightColorImage = new Mat();
			    color.convertTo(brightColorImage, -1,1.17,5);
			  Imgcodecs.imwrite("Co_"+fileName, brightColorImage);
			  
			    
			  
				    Mat blackAndWhiteImage = new Mat();
				   Imgproc.adaptiveThreshold(gray, blackAndWhiteImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 7);
				   Imgcodecs.imwrite("Ba111_"+fileName, blackAndWhiteImage);
				   Imgproc.threshold(blackAndWhiteImage,blackAndWhiteImage,150,255,Imgproc.THRESH_TOZERO);
				   Imgcodecs.imwrite("Ba22_"+fileName, blackAndWhiteImage);
				 Imgcodecs.imwrite("Ba_"+fileName, blackAndWhiteImage);				    
				 
				    if (scanType.equals(ScanType.GRAY)){
				    	 File fi = new File("Gr_"+fileName);
						   sf.setFile_byte_post_scan(Files.readAllBytes(fi.toPath()));
						   sf.setScanStaus(true);
				    }else if (scanType.equals(ScanType.COLOR)){
				    	 File fi = new File("Co_"+fileName);
						   sf.setFile_byte_post_scan(Files.readAllBytes(fi.toPath()));
						   sf.setScanStaus(true);
				    }else{
				    	  File fi = new File("Ba__"+fileName);
				    	   sf.setFile_byte_post_scan( Files.readAllBytes(fi.toPath()));
				    	   sf.setScanStaus(true);
				    }	
			    
	return sf;
	
}

public ScanFile cropImage2(ScanFile sf) throws Exception{

	  loadOpenCV();
	  Mat originalImage=null;
	  String fileName = sf.getFileName()+"."+sf.getFileType();
	  Mat encoded = new Mat(1, sf.getFile_byte_pre_scan().length, CvType.CV_8S);
	  encoded.put(0, 0, sf.getFile_byte_pre_scan());
	  originalImage = Imgcodecs.imdecode(encoded, Imgcodecs.CV_LOAD_IMAGE_COLOR);
	   double imageSize = (Math.round(sf.getFile_byte_pre_scan().length*100/1024/1024)/100.0d) ;
			originalWidth = originalImage.width();
			originalHeight = originalImage.height();

			double	newHeight=Double.valueOf(sf.getArrayOfCordinates()[3]) - Double.valueOf(sf.getArrayOfCordinates()[1]);
			double	newWidth=Double.valueOf(sf.getArrayOfCordinates()[6]) - Double.valueOf(sf.getArrayOfCordinates()[0]);
			   Point p1 = new Point(Double.valueOf(sf.getArrayOfCordinates()[0]), Double.valueOf(sf.getArrayOfCordinates()[1]));

			    
			    
			    Point p2 = new Point(Double.valueOf(sf.getArrayOfCordinates()[2]), Double.valueOf(sf.getArrayOfCordinates()[3]));

			    
			    Point p3 = new Point(Double.valueOf(sf.getArrayOfCordinates()[4]), Double.valueOf(sf.getArrayOfCordinates()[5]));
			 
			    Point p4 = new Point(Double.valueOf(sf.getArrayOfCordinates()[6]), Double.valueOf(sf.getArrayOfCordinates()[7]));
			    List<Point> source = new ArrayList<Point>();
			    source.add(p1);
			    source.add(p2);
			    source.add(p3);
			    source.add(p4);
			    Mat startM = Converters.vector_Point2f_to_Mat(source);
			    Mat color=warp(originalImage,startM, newHeight, newWidth);
			  		
			    if (sf.getScanType().equals(ScanType.NATURAL)){
				    Mat gray = new Mat();
					  String outputFileName = "Na_"+fileName;
					Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, gray);
					  MatOfByte outBuffer = new MatOfByte();
					     Imgcodecs.imencode("."+sf.getFileType(), gray, outBuffer);
				   sf.setFile_byte_post_scan(outBuffer.toArray());
					   sf.setScanStaus(true);
					   sf.setOutputFileName(outputFileName);
			    }
			    else if (sf.getScanType().equals(ScanType.GRAY)){
					    Mat gray = new Mat();
						  Imgproc.cvtColor(color, gray, Imgproc.COLOR_RGB2GRAY);
						  String outputFileName = "Gr_"+fileName;
						 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, gray);
						  MatOfByte outBuffer = new MatOfByte();
						     Imgcodecs.imencode("."+sf.getFileType(), gray, outBuffer);
					   sf.setFile_byte_post_scan(outBuffer.toArray());
						   sf.setScanStaus(true);
						   sf.setOutputFileName(outputFileName);
				    }else if (sf.getScanType().equals(ScanType.COLOR)){
					    Mat brightColorImage = new Mat(color.rows(),color.cols(),color.type());
					    double minGray=10;
					    double maxGray=0;
					    Mat gray = new Mat();
					    Imgproc.cvtColor(color, gray, Imgproc.COLOR_RGB2GRAY);
					    Imgcodecs.imwrite("/Users/amitkiswani/DCSSWorkspace/eclipse_IF20170505/workspace/Scanner/images/ScanBulkImages/"+"min.png", gray);
					    MinMaxLocResult result =   Core.minMaxLoc(gray);
					   double alpha = 1.5;
					   double beta = 30;
					   double clipPercent = 10; 
					    minGray = Math.round(minGray * (1 + clipPercent/100.0) );
					   double inputRange =  Core.mean(gray).val[0];;
					  
					    color.convertTo(brightColorImage, -1, alpha, beta);
				    Imgproc.threshold(brightColorImage,brightColorImage,60,255,Imgproc.THRESH_TOZERO);
				    String outputFileName = "Co_"+fileName;
				   Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, brightColorImage);
				    MatOfByte outBuffer = new MatOfByte();
				     Imgcodecs.imencode("."+sf.getFileType(), brightColorImage, outBuffer);
						   sf.setFile_byte_post_scan(outBuffer.toArray());
						   sf.setScanStaus(true);
						   sf.setOutputFileName(outputFileName);
				    }else{
					    Mat gray = new Mat();
						  Imgproc.cvtColor(color, gray, Imgproc.COLOR_RGB2GRAY);
					    Mat blackAndWhiteImage = new Mat();
						   Imgproc.adaptiveThreshold(gray, blackAndWhiteImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 7);

						   
						    MatOfByte outBuffer = new MatOfByte();
						     Imgcodecs.imencode("."+sf.getFileType(), blackAndWhiteImage, outBuffer);
								   sf.setFile_byte_post_scan(outBuffer.toArray());
				    	   sf.setScanStaus(true);
				    	   String outputFileName = "Ba_"+fileName;
							  Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName		, blackAndWhiteImage);
						    sf.setOutputFileName(outputFileName);
				    }
				 
			    
	return sf;
	

}

public ScanFile cropImage1(ScanFile sf) throws Exception{

	  loadOpenCV();
	  double pixel =  (sf.getOriHeight() * sf.getOriWidth())/1000000;
	  Mat originalImage=null;
	  String fileName = sf.getFileName()+"."+sf.getFileType();
	  Mat encoded = new Mat(1, sf.getFile_byte_pre_scan().length, CvType.CV_8S);
	  encoded.put(0, 0, sf.getFile_byte_pre_scan());
	  originalImage = Imgcodecs.imdecode(encoded, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			
			double newHeight=Double.valueOf(sf.getArrayOfCordinates()[3]) - Double.valueOf(sf.getArrayOfCordinates()[1]);
			double newWidth=Double.valueOf(sf.getArrayOfCordinates()[6]) - Double.valueOf(sf.getArrayOfCordinates()[0]);
			   Point p1 = new Point(Double.valueOf(sf.getArrayOfCordinates()[0]), Double.valueOf(sf.getArrayOfCordinates()[1]));
			    Point p2 = new Point(Double.valueOf(sf.getArrayOfCordinates()[2]), Double.valueOf(sf.getArrayOfCordinates()[3]));
			    Point p3 = new Point(Double.valueOf(sf.getArrayOfCordinates()[4]), Double.valueOf(sf.getArrayOfCordinates()[5]));
			    Point p4 = new Point(Double.valueOf(sf.getArrayOfCordinates()[6]), Double.valueOf(sf.getArrayOfCordinates()[7]));
			    
			   
			    List<Point> source = new ArrayList<Point>();
			    source.add(p1);
			    source.add(p2);
			    source.add(p3);
			    source.add(p4);
			    Mat startM = Converters.vector_Point2f_to_Mat(source);
			    Mat color=warp(originalImage,startM, newHeight, newWidth);
			    
			    
			    if (sf.getScanType().equals(ScanType.NATURAL) || sf.isGenAllColor()){
					  String outputFileName = "Na_"+fileName;
					 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, color);
					  MatOfByte outBuffer = new MatOfByte();
					     Imgcodecs.imencode("."+sf.getFileType(), color, outBuffer);
				   sf.setFile_byte_post_scan(outBuffer.toArray());
					   sf.setScanStaus(true);
					   sf.setOutputFileName(outputFileName);
			    }
			     if (sf.getScanType().equals(ScanType.GRAY) || sf.isGenAllFiles()){
					    Mat gray = new Mat();
						  Imgproc.cvtColor(color, gray, Imgproc.COLOR_RGB2GRAY);
						  String outputFileName = "Gr_"+fileName;
						 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, gray);
						  MatOfByte outBuffer = new MatOfByte();
						     Imgcodecs.imencode("."+sf.getFileType(), gray, outBuffer);
					   sf.setFile_byte_post_scan(outBuffer.toArray());
						   sf.setScanStaus(true);
						   sf.setOutputFileName(outputFileName);
				    } 
			     if (sf.getScanType().equals(ScanType.COLOR) || sf.isGenAllColor()){
					    Mat brightColorImage = new Mat(color.rows(),color.cols(),color.type());

					
					    Mat hsv = new Mat();
					    Imgproc.cvtColor(color,hsv, Imgproc.COLOR_BGR2HSV);
					    List<Mat> channels = new ArrayList<Mat>(3);
					    Core.split(hsv, channels);
					    Scalar s =  Core.mean(channels.get(2));
					    double mean =  s.val[0];
					    double alpha =1.0;
					    double beta=50.0;
					    double thresh =150;
					    if (pixel > 3.5 && mean > 180)  {
					    brightColorImage = color;
					    }else if (pixel > 3.5 && mean > 130 && mean < 180){
					    	alpha=1.2;
					    	  color.convertTo(brightColorImage, -1, alpha, beta);
					    }else if (pixel > 3.5 && mean < 130){
					    	alpha=1.4;
					    	 thresh=50;
					    	  color.convertTo(brightColorImage, -1, alpha, beta);
					    	  Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
					    }else if (pixel <= 3.5 && mean > 180){
						   thresh=130;
						   alpha=1.2;
						   brightColorImage = color;
						   Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
					   }
					   else if (pixel <= 3.5 && mean > 130 && mean < 180){
						   thresh=150;
							alpha=1.4;
					    	color.convertTo(brightColorImage, -1, alpha, beta);
						   Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
					   }
					   else {
						   thresh=170;
						   Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
					   }
					   
					 

				    String outputFileName = "Co_"+fileName;
				  Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, brightColorImage);
				    MatOfByte outBuffer = new MatOfByte();
				     Imgcodecs.imencode("."+sf.getFileType(), brightColorImage, outBuffer);
						   sf.setFile_byte_post_scan(outBuffer.toArray());
						   sf.setScanStaus(true);
						   sf.setOutputFileName(outputFileName);
				    }
			     if (sf.getScanType().equals(ScanType.BLACKWHITE) || sf.isGenAllColor()){
					    Mat gray = new Mat();
						  Imgproc.cvtColor(color, gray, Imgproc.COLOR_RGB2GRAY);
					
					    Mat blackAndWhiteImage = new Mat();
					    Imgproc.adaptiveThreshold(gray, blackAndWhiteImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 63, 20);
						 
					    MatOfByte outBuffer = new MatOfByte();
						     Imgcodecs.imencode("."+sf.getFileType(), blackAndWhiteImage, outBuffer);
								   sf.setFile_byte_post_scan(outBuffer.toArray());
				    	   sf.setScanStaus(true);
				    	   String outputFileName = "Ba_"+fileName;
							  Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName		, blackAndWhiteImage);
						    sf.setOutputFileName(outputFileName);
				    }
				  


	return sf;
	

}

public ScanFile cropImage(ScanFile sf) throws Exception{
	double maxBlackAndWhiteGrayFileSize = 1.5;
	double maxGrayFileSize = 1.0;
	double maxColorFileSize=5.0;

	  loadOpenCV();
	  double pixel =  (sf.getOriHeight() * sf.getOriWidth())/1000000;
	  Mat originalImage=null;
	  String fileName = sf.getFileName()+"."+sf.getFileType();
	  Mat encoded = new Mat(1, sf.getFile_byte_pre_scan().length, CvType.CV_8S);
	  encoded.put(0, 0, sf.getFile_byte_pre_scan());
	  originalImage = Imgcodecs.imdecode(encoded, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			
			sf.setScannedHeight(Double.valueOf(sf.getArrayOfCordinates()[3]) - Double.valueOf(sf.getArrayOfCordinates()[1]));
			sf.setScannedWidth(Double.valueOf(sf.getArrayOfCordinates()[6]) - Double.valueOf(sf.getArrayOfCordinates()[0]));
			  
			
			   Point p1 = new Point(Double.valueOf(sf.getArrayOfCordinates()[0]), Double.valueOf(sf.getArrayOfCordinates()[1]));
			    Point p2 = new Point(Double.valueOf(sf.getArrayOfCordinates()[2]), Double.valueOf(sf.getArrayOfCordinates()[3]));
			    Point p3 = new Point(Double.valueOf(sf.getArrayOfCordinates()[4]), Double.valueOf(sf.getArrayOfCordinates()[5]));
			    Point p4 = new Point(Double.valueOf(sf.getArrayOfCordinates()[6]), Double.valueOf(sf.getArrayOfCordinates()[7]));
			    
			   
			    List<Point> source = new ArrayList<Point>();
			    source.add(p1);
			    source.add(p2);
			    source.add(p3);
			    source.add(p4);
			    Mat startM = Converters.vector_Point2f_to_Mat(source);
			    Mat originalAfterCrop=warp(originalImage,startM, sf.getScannedHeight(), sf.getScannedWidth());
			    
			    for (ScanType scantype : sf.getScanFileType()){
			    
			    	
			    	
			    if (scantype.equals(ScanType.NATURAL)){
			    	
					  String outputFileName = "Na_"+fileName;
					 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, originalAfterCrop);
					
					 Mat resized = new Mat();
					 File file = new File(sf.getFilePath()+"/"+outputFileName);
					 double fileSize = file.length() /1048576.00;
					 if (fileSize > maxColorFileSize){
						 double ratio = fileSize/maxColorFileSize;
						 Imgproc.resize(originalAfterCrop, resized, new Size(sf.getScannedWidth()/ratio,sf.getScannedHeight()/ratio));
						 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, resized);
					 }	
				   
					 Thumnail t = new Thumnail();
				   t.setOriginalFileName(outputFileName);
				   sf.setFileSizeAftScan(file.length());
				  if ( createThumnailThroughtMAT(originalAfterCrop, t, sf))
					   {
						   sf.setScanStaus(true);
					   sf.getThumnails().add(t);
					   }
					   else {
					    sf.setScanStaus(false);
					    return sf;
					   }
					   sf.setOutputFileName(outputFileName);
			    }
			     if (scantype.equals(ScanType.GRAY)){
					    Mat gray = new Mat();
						  Imgproc.cvtColor(originalAfterCrop, gray, Imgproc.COLOR_RGB2GRAY);

						  String outputFileName = "Gr_"+fileName;
						  gray.convertTo(gray, -1, 1.2, 15.0);
							Mat element = getKernelFromShape(0, 0);
							Imgproc.erode(gray,gray, element);
						 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, gray);
						 
						 Mat resized = new Mat();
						 File file = new File(sf.getFilePath()+"/"+outputFileName);
						 double fileSize = file.length() /1048576.00;
						 if (fileSize > maxGrayFileSize){
							 double ratio = fileSize/maxGrayFileSize;
							 Imgproc.resize(gray, resized, new Size(sf.getScannedWidth()/ratio,sf.getScannedHeight()/ratio));
							 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, resized);
						 }	
					   Thumnail t = new Thumnail();
					   t.setOriginalFileName(outputFileName);
					   sf.setFileSizeAftScan(file.length());
					  if ( createThumnailThroughtMAT(gray, t, sf))
					  {sf.setScanStaus(true);
					   sf.getThumnails().add(t);
					   }else {
						   sf.setScanStaus(false);
						   return sf;
						   }
						   sf.setOutputFileName(outputFileName);
				    } 
			     if (scantype.equals(ScanType.COLOR)){
					    Mat brightColorImage = new Mat();

					
					    Mat hsv = new Mat();
					    Imgproc.cvtColor(originalAfterCrop,hsv, Imgproc.COLOR_BGR2HSV);
					    List<Mat> channels = new ArrayList<Mat>(3);
					    Core.split(hsv, channels);
					    Scalar s =  Core.mean(channels.get(2));
					    double mean =  s.val[0];
					    double alpha =1.0;
					    double beta=50.0;
					    double thresh =150;
					    if (mean < 130){
					    	alpha=1.4;
					    	 thresh=50;
					    	 originalAfterCrop.convertTo(brightColorImage, -1, alpha, beta);
					    	  Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
					    }
					    else if (mean > 130 && mean < 180){
							   thresh=50;
								alpha=1.3;
								originalAfterCrop.convertTo(brightColorImage, -1, alpha, beta);
							   Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
						   }
					    
					    else if (mean > 180){
							   thresh=50;
							   alpha=1.1;
							   brightColorImage = originalAfterCrop.clone();
							   originalAfterCrop.convertTo(brightColorImage, -1, alpha, beta);
							   Imgproc.threshold(brightColorImage,brightColorImage,thresh,255,Imgproc.THRESH_TOZERO);
						   }
					 
						Mat element = getKernelFromShape(1, 1);
						Imgproc.erode(brightColorImage,brightColorImage, element);
						
				    String outputFileName = "Co_"+fileName;
				  Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, brightColorImage);
				  
				  Mat resized = new Mat();
					 File file = new File(sf.getFilePath()+"/"+outputFileName);
					 double fileSize = file.length() /1048576.00;
					 if (fileSize > maxColorFileSize){
						 double ratio = fileSize/maxColorFileSize;
						 Imgproc.resize(brightColorImage, resized, new Size(sf.getScannedWidth()/ratio,sf.getScannedHeight()/ratio));
						 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, resized);
					 }	

						   Thumnail t = new Thumnail();
						   t.setOriginalFileName(outputFileName);
						   sf.setFileSizeAftScan(file.length());
						  if ( createThumnailThroughtMAT(brightColorImage, t, sf))
						  {sf.setScanStaus(true);
						   sf.getThumnails().add(t);
						   }else {
							   sf.setScanStaus(false);
							   return sf;
						   }
						   sf.setOutputFileName(outputFileName);
				    }
			     if (scantype.equals(ScanType.BLACKWHITE)){
					    Mat gray = new Mat();
						  Imgproc.cvtColor(originalAfterCrop, gray, Imgproc.COLOR_RGB2GRAY);
						  String outputFileName = "Ba_"+fileName;
					    Mat blackAndWhiteImage = new Mat();
					    Imgproc.adaptiveThreshold(gray, blackAndWhiteImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 251, 12);
					   
					    Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName		, blackAndWhiteImage);
					    
					    Mat resized = new Mat();
						 File file = new File(sf.getFilePath()+"/"+outputFileName);
						 double fileSize = file.length() /1048576.00;
						 if (fileSize > maxBlackAndWhiteGrayFileSize){
							 double ratio = fileSize/maxBlackAndWhiteGrayFileSize;
							 Imgproc.resize(blackAndWhiteImage, resized, new Size(sf.getScannedWidth()/ratio,sf.getScannedHeight()/ratio));
							 Imgcodecs.imwrite(sf.getFilePath()+"/"+outputFileName, resized);
						 }	
					    
								   Thumnail t = new Thumnail();
								   t.setOriginalFileName(outputFileName);
								   sf.setFileSizeAftScan(file.length());
								  if ( createThumnailThroughtMAT(blackAndWhiteImage, t, sf))
								  {sf.setScanStaus(true);
								   sf.getThumnails().add(t);
								   }else {
									   sf.setScanStaus(false);
									   return sf;
								   }
						    sf.setOutputFileName(outputFileName);
				    }
				  
			    }

	return sf;
	

}

public  Mat warp(Mat inputMat,Mat startM, double newHeight, double newWidth) {
	
	    Mat outputMat = new Mat((int)newWidth, (int)newHeight, CvType.CV_8UC4);
	    Point ocvPOut1 = new Point(0, 0);
	    Point ocvPOut2 = new Point(0, newHeight);
	    Point ocvPOut3 = new Point(newWidth, newHeight);
	    Point ocvPOut4 = new Point(newWidth, 0);
	    List<Point> dest = new ArrayList<Point>();
	    dest.add(ocvPOut1);
	    dest.add(ocvPOut2);
	    dest.add(ocvPOut3);
	    dest.add(ocvPOut4);
	    Mat endM = Converters.vector_Point2f_to_Mat(dest);      

	    Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

	    Imgproc.warpPerspective(inputMat, 
	                            outputMat,
	                            perspectiveTransform,
	                            new Size(newWidth, newHeight), 
	                            Imgproc.INTER_CUBIC);
	    
	    return outputMat;
	}


public byte[] MatToBytes(Mat mat){
	   int elemSize = (int)mat.elemSize(); 
	    int cols = mat.cols();  
       int rows = mat.rows();
       byte[] data = new byte[cols * rows * elemSize];  
       mat.get(0, 0, data); 
       return data;
};


public  void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


public static boolean createThumnail(byte [] bytes,Thumnail t, ScanFile sf) throws IOException{
	t.setHeight(150);
	t.setWidth(100);
	
	if (  sf.getScannedWidth() > sf.getScannedHeight()){
		t.setHeight(100);
		t.setWidth(150);
	}

	BufferedImage orignial = ImageIO.read(new ByteArrayInputStream(bytes));
	Image scaledImg =  orignial.getScaledInstance(t.getWidth(), t.getHeight(), BufferedImage.SCALE_SMOOTH);
	BufferedImage thumbnail = new BufferedImage(t.getWidth(), t.getHeight(), BufferedImage.TYPE_INT_RGB);
	thumbnail.createGraphics().drawImage(scaledImg,0,0,null);
	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 baos.flush();
		ImageIO.write( thumbnail, "png", baos );
		baos.flush();
		byte[] thumnail = baos.toByteArray();
		baos.close();
		if (sf.isGenAllFiles())new FileOutputStream("temp/thum_"+t.getOriginalFileName()).write(thumnail);
		t.setThumnail_content("data:image/png;base64," +DatatypeConverter.printBase64Binary(thumnail));
		return true;
}

public static boolean createThumnailThroughtMAT(Mat mat,Thumnail t, ScanFile sf) throws IOException{
	Mat thumnail = new Mat();
	t.setHeight(150);
	t.setWidth(100);
	
	if (  sf.getScannedWidth() > sf.getScannedHeight()){
		t.setHeight(100);
		t.setWidth(150);
	}
	
	 Imgproc.resize( mat,thumnail, new Size(t.getWidth(),t.getHeight()));
	 MatOfByte outBuffer = new MatOfByte();
		Imgcodecs.imencode("."+sf.getFileType(), thumnail, outBuffer);
		t.setThumnail_content("data:image/png;base64," +DatatypeConverter.printBase64Binary(outBuffer.toArray()));
		return true;
}

private static Mat getKernelFromShape(int elementSize, int elementShape) { 
	return Imgproc.getStructuringElement(elementShape, new Size(elementSize*2+1, elementSize*2+1), new Point(elementSize, elementSize) );
		}

public  Mat equalizeHist (Mat src){
    Mat crcb = new Mat();
    Mat output = new Mat();
    Imgproc.cvtColor(src,crcb, Imgproc.COLOR_BGR2YCrCb);
    List<Mat> channels = new ArrayList<Mat>(3);
    Core.split(crcb, channels);
    Imgproc.equalizeHist(channels.get(0), channels.get(0));
    Core.merge(channels, crcb);
     Imgproc.cvtColor(crcb, output, Imgproc.COLOR_YCrCb2BGR);
     return output;
	}


}


