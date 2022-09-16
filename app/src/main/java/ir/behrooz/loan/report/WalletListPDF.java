package ir.behrooz.loan.report;

import android.content.Context;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.WalletEntity;

import static ir.behrooz.loan.common.StringUtil.moneySeparator;


public class WalletListPDF extends BasePDF {

    public WalletListPDF(Context context) {
        super(context);
    }

    @Override
    protected void createTable(Paragraph paragraph, Object list) {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell(createPdfPCell(0, context.getString(R.string.description), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.amount), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.status), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.date), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.fullName), font_MD_14));
        table.addCell(createPdfPCellSmall(0, context.getString(R.string.rowNum), font_MD_14));

        table.setHeaderRows(1);
        Long sum = 0L;
        for (int i = 0; i < ((List) list).size(); i++) {
            Object object = ((List) list).get(i);
            WalletEntity entity = (WalletEntity) object;
            table.addCell(createPdfPCell(i, entity.getDescription() == null ? "---" : entity.getDescription()));
            table.addCell(createPdfPCell(i, moneySeparator(context, entity.getValue())));
            table.addCell(createPdfPCell(i, entity.getStatus() ? context.getString(R.string.deposit) : context.getString(R.string.withdrawal)));
            if (entity.getStatus())
                sum += entity.getValue();
            else sum -= entity.getValue();
            table.addCell(createPdfPCell(i, DateUtil.toPersianString(entity.getDate(), false)));
            PersonEntity personEntity = DBUtil.getReadableInstance(context).getPersonEntityDao().load(entity.getPersonId());
            table.addCell(createPdfPCell(i, String.format("%s %s", personEntity.getName(), personEntity.getFamily())));
            table.addCell(createPdfPCellSmall(i, (i + 1) + "", font_LT_12));
        }
        paragraph.add(table);

        PdfPTable sumTbl = new PdfPTable(2);
        sumTbl.setWidthPercentage(100);
        sumTbl.addCell(createPdfPCell(0, moneySeparator(context, sum), font_LT_12, PdfPCell.BOX, Element.ALIGN_LEFT));
        sumTbl.addCell(createPdfPCell(0, context.getString(R.string.totalSum), font_MD_14, PdfPCell.BOX, Element.ALIGN_RIGHT));

        paragraph.add(sumTbl);
    }

    @Override
    public void createPdf(String fileName, Object list) {
        try {
            Document document = createDocument(fileName);
            pageHeader(document, context.getString(R.string.title_activity_wallet_list));
            pageContent(document, list);
            document.close();
        } catch (Exception e) {
            Log.e("WalletList-PDF", "Exception thrown while creating PDF", e);
        }
    }
}
