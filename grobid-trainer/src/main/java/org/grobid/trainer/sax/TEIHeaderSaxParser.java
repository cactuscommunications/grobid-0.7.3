package org.grobid.trainer.sax;

import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * SAX parser for the TEI format header data encoded for training. Normally all training data for the header model 
 * should be in this unique format (which replaces for instance the CORA format). Segmentation of tokens must be 
 * identical as the one from pdf2xml files so that training and online input tokens are aligned.
 *
 * @author Patrice Lopez
 */
public class TEIHeaderSaxParser extends DefaultHandler {

    private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

    private String output = null;
    private String currentTag = null;

    /*private String title = null;
    private String affiliation = null;
    private String address = null;
    private String note = null;
    private String keywords = null;
    private String dateString = null;*/

    private String fileName = null;
    //public TreeMap<String, String> pdfs = null;
    private String pdfName = null;

    private ArrayList<String> labeled = null; // store line by line the labeled data

    public TEIHeaderSaxParser() {
        labeled = new ArrayList<String>();
    }

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        return accumulator.toString().trim();
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public String getPDFName() {
        return pdfName;
    }

    public ArrayList<String> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
        if ((qName.equals("titlePart")) || (qName.equals("note")) || (qName.equals("docAuthor")) ||
                (qName.equals("affiliation")) || (qName.equals("address")) || (qName.equals("email")) ||
                (qName.equals("idno")) || (qName.equals("date")) || (qName.equals("biblScope")) ||
                (qName.equals("keywords")) || (qName.equals("reference")) || (qName.equals("degree")) ||
                (qName.equals("keyword")) || (qName.equals("ptr")) || (qName.equals("div")) || (qName.equals("p")) ||
                (qName.equals("web")) || (qName.equals("english-title")) ||
                (qName.equals("title")) || (qName.equals("introduction")) || (qName.equals("editor")) || 
                (qName.equals("intro")) || (qName.equals("version")) || (qName.equals("meeting")) || (qName.equals("location"))
                ) {
            //String text = getText();
            writeData();

            // we segment the text
            /*StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
            boolean begin = true;
            while (st.hasMoreTokens()) {
                String tok = st.nextToken().trim();
                if (tok.length() == 0) continue;

                if (tok.equals("+L+")) {
                    labeled.add("@newline\n");
                } else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    labeled.add("@newline\n");
                } else {
                    String content = tok;
                    int i = 0;
                    if (content.length() > 0) {
                        if (begin) {
                            labeled.add(content + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add(content + " " + currentTag + "\n");
                        }
                    }
                }

                begin = false;
            }*/

            accumulator.setLength(0);
        } /*else if (qName.equals("lb")) {
            // we note a line break
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            // we note a page break
            accumulator.append(" +PAGE+ ");
        } */
        else if (qName.equals("front")) {
            // write remaining test as <other>
            String text = getText();
            if (text != null) {
                if (text.length() > 0) {
                    currentTag = "<other>";
                    writeData();
                }
            }
            accumulator.setLength(0);
        }
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        if (qName.equals("lb")) {
            accumulator.append(" +LINE+ ");
        } else if (qName.equals("space")) {
            accumulator.append(" ");
        } else {
            // add acumulated text as <other>
            String text = getText();
            if (text != null) {
                if (text.length() > 0) {
                    currentTag = "<other>";
                    writeData();
                }
            }
            accumulator.setLength(0);
        }

