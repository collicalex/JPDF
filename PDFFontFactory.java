package lib.pdf;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


public class PDFFontFactory {
	
	private static Cache _cache = null;
	
	private static LinkedHashMap<String, PDFFont> _usedFont = null;
	
	private Font _javaFont;
	private boolean _usedOnlyPdfDefaultFont;
	private boolean _embededFont;

	public PDFFontFactory(boolean usedOnlyPdfDefaultFont, boolean embededFont) throws IOException  {
		_javaFont = Font.decode(null);
		_usedFont = new LinkedHashMap<String, PDFFont>();
		_usedOnlyPdfDefaultFont = usedOnlyPdfDefaultFont;
		_embededFont = embededFont;
		
		if (_cache == null) {
			_cache = new Cache();
		}
	}
	
	public void updateOptions(boolean usedOnlyPdfDefaultFont, boolean embededFont) {
		_usedOnlyPdfDefaultFont = usedOnlyPdfDefaultFont;
		_embededFont = embededFont;
	}
	
	public int getPdfObjectId(int fontId) {
		for (String key : _usedFont.keySet()) {
			PDFFont pdfont = _usedFont.get(key);
			if (pdfont.getId() == fontId) {
				return pdfont.getPdfObjectId();
			}
		}
		return 0;
	}
	
	public int getFontId() {
		String realFontName = getFontName(_javaFont);
		String pdfDefaultFontName = getPdfDefaultFont(realFontName);
		
		if ((pdfDefaultFontName == null) && (_usedOnlyPdfDefaultFont)) { //it's not a default font, but we want to use only default font, so get the nearest default font
			pdfDefaultFontName = getNearPdfDefaultFont(_javaFont);
		}
		
		if (pdfDefaultFontName != null) {
			return getFontId(pdfDefaultFontName);
		} else {
			
			PDFFont pdfont = _usedFont.get(realFontName);
			if (pdfont == null) {
				File fontFile = _cache.getFile(_javaFont);
				if (fontFile == null) {
					System.err.println("LIBPDF : Unable to find the corresponding ttf file for font : " + realFontName);
					return getFontId(getNearPdfDefaultFont(_javaFont));
				} else {
					pdfont = new PDFFont(_usedFont.size() + 1, fontFile, _javaFont);
					_usedFont.put(realFontName, pdfont);
				}
			}
			return pdfont.getId();
		}
	}
	
