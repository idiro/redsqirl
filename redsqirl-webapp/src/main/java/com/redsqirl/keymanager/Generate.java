package com.redsqirl.keymanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.keymanager.ciphers.Decrypter;
import com.redsqirl.keymanager.ciphers.Encrypter;
import com.redsqirl.keymanager.ciphers.Types;

/**
 * Hello world!
 *
 */
public class Generate 
{
	private static Logger logger = Logger.getLogger(Generate.class);
	
    public static void main( String[] args )
    {
    	List<String> argsVals = new ArrayList<String>();
        for (String s : args){
        	argsVals.add(s);
        }
        
        String type = args[1].trim().toLowerCase();
        List<String> types = new ArrayList<String>();
        for (Types t :Types.values()){
        	types.add(t.name());
        	
        }
        if(!types.contains(type)){
        	logger.error("Arg 1 Must be of type "+types.toString());
        	System.exit(1);
        }
        
        if(args[0].equals("encrypt")){
        	if (argsVals.size() < 3){
        		
        	}
        	Encrypter enc = new Encrypter();
        	String encriptKey = enc.encryptKey(Types.valueOf(type), args[2] ,Integer.valueOf(args[3]));
        	logger.info(encriptKey);
        }else if (args[0].equalsIgnoreCase("decrypt")){
        	Decrypter dec = new Decrypter();
        	dec.decrypt(Types.valueOf(type) , args[2]);
        }
    }
}