        if (qName.equals("div")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("abstract")) {
                            currentTag = "<abstract>";
                        } else if (value.equals("intro") | value.equals("introduction")) {
                            currentTag = "<intro>";
                        } else if (value.equals("paragraph")) {
                            currentTag = "<other>";
                        }
                    }
                }
            }
        } else if (qName.equals("note")) {
            int length = atts.getLength();
            currentTag = "<note>";
            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("phone")) {
                            currentTag = "<phone>";
                        } else if (value.equals("degree")) {
                            currentTag = "<degree>";
                        } else if (value.equals("dedication")) {
                            currentTag = "<dedication>";
                        } else if (value.equals("submission")) {
                            currentTag = "<submission>";
                        } else if (value.equals("english-title")) {
                            currentTag = "<entitle>";
                        } else if (value.equals("other")) {
                            currentTag = "<note>";
                        } else if (value.equals("reference")) {
                            currentTag = "<reference>";
                        } else if (value.equals("copyright")) {
                            currentTag = "<copyright>";
                        } else if (value.equals("grant")) {
                            currentTag = "<grant>";
                        } else if (value.equals("acknowledgment")) {
                            currentTag = "<note>";
                        } else if (value.equals("document_type") || value.equals("doctype") || value.equals("documentType")) {
                            currentTag = "<doctype>";
                        } else if (value.equals("version")) {
                            currentTag = "<version>";
                        } else if (value.equals("release")) {
                            currentTag = "<other>";
                        } else if (value.equals("group")) {
                            currentTag = "<group>";
                        } else
                            currentTag = "<note>";
                    }
                } else
                    currentTag = "<note>";
            }
        } else if (qName.equals("ptr")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("web")) {
                            currentTag = "<web>";
                        }
                    }
                }
            }
        } else if (qName.equals("biblScope")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("vol")) {
                            currentTag = "<volume>";
                        } else if (value.equals("pp")) {
                            currentTag = "<pages>";
                        }
                    }
                }
            }
        } else if (qName.equals("titlePart")) {
            currentTag = "<title>";
        } else if (qName.equals("idno")) {
            currentTag = "<pubnum>";
        } else if (qName.equals("reference")) {
            currentTag = "<reference>";
        } else if (qName.equals("degree")) {
            currentTag = "<degree>";
        } else if (qName.equals("docAuthor")) {
            currentTag = "<author>";
        } else if (qName.equals("web")) {
            currentTag = "<web>";
        } else if (qName.equals("affiliation")) {
            currentTag = "<affiliation>";
            accumulator.setLength(0);
        } else if (qName.equals("address")) {
            currentTag = "<address>";
            accumulator.setLength(0);
        } else if (qName.equals("email")) {
            currentTag = "<email>";
        } else if (qName.equals("meeting")) {
            currentTag = "<meeting>";
        } else if (qName.equals("location")) {
            currentTag = "<location>";
        } else if (qName.equals("editor")) {
            currentTag = "<editor>";
        } else if (qName.equals("date")) {
            currentTag = "<date>";
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("submission")) {
                            currentTag = "<date-submission>";
                        } else if (value.equals("download")) {
                            currentTag = "<date-download>";
                        } 
                    }
                } 
            }
        } else if (qName.equals("p")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("intro") || value.equals("introduction")) {
                            currentTag = "<intro>";
                        }
                    }
                }
            }
        } else if ((qName.equals("keywords")) | (qName.equals("keyword"))) {
            currentTag = "<keyword>";
            accumulator.setLength(0);
        } else if (qName.equals("title")) {
            currentTag = "<journal>";
        } else if ((qName.equals("introduction")) | (qName.equals("intro"))) {
            currentTag = "<intro>";
        } else if (qName.equals("fileDesc")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("xml:id")) {
                        /*if (pdfs != null) {
                                  pdfs.put(fileName, value);
                              }*/
                        pdfName = value;
                    }
                }
            }
        }
    }

    private void writeData() {
        if (currentTag == null) {
            return;
        }

        String text = getText();
        // we segment the text
        StringTokenizer st = new StringTokenizer(text, TextUtilities.delimiters, true);
        boolean begin = true;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim();
            if (tok.length() == 0) 
                continue;

            String content = tok;
            int i = 0;
            if (content.length() > 0) {
                if (begin) {
                    labeled.add(content + " I-" + currentTag + "\n");
                    begin = false;
                } else {
                    labeled.add(content + " " + currentTag + "\n");
                }
            }
            begin = false;
        }
        accumulator.setLength(0);
    }
    
}