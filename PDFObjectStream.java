package lib.pdf;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PDFObjectStream extends PDFFileOutputStream {
	
	private List<Long> _objectPosition;
	
	protected PDFObjectStream(File file) throws IOException {
		super(file);
		_objectPosition = new LinkedList<Long>();
	}
	
	protected void newPdfObject() {
		_objectPosition.add(new Long(this.getWrittentSize()));
	}
	
	protected void removeFirstOjbect() {
		_objectPosition.remove(0);
	}
	
	protected int getPdfObjectCount() {
		return _objectPosition.size();
	}

	public List<Long> getObjectPosition() {
		return _objectPosition;
	}
	
	
	protected void merge(PDFObjectStream pdfobjectstream) throws IOException {
		//Close pdfojbectstream writer
		pdfobjectstream.close();
		
		//Copy its tree content to current tree
		for (int o = 0; o < pdfobjectstream._objectPosition.size(); ++o) {
			long position = pdfobjectstream._objectPosition.get(o);
			_objectPosition.add(position + this.getWrittentSize());
		}
		
		//Copy its file content to current stream
		super.merge(pdfobjectstream);
		
		//delete the merged file
		pdfobjectstream.delete();		
	}
}
