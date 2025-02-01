JPDF is a java pdf lib, to generate pdf by drawing on Graphics2D.

Code is inspired by gnujpdf.

Example usage:


		PageSize ps = new PageSize(); //get your default printer page size   

		PDF pdf = new PDF(new File("report.pdf"));
		pdf.enableCompression(false);
		pdf.embeddedFonts(false);
		pdf.usedOnlyPdfDefaultFont(false);
		
		pdf.setTitle("My Report");
		pdf.setAuthor("Firstname Lastname");
		pdf.setCreator("My Company");
		pdf.setSubject("My Title");
		pdf.setKeywords("plip,plop,plup");
		
		for (int page = 0; page < 42; ++page) {
			Graphics2D g2d = pdf.newPage(ps.getWidth(), ps.getHeight());
			drawPage(g2d, page, 0); //a function to draw a page on a Graphics2D --> so you can also render your document in your swing application (on sceeen).
			g2d.dispose();
		}
		
		pdf.close();
