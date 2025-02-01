package lib.pdf;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;

public class PDFImageFactory {
	
	private LinkedHashMap<PDFImage, PDFImage> _cache;

	public PDFImageFactory() {
		_cache = new LinkedHashMap<PDFImage, PDFImage>();
	}
	
	public int getImageId(BufferedImage image) throws IOException {
		int id = _cache.size() + 1;
		PDFImage pdfimage = new PDFImage(id, image);
		PDFImage cachedPdfImage = _cache.get(pdfimage);
		if (cachedPdfImage == null) {
			pdfimage.saveData(image);
			_cache.put(pdfimage, pdfimage);
			cachedPdfImage = pdfimage;
		}
		return cachedPdfImage.getId();
	}
	
	public int getImageId(Image image) throws IOException {
		if (image instanceof BufferedImage) {
			return getImageId((BufferedImage)image);
		} else {
			BufferedImage bi = new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_RGB);
			Graphics bg = bi.getGraphics();
			bg.drawImage(image, 0, 0, null);
			bg.dispose();
			return getImageId(bi);
		}
	}

	public void generate(PDFObjectStream output) throws IOException {
		for (PDFImage pdfimage : _cache.keySet()) {
			pdfimage.generate(output);
		}
		/*
		Iterator<PDFImage> it = _cache.iterator();
		while(it.hasNext()) {
			PDFImage pdfimage = it.next();
			pdfimage.generate(output);
		}
		*/
	}
	
	public int getPdfObjectId(int fontId) {
		for (PDFImage pdfimage : _cache.keySet()) {
			if (pdfimage.getId() == fontId) {
				return pdfimage.getPdfObjectId();
			}
		}		
		
		/*
		Iterator<PDFImage> it = _cache.iterator();
		while(it.hasNext()) {
			PDFImage pdfimage = it.next();
			if (pdfimage.getId() == fontId) {
				return pdfimage.getPdfObjectId();
			}
		}
		*/		
		return 0;
	}

}
