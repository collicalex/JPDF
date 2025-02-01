package lib.pdf;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class PDF {

	private File _file;
	private List<PDFPage> _pages;
	private PDFObjectStream _outputTmp;
	private PDFFontFactory _fontFactory;
	private PDFImageFactory _imageFactory;
	
	private boolean _compress = false;
	private boolean _usedOnlyPdfDefaultFont = false;
	private boolean _embeddedFont = true;
	
	//-- Meta data ------------------------------------------------------------
	private String _title = null; //the document title
	private String _author = null; //the document author
	private String _creator = null; //the software which create the pdf
	private String _subject = null; //the document subject
	private String _keywords = null; //the document keywords
	private String _producer = "PdfLib"; //the name of this lib; which produce the pdf
	//-- Meta data ------------------------------------------------------------
	
	public PDF(File file) throws IOException {
		_outputTmp = new PDFObjectStream(new File("tmp.pdf"));
		_outputTmp.newPdfObject(); //add 3 fakes pdf object (to increment pdf object id counter)
		_outputTmp.newPdfObject(); //the first represent the pdf object catalog; the second represent the  list of pages object
		_outputTmp.newPdfObject(); //the third is the pdf meta data
		_fontFactory = new PDFFontFactory(false, true);
		_imageFactory = new PDFImageFactory();
		_file = file;
		_pages = new LinkedList<PDFPage>();		
	}
	
	public void setTitle(String title) {
		_title = title;
	}
	
	public void setAuthor(String author) {
		_author = author;
	}
	
	public void setCreator(String creator) {
		_creator = creator;
	}
	
	public void setSubject(String subject) {
		_subject = subject;
	}
	
	public void setKeywords(String keywords) {
		_keywords = keywords;
	}
	
	public void enableCompression(boolean state) {
		_compress = state;
	}
	
	public void embeddedFonts(boolean state) {
		_embeddedFont = state;
		_fontFactory.updateOptions(_usedOnlyPdfDefaultFont, _embeddedFont);
	}

	public void usedOnlyPdfDefaultFont(boolean state) {
		_usedOnlyPdfDefaultFont = state;
		_fontFactory.updateOptions(_usedOnlyPdfDefaultFont, _embeddedFont);
	}
	
	public Graphics2D newPage(int width, int height) throws IOException {
		closeLastPage();
		PDFPage page = new PDFPage(_outputTmp, width, height, _fontFactory, _imageFactory);
		_pages.add(page);
		return page.getGraphics();
	}
	
	public Graphics2D newPage(double width, double height) throws IOException {
		closeLastPage();
		PDFPage page = new PDFPage(_outputTmp, width, height, _fontFactory, _imageFactory);
		_pages.add(page);
		return page.getGraphics();
	}
	
	private void closeLastPage() throws IOException {
		if (_pages.size() > 0) {
			PDFPage lastPage = _pages.get(_pages.size() - 1);
			lastPage.close(_compress);
		}
	}

	public void close() throws IOException {
		if (_pages.size() == 0) {
			_outputTmp.delete();
		} else {
			closeLastPage();
			PDFObjectStream output = new PDFObjectStream(_file);
			//write pdf header
			output.writeln("%PDF-1.7");
			if (_compress || hasRessources()) {
				output.writeln("%\342\343\317\323");
			}
			//write pdf catalog object
			output.newPdfObject();
			output.writeln("1 0 obj <</Type /Catalog /Pages 2 0 R>>");
			output.writeln("endobj");
			//write pdf pages object (ie list of page)
			output.newPdfObject();
			output.write("2 0 obj <</Type /Pages /Kids [");
			for (int p = 0; p < _pages.size(); ++p) {
				output.write(_pages.get(p).getPdfID() + " 0 R");
				if (p+1 != _pages.size()) {
					output.write(" ");
				}
			}	
			output.write("] /Count " + _pages.size());
			output.writeln(">>");
			output.writeln("endobj");
			//write the pdf meta data
			output.newPdfObject();
			output.writeln("3 0 obj");
			output.writeln("  <<");
			if (_title != null) output.writeln("    /Title (" + _title + ")");
			if (_author != null) output.writeln("    /Author (" + _author + ")");
			if (_subject != null) output.writeln("    /Subject (" + _subject + ")");
			if (_keywords != null) output.writeln("    /Keywords (" + _keywords + ")");
			if (_creator != null) output.writeln("    /Creator (" + _creator + ")");
			if (_producer != null) output.writeln("    /Producer (" + _producer + ")");
			String date = getCurrentDate();
			output.writeln("    /CreationDate(D:"+date+")");
			output.writeln("    /ModDate(D:"+date+")");
			output.writeln("  >>");
			output.writeln("endobj");
			
			
			//write each pdf page object
			//the next object will be the first page... which will have the id 3!
			//append tmp.pdf to the current pdf (it will close _outputTmp);
			_outputTmp.removeFirstOjbect(); //remove the three first fake ojects
			_outputTmp.removeFirstOjbect();
			_outputTmp.removeFirstOjbect();
			output.merge(_outputTmp);
			
			//write all used font objects
			if (hasTextDrawn()) {
				_fontFactory.generate(output);
			}
			
			//write all used image objects
			if (hasImageDrawn()) {
				_imageFactory.generate(output);
			}
			
			//write pdf xref object
			long xrefPosition = output.getWrittentSize();
			output.writeln("xref");
			
			int nbOjbects = output.getPdfObjectCount() + 1; //Add 1 to the null first object
			output.writeln("0 " + nbOjbects);
			output.writeln("0000000000 65535 f"); //the null first object
			for (int o = 0; o < output.getObjectPosition().size(); ++o) {
				long position = output.getObjectPosition().get(o);
				output.writeln(String.format("%010d", position) + " 00000 n");
			}
	
			//write pdf trailer object
			output.writeln("trailer <</Size " + nbOjbects + " /Root 1 0 R /Info 3 0 R>>");
			//write pdf footer
			output.writeln("startxref");
			output.writeln("" + xrefPosition);
			output.writeln("%%EOF");
			output.close();
			
			//update font references
			if (hasRessources()) {
				RandomAccessFile file = new RandomAccessFile(_file, "rw");
				for (PDFPage page : _pages) {
					if (page.hasRessources()) {
						int resourceId = page.getPdfID() + 1;
						long position = output.getObjectPosition().get(resourceId - 1); //resourcesID start at 1 not 0
						file.seek(position);
						String line = "";
						while (line.compareTo("endobj") != 0)  {
							position = file.getFilePointer();
							line = file.readLine();
							long savedPosition = file.getFilePointer();
							//System.out.println(line);
							

							if (line.startsWith("/Font <<")) {
								int findex = line.indexOf("/Font");
								while (findex != -1) {
									findex = line.indexOf("/F", findex+1);
									if (findex != -1) {
										int aaaindex = line.indexOf("AAA", findex);
										int fid = Integer.parseInt(line.substring(findex+2, aaaindex-1));
										String pdfobjectid = String.format("% 3d", _fontFactory.getPdfObjectId(fid));
										file.seek(position + aaaindex);
										file.write(pdfobjectid.getBytes());
									}
								}
							}
							
							if (line.startsWith("/XObject <<")) {
								int findex = line.indexOf("/XObject");
								while (findex != -1) {
									findex = line.indexOf("/Im", findex+1);
									if (findex != -1) {
										int aaaindex = line.indexOf("AAA", findex);
										int fid = Integer.parseInt(line.substring(findex+3, aaaindex-1));
										String pdfobjectid = String.format("% 3d", _imageFactory.getPdfObjectId(fid));
										file.seek(position + aaaindex);
										file.write(pdfobjectid.getBytes());
									}
								}
							}							
							
							file.seek(savedPosition);
							
						
						}
						
					}
				}

				file.close();
			}
		}
	}
	
	private String getCurrentTimezoneOffset() {
	    TimeZone tz = TimeZone.getDefault();  
	    Calendar cal = GregorianCalendar.getInstance(tz);
	    int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
	    if (offsetInMillis == 0) {
	    	return "Z";
	    }

	    String offset = String.format("%02d'%02d'", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
	    offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

	    return offset;
	}
	
	private String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(new Date()) + getCurrentTimezoneOffset();
	}
	
	private boolean hasRessources() {
		return (hasTextDrawn() || hasImageDrawn());
	}
	
	private boolean hasTextDrawn() {
		for (PDFPage page : _pages) {
			if (page.hasTextDrawn()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasImageDrawn() {
		for (PDFPage page : _pages) {
			if (page.hasImageDrawn()) {
				return true;
			}
		}
		return false;
	}


}
