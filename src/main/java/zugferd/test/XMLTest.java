/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zugferd.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import zugferd.InvoiceData;
import zugferd.xml.BASICInvoiceDOM;
import zugferd.xml.BASICInvoice;
import zugferd.pojo.Invoice;
import zugferd.pojo.PojoFactory;

/**
 *
 * @author iText
 */
public class XMLTest {
    public static void main(String[] args) throws SQLException, ParserConfigurationException, SAXException, IOException, TransformerException {
        PojoFactory factory = PojoFactory.getInstance();
        List<Invoice> invoices = factory.getInvoices();
        InvoiceData invoiceData = new InvoiceData();
        BASICInvoice basic;
        BASICInvoiceDOM dom;
        for (Invoice invoice : invoices) {
            basic = invoiceData.importInvoice(invoice);
            dom = new BASICInvoiceDOM();
            dom.importData(basic);
            byte[] xml = dom.exportDoc();
            System.out.println(new String(xml));
        }
        factory.close();
    }

}