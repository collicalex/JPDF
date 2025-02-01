package lib.pdf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PDFGraphics extends Graphics2D {
	
	private PDFGraphicsStream _output;
	private static final boolean _drawFontAsVector = false;
	private PDFFontFactory _fontFactory;
	private PDFImageFactory _imageFactory;
	private Set<Integer> _usedFont;
	private Set<Integer> _usedImage;
	private Color _currentColor;
	private Point _lastStrokePoint = null;
	private BasicStroke _currentStroke = new BasicStroke();
	private RenderingHints _renderingHints = new RenderingHints(null);
	
	public PDFGraphics(int width, int height, PDFFontFactory fontFactory, PDFImageFactory imageFactory) throws FileNotFoundException {
		_output = new PDFGraphicsStream(new File("stream.txt"), width, height);
		_fontFactory = fontFactory;
		_imageFactory = imageFactory;
		_usedFont = new HashSet<Integer>();
		_usedImage = new HashSet<Integer>();
		_currentColor = Color.BLACK;
	}
	
	public PDFGraphics(double width, double height, PDFFontFactory fontFactory, PDFImageFactory imageFactory) throws FileNotFoundException {
		_output = new PDFGraphicsStream(new File("stream.txt"), width, height);
		_fontFactory = fontFactory;
		_imageFactory = imageFactory;
		_usedFont = new HashSet<Integer>();
		_usedImage = new HashSet<Integer>();
		_currentColor = Color.BLACK;
	}

	protected PDFFileOutputStream getPDFFileOutputStream() {
		return _output;
	}
	
	protected PDFFontFactory getPDFFontFactory() {
		return _fontFactory;
	}

	protected void close() {
		this.closeStroke();
	}
	
	protected boolean hasTextDrawn() {
		return (_usedFont.size() > 0);
	}
	
	protected boolean hasImageDrawn() {
		return (_usedImage.size() > 0);
	}
	
	protected Set<Integer> getUsedFontId() {
		return _usedFont;
	}
	
	protected Set<Integer> getUsedImageId() {
		return _usedImage;
	}
	
	private void notimpletedyet() {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		String methodName = ste[2].getMethodName();
		System.err.println("PDFGraphics::"+methodName+"() : Not implemented yet");
	}
	
	private void notimpletedyet(String str) {
		  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		  String methodName = ste[2].getMethodName();
		  System.err.println("PDFGraphics::"+methodName+"() : Not implemented yet " + str);
	}
	
	//-------------------------------------------------------------------------
	// Helper general functions
	//-------------------------------------------------------------------------	
	
	private boolean isEqual(double a, double b) {
		return (_output.num2str(a).compareTo(_output.num2str(b)) == 0);
	}
	
	private boolean isEqual(float a[], float b[]) {
		if ((a == null) && (b != null)) {
			return (b.length == 0) ? true : false;
		}
		if ((a != null) && (b == null)) {
			return (a.length == 0) ? true : false;
		}
		if ((a == null) && (b == null)) {
			return true;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; ++i) {
			if (_output.num2str(a[i]).compareTo(_output.num2str(b[i])) != 0) {
				return false;
			}
		}
		return true;		
	}	
	
	private boolean isEqual(double a[], double b[]) {
		if ((a == null) && (b != null)) {
			return (b.length == 0) ? true : false;
		}
		if ((a != null) && (b == null)) {
			return (a.length == 0) ? true : false;
		}
		if ((a == null) && (b == null)) {
			return true;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; ++i) {
			if (_output.num2str(a[i]).compareTo(_output.num2str(b[i])) != 0) {
				return false;
			}
		}
		return true;		
	}
	
	private boolean isEqual(int a, int b) {
		return a == b;
	}
	
	private boolean isEqual(int a[], int b[]) {
		if ((a == null) && (b != null)) {
			return (b.length == 0) ? true : false;
		}
		if ((a != null) && (b == null)) {
			return (a.length == 0) ? true : false;
		}
		if ((a == null) && (b == null)) {
			return true;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; ++i) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isSameStroke(BasicStroke a, BasicStroke b) {
		if (a.getEndCap() != a.getEndCap())
			return false;
		
		if (a.getLineJoin() != a.getLineJoin())
			return false;
		
		if (isEqual(a.getLineWidth(), b.getLineWidth()) == false)
			return false;
		
		if (a.getLineJoin() == BasicStroke.JOIN_MITER) {
			if (isEqual(a.getMiterLimit(), b.getMiterLimit()) == false)
				return false;
		}
		
		if (isEqual(a.getDashArray(), b.getDashArray()) == false)
			return false;
		
		if (isEqual(a.getDashPhase(), b.getDashPhase()) == false)
			return false;
		
		return true;
	}	
	
	//-------------------------------------------------------------------------
	// Helper drawing functions
	//-------------------------------------------------------------------------	
	
	private void closeStroke() {
		if (_lastStrokePoint != null) {
			_output.strokePath();
			_lastStrokePoint = null;
		}
	}	
	
	private void draw_polygon(int[] xp, int[] yp, int np) {
		_output.moveTo(xp[0], yp[0]);
		for(int i = 1; i < np; i++) {
			_output.lineTo(xp[i], yp[i]);
		}
	}
	
	private int draw_shape(Shape s) {
		PathIterator points = s.getPathIterator(null);
		int segments = 0;
		float[] coords = new float[6];
		while(!points.isDone()) {
			segments++;
			int segtype = points.currentSegment(coords);
			switch(segtype) {
				case PathIterator.SEG_CLOSE:
					_output.closePath();
					break;
				case PathIterator.SEG_CUBICTO:
					_output.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					break;
				case PathIterator.SEG_LINETO:
					_output.lineTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_MOVETO:
					_output.moveTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_QUADTO:
					_output.curveTo(coords[0], coords[1], coords[2], coords[3]);
					break;
			}
			points.next();
		}
		
		if (segments == 0) {
			return -1;
		} else {
			return points.getWindingRule();
		}
	}
	
	private boolean  drawImage_(Image image, int x, int y, int width, int height) {
		try {
			int id = _imageFactory.getImageId(image);
			_output.drawImage(x, y, width, height, id);
			_usedImage.add(id);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}	
	
	//-------------------------------------------------------------------------
	// Graphics2D methods fully overridden
	//-------------------------------------------------------------------------
	
	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		if (_lastStrokePoint != null) {
			if ((_lastStrokePoint.x != x1) || (_lastStrokePoint.y != y1)) {
				_output.moveTo(x1,y1);	
			}
		} else {
			_output.moveTo(x1,y1);
		}
		_output.lineTo(x2,y2);
		_lastStrokePoint = new Point(x2, y2);
	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
		closeStroke();
		_output.rectangle(x, y, width, height);
		_output.strokePath();
	}
	
	@Override
	public void fillRect(int x, int y, int width, int height) {
		closeStroke();
		_output.rectangle(x, y, width, height);
		_output.fillStrokePath();	
	}	
	
	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		closeStroke();
		draw_polygon(xPoints, yPoints, nPoints);
		_output.closeStrokePath();
	}
	
	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		closeStroke();
		draw_polygon(xPoints, yPoints, nPoints);
	    _output.closeFillStrokePath();
	}
	
	@Override
	public void drawOval(int x, int y, int width, int height) {
		draw(new Ellipse2D.Double(x, y, width, height));
	}
	
	@Override
	public void drawString(String str, int x, int y) {
		closeStroke();
		if (_drawFontAsVector) {
			TextLayout layout = new TextLayout(str, getFont(), getFontRenderContext());
			//layout.draw(this, x, y);
			Shape s = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
			fill(s);
		} else { //draw font as text
			int fontId = _fontFactory.getFontId();
			int size = _fontFactory.getFontSize();
			double[] matrix = null;
			if (_fontFactory.getJavaFont().isTransformed()) {
				AffineTransform transform = AffineTransform.getTranslateInstance(x, _output.getHeight() - y);
				transform.concatenate(_fontFactory.getJavaFont().getTransform());
				matrix = new double[6];
				transform.getMatrix(matrix);
				
				double tmp = matrix[2];
				matrix[2] = matrix[1];
				matrix[1] = tmp;				
				
				_output.drawString(str, matrix, fontId, size);
			} else {
				_output.drawString(str, x, y, fontId, size);
			}
			_usedFont.add(fontId);
		}
	}

	@Override
	public void drawString(String str, float x, float y) {
		closeStroke();
		if (_drawFontAsVector) {
			TextLayout layout = new TextLayout(str, getFont(), getFontRenderContext());
			//layout.draw(this, x, y);
			Shape s = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
			fill(s);
		} else { //draw font as text
			int fontId = _fontFactory.getFontId();
			int size = _fontFactory.getFontSize();
			double[] matrix = null;
			if (_fontFactory.getJavaFont().isTransformed()) {
				AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
				transform.concatenate(_fontFactory.getJavaFont().getTransform());
				matrix = new double[6];
				transform.getMatrix(matrix);
				
				double tmp = matrix[2];
				matrix[2] = matrix[1];
				matrix[1] = tmp;
				
				_output.drawString(str, matrix, fontId, size);
			} else {
				_output.drawString(str, x, y, fontId, size);
			}
			_usedFont.add(fontId);
		}
	}
	
	@Override
	public void draw(Shape shape) {
		closeStroke();
		int windingRule = draw_shape(shape);
		if (windingRule != -1) {
			_output.strokePath();
		}
	}
	
	@Override
	public void fill(Shape shape) {
		closeStroke();
		int windingRule = draw_shape(shape);
        if (windingRule == PathIterator.WIND_EVEN_ODD) {
        	_output.fillStrokePath_WIND_EVEN_ODD();
        } else if (windingRule == PathIterator.WIND_NON_ZERO) {
        	_output.fillStrokePath_WIND_NON_ZERO();
        }
	}
	
	@Override
	public Font getFont() {
		return _fontFactory.getJavaFont();
	}
	
	@Override
	public void setFont(Font font) {
		_fontFactory.setJavaFont(font);
	}
	

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return this.drawImage(img, x, y, null, observer);
	}
	
	@Override
	public Color getColor() {
		return _currentColor;
	}
	
	@Override
	public void setStroke(Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke) stroke;
			
			//test if same stroke as the current one
			if (isSameStroke(bs, _currentStroke)) {
				return ;
			}
			
			_output.strokePath(); //if not same, close all current drawing operation
			
			//if not same cap, update it
			if (bs.getEndCap() != _currentStroke.getEndCap()) {
				int cap = bs.getEndCap();
				if (cap == BasicStroke.CAP_BUTT) {
					_output.setLineCap(PDFGraphicsStream.BUTT_CAP);
				} else if (cap == BasicStroke.CAP_ROUND) {
					_output.setLineCap(PDFGraphicsStream.ROUND_CAP);
				} else if (cap == BasicStroke.CAP_SQUARE) {
					_output.setLineCap(PDFGraphicsStream.PROJECTING_SQUARE);
				}
			}
			
			//if not same line join, update it
			if (bs.getLineJoin() != _currentStroke.getLineJoin()) {
				int join = bs.getLineJoin();
				if (join == BasicStroke.JOIN_BEVEL) {
					_output.setLineJoin(PDFGraphicsStream.JOIN_BEVEL);
				} else if (join == BasicStroke.JOIN_MITER) {
					_output.setLineJoin(PDFGraphicsStream.JOIN_MITER);
				} else if (join == BasicStroke.JOIN_ROUND) {
					_output.setLineJoin(PDFGraphicsStream.JOIN_ROUND);
				}
			}
			
			//if not same line width, update it
			if (isEqual(bs.getLineWidth(), _currentStroke.getLineWidth()) == false) {
				_output.setLineWidth(bs.getLineWidth());
			}

			//if not same mitter limit, update it
			if (bs.getLineJoin() == BasicStroke.JOIN_MITER) {
				if (_currentStroke.getLineJoin() != BasicStroke.JOIN_MITER) {
					_output.setMitterLimit(bs.getMiterLimit());
				} else if (isEqual(bs.getMiterLimit(), _currentStroke.getMiterLimit()) == false) {
					_output.setMitterLimit(bs.getMiterLimit());
				}
			}
			
			if ((isEqual(bs.getDashArray(), _currentStroke.getDashArray()) == false) || (isEqual(bs.getDashPhase(), _currentStroke.getDashPhase()) == false)) {
				_output.setDashArrayAndPhase(bs.getDashArray(), bs.getDashPhase());
			}
			
			_currentStroke = bs;
		} else {
			//Note: there is no another known Stroke implementation (subclass) in java; so we will never be here
			notimpletedyet("Stroke not an instanceof BasicStroke");
		}
	}
	
	@Override
	public Stroke getStroke() {
		return _currentStroke;
	}
	
	//-------------------------------------------------------------------------
	// Graphics2D methods partially overridden
	//-------------------------------------------------------------------------	
	
	@Override
	public void setColor(Color c) {
		if (c.getAlpha() != 255) {
			//TODO
			notimpletedyet(" with alpha != 255");
		} else {
			closeStroke();
			_output.setColor(c.getRed(), c.getGreen(), c.getBlue());
		}
		_currentColor = c;
	}

	
	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		if (observer != null) {
			//TODO
			notimpletedyet(" with observer != null");
			return false;
		} else {
			int width = img.getWidth(null);
			int height = img.getHeight(null);
			if ((width == -1) || (height == -1)) {
				notimpletedyet("img is not completly loaded");
				return false;
			}
			
			if (bgcolor != null) {
				Color c = this.getColor();
				this.setColor(bgcolor);
				this.fillRect(x, y, width, height);
				this.setColor(c);
			}
			return this.drawImage_(img, x, y, width, height);
		}
	}	
	
	@Override
	public FontRenderContext getFontRenderContext() {
		//TODO: What to do exactly??
		boolean antialias = RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING));
		boolean fractions = RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS));
		return new FontRenderContext(new AffineTransform(), antialias, fractions);
	}
	
	@Override
	public FontMetrics getFontMetrics(Font font) {
		// TODO Find a better way for creating a new FontMetrics instance
		BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics g = bi.getGraphics();
		FontMetrics fontMetrics = g.getFontMetrics(font);
		g.dispose();
		bi = null;
		return fontMetrics;
	}
	
	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		draw(g.getOutline(x, y));
	}
	
	
	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		_renderingHints.putAll(hints);
	}
	
	@Override
	public Object getRenderingHint(Key key) {
		//TODO ?
		/*
		if (RenderingHints.KEY_ANTIALIASING.equals(key)) {
			return RenderingHints.VALUE_ANTIALIAS_OFF;
		} else if (RenderingHints.KEY_TEXT_ANTIALIASING.equals(key)) {
			return RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
		} else if (RenderingHints.KEY_FRACTIONALMETRICS.equals(key)) {
			return RenderingHints.VALUE_FRACTIONALMETRICS_ON;
		}
		*/
		return _renderingHints.get(key);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return _renderingHints;
	}
	
	@Override
	public void setRenderingHint(Key key, Object value) {
		if (value != null) {
			_renderingHints.put(key, value);
		} else {
			_renderingHints.remove(key);
		}
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		_renderingHints.clear();
		_renderingHints.putAll(hints);	
	}	
	
	//-------------------------------------------------------------------------
	// Graphics2D methods not yet overridden
	//-------------------------------------------------------------------------
	
	
	@Override
	public void rotate(double theta) {
		//TODO _output.strokePath(); //if not same, close all current drawing operation
		notimpletedyet();
		//_output.writeRotate(theta);
	}	
	
	@Override
	public void translate(double tx, double ty) {
		//TODO _output.strokePath(); //if not same, close all current drawing operation
		notimpletedyet();
		//_output.writeTranslate(tx, ty);
	}
	
	@Override
	public void rotate(double theta, double x, double y) {
		notimpletedyet();
		//TODO
		//translate(x, y);
		//rotate(theta);
		//translate(-x, -y);
	}
	
	@Override
	public void scale(double sx, double sy) {
		//TODO _output.strokePath(); //if not same, close all current drawing operation
		notimpletedyet();
		//_output.writeScale(sx, sy);
	}
	
	@Override
	public void shear(double shx, double shy) {
		//TODO _output.strokePath(); //if not same, close all current drawing operation
		notimpletedyet();
		//_output.writeSkew(shx, shy);
	}	
	
	@Override
	public void clip(Shape arg0) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public boolean drawImage(Image arg0, AffineTransform arg1, ImageObserver arg2) {
		// TODO Auto-generated method stub
		notimpletedyet("method 1");
		return false;
	}

	@Override
	public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		notimpletedyet("method 2");
	}

	@Override
	public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
		// TODO Auto-generated method stub
		notimpletedyet("method 3");
	}

	@Override
	public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
		// TODO Auto-generated method stub
		notimpletedyet("method 4");
	}
	
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		// TODO Auto-generated method stub
		notimpletedyet("method 5");
		return false;
	}	

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		notimpletedyet("method 6");
		return false;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		// TODO Auto-generated method stub
		notimpletedyet("method 7");
		return false;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		notimpletedyet("method 8");
		return false;
	}	
	
	@Override
	public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void drawString(AttributedCharacterIterator arg0, float arg1,
			float arg2) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public Color getBackground() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public Composite getComposite() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public Paint getPaint() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public AffineTransform getTransform() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
		// TODO Auto-generated method stub
		notimpletedyet();
		return false;
	}

	@Override
	public void setBackground(Color arg0) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void setComposite(Composite arg0) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void setPaint(Paint arg0) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void setTransform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		notimpletedyet();	
	}

	@Override
	public void transform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void translate(int arg0, int arg1) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public Graphics create() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public Shape getClip() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public Rectangle getClipBounds() {
		// TODO Auto-generated method stub
		notimpletedyet();
		return null;
	}

	@Override
	public void setClip(Shape clip) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void setPaintMode() {
		// TODO Auto-generated method stub
		notimpletedyet();
	}

	@Override
	public void setXORMode(Color c1) {
		// TODO Auto-generated method stub
		notimpletedyet();
	}


}
