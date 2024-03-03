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
import ir.behrooz.loan.common.DateUtil;
import ir.behrooz.loan.common.LanguageUtils;
import ir.behrooz.loan.common.sql.DBUtil;
import ir.behrooz.loan.common.sql.Oprator;
import ir.behrooz.loan.common.sql.WhereCondition;
import ir.behrooz.loan.entity.DebitCreditEntityDao;
import ir.behrooz.loan.entity.LoanEntity;
import ir.behrooz.loan.entity.PersonEntity;

import static ir.behrooz.loan.common.StringUtil.moneySeparator;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.Date;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.LoanId;
import static ir.behrooz.loan.entity.DebitCreditEntityDao.Properties.PayStatus;


public class LoanListPDF extends BasePDF {

    public LoanListPDF(Context context) {
        super(context);
    }

    @Override
    protected void createTable(Paragraph paragraph, Object list) {
        Long loanSum = 0L;
        Long remainSum = 0L;

        for (int i = 0; i < ((List) list).size(); i++) {
            Object object = ((List) list).get(i);
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            LoanEntity entity = (LoanEntity) object;

            PdfPTable tbl6 = new PdfPTable(1);
            tbl6.addCell(createPdfPCellNoBorder(i, context.getString(R.string.installment), font_MD_14));
            tbl6.addCell(createPdfPCellNoBorder(i, LanguageUtils.getPersianNumbers(entity.getInstallment() + ""), font_LT_12));
            table.addCell(tbl6);

            PdfPTable tbl7 = new PdfPTable(1);
            tbl7.addCell(createPdfPCellNoBorder(i, context.getString(R.string.dayOfMonth), font_MD_14));
            tbl7.addCell(createPdfPCellNoBorder(i, LanguageUtils.getPersianNumbers(entity.getDayInMonth() + ""), font_LT_12));
            table.addCell(tbl7);

            PdfPTable tbl8 = new PdfPTable(1);
            tbl8.addCell(createPdfPCellNoBorder(i, context.getString(R.string.loanAmount), font_MD_14));
            tbl8.addCell(createPdfPCellNoBorder(i, moneySeparator(context, entity.getValue()), font_LT_12));
            loanSum += entity.getValue();
            table.addCell(tbl8);

            PdfPTable tbl9 = new PdfPTable(1);
            tbl9.addCell(createPdfPCellNoBorder(i, context.getString(R.string.loanTitle), font_MD_14));
            tbl9.addCell(createPdfPCellNoBorder(i, entity.getName(), font_LT_12));
            table.addCell(tbl9);

            PdfPTable tbl10 = new PdfPTable(1);
            tbl10.addCell(createPdfPCellNoBorder(i, context.getString(R.string.name), font_MD_14));
            PersonEntity personEntity = DBUtil.getReadableInstance(context).getPersonEntityDao().load(entity.getPersonId());
            tbl10.addCell(createPdfPCellNoBorder(i, String.format("%s %s", personEntity.getName(), personEntity.getFamily()), font_LT_12));
            table.addCell(tbl10);

            table.addCell(createPdfPCellSmall(0, context.getString(R.string.rowNum), font_MD_14));

            PdfPTable tbl1 = new PdfPTable(1);
            tbl1.addCell(createPdfPCellNoBorder(i, context.getString(R.string.receiveDate), font_MD_14));
            tbl1.addCell(createPdfPCellNoBorder(i, DateUtil.toPersianString(entity.getDate(), false), font_LT_12));
            table.addCell(tbl1);

            PdfPTable tbl2 = new PdfPTable(1);
            long delayedCount = DBUtil.getReadableInstance(context).getDebitCreditEntityDao().queryBuilder().where(LoanId.eq(entity.getId()), PayStatus.eq(false), Date.lt(new Date())).count();
            tbl2.addCell(createPdfPCellNoBorder(i, context.getString(R.string.delayed), font_MD_14));
            tbl2.addCell(createPdfPCellNoBorder(i, LanguageUtils.getPersianNumbers(delayedCount + ""), font_LT_12));
            table.addCell(tbl2);

            PdfPTable tbl3 = new PdfPTable(1);
            tbl3.addCell(createPdfPCellNoBorder(i, context.getString(R.string.remainLoan), font_MD_14));
            Long remain = DBUtil.sum(context, DebitCreditEntityDao.Properties.Value, DebitCreditEntityDao.TABLENAME, new WhereCondition(LoanId, entity.getId().toString(), Oprator.EQUAL, "AND"), new WhereCondition(PayStatus, "0", Oprator.EQUAL));
            remainSum += remain;
            tbl3.addCell(createPdfPCellNoBorder(i, moneySeparator(context, remain), font_LT_12));
            table.addCell(tbl3);

            PdfPTable tbl4 = new PdfPTable(1);
            tbl4.addCell(createPdfPCellNoBorder(i, context.getString(R.string.settled), font_MD_14));
            tbl4.addCell(createPdfPCellNoBorder(i, entity.getSettled() ? context.getString(R.string.done) : context.getString(R.string.not), font_LT_12));
            table.addCell(tbl4);

            PdfPTable tbl5 = new PdfPTable(1);
            tbl5.addCell(createPdfPCellNoBorder(i, context.getString(R.string.installmentAmount), font_MD_14));
            tbl5.addCell(createPdfPCellNoBorder(i, moneySeparator(context, entity.getInstallmentAmount()), font_LT_12));
            table.addCell(tbl5);

            table.addCell(createPdfPCellSmall(i, (i + 1) + "", font_LT_12));

            paragraph.add(table);
        }

        PdfPTable sumTbl = new PdfPTable(4);
        sumTbl.setWidthPercentage(100);
        sumTbl.addCell(createPdfPCell(0, moneySeparator(context, remainSum), font_LT_12, PdfPCell.BOX, Element.ALIGN_LEFT));
        sumTbl.addCell(createPdfPCell(0, context.getString(R.string.remainSum), font_MD_14, PdfPCell.BOX, Element.ALIGN_RIGHT));
        sumTbl.addCell(createPdfPCell(0, moneySeparator(context, loanSum), font_LT_12, PdfPCell.BOX, Element.ALIGN_LEFT));
        sumTbl.addCell(createPdfPCell(0, context.getString(R.string.loanSum), font_MD_14, PdfPCell.BOX, Element.ALIGN_RIGHT));

        paragraph.add(sumTbl);
    }

    @Override
    public void createPdf(String fileName, Object list) {
        try {
            Document document = createDocument(fileName);
            pageHeader(document, context.getString(R.string.title_activity_loan_list));
            pageContent(document, list);
            document.close();
        } catch (Exception e) {
            Log.e("LoanList-PDF", "Exception thrown while creating PDF", e);
        }
    }
}
