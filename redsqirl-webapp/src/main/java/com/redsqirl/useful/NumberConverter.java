package com.redsqirl.useful;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.commons.lang3.math.NumberUtils;

public class NumberConverter implements Converter, Serializable {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		
		if(value == null){
			return null;
		}
		
		if(NumberUtils.isNumber(value.toString())){
			double dblValue = Double.parseDouble(value.toString());
			if(dblValue - ((int) dblValue) > 0){
				if(dblValue > 10E5 || dblValue - ((int) dblValue) < 10E-3){
					return String.format("%.2E", dblValue);
				}else{
					return String.format("%.2f", dblValue);
				}
			}
		}
		return (String) value;
	}

}