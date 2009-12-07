package org.ginkgo.gcov.model;

import java.sql.Timestamp;

public class CoverageData {
	String elementType = "";
	String elementName = "";
	String persent = "";
	String totalLine = "";
	Timestamp timeStamp = null;
	public String getElementType() {
		return elementType;
	}
	public String getElementName() {
		return elementName;
	}
	public String getPersent() {
		return persent;
	}
	public String getTotalLine() {
		return totalLine;
	}
	public void setElementType(String elementType) {
		this.elementType = elementType;
	}
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	public void setPersent(String persent) {
		this.persent = persent;
	}
	public void setTotalLine(String totalLine) {
		this.totalLine = totalLine;
	}
	public Timestamp getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String toString(){
		return this.getTimeStamp().toString();// + " " + this.getPersent();
	}
}
