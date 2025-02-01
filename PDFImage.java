package lib.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;


public class PDFImage {
	
	private int _id;  // used to generate the "/Imid Do" pdf tag (example: /Im2 Do)
	private int _pdfObjectId; //used to udpate the generated "5 0 obj <</XOjbect <</Im1 AAA 0>>>>" by replacing AAA with _pdfObjectId
	private int _width;
	private int _height;
	private int _bpp; //1, 2, 4, 8 or 16
	
	private File _rgbFile; //where we temporaraly saved the bufferedimage
	private File _alphaFile;
	
	//-- Stats -------
	
	private boolean _isGrayscale;
	private int _countAlpha;
	private int _countRed;
	private int _countGreen;
	private int _countBlue;
	
	private String _md5;;

	public PDFImage(int id, BufferedImage image) throws IOException {
		_id = id;
		preprocessData(image);
	}
	
	public int getId() {
		return _id;
	}
	
	public int getPdfObjectId() {
		return _pdfObjectId;
	}	
	
	private void preprocessData(BufferedImage image) throws IOException {
		_width = image.getWidth();
		_height = image.getHeight();
		
		_bpp = image.getColorModel().getPixelSize();
		
		//Retrieve some stats
		
		_countAlpha = 0;
		_countRed = 0;
		_countGreen = 0;
		_countBlue = 0;
		_isGrayscale = true;
		
        for (int i = 0; i < _width; i++) {  
            for (int j = 0; j < _height; j++) {
            	int pixel = image.getRGB(i, j);
            	int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;  
                int green = (pixel >> 8) & 0xFF;  
                int blue = (pixel) & 0xFF;
                
                if (_isGrayscale) {
                	if ((red != green) || (red != blue) || (green != blue)) {
                		_isGrayscale = false;
                	}
                }

                if (alpha < 255)
                	_countAlpha++;
                if (red > 0)
                	_countRed++;
                if (green > 0)
                	_countGreen++;
                if (blue > 0)
                	_countBlue++;
            }
        }
        
        //Compute the md5 representation of this image
        _md5 = "";
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", outputStream);
		byte[] data = outputStream.toByteArray();
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			byte[] hash = md.digest();
			
	        _md5 = "";
	        for (int i=0; i < hash.length; i++) {
	        	_md5 += Integer.toString( ( hash[i] & 0xff ) + 0x100, 16).substring( 1 );
	        }			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
	
	
	protected void saveData(BufferedImage image) throws IOException {
		_rgbFile = new File("imageRGB" + _id + ".txt");
        
        byte[] pixels = new byte[_width * _height * 3];
        byte[] alphas = new byte[_width * _height];
    	int i = 0;
    	int j = 0;
    	for (int y = 0; y < _height; ++y) {
    		for (int x = 0; x < _width; ++x) {
            	int pixel = image.getRGB(x, y);
            	alphas[j++] = (byte) ((pixel >> 24) & 0xFF);	//alpha
            	pixels[i++] = (byte) ((pixel >> 16) & 0xFF);	//red
            	pixels[i++] = (byte) ((pixel >> 8) & 0xFF);		//green
            	pixels[i++] = (byte) ((pixel) & 0xFF);			//blue      	
            }
        }
        
		FileOutputStream fos = new FileOutputStream(_rgbFile);
		DeflaterOutputStream dos = new DeflaterOutputStream(fos);
		dos.write(pixels, 0, pixels.length);
		dos.flush();
		dos.close();
		
		if (hasAlpha()) {
			_alphaFile = new File("imageALPHA" + _id + ".txt");
			fos = new FileOutputStream(_alphaFile);
			dos = new DeflaterOutputStream(fos);
			dos.write(alphas, 0, alphas.length);
			dos.flush();
			dos.close();
		} else {
			_alphaFile = null;
		}
		
		pixels = null;
		alphas = null;
	}	
	
	@Override
	public int hashCode() {
		return _md5.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PDFImage) {
			PDFImage pdfimage = (PDFImage)other;
			return (
					(pdfimage._width == this._width) && 
					(pdfimage._height == this._height) && 
					(pdfimage._bpp == this._bpp) && 
					(pdfimage._countAlpha == this._countAlpha) && 
					(pdfimage._countRed == this._countRed) &&
					(pdfimage._countGreen == this._countGreen) &&
					(pdfimage._countBlue == this._countBlue)
					);
		} else {
			return false;
		}
	}
	
	private boolean hasAlpha() {
		return (_countAlpha > 0);
	}

	public void generate(PDFObjectStream output) throws IOException {
		output.newPdfObject();
		_pdfObjectId = output.getPdfObjectCount();
		output.writeln(_pdfObjectId + " 0 obj <<");
		output.writeln("/Type /XObject");
		output.writeln("/Subtype /Image");
		output.writeln("/Width " + _width);
		output.writeln("/Height " + _height);
		output.writeln("/ColorSpace /DeviceRGB");
		output.writeln("/BitsPerComponent 8");
		if (hasAlpha()) {
			output.writeln("/SMask " + (_pdfObjectId + 1) + " 0 R");	
		}
		output.writeln("/Filter /FlateDecode");
		output.writeln("/Length " + _rgbFile.length());
		output.writeln(">>stream");
		
		//write binary data stream
		byte[] buffer = new byte[8192];
		int count;		
		FileInputStream fis = new FileInputStream(_rgbFile);
		while ((count = fis.read(buffer)) > 0) {
			output.write(buffer, count);
		}
		fis.close();		
		
		output.writeln("endstream");
		output.writeln("endobj");		

		_rgbFile.delete();
		
		if (hasAlpha()) {
			output.newPdfObject();
			output.writeln((_pdfObjectId+1) + " 0 obj <<");
			output.writeln("/Type /XObject");
			output.writeln("/Subtype /Image");
			output.writeln("/Width " + _width);
			output.writeln("/Height " + _height);
			output.writeln("/ColorSpace /DeviceGray");
			output.writeln("/BitsPerComponent 8");
			output.writeln("/Filter /FlateDecode");
			output.writeln("/Length " + _alphaFile.length());
			output.writeln(">>stream");
			
			fis = new FileInputStream(_alphaFile);
			while ((count = fis.read(buffer)) > 0) {
				output.write(buffer, count);
			}
			fis.close();			
			
			output.writeln("endstream");
			output.writeln("endobj");
			
			_alphaFile.delete();
		}

	}

}
