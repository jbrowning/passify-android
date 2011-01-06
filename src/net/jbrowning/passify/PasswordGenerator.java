package net.jbrowning.passify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PasswordGenerator {

	
	/*
	private static String[] lowerCaseList = {
			"a", "b", "c", "d", "e", "f", "g",
	        "h", "i", "j", "k", "m", "n", "p",
	        "q", "r", "s", "t", "u", "v", "w",
	        "x", "y", "z"
	    };
	
	private static String[] upperCase = {
			"A", "B", "C", "D", "E", "F", "G",
			"H", "J", "K", "L", "M", "N", "P",
			"Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z" 
		};
	
	private static String[] numbers = {
			"1", "2", "3", "4", "5", "6", "7",
			"8", "9"
		};
	
	private static String[] symbols= {
			"!", "@", "#", "$", "%", "^", "&", "*",
			"(", ")", "?"
		};
	*/

	private static ArrayList<String> lowerCaseList = new ArrayList<String>(Arrays.asList(
			"a", "b", "c", "d", "e", "f", "g",
	        "h", "i", "j", "k", "m", "n", "p",
	        "q", "r", "s", "t", "u", "v", "w",
	        "x", "y", "z"));
	
	private static ArrayList<String> upperCaseList = new ArrayList<String>(Arrays.asList(
			"A", "B", "C", "D", "E", "F", "G",
			"H", "J", "K", "L", "M", "N", "P",
			"Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z" ));
	
	private static ArrayList<String> numbersList = new ArrayList<String>(Arrays.asList(
			"1", "2", "3", "4", "5", "6", "7",
			"8", "9"));
	
	private static ArrayList<String> symbolsList = new ArrayList<String>(Arrays.asList(
			"!", "@", "#", "$", "%", "^", "&", "*",
			"(", ")", "?"));
	
	public PasswordGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	public static String generate(int passLength, boolean mixedCase, boolean symbols, boolean numbers) {
		
		ArrayList<String> pickList = (ArrayList<String>) lowerCaseList.clone();
		String generatedPass;
		Random randomizer = new Random();
		
		if (mixedCase == true) {
			pickList.addAll(upperCaseList);
		}
		
		if (symbols == true) {
			pickList.addAll(symbolsList);
		}
		
		if (numbers == true) {
			pickList.addAll(numbersList);
		}
		
		java.util.Collections.shuffle(pickList);
		
		boolean allGood = false;
		
		do {
			
			generatedPass = "";
			
			boolean mixedCaseGood = false;
			boolean symbolsGood = false;
			boolean numbersGood = false;
			
			// Generate a random password
			for (int x = 0; x < passLength; x++) {
				int randNum = randomizer.nextInt(pickList.size());
				generatedPass += pickList.get(randNum);
			}
			
			// Validate
			if (mixedCase) {
				boolean lowerCaseGood = false;
				boolean upperCaseGood = false;
				String testLowerLetter = "";
				String testUpperLetter = "";
				
				for (int x = 0; x < lowerCaseList.size(); x++) {
					// Check if current character is in the lower case list
					if (generatedPass.contains(lowerCaseList.get(x))) {
						lowerCaseGood = true;
						if (upperCaseGood) {
							break;
						}
					}
					
					// Check if current character is in the upper case list
					if (generatedPass.contains(upperCaseList.get(x))) {
						upperCaseGood = true;
						if (lowerCaseGood) {
							break;
						}
					}
					
					if (lowerCaseGood && upperCaseGood) {
						mixedCaseGood = true;
					}
				}
				
				// If 
				if (lowerCaseGood && upperCaseGood) {
					mixedCaseGood = true;
				}
				
			} else {
				mixedCaseGood = true;
			}
			
			if (symbols) {
				for (int x = 0; x < symbolsList.size(); x++) {
					if (generatedPass.contains(symbolsList.get(x))) {
						symbolsGood = true;
						break;
					}
				}
			} else {
				symbolsGood = true;
			}
			
			if (numbers) {
				for (int x = 0; x < numbersList.size(); x++) {
					if (generatedPass.contains(numbersList.get(x))) {
						numbersGood = true;
						break;
					}
				}
			} else {
				numbersGood = true;
			}
			
			if (mixedCaseGood && symbolsGood && numbersGood) {
				allGood = true;
			}
			
		} while (!allGood);
		
		return generatedPass;
	}

}
