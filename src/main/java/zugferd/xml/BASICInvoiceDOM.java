/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 2014-2015 iText Group NV
 * Authors: Bruno Lowagie, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package zugferd.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author iText
 */
public class BASICInvoiceDOM {
    public static final String XML = "resources/zugferd/basic.xml";
    
    protected Document doc;
    
    public BASICInvoiceDOM() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	doc = docBuilder.parse(XML);
    }
    
    public void importData(BASICInvoice data) {
        setNodeContent(doc, "udt:Indicator", 0, data.getTestIndicator() ? "true" : "false");
        setNodeContent(doc, "ram:ID", 1, data.getId());
        setNodeContent(doc, "ram:Name", 0, data.getName());
        setNodeContent(doc, "ram:TypeCode", 0, data.getTypeCode());
        setDateTime(doc, "ram:IssueDateTime", 0, data.getDateTime(), data.getDateTimeFormat());
        setNodeSubContent(doc, "ram:IncludedNote", 0, data.getNotes(), null);
        processTraderParty(doc, "ram:SellerTradeParty", 0,
                data.getSellerName(), data.getSellerPostcode(),
                data.getSellerLineOne(), data.getSellerLineTwo(),
                data.getSellerCityName(), data.getSellerCountryID(),
                data.getSellerTaxRegistrationID(), data.getSellerTaxRegistrationSchemeID());
        processTraderParty(doc, "ram:BuyerTradeParty", 0,
                data.getBuyerName(), data.getBuyerPostcode(),
                data.getBuyerLineOne(), data.getBuyerLineTwo(),
                data.getBuyerCityName(), data.getBuyerCountryID(),
                data.getBuyerTaxRegistrationID(), data.getBuyerTaxRegistrationSchemeID());
        setDateTime(doc, "ram:OccurrenceDateTime", 0, data.getDateTime(), data.getDateTimeFormat());
        setNodeContent(doc, "ram:PaymentReference", 0, data.getPaymentReference());
        setNodeContent(doc, "ram:InvoiceCurrencyCode", 0, data.getInvoiceCurrencyCode());
        processPaymentMeans(doc, data);
        processTax(doc, data);
        setNodeContent(doc, "ram:LineTotalAmount", 0, data.getLineTotalAmount(), "currencyID", data.getLineTotalAmountCurrencyID());
        setNodeContent(doc, "ram:ChargeTotalAmount", 0, data.getChargeTotalAmount(), "currencyID", data.getChargeTotalAmountCurrencyID());
        setNodeContent(doc, "ram:AllowanceTotalAmount", 0, data.getAllowanceTotalAmount(), "currencyID", data.getAllowanceTotalAmountCurrencyID());
        setNodeContent(doc, "ram:TaxBasisTotalAmount", 0, data.getTaxBasisTotalAmount(), "currencyID", data.getTaxBasisTotalAmountCurrencyID());
        setNodeContent(doc, "ram:TaxTotalAmount", 0, data.getTaxTotalAmount(), "currencyID", data.getTaxTotalAmountCurrencyID());
        setNodeContent(doc, "ram:GrandTotalAmount", 0, data.getGrandTotalAmount(), "currencyID", data.getGrandTotalAmountCurrencyID());
        processLines(doc, data);
    }
    
    protected void setNodeContent(Document doc, String tagname, int idx, String content, String... attributes) {
        Node node = doc.getElementsByTagName(tagname).item(idx);
        if (node == null)
            return;
        node.setTextContent(content);
        int n = attributes.length;
        if (n == 0) return;
        String attrName, attrValue;
        NamedNodeMap attrs = node.getAttributes();
        Node attr;
        for (int i = 0; i < n; i++) {
            attrName = attributes[i];
            if (++i == n) continue;
            attrValue = attributes[i];
            attr = attrs.getNamedItem(attrName);
            if (attr != null)
                attr.setTextContent(attrValue);
        }
    }
    
    protected void setDateTime(Document doc, String tagname, int idx, String datetime, String format) {
        Node node = doc.getElementsByTagName(tagname).item(idx);
        if (node == null)
            return;
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                childNode.setTextContent(datetime);
                Node attr = childNode.getAttributes().item(0);
                attr.setTextContent(format);
            }
        }
    }
    
    protected void setNodeSubContent(Document doc, String tagname, int idx, String[] content, String[] attrs) {
        if (content.length == 0)
            return;
        Node node = doc.getElementsByTagName(tagname).item(idx);
        for (String text : content) {
            Node newNode = node.cloneNode(true);
            NodeList list = newNode.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node childNode = list.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    childNode.setTextContent(text);
                    if (attrs != null && attrs.length <= i) {
                        Node attr = childNode.getAttributes().item(0);
                        attr.setTextContent(attrs[i]);
                    }
                }
            }
            Node parent = node.getParentNode();
            parent.insertBefore(newNode, node);
        }
    }

    protected void setNodeSubContent(Node parent, Node node, String[] content, String[] attrs) {
        if (content.length == 0)
            return;
        for (String text : content) {
            Node newNode = node.cloneNode(true);
            NodeList list = newNode.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node childNode = list.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    childNode.setTextContent(text);
                    if (attrs != null && attrs.length <= i) {
                        Node attr = childNode.getAttributes().item(0);
                        attr.setTextContent(attrs[i]);
                    }
                }
            }
            parent.insertBefore(newNode, node);
        }
    }
    
    protected void processTraderParty(Document doc, String tagname, int idx,
        String name, String postcode, String lineOne, String lineTwo,
        String cityName, String countryID,
        String[] taxRegistrationID, String[] taxRegistrationSchemeID) {
        Node node = doc.getElementsByTagName(tagname).item(idx);
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if ("ram:Name".equals(childNode.getNodeName())) {
                childNode.setTextContent(name);
            }
            else if ("ram:PostalTradeAddress".equals(childNode.getNodeName()))
                processPostalAddres(childNode, postcode, lineOne, lineTwo, cityName, countryID);
            else if ("ram:SpecifiedTaxRegistration".equals(childNode.getNodeName())) {
                setNodeSubContent(node, childNode, taxRegistrationID, taxRegistrationSchemeID);
                break;
            }
        }
    }

    protected void processPostalAddres(Node node, String... content) {
        NodeList list = node.getChildNodes();
        int counter = 0;
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            childNode.setTextContent(content[counter++]);
        }
    }
    
    protected void processPaymentMeans(Document doc, BASICInvoice data) {
        String[] pmID = data.getPaymentMeansID();
        String[] pmSchemeAgencyID = data.getPaymentMeansSchemeAgencyID();
        String[] pmIBAN = data.getPaymentMeansPayeeAccountIBAN();
        String[] pmAccountName = data.getPaymentMeansPayeeAccountAccountName();
        String[] pmAccountID = data.getPaymentMeansPayeeAccountProprietaryID();
        String[] pmBIC = data.getPaymentMeansPayeeFinancialInstitutionBIC();
        String[] pmGermanBankleitzahlID = data.getPaymentMeansPayeeFinancialInstitutionGermanBankleitzahlID();
        String[] pmFinancialInst = data.getPaymentMeansPayeeFinancialInstitutionName();
        for (int i = 0; i < pmID.length; i++) {
            processPaymentMeans(doc, data, i,
                    pmID[i],
                    pmSchemeAgencyID[i],
                    pmIBAN[i],
                    pmAccountName[i],
                    pmAccountID[i],
                    pmBIC[i],
                    pmGermanBankleitzahlID[i],
                    pmFinancialInst[i]
            );
        }
    }
    
    protected void processPaymentMeans(Document doc, BASICInvoice data, int n,
        String... paymentMeans) {
        Node node = doc.getElementsByTagName("ram:SpecifiedTradeSettlementPaymentMeans").item(0);
        Node newNode = node.cloneNode(true);
        NodeList list = newNode.getChildNodes();
        Node childNode;
        NodeList l;
        Node grandchildNode;
        int counter = 0;
        for (int i = 0; i < list.getLength(); i++) {
            childNode = list.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                if (counter == 0) {
                    childNode.setTextContent(paymentMeans[counter++]);
                    childNode.getAttributes().item(0).setTextContent(paymentMeans[counter++]);
                }
                else {
                    l = childNode.getChildNodes();
                    for (int j = 0; j < l.getLength(); j++) {
                        grandchildNode = l.item(j);
                        if (grandchildNode.getNodeType() == Node.ELEMENT_NODE) {
                            grandchildNode.setTextContent(paymentMeans[counter++]);
                        }
                        else {
                            grandchildNode.setTextContent("");
                        }
                    }
                }
            }
        }
        Node parent = node.getParentNode();
        parent.insertBefore(newNode, node);
    }
    
    protected void processTax(Document doc, BASICInvoice data) {
        String[] calculated = data.getTaxCalculatedAmount();
        String[] calculatedCurr = data.getTaxCalculatedAmountCurrencyID();
        String[] typeCode = data.getTaxTypeCode();
        String[] basisAmount = data.getTaxBasisAmount();
        String[] basisAmountCurr = data.getTaxBasisAmountCurrencyID();
        String[] percent = data.getTaxApplicablePercent();
        for (int i = 0; i < calculated.length; i++) {
            processTax(doc, calculated[i], calculatedCurr[i],
                    typeCode[i], basisAmount[i], basisAmountCurr[i], percent[i]);
        }
    }
    
    protected void processTax(Document doc, String... content) {
        Node node = doc.getElementsByTagName("ram:ApplicableTradeTax").item(0);
        int counter = 0;
        Node newNode = node.cloneNode(true);
        Node childNode;
        NodeList list = newNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            childNode = list.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                childNode.setTextContent(content[counter++]);
                if (counter == 1 || counter == 4) {
                    childNode.getAttributes().item(0).setTextContent(content[counter++]);
                }
            }
        }
        Node parent = node.getParentNode();
        parent.insertBefore(newNode, node);
    }
    
    protected void processLines(Document doc, BASICInvoice data) {
        String[] quantity = data.getBilledQuantity();
        String[] quantityCode = data.getBilledQuantityUnitCode();
        String[] name = data.getSpecifiedTradeProductName();
        for (int i = quantity.length - 1; i >= 0; i--) {
            processLine(doc, quantity[i], quantityCode[i], name[i]);
        }
    }
    
    protected void processLine(Document doc, String quantity, String code, String name) {
        Node node = doc.getElementsByTagName("ram:IncludedSupplyChainTradeLineItem").item(0);
        Node newNode = node.cloneNode(true);
        Node childNode;
        NodeList list = newNode.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            childNode = list.item(i);
            if ("ram:SpecifiedSupplyChainTradeDelivery".equals(childNode.getNodeName())) {
                NodeList l = childNode.getChildNodes();
                for (int j = 0; j < l.getLength(); j++) {
                    Node grandchildNode = l.item(j);
                    if (grandchildNode.getNodeType() == Node.ELEMENT_NODE) {
                        grandchildNode.setTextContent(quantity);
                        grandchildNode.getAttributes().item(0).setTextContent(code);
                    }
                }
            }
            else if ("ram:SpecifiedTradeProduct".equals(childNode.getNodeName())) {
                NodeList l = childNode.getChildNodes();
                for (int j = 0; j < l.getLength(); j++) {
                    Node grandchildNode = l.item(j);
                    if (grandchildNode.getNodeType() == Node.ELEMENT_NODE)
                        grandchildNode.setTextContent(name);
                }
            }
        }
        Node parent = node.getParentNode();
        parent.insertBefore(newNode, node);
    }
    
    public byte[] exportDoc() throws TransformerException {
        removeNodes(doc);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	DOMSource source = new DOMSource(doc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Result result = new StreamResult(out);
        transformer.transform(source, result);
        return out.toByteArray();
    }
    
    protected static void removeNodes(Node node) {
        NodeList list = node.getChildNodes();
        for (int i = list.getLength() - 1; i >= 0; i--) {
            removeNodes(list.item(i));
        }
        boolean emptyElement = node.getNodeType() == Node.ELEMENT_NODE
            && node.getChildNodes().getLength() == 0;
        boolean emptyText = node.getNodeType() == Node.TEXT_NODE
            && node.getNodeValue().trim().isEmpty();
        if (emptyElement || emptyText) {
            node.getParentNode().removeChild(node);
        }
    }
}