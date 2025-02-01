package lib.pdf;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.Set;

public class PDFPage {

	private PDFObjectStream _output;
	private PDFGraphics _graphics;
	private int _pdfID;
	
	private double _width;
	private double _height;

	public PDFPage(PDFObjectStream output, int width, int height, PDFFontFactory fontFactory, PDFImageFactory imageFactory) throws IOException {
		_output = output;
		_graphics = new PDFGraphics(width, height, fontFactory, imageFactory);
		_width = width;
		_height = height;
	}

	public PDFPage(PDFObjectStream output, double width, double height, PDFFontFactory fontFactory, PDFImageFactory imageFactory) throws IOException {
		_output = output;
		_graphics = new PDFGraphics(width, height, fontFactory, imageFactory);
		_width = width;
		_height = height;		
	}

	protected void close(boolean compressStream) throws IOException {
		_graphics.close();
		String filter = "";
		
		//write pdf page object
		_output.newPdfObject();
		_pdfID = _output.getPdfObjectCount();
		int id = _pdfID;
		_output.write(id++ + " 0 obj <</Type /Page /Parent 2 0 R /MediaBox [0 0 " + _width + " " + _height + "] ");
		if (this.hasRessources()) {
			_output.write("/Resources " + id + " 0 R ");
			_output.writeln("/Contents "+ (id+1) +" 0 R>>");
		} else {
			_output.write("/Resources <<>> ");
			_output.writeln("/Contents "+ id +" 0 R>>");
		}
		_output.writeln("endobj");
		
		if (this.hasRessources()) {
			_output.newPdfObject();
			_output.writeln(id++ + " 0 obj <<");
			
			if (this.hasTextDrawn()) {
				_output.write("/Font <<");
				Set<Integer> ids = _graphics.getUsedFontId();
				for (Integer i : ids) {
					_output.write("/F" + i + " AAA 0 R "); //we will rewrite AAA to the corresponding font ID object later
				}
				_output.writeln(">>");
			}
			
			if (this.hasImageDrawn()) {
				_output.write("/XObject <<");
				Set<Integer> ids = _graphics.getUsedImageId();
				for (Integer i : ids) {
					_output.write("/Im" + i + " AAA 0 R "); //we will rewrite AAA to the corresponding image ID object later
				}
				_output.writeln(">>");
			}			
			
			_output.writeln(">>");
			_output.writeln("endobj");
		}
		
		//write pdf page content object (ie the graphics ojbect)
		_output.newPdfObject();
		_output.writeln(id++ + " 0 obj");
		
		if (_graphics.getPDFFileOutputStream().getWrittentSize() == 0) {
			compressStream = false;
		}
			
		
		if (compressStream) {
			_graphics.getPDFFileOutputStream().compress();
			filter = " /Filter /FlateDecode";
		}

		_output.writeln("<</Length " + _graphics.getPDFFileOutputStream().getWrittentSize() + filter + ">>");
		_output.writeln("stream");
		_output.merge(_graphics.getPDFFileOutputStream());
		_output.writeln("endstream");
		_output.writeln("endobj");			

		_graphics.getPDFFileOutputStream().delete();
	}
	
	protected Graphics2D getGraphics() {
		return _graphics;
	}
	
	protected int getPdfID() {
		return _pdfID;
	}
	
	protected boolean hasRessources() {
		return (hasTextDrawn() || hasImageDrawn());
	}
	
	protected boolean hasTextDrawn() {
		return _graphics.hasTextDrawn();
	}
	
	protected boolean hasImageDrawn() {
		return _graphics.hasImageDrawn();
	}


}
