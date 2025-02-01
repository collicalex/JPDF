package core.report;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

public class PageSize {
	
	private PageFormat _pageFormat;
	private double _scale;
	
	public PageSize() {
		this.init();
		_scale = 1;
	}
	
	public PageSize(double scale) {
		this.init();
		_scale = scale;
		
		Paper paper = _pageFormat.getPaper();
		paper.setSize(paper.getWidth() * scale, paper.getHeight() * scale);
		paper.setImageableArea(paper.getImageableX() * scale, paper.getImageableY() * scale, paper.getImageableWidth() * scale, paper.getImageableHeight() * scale);
		_pageFormat.setPaper(paper);
	}
	
	private void init() {
		PageFormat pf0 = PrinterJob.getPrinterJob().defaultPage(); //must be A4
		PageFormat pf1 = (PageFormat) pf0.clone();
	    Paper p = pf0.getPaper();
	    p.setImageableArea(0, 0,pf0.getWidth(), pf0.getHeight()); //set incorrect 0 margin
	    pf1.setPaper(p);
	    _pageFormat = PrinterJob.getPrinterJob().validatePage(pf1); //printerjob will correct the 0 margin to set a minimum acceptable margin		
	}
	
	public double getScale() {
		return _scale;
	}
	
	private int toint(double d) {
		return (int)(d + 0.5);
	}
	
	public int getWidth() {
		return toint(_pageFormat.getWidth());
	}
	
	public int getHeight() {
		return toint(_pageFormat.getHeight());
	}
	
	public int getImageableX() {
		return toint(_pageFormat.getImageableX());
	}
	
	public int getImageableY() {
		return toint(_pageFormat.getImageableY());
	}
	
	public int getImageableWidth() {
		return toint(_pageFormat.getImageableWidth());
	}

	public int getImageableHeight() {
		return toint(_pageFormat.getImageableHeight());
	}
	
	public int x1() {
		return getImageableX();
	}
	
	public int y1() {
		return getImageableY();
	}
	
	public int x2() {
		return getImageableX() + getImageableWidth();
	}
	
	public int y2() {
		return getImageableY() + getImageableHeight();
	}		
}