	private String getFontName(Font javaFont) {
		try {
			File file = _cache.getFile(_javaFont);
			Font font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file));
			return font.getFontName(Locale.US);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return javaFont.getFontName(Locale.US);
	}
	
	private int getFontId(String pdfDefaultFontName) {
		PDFFont pdfont = _usedFont.get(pdfDefaultFontName);
		if (pdfont == null) {
			pdfont = new PDFFont(_usedFont.size() + 1, pdfDefaultFontName);
			_usedFont.put(pdfDefaultFontName, pdfont);
		}
		return pdfont.getId();
	}

	public int getFontSize() {
		return _javaFont.getSize();
	}

	public void setJavaFont(Font font) {
		_javaFont = font;
	}
	
	public Font getJavaFont() {
		return _javaFont;
	}
	
	public void generate(PDFObjectStream output) throws IOException {
		for (String key : _usedFont.keySet()) {
			PDFFont pdfont = _usedFont.get(key);
			pdfont.generate(output, _embededFont);
		}
	}
	
	//-------------------------------------------------------------------------
	private String getNearPdfDefaultFont(Font font) {
		//TODO: check if font is monospace, or serif or sans serif or dingbats
		//TODO: then return the correspind pdf font.
		if (font.isBold() && font.isItalic()) {
			return "Times-BoldItalic";
		} else if (font.isBold()) {
			return "Times-Bold";
		} else if (font.isItalic()) {
			return "Times-Italic";
		} else {
			return "Times-Roman";
		}
	}
	
	//PDF define 14 default fonts
	private String getPdfDefaultFont(String realFontName) {
		String fontName = realFontName;
		if (fontName.compareTo("Courier New") == 0) {
			return "Courier";
		} else if ((fontName.compareTo("Courier New Gras") == 0) || (fontName.compareTo("Courier New Bold") == 0)) {
			return "Courier-Bold";
		} else if ((fontName.compareTo("Courier New Italique") == 0) || (fontName.compareTo("Courier New Italic") == 0)) {
			return "Courier-Oblique";	
		} else if ((fontName.compareTo("Courier New Gras Italique") == 0) || (fontName.compareTo("Courier New Bold Italic") == 0)) {
			return "Courier-BoldOblique";	
		} else if (fontName.compareTo("Arial") == 0) {
			return "Helvetica";
		} else if ((fontName.compareTo("Arial Gras") == 0) || (fontName.compareTo("Arial Bold") == 0)) {
			return "Helvetica-Bold";
		} else if ((fontName.compareTo("Arial Italique") == 0) || (fontName.compareTo("Arial Italic") == 0)) {
			return "Helvetica-Oblique";
		} else if ((fontName.compareTo("Arial Gras Italique") == 0) || (fontName.compareTo("Arial Bold Italic") == 0)) {
			return "Helvetica-BoldOblique";
		} else if (fontName.compareTo("Symbol") == 0) {
			return "Symbol";
		} else if (fontName.compareTo("Times New Roman") == 0) {
			return "Times-Roman";
		} else if ((fontName.compareTo("Times New Roman Gras") == 0) || (fontName.compareTo("Times New Roman Bold") == 0)) {
			return "Times-Bold";
		} else if ((fontName.compareTo("Times New Roman Italique") == 0) || (fontName.compareTo("Times New Roman Italic") == 0)) {
			return "Times-Italic";
		} else if ((fontName.compareTo("Times New Roman Gras Italique") == 0) || (fontName.compareTo("Times New Roman Bold Italic") == 0)) {
			return "Times-BoldItalic";
		} else if (fontName.compareTo("Zapf Dingbats") == 0) { //not present on windows
			return "ZapfDingbats";
		} else {
			return null;
		}		
	}
	
	//-------------------------------------------------------------------------

	private class Cache {
		private CacheLogical _cacheLogical = null;
		private CachePhysical _cachePhysical = null;
		
		public Cache() {
			_cacheLogical = new CacheLogical();
			_cachePhysical = new CachePhysical();
		}
		
		public File getFile(Font font) {
			File file = _cacheLogical.getFile(font);
			if (file != null) {
				return file;
			} else {
				return _cachePhysical.getFile(font);
			}
		}
	}
	
	private class CachedFont {
		private String familyName;
		private File plain;
		private File bold;
		private File italic;
		private File bolditalic;
		
		public void setFontFile(File file, Font font) {
			familyName = font.getFamily();
			if (isBoldItalic(font)) {
				bolditalic = file;
			} else if (isBold(font)) {
				bold = file;
			} else if (isItalic(font)) {
				italic = file;
			} else if (isPlain(font)) {
				plain = file;
			}
		}
		
		public File getFile(Font font) {
			if (font.getFamily().compareTo(familyName) == 0) {
				if (isBoldItalic(font)) {
					return bolditalic;
				} else if (isBold(font)) {
					return bold;
				} else if (isItalic(font)) {
					return italic;
				} else if (isPlain(font)) {
					return plain;
				}
			}
			return null;
		}
		
		private boolean isBoldItalic(Font font) {
			if (font.isBold() && font.isItalic()) {
				return true;
			} else {
				String usName = font.getFontName(Locale.US);
				if (usName.compareTo(font.getFamily() + " Bold Italic") == 0) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		private boolean isBold(Font font) {
			if (font.isBold()) {
				return true;
			} else {
				String usName = font.getFontName(Locale.US);
				if ((usName.compareTo(font.getFamily() + " Bold") == 0) || (usName.compareTo(font.getFamily() + " Bold Italic") == 0)) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		private boolean isItalic(Font font) {
			if (font.isItalic()) {
				return true;
			} else {
				String usName = font.getFontName(Locale.US);
				if ((usName.compareTo(font.getFamily() + " Italic") == 0) || (usName.compareTo(font.getFamily() + " Bold Italic") == 0)) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		private boolean isPlain(Font font) {
			return ((this.isBold(font) == false) && (this.isItalic(font) == false));
		}
		
		public String toString() {
			String res = familyName;
			res += (plain == null) ? "0" : "1";
			res += (bold == null) ? "0" : "1";
			res += (italic == null) ? "0" : "1";
			res += (bolditalic == null) ? "0" : "1";
			return res;
		}
	}
	
	private class CacheLogical {
		private Map<String, File> _cache = null;
		
		public File getFile(Font font) {
			this.build();
			return _cache.get(font.getFontName().toLowerCase());
		}
		
		private File getFile(String filename) {
			filename = filename.toLowerCase();
			File[] paths = SystemUtils.FONT_PATH;
			for (int i = 0; i < paths.length; ++i) {
				File path = paths[i];
				File[] fonts = path.listFiles();
				for (int j = 0; j < fonts.length; ++j) {
					File fontFile = fonts[j];
					if (fontFile.getName().toLowerCase().compareTo(filename) == 0) {
						return fontFile;
					}
				}
			}
			return null;
		}		
		
		private void build() {
			if (_cache == null) {
				_cache = new HashMap<String, File>();
				
				File path = new File(System.getProperty("java.home"), "lib");
				File fontProperties = new File(path, "fontconfig.properties.src");
				Properties javaFontsProperties = new Properties();
				try {
					javaFontsProperties.load(new FileReader(fontProperties));
				} catch (Exception e) {
					return ;
				}
				
				List<String> toLoad = new LinkedList<String>();
				Enumeration<Object> keys = javaFontsProperties.keys();
				while(keys.hasMoreElements()){
					String key = (String) keys.nextElement();
					if (key.endsWith(".alphabetic")) {
						toLoad.add(key);
					}
				}
				
				for (int i = 0; i < toLoad.size(); ++i) {
					String key = toLoad.get(i);
					String fontName = javaFontsProperties.getProperty(key);
					String keyFont = "filename." + fontName.replaceAll(" ", "_");
					String fontFileName = javaFontsProperties.getProperty(keyFont);
					if (fontFileName != null) {
						File fontFile = this.getFile(fontFileName);
						if (fontFile != null) {
							_cache.put(key.substring(0,  key.length() - 11), fontFile);
						}
					}
				}
				
			}
		}		
		
	}
	
	private class CachePhysical {
		private Map<String, CachedFont> _cache = null; 
		
		public File getFile(Font font) {
			this.build();
			CachedFont cachedFont = this.getCachedFont(font);
			if (cachedFont == null) {
				return null;
			} else {
				return cachedFont.getFile(font);
			}
		}
		
		private void putCachedFont(Font font, CachedFont cachedFont) {
			_cache.put(font.getFamily(), cachedFont);
		}
		
		private CachedFont getCachedFont(Font font) {
			return _cache.get(font.getFamily());
		}
		
		private void build() {
			if (_cache == null) {
				_cache = new HashMap<String, CachedFont>();
				File[] paths = SystemUtils.FONT_PATH;
				for (int i = 0; i < paths.length; ++i) {
					File path = paths[i];
					File[] fonts = path.listFiles();
					for (int j = 0; j < fonts.length; ++j) {
						File fontFile = fonts[j];
						if (fontFile.getName().toLowerCase().endsWith(".ttf")) {
							try {
								Font tmp = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontFile));
								CachedFont cachedFont = this.getCachedFont(tmp);
								if (cachedFont == null) {
									cachedFont = new CachedFont();
								}
								cachedFont.setFontFile(fontFile, tmp);
								this.putCachedFont(tmp, cachedFont);
							} catch (Exception e) {
							}
						}
					}
				}
			}			
		}
	}
	
	
	//-------------------------------------------------------------------------

	public static class SystemUtils {
		private static String OS = System.getProperty("os.name").toLowerCase();
		
		private static boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
		private static boolean IS_MAC = (OS.indexOf("mac") >= 0);
		private static boolean IS_UNIX = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0);
		private static boolean IS_AIX = (OS.indexOf("aix") > 0);
		private static boolean IS_SOLARIS = (OS.indexOf("sun") >= 0);
		private static boolean IS_HP = (OS.indexOf("hp") >= 0);
		
		public static File[] FONT_PATH = getFontPath();
		
		private static File[] getFontPath() {
			if (IS_WINDOWS) {
				File file[] = new File[1];
				file[0] = new File("C:\\Windows\\Fonts");
				return file;
			} else if (IS_MAC) {
				File file[] = new File[5];
				file[0] = new File("~/Library/Fonts/");
				file[1] = new File("/Library/Fonts/");
				file[2] = new File("/Network/Library/Fonts/");
				file[3] = new File("/System/Library/Fonts/");
				file[4] = new File("/System Folder/Fonts/");
			} else if (IS_UNIX) {
				File file[] = new File[3];
				file[0] = new File("/usr/lib/X11/fonts/TrueType");
				file[1] = new File("/usr/share/fonts");
				file[2] = new File("/usr/X11R6/lib/X11/Fonts");
				file[3] = new File("/usr/X11R6/lib/X11/fonts/TrueType");
				return file;
			} else if (IS_HP) {
				File file[] = new File[1];
				file[0] = new File("/usr/lib/X11/fonts/ms.st/typefaces");
				return file;
			} else if (IS_AIX) {
				File file[] = new File[1];
				file[0] = new File("/usr/lpp/X11/lib/X11/fonts/TrueType");
				return file;
			} else if (IS_SOLARIS) {
				File file[] = new File[1];
				file[0] = new File("/usr/openwin/lib/X11/fonts/TrueType");
				return file;
			}
			return new File[0];
		}
	}
	
}
