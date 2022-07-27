package org.document.scanner;

public class Thumnail {
	
	private String originalFileName;
	private String thumnail_content;
	private int height=0; 
	private int width=0;
	public String getOriginalFileName() {
		return originalFileName;
	}
	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}
	public String getThumnail_content() {
		return thumnail_content;
	}
	public void setThumnail_content(String thumnail_content) {
		this.thumnail_content = thumnail_content;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}

	
	//When generating to string for thumnail make sure dont print thumnail content instead use this thumnail_content= " + thumnail_content.substring(0,20)+"..."
	@Override
	public String toString() {
		return "Thumnail ["
				+ (originalFileName != null ? "originalFileName="
						+ originalFileName + ", " : "")
				+ (thumnail_content != null ? "thumnail_content= " + thumnail_content.substring(0,20)+"..." + ", " : "") + "height=" + height
				+ ", width=" + width + "]";
	}
	

}
