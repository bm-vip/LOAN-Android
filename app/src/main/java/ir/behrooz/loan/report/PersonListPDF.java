package ir.behrooz.loan.report;

import android.content.Context;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.util.Date;
import java.util.List;

import ir.behrooz.loan.R;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Operator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.PersonEntity;
import ir.behrooz.loan.entity.WalletEntityDao;

import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PersonId;
import static ir.behrooz.loan.entity.WalletEntityDao.Properties.Value;


public class PersonListPDF extends BasePDF {

    public PersonListPDF(Context context) {
        super(context);
    }

    @Override
    protected void createTable(Paragraph paragraph, Object list) {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell(createPdfPCell(0, context.getString(R.string.delayed), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.wallet), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.subscribCode), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.phone), font_MD_14));
        table.addCell(createPdfPCell(0, context.getString(R.string.fullName), font_MD_14));
        table.addCell(createPdfPCellSmall(0, context.getString(R.string.rowNum), font_MD_14));

        table.setHeaderRows(1);
        Long sum = 0L;
        for (int i = 0; i < ((List) list).size(); i++) {
            Object object = ((List) list).get(i);
            PersonEntity entity = (PersonEntity) object;
            long delayedCount = DBUtil.getReadableInstance(context).getDebitCreditEntityDao().queryBuilder().where(PersonId.eq(entity.getId()), PayStatus.eq(false), DebitCreditEntityDao.Properties.Date.lt(new Date())).count();
            table.addCell(createPdfPCell(i, LanguageUtils.getPersianNumbers(delayedCount + "")));
            Long wallet = 0L;
            if (cashtEntity.getWithDeposit()) {
                wallet = DBUtil.sum(context, Value, WalletEntityDao.TABLENAME, new WhereCondition(PersonId, entity.getId().toString()), new WhereCondition(WalletEntityDao.Properties.Status, "1"));
                wallet -= DBUtil.sum(context, Value, WalletEntityDao.TABLENAME, new WhereCondition(PersonId, entity.getId().toString()), new WhereCondition(WalletEntityDao.Properties.Status, "0"));
            }
            else
                wallet = DBUtil.sum(context, Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(PersonId, entity.getId().toString()), new WhereCondition(PayStatus, "1"));
            sum += wallet;
            table.addCell(createPdfPCell(i, moneySeparator(context, wallet)));
            table.addCell(createPdfPCell(i, LanguageUtils.getPersianNumbers(entity.getNationalCode())));
            table.addCell(createPdfPCell(i, LanguageUtils.getPersianNumbers(entity.getPhone())));
            table.addCell(createPdfPCell(i, String.format("%s %s", entity.getName(), entity.getFamily())));
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
            pageHeader(document, context.getString(R.string.title_activity_person_list));
            pageContent(document, list);
            document.close();
        } catch (Exception e) {
            Log.e("PersonList-PDF", "Exception thrown while creating PDF", e);
        }
    }
}
