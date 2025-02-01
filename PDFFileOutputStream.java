package lib.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

public class PDFFileOutputStream {

	private File _file;
	private FileOutputStream _output;
	private long _writtenSize;
	
	public PDFFileOutputStream(File file) throws FileNotFoundException {
		_file = file;
		_output = new FileOutputStream(file);
		_writtenSize = 0;
	}
	
	protected void write(byte[] buffer, int length) throws IOException {
		_output.write(buffer, 0, length);
		_writtenSize += length;
	}	
	
	protected void write(String str) throws IOException {
		byte[] b = str.getBytes();
		_output.write(b);
		_writtenSize += b.length;
	}
	
	protected void writeln(String str) throws IOException {
		this.write(str);
		this.write("\r\n");
	}
	
	protected void close() throws IOException {
		if (_output != null) {
			_output.flush();
			_output.close();
			_output = null;
		}
	}
	
	protected void delete() throws IOException {
		this.close();
		_file.delete();
	}
	
	protected long getWrittentSize() {
		return _writtenSize;
	}
	
	protected void merge(PDFFileOutputStream other) throws IOException {
		other.close();
		
		FileInputStream fis = new FileInputStream(other._file);
		byte[] buffer = new byte[8192];
		int count;
		while ((count = fis.read(buffer)) > 0) {
			this.write(buffer, count);
		}
		fis.close();		
	}
	
	protected void compress() throws IOException {
		this.close();
		File compressfile = new File("compress.txt");
		
		FileInputStream fis = new FileInputStream(_file);
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
		
		
		_writtenSize = compressfile.length();
		_file.delete();
		compressfile.renameTo(_file);
	}
	
	
}
