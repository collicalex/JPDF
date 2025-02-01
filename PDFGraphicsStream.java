package lib.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PDFGraphicsStream extends PDFFileOutputStream {
	
	private double _width;
	private double _height;
	
	private DecimalFormat _df;
	
	public PDFGraphicsStream(File file, int width, int height) throws FileNotFoundException {
		super(file);
		_width = width;
		_height = height;
		
		DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.getDefault());
		decimalSymbol.setDecimalSeparator('.');
		_df = new DecimalFormat("#.###");
		_df.setDecimalFormatSymbols(decimalSymbol);
		_df.setGroupingUsed(false);
	}

	public PDFGraphicsStream(File file, double width, double height) throws FileNotFoundException {
		super(file);
		//_width = width;
		_height = height;
		
		DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.getDefault());
		decimalSymbol.setDecimalSeparator('.');
		_df = new DecimalFormat("#.###");
		_df.setDecimalFormatSymbols(decimalSymbol);
		_df.setGroupingUsed(false);
	}
	
	//-------------------------------------------------------------------------
	
	public double getHeight() {
		return _height;
	}
	
	public double getWidth() {
		return _width;
	}
	
	//-------------------------------------------------------------------------	
	
	public String num2str(int num) {
		return "" + num;
	}
	
	
	public String num2str(double num) {
		return _df.format(num);
	}
	
	private String mat2str(int[] mat) {
		if (mat == null) {
			return "";
		} else {
			String res = "";
			for (int i = 0; i < mat.length; ++i) {
				res += num2str(mat[i]) + " ";
			}
			return res.trim();
		}
	}

	private String mat2str(float[] mat) {
		if (mat == null) {
			return "";
		} else {
			String res = "";
			for (int i = 0; i < mat.length; ++i) {
				res += num2str(mat[i]) + " ";
			}
			return res.trim();
		}
	}
	
	private String mat2str(double[] mat) {
		if (mat == null) {
			return "";
		} else {
			String res = "";
			for (int i = 0; i < mat.length; ++i) {
				res += num2str(mat[i]) + " ";
			}
			return res.trim();
		}
	}
	
	//-------------------------------------------------------------------------
	
	private String getX(int x) {
		return num2str(x);
	}
	
	private String getY(int y) {
		return num2str(_height - y);
	}	
	
	private String getX(double x) {
		return num2str(x);
	}
	
	private String getY(double y) {
		return num2str(_height - y);
	}
	
	//-------------------------------------------------------------------------

	protected void writeln(String str) {
		try {
			super.writeln(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	public void moveTo(int x, int y) {
		this.writeln(getX(x) + " " + getY(y) + " m");
	}
	
	public void moveTo(double x, double y) {
		this.writeln(getX(x) + " " + getY(y) + " m");
	}
	
	//-------------------------------------------------------------------------
	
	public void lineTo(int x, int y) {
		this.writeln(getX(x) + " " +  getY(y) + " l");
	}
	
	public void lineTo(double x, double y) {
		this.writeln(getX(x) + " " +  getY(y) + " l");
	}
	
	//-------------------------------------------------------------------------
	
	public void rectangle(int x, int y, int width, int height) {
		//be careful to the mirror symmetry of the rectangle into the pdf space coordinates
		this.writeln(getX(x) + " " + getY(y + height) + " " + width + " " + height + " re"); //it's equivalent to call, moveTo(x, y) then 3 times lineTo(...) and closePath()
	}
	
	public void rectangle(double x, double y, double width, double height) {
		//be careful to the mirror symmetry of the rectangle into the pdf space coordinates
		this.writeln(getX(x) + " " + getY(y + height) + " " + width + " " + height + " re"); //it's equivalent to call, moveTo(x, y) then 3 times lineTo(...) and closePath()
	}
	
	//-------------------------------------------------------------------------

	public void curveTo(int x1, int y1, int x2, int y2) {
		this.writeln(getX(x1) + " " + getY(y1) + " " + getX(x2) + " " + getY(y2) +" v");
	}	
	
	public void curveTo(double x1, double y1, double x2, double y2) {
		this.writeln(getX(x1) + " " + getY(y1) + " " + getX(x2) + " " + getY(y2) +" v");
	}
	
	//-------------------------------------------------------------------------
	
	public void curveTo(int x1, int y1, int x2, int y2, int x3, int y3) {
		this.writeln(getX(x1) + " " + getY(y1) + " " + getX(x2) + " " + getY(y2) + " " + getX(x3) + " " + getY(y3) + " c");
	}	
	
	public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
		this.writeln(getX(x1) + " " + getY(y1) + " " + getX(x2) + " " + getY(y2) + " " + getX(x3) + " " + getY(y3) + " c");
	}
	
	//-------------------------------------------------------------------------
	
	private double tc = 0; //character spacing 0 default
	private double tw = 0; //word spacing 0 default
	private int tz = 100; //horizontal scalling 100 default
	
	public void drawString(String str, int x, int y, int fontId, int fontSize) {
		//this.saveGraphicsState();
		this.writeln("BT");
		this.writeln(" /F" + fontId + " " + num2str(fontSize) + " Tf");
		this.writeln(" " + getX(x) + " " + getY(y) + " Td");
		
		this.writeln(" " + num2str(tc) + " Tc");
		this.writeln(" " + num2str(tw) + " Tw");
		this.writeln(" " + num2str(tz) + " Tz");
		
		this.writeln(" " + makePDFString(str) + " Tj");
		this.writeln("ET");
		//this.restoresGraphicState();
	}	
	
	public void drawString(String str, double x, double y, int fontId, double fontSize) {
		//this.saveGraphicsState();
		this.writeln("BT");
		this.writeln(" /F" + fontId + " " + num2str(fontSize) + " Tf");
		this.writeln(" " + getX(x) + " " + getY(y) + " Td");
		
		this.writeln(" " + num2str(tc) + " Tc");
		this.writeln(" " + num2str(tw) + " Tw");
		this.writeln(" " + num2str(tz) + " Tz");		
		
		this.writeln(" " + makePDFString(str) + " Tj");
		this.writeln("ET");
		//this.restoresGraphicState();
	}
	
	
	public void drawString(String str, double[] transform, int fontId, int fontSize) {
		//this.saveGraphicsState();
		this.writeln("BT");
		this.writeln(" /F" + fontId + " " + num2str(fontSize) + " Tf");
		this.writeln(" " + mat2str(transform) + " Tm");
		
		this.writeln(" " + num2str(tc) + " Tc");
		this.writeln(" " + num2str(tw) + " Tw");
		this.writeln(" " + num2str(tz) + " Tz");
		
		this.writeln(" " + makePDFString(str) + " Tj");
		this.writeln("ET");
		//this.restoresGraphicState();
	}	
	
	//-------------------------------------------------------------------------
	
	public void drawImage(int x, int y, int width, int height, int imageId) {
		this.saveGraphicsState();
		// Move image to correct position and scale it to (width, height)
		//be careful to the mirror symmetry of the rectangle into the pdf space coordinates
		this.writeln(num2str(width) + " 0 0 " + num2str(height) + " " + getX(x) + " " + getY(y + height) + " cm");
		//150 0 0 80 0 0 cm% Scale
		this.writeln("/Im" + imageId + " Do");
		this.restoresGraphicState();
	}
	
	public void drawImage(double x, double y, double width, double height, int imageId) {
		this.saveGraphicsState();
		//be careful to the mirror symmetry of the rectangle into the pdf space coordinates
		this.writeln(num2str(width) + " 0 0 " + num2str(height) + " " + getX(x) + " " + getY(y + height) + " cm");
		//150 0 0 80 0 0 cm% Scale
		this.writeln("/Im" + imageId + " Do");
		this.restoresGraphicState();
	}
	
	//-------------------------------------------------------------------------
	
	public void saveGraphicsState() {
		this.writeln("q");
	}
	
	public void restoresGraphicState() {
		this.writeln("Q");
	}
	
	//-------------------------------------------------------------------------
	
	public void strokePath() {
		this.writeln("S");
	}
	
	public void closePath() {
		this.writeln("h");
	}
	
	public void closeStrokePath() {
		this.writeln("s"); //it's equivalent to call closePath() then strokePath();
	}
	
	public void fillStrokePath() {
		this.writeln("B");
	}
	
	public void fillStrokePath_WIND_NON_ZERO() {
		this.fillStrokePath();
	}
	
	public void fillStrokePath_WIND_EVEN_ODD() {
		this.writeln("B*");
	}
	
	public void closeFillStrokePath() {
		this.writeln("b");
	}
	
	public void setColor(int red, int green, int blue) {
		double r = ((double) red) / 255.0;
		double g = ((double) green) / 255.0;
		double b = ((double) blue) / 255.0;
		String rgb = _df.format(r) + " " + _df.format(g) + " " + _df.format(b);
		this.writeln("" + rgb + " rg");
		this.writeln("" + rgb + " RG");
	}
	
	private String makePDFString(String s) {
		s = s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\t", "\\\\t").replaceAll("\b", "\\\\b").replaceAll("\f", "\\\\f").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
		return "("+s+")";
	}
	
	//-------------------------------------------------------------------------
	
	public static int BUTT_CAP = 0;
	public static int ROUND_CAP = 1;
	public static int PROJECTING_SQUARE = 2;
	
	public void setLineCap(int cap) {
		this.writeln(cap + " J");
	}
	
	
	public static int JOIN_MITER = 0;
	public static int JOIN_ROUND = 1;
	public static int JOIN_BEVEL = 2;
	
	public void setLineJoin(int join) {
		this.writeln(join + " j");
	}
	
	public void setLineWidth(float width) {
		this.writeln(num2str(width) + " w");
	}
	
	public void setMitterLimit(float limit) throws IllegalArgumentException {
		if (limit < 1f) {
			throw new IllegalArgumentException("miter limit < 1");
		}
		this.writeln(num2str(limit) + " M");
	}
	
	public void setDashArrayAndPhase(int dashArray[], int dashPhase) {
		String dashArrayStr = "";
		if (dashArray != null) {
			for (int i = 0; i < dashArray.length; ++i) {
				dashArrayStr += num2str(dashArray[i]) + " ";
			}
		}
		this.writeln("[" + dashArrayStr.trim() + "] " + num2str(dashPhase) + " d");
	}
	
	public void setDashArrayAndPhase(float dashArray[], float dashPhase) {
		this.writeln("[" + mat2str(dashArray) + "] " + num2str(dashPhase) + " d");
	}		
	
	public void setDashArrayAndPhase(double dashArray[], double dashPhase) {
		this.writeln("[" + mat2str(dashArray) + "] " + num2str(dashPhase) + " d");
	}
	
	//-------------------------------------------------------------------------
	

	//If several transformations are combined, the order in which they are applied is significant.
	//For example, first scaling and then translating the x axis is not the same as first translating
	//and then scaling it. In general, to obtain the expected results, transformations should be done
	//in the following order: Translate, Rotate, Scale or skew.

	/*
	public void writeTranslate(double x, double y) {
		this.writeln("1 0 0 1 " + num2str(x) + " " + num2str(-y) + " cm");
	}
	
	public void writeRotate(double r) {
		String sr = num2str(Math.sin(r));
		String cr = num2str(Math.cos(r));
		this.writeln(cr + " " + sr + " -" + sr + " " + cr + " 0 0 cm");
	}
	
	public void writeScale(double w, double h) {
		this.writeln(num2str(w) + " 0 0 " + num2str(h) + " 0 0 cm");
	}
	
	public void writeSkew(double xAxisAngle, double yAxisAngle) {
		this.writeln("1 " + num2str(Math.tan(xAxisAngle)) + " " + num2str(Math.tan(yAxisAngle)) + " 1 0 0 cm");
	}
	*/

/*
public Point2D.Double originWithRotation( double r ) {   //calculates a rotated origin in pdf coordinates (use flipY to get java y coord.)
   return new Point2D.Double(page.getDimension().getHeight() * Math.sin(r), page.getDimension().getHeight() * Math.cos(r));
}
*/
	
/*	
	 // static method added 1/13/12 by Claude Heintz dev@macluxpro.com
	 // returns a clipped copy of an image suitable for drawImage methods that support destination/source
	 // eg. drawImage(Image img,int dx1,int dy1,int dx2, int dy2,int sx1,int sy1,int sx2,int sy2, ImageObserver obs)

	 public static BufferedImage cropImage(Image img, int dw, int dh, int sx1, int sy1, int sx2, int sy2) {
	   BufferedImage croppedimage = new BufferedImage( dw, dh, BufferedImage.TYPE_INT_ARGB );
	   Graphics2D rg = croppedimage.createGraphics();
	   rg.drawImage(img, 0, 0, dw, dh, sx1, sy1, sx2, sy2, null);
	   rg.dispose();
	   return croppedimage;
	 }
*/

	 /**
	 * Draw's an image onto the page, with scaling
	 *
	 * @param img The java.awt.Image
	 * @param dx1 coordinate on page
	 * @param dy1 coordinate on page
	 * @param dx2 coordinate on page
	 * @param dy2 coordinate on page
	 * @param sx1 coordinate on image
	 * @param sy1 coordinate on image
	 * @param sx2 coordinate on image
	 * @param sy2 coordinate on image
	 * @param obs ImageObserver
	 * @return true if drawn
	 */
	/*
	 public boolean drawImage(Image img,int dx1,int dy1,int dx2,
	   int dy2,int sx1,int sy1,int sx2,int sy2,
	   ImageObserver obs) {
	   Image ci = PDFGraphics.cropImage(img, dx2-dx1, dy2-dy1, sx1, sy1, sx2, sy2);
	   return drawImage(ci, dx1, dy1, (int) ci.getWidth(obs), (int) ci.getHeight(obs), null);
	 }	
*/	 
}
