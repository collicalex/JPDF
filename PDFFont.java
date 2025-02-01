package lib.pdf;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

public class PDFFont {
	public static final int TYPE_BASEFONT = 0;
	public static final int TYPE_TTF = 1;
	
	private int _id; // used to generate the "/Fid size Tf" pdf tag (example: /F2 11 Tf)
	private String _baseFontName;
	private File _fontFile;
	private int _type;
	private int _pdfObjectId; //used to udpate the generated "5 0 obj <</Font <</F1 AAA 0>>>>" by replacing AAA with _pdfObjectId
	private Font _javaFont;
	
	public PDFFont(int id, String baseFontName) {
		_id = id;
		_baseFontName = baseFontName;
		_type = TYPE_BASEFONT;
	}
	
	public PDFFont(int id, File fontFile, Font javaFont) {
		_id = id;
		_fontFile = fontFile;
		_type = TYPE_TTF;
		_javaFont = javaFont;
	}		
	
	public int getId() {
		return _id;
	}
	
	public int getPdfObjectId() {
		return _pdfObjectId;
	}

	public void generate(PDFObjectStream output, boolean embeded) throws IOException {
		if (_type == TYPE_BASEFONT) {
			this.generate_BaseFont(output);
		} else {
			this.generate_TrueTypeFont(output, embeded);
		}
	}	
	
	private void generate_BaseFont(PDFObjectStream output) throws IOException {
		//-- Write font object
		output.newPdfObject();
		_pdfObjectId = output.getPdfObjectCount();
		output.writeln(_pdfObjectId + " 0 obj <</Type /Font /Subtype /Type1 /BaseFont /" + _baseFontName + ">>");
		output.writeln("endobj");
	}
	
	private void generate_TrueTypeFont(PDFObjectStream output, boolean embeded) throws IOException {
		retrievePdfFontAttributes();
		
		//-- Write font object
		output.newPdfObject();
		_pdfObjectId = output.getPdfObjectCount();				
		output.writeln(_pdfObjectId + " 0 obj <<");
		//common attribute to all type of font
		output.writeln("/Type /Font");
		

		output.writeln("/FirstChar " + _firstChar);
		output.writeln("/LastChar " + _lastChar);
		output.writeln("/Widths["+ _pdfWidths +"]");
		//Optional attributes
		output.writeln("/Name /F" + _id);
		output.writeln("/Encoding /WinAnsiEncoding");
		//others attributes for TrueType fonts
		output.writeln("/Subtype /TrueType /BaseFont /" + _javaFont.getPSName() + " /FontDescriptor " + (_pdfObjectId+1) + " 0 R");
		output.writeln(">>");				
		output.writeln("endobj");
		
		//--- Write font descriptor object
		output.newPdfObject();
		output.writeln((_pdfObjectId+1) + " 0 obj <<");
		output.writeln("/Type /FontDescriptor");
		output.writeln("/Ascent " + _pdfAscent);
		output.writeln("/CapHeight " + _pdfCapHeight);
		output.writeln("/Descent " + _pdfDescent);
		output.writeln("/Flags 32");
		output.writeln("/FontBBox [" + _pdfFontBBox + "]");
		output.writeln("/FontName /" + _javaFont.getPSName());
		output.writeln("/ItalicAngle " + _pdfItalicAngle);
		output.writeln("/StemV " + _pdfStemV);
		output.writeln("/XHeight " + _pdfXHeight);
		if (embeded) {
			output.writeln("/FontFile2 " + (_pdfObjectId+2) + " 0 R");
		}
		output.writeln(">>");				
		output.writeln("endobj");
		
		//--- Write font file object
		if (embeded) {
			
			//compress the font
			File compressfile = new File("ttf_compress.txt");
			FileInputStream fis = new FileInputStream(_fontFile);
			FileOutputStream fos = new FileOutputStream(compressfile);
			DeflaterOutputStream dos = new DeflaterOutputStream(fos);
			
			byte[] buffer = new byte[8192];
			int count;
			while ((count = fis.read(buffer)) > 0) {
				dos.write(buffer, 0, count);
			}
			dos.flush();
			dos.close();
			fis.close();					
			
			//write pdf font file info
			output.newPdfObject();
			output.writeln((_pdfObjectId+2) + " 0 obj");
			output.writeln("<</Filter /FlateDecode /Length1 " + _fontFile.length() + " /Length "+compressfile.length()+">>stream");
			
			//write binary data stream
			fis = new FileInputStream(compressfile);
			while ((count = fis.read(buffer)) > 0) {
				output.write(buffer, count);
			}
			fis.close();
			compressfile.delete();
			
			//write end pdf objectobject
			output.writeln("endstream");
			output.writeln("endobj");
		}
	}
	
	
	//-------------------------------------------------------------------------
	
	private int _firstChar = 0;
	private int _lastChar = 255;
	private String _pdfWidths;
	private int _pdfAscent;
	private int _pdfDescent;
	private int _pdfCapHeight;
	private String _pdfFontBBox;
	private int _pdfItalicAngle;
	private int _pdfStemV;
	private int _pdfXHeight;
	

	private void retrievePdfFontAttributes() {
		//Ensure to use the original font (java can apply some transformation to a font, like rotation angle,... and FontMetrics will not work properly of these kinds of font)
		try {
			Font tmp = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(_fontFile));
			_javaFont = tmp;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (FontFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//All number are relative to glyph unit, in 1000unit
		BufferedImage img = new BufferedImage(2000, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		float size = 1000f;
		_javaFont = _javaFont.deriveFont(size);
		FontMetrics fm = g2d.getFontMetrics(_javaFont);
		
		//Retrieve Widths attribute
		_pdfWidths = "";
		for (int i = _firstChar; i <= _lastChar; ++i) {
			int width = fm.charWidth(i);
			_pdfWidths += width + " ";
		}
		
		//Retrieve attributes from FontMetrics java class
		_pdfAscent = fm.getAscent();
		_pdfDescent = -fm.getDescent();
		_pdfCapHeight = (int) (fm.getLineMetrics("H", g2d).getAscent());
		_pdfItalicAngle = (int) _javaFont.getItalicAngle();
		_pdfFontBBox = "0 -" + fm.getMaxDescent() + " " + fm.getMaxAdvance() + " " + fm.getMaxAscent();
		_pdfXHeight = (int) (fm.getLineMetrics("x", g2d).getAscent());
		
		//Draw the H char and retrieve font attributes that cannot be found on Java Classes
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.setPaint(Color.BLACK);
        g2d.setFont(_javaFont);
        g2d.drawString("H", 0, img.getHeight());
        g2d.dispose();
        
        _pdfStemV = 0;
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int i = 0;
        while (i < pixels.length) {
        	if ((pixels[i++] & 0x00FFFFFF) == 0) {
        		break ;
        	}
        }
        
        while (i < pixels.length) {
        	_pdfStemV++;
        	if ((pixels[i++] & 0x00FFFFFF) != 0) {
        		break ;
        	}
        }     
	}
}
