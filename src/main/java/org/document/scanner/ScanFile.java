package org.document.scanner;

import java.util.ArrayList;



public class ScanFile {

	private  boolean scanStaus = false;
	private byte [] file_byte_pre_scan;
	private byte [] file_byte_post_scan;
	private String fileName;
	private ScanFileType fileType;
	private ScanType scanType;
	private String[] arrayOfCordinates;
	private String filePath;
	private String dataUrl;
	private String outputFileName;
	private int OriHeight=0; 
	private int OriWidth=0;
	private double scannedHeight=0; 
	private double scannedWidth=0;
	private double fileSizeAftScan=0; 
	private boolean genAllFiles=false;
	private boolean genAllColor=false;
	private ArrayList<ScanType> scanFileType;
	private ArrayList<Thumnail> thumnails = new ArrayList<Thumnail>();
	
	
	
	
	public boolean isScanStaus() {
		return scanStaus;
	}
	public void setScanStaus(boolean scanStaus) {
		this.scanStaus = scanStaus;
	}
	public byte[] getFile_byte_pre_scan() {
		return file_byte_pre_scan;
	}
	public void setFile_byte_pre_scan(byte[] file_byte_pre_scan) {
		this.file_byte_pre_scan = file_byte_pre_scan;
	}
	public byte[] getFile_byte_post_scan() {
		return file_byte_post_scan;
	}
	public void setFile_byte_post_scan(byte[] file_byte_post_scan) {
		this.file_byte_post_scan = file_byte_post_scan;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public ScanFileType getFileType() {
		return fileType;
	}
	public void setFileType(ScanFileType fileType) {
		this.fileType = fileType;
	}
	public ScanType getScanType() {
		return scanType;
	}
	public void setScanType(ScanType scanType) {
		this.scanType = scanType;
	}
	public String[] getArrayOfCordinates() {
		return arrayOfCordinates;
	}
	public void setArrayOfCordinates(String[] arrayOfCordinates) {
		this.arrayOfCordinates = arrayOfCordinates;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getDataUrl() {
		return dataUrl;
	}
	public void setDataUrl(String data) {
		this.dataUrl = data;
	}
	public String getOutputFileName() {
		return outputFileName;
	}
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	public int getOriHeight() {
		return OriHeight;
	}
	public void setOriHeight(int oriHeight) {
		OriHeight = oriHeight;
	}
	public int getOriWidth() {
		return OriWidth;
	}
	public void setOriWidth(int oriWidth) {
		OriWidth = oriWidth;
	}
	public boolean isGenAllFiles() {
		return genAllFiles;
	}
	public void setGenAllFiles(boolean genAllFiles) {
		this.genAllFiles = genAllFiles;
	}
	public boolean isGenAllColor() {
		return genAllColor;
	}
	public void setGenAllColor(boolean genAllColor) {
		this.genAllColor = genAllColor;
	}
	public ArrayList<Thumnail> getThumnails() {
		return thumnails;
	}
	public void setThumnails(ArrayList<Thumnail> thumnails) {
		this.thumnails = thumnails;
	}
	public double getScannedHeight() {
		return scannedHeight;
	}
	public void setScannedHeight(double scannedHeight) {
		this.scannedHeight = scannedHeight;
	}
	public double getScannedWidth() {
		return scannedWidth;
	}
	public void setScannedWidth(double scannedWidth) {
		this.scannedWidth = scannedWidth;
	}
	public double getFileSizeAftScan() {
		return fileSizeAftScan;
	}
	public void setFileSizeAftScan(double fileSizeAftScan) {
		this.fileSizeAftScan = fileSizeAftScan;
	}
	public ArrayList<ScanType> getScanFileType() {
		return scanFileType;
	}
	public void setScanFileType(ArrayList<ScanType> scanFileType) {
		this.scanFileType = scanFileType;
	}

	
	

}
