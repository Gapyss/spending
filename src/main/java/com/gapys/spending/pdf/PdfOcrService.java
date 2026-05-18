package com.gapys.spending.pdf;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfOcrService {

    private final PdfProperties pdfProperties;
    private final OcrProperties ocrProperties;

    public PdfOcrService(PdfProperties pdfProperties, OcrProperties ocrProperties) {
        this.pdfProperties = pdfProperties;
        this.ocrProperties = ocrProperties;
    }

    public OcrResult extract(InputStream pdfStream) throws IOException, TesseractException {
        try (PDDocument document = openDocument(pdfStream)) {
            PDFRenderer renderer = new PDFRenderer(document);
            ITesseract tesseract = newTesseract();

            List<PageText> pages = new ArrayList<>();
            StringBuilder all = new StringBuilder();

            int pageCount = document.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, ocrProperties.getDpi(), ImageType.GRAY);
                String text = tesseract.doOCR(image);
                pages.add(new PageText(i + 1, text));
                all.append(text);
                if (i < pageCount - 1) {
                    all.append("\n\n");
                }
            }
            return new OcrResult(pageCount, pages, all.toString());
        }
    }

    private PDDocument openDocument(InputStream pdfStream) throws IOException {
        String password = pdfProperties.getPassword() == null ? "" : pdfProperties.getPassword();
        return Loader.loadPDF(new RandomAccessReadBuffer(pdfStream), password);
    }

    private ITesseract newTesseract() {
        Tesseract tesseract = new Tesseract();
        if (ocrProperties.getTessdataPath() != null && !ocrProperties.getTessdataPath().isBlank()) {
            tesseract.setDatapath(ocrProperties.getTessdataPath());
        }
        tesseract.setLanguage(ocrProperties.getLanguages());
        tesseract.setPageSegMode(3);
        return tesseract;
    }

    public record PageText(int page, String text) {}

    public record OcrResult(int pageCount, List<PageText> pages, String text) {}
}
