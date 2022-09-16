package ir.behrooz.loan.report;

import android.content.Context;
import android.os.Environment;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.mojtaba.materialdatetimepicker.utils.LanguageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.entity.CashtEntity;

import static ir.behrooz.loan.common.Constants.IRANSANS_BL;
import static ir.behrooz.loan.common.Constants.IRANSANS_LT;
import static ir.behrooz.loan.common.Constants.IRANSANS_MD;

public abstract class BasePDF extends PdfPageEventHelper {

    public BasePDF(Context context) {
        this.context = context;
        this.cashtEntity = new CashtEntity(context);
    }

    protected static Font font_LT_12 = getFont(IRANSANS_LT, 12);
    protected static Font font_MD_14 = getFont(IRANSANS_MD, 12);
    protected static Font font_BL_16 = getFont(IRANSANS_BL, 16);

    Context context;
    PdfTemplate template;
    PdfContentByte contentByte;
    CashtEntity cashtEntity;

    protected static Font getFont(String fontFile, int size) {
        BaseFont font = null;
        try {
            font = BaseFont.createFont("assets/".concat(fontFile), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Font(font, size);
    }

    protected static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    protected PdfPCell createPdfPCell(int i, String text) {
        return createPdfPCell(i, text, font_LT_12, PdfPCell.BOX, Element.ALIGN_CENTER);
    }

    protected PdfPCell createPdfPCell(int i, String text, Font font) {
        return createPdfPCell(i, text, font, PdfPCell.BOX, Element.ALIGN_CENTER);
    }

    protected PdfPCell createPdfPCellNoBorder(int i, String text, Font font) {
        return createPdfPCell(i, text, font, PdfPCell.NO_BORDER, Element.ALIGN_CENTER);
    }

    protected PdfPCell createPdfPCellSmall(int i, String text, Font font) {
        PdfPCell pdfPCell = createPdfPCell(i, text, font, PdfPCell.BOX, Element.ALIGN_CENTER);
        pdfPCell.setPadding(0);
        pdfPCell.setPaddingLeft(0);
        pdfPCell.setPaddingRight(0);
        pdfPCell.setPaddingBottom(5);
        pdfPCell.setPaddingTop(5);
        return pdfPCell;
    }

    protected PdfPCell createPdfPCell(int i, String text, Font font, int border, int horizontalAlignment) {
        PdfPCell pdfPCell = new PdfPCell(new Phrase(text, font));
        pdfPCell.setHorizontalAlignment(horizontalAlignment);
        pdfPCell.setVerticalAlignment(Element.ALIGN_TOP);
        pdfPCell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        pdfPCell.setBorder(border);
        pdfPCell.setPadding(5);
        if (i % 2 == 1)
            pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return pdfPCell;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        this.contentByte = writer.getDirectContent();
        this.template = contentByte.createTemplate(50, 50);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        super.onEndPage(writer, document);
        String text = " زا " + LanguageUtils.getPersianNumbers(writer.getPageNumber() + "") + " ص ";
        Rectangle pageSize = document.getPageSize();
        float textLen = font_LT_12.getBaseFont().getWidthPoint(text, font_LT_12.getSize());
//        float center = (pageSize.LEFT + pageSize.RIGHT) / 2;

        contentByte.setRGBColorFill(100, 100, 100);
        contentByte.beginText();
        contentByte.setFontAndSize(font_MD_14.getBaseFont(), font_MD_14.getSize());
        contentByte.setTextMatrix(pageSize.getRight(90), pageSize.getBottom(30));
        contentByte.showText(text);
        contentByte.endText();

        contentByte.addTemplate(template, pageSize.getRight(90) - 5, pageSize.getBottom(30));
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        template.beginText();
        template.setFontAndSize(font_MD_14.getBaseFont(), font_MD_14.getSize());
        template.setTextMatrix(0, 0);
        //درج تعداد کل صفحات در تمام قالب‌های اضافه شده
        template.showText(LanguageUtils.getPersianNumbers(writer.getPageNumber() + ""));
        template.endText();
    }

    // iText allows to add metadata to the PDF which can be viewed in your Adobe
    // Reader
    // under File -> Properties
    private void pageMetaData(Document document, String title) {
        document.addTitle(title);
        document.addSubject(cashtEntity.getName());
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("behrooz.mohamadi.66@gmail.com");
        document.addCreator("Behrooz Mohamadi");
    }

    protected void pageHeader(Document document, String title) throws DocumentException {
        pageMetaData(document, title);

        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        PdfPTable dateTbl = new PdfPTable(2);
        dateTbl.addCell(createPdfPCell(0, DateUtil.toPersianString(new Date(), false), font_LT_12, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT));
        dateTbl.addCell(createPdfPCell(0, context.getString(R.string.date), font_MD_14, PdfPCell.NO_BORDER, Element.ALIGN_RIGHT));

        PdfPCell pdfPCell = new PdfPCell(dateTbl);
        pdfPCell.setBorder(0);
        table.addCell(pdfPCell);
        table.addCell(createPdfPCellNoBorder(0, title, font_BL_16));
        table.addCell(createPdfPCell(0, cashtEntity.getName(), font_MD_14, PdfPCell.NO_BORDER, Element.ALIGN_LEFT));

        paragraph.add(table);
        addEmptyLine(paragraph, 1);
        document.add(paragraph);
    }

    protected void pageContent(Document document, Object list) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_CENTER);
        createTable(paragraph, list);
        document.add(paragraph);
    }

    protected Document createDocument(String fileName) throws FileNotFoundException, DocumentException {
        Document document = new Document(PageSize.A4);
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, fileName);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(file));
        pdfWriter.setPageEvent(this);
        document.open();
        return document;
    }

    protected abstract void createTable(Paragraph paragraph, Object list);

    public abstract void createPdf(String fileName, Object list);
}
