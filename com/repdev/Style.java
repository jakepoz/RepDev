package com.repdev;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Supports styles for the editor.
 * Uses XML files to store style information for easy
 * editing and sharing of styles
 * 
 * @author Ryan
 *
 */


public class Style {
    public String name, version, description, author; 

    private Element style;

    public static void main(String[] args) {
	//Style me = new Style( new File("styles\\default.xml") );
	//System.out.println(me.getColor("comments", "fgColor").toString());
    	//System.out.println(SWT.GREY);
    }


    public Style(File xmlFile) {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db;
	Element head, header;

	try {
	    db = dbf.newDocumentBuilder();
	    Document d = db.parse(xmlFile);

	    head = (Element)d.getElementsByTagName("RepDevStyle").item(0);
	    header = (Element)d.getElementsByTagName("header").item(0);
	    this.name = header.getElementsByTagName("name").item(0).getTextContent();
	    this.version = header.getElementsByTagName("version").item(0).getTextContent();
	    this.description = header.getElementsByTagName("description").item(0).getTextContent();
	    this.author = header.getElementsByTagName("author").item(0).getTextContent();

	    style = ((Element)head.getElementsByTagName("style").item(0));

	} catch (ParserConfigurationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    //e.printStackTrace(); was annoying since if config never sets style this will always trace
		if( xmlFile.toString().contains("null") ){
			System.out.println("Please set your default style");
		}else{
			System.out.println("Style does not exist");
		}
	}
    }

    public RGB getColor(String item, String attrib) {
	String hexColor = "";
	for( int i=0; i<style.getChildNodes().getLength(); i++ ) {
	    Node cur = style.getChildNodes().item(i);
	    if( cur.getNodeName().equals(item) ) {
		for( int j=0; j<cur.getAttributes().getLength(); j++ ) {
		    if( cur.getAttributes().item(j).getNodeName().equals(attrib) ) {
			hexColor = cur.getAttributes().item(j).getNodeValue();
		    }	    
		}
	    }
	}
	int[] rgb = {0,0,0};
	if( hexColor.equals("") ) return null; 
	if( hexColor.indexOf("#") == 0 ) hexColor = hexColor.substring(1);
	if( hexColor.length() != 6 && hexColor.length() != 3 && hexColor.indexOf("$") == -1) return null;
	if( hexColor.indexOf("$") == 0 ){
		hexColor = hexColor.substring(1);
		if( hexColor.indexOf("rand") != -1 ){
			rgb[0] = (int)(Math.random()*255.0);
			rgb[1] = (int)(Math.random()*255.0);
			rgb[2] = (int)(Math.random()*255.0);
		}else if( hexColor.indexOf("red") != -1){
			rgb[0] = (int)(Math.random()*255.0);
			rgb[1] = 7*16+7;
			rgb[2] = 7*16+7;
		}else if( hexColor.indexOf("green") != -1){
			rgb[0] = 7*16+7;
			rgb[1] = (int)(Math.random()*255.0);
			rgb[2] = 7*16+7;
		}else if( hexColor.indexOf("blue") != -1){
			rgb[0] = 7*16+7;
			rgb[1] = 7*16+7;
			rgb[2] = (int)(Math.random()*255.0);
		}else if( hexColor.indexOf("!") == 0){
			hexColor = hexColor.substring(1);
			if( hexColor.length() == 6 ){
				int colors[] = {Integer.parseInt(hexColor.substring(0,1), 17),
								Integer.parseInt(hexColor.substring(1,2), 17),
								Integer.parseInt(hexColor.substring(2,3), 17),
								Integer.parseInt(hexColor.substring(3,4), 17),
								Integer.parseInt(hexColor.substring(4,5), 17),
								Integer.parseInt(hexColor.substring(5,6), 17)};
				for( int i = 0; i < 6; i++ ){
					colors[i]=(colors[i]==16)?(int)(Math.random()*16):colors[i];
				}
				for( int i = 0; i < 3; i++ ){
					rgb[i]=colors[i*2]*16+colors[i*2+1];
				}
			}else{
				return null;
			}
		}
    }else if( hexColor.length() == 6 ){
		rgb[0] = Integer.parseInt(hexColor.substring(0,2), 16);
		rgb[1] = Integer.parseInt(hexColor.substring(2,4), 16);
		rgb[2] = Integer.parseInt(hexColor.substring(4), 16);
	}else{
		rgb[0] = Integer.parseInt(hexColor.substring(0,1), 16)*16;
		rgb[1] = Integer.parseInt(hexColor.substring(1,2), 16)*16;
		rgb[2] = Integer.parseInt(hexColor.substring(2), 16)*16;
	}
	return new RGB(rgb[0],rgb[1],rgb[2]);
    }

    public int getStyle(String item){
	int swtStyle = SWT.DEFAULT;
	String styleText = "";
	for( int i=0; i<style.getChildNodes().getLength(); i++ ) {
	    Node cur = style.getChildNodes().item(i);
	    if( cur.getNodeName().equals(item) ) {
		for( int j=0; j<cur.getAttributes().getLength(); j++ ) {
		    if( cur.getAttributes().item(j).getNodeName().equals("style") ) {
			styleText = cur.getAttributes().item(j).getNodeValue();
		    }	    
		}
	    }
	}
	if( styleText.equalsIgnoreCase("bold") ) swtStyle = SWT.BOLD;
	if( styleText.equalsIgnoreCase("italic") ) swtStyle = SWT.ITALIC;
	return swtStyle;

    }
}

/* Ryan and Sean have 1337 ascii art skillz */

;;     ;;   ;;;;;;;;;
;;     ;;      ;;
;;;;;;;;;      ;;
;;     ;;      ;;
;;     ;;   ;;;;;;;;;

;;            ;;
;;  ;;        ;;  ;;


;;;;;;;;;; 


