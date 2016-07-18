package br.ufrn.deliverydelay.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeRepositoryUtil {
	
	public static String getPreviousRevision(String currentRevision, List<String> fileRevisions) {
		int index = fileRevisions.indexOf(currentRevision);
		return (index >= 1)?fileRevisions.get(index - 1):null;
	}
	
	public static List<String> getContentByLines(String in) {
		return Arrays.asList(in.split("[\r\n]+"));
	}
	
	public static boolean isComment(String line) {
		if(line.trim().length() == 0) return true; 

		boolean result;
		Pattern pattern = Pattern.compile("(?<!.+)^//.+$");
		Matcher matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)//(?!.+)");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)\\*(?!.+)");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^/\\*.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^\\*.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^\\s+\\*.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^\\*+/.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("^C:");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;
		//pattern = Pattern.compile("(?<!.+)}(?!.+)");
		//matcher = pattern.matcher(line.trim());

		//result = matcher.find();
		//if(result) return true;

		return false;
	}
	
	public static Integer getTaskNumberFromCommitComment(String comment){
    	Pattern pattern = Pattern.compile(
				"(#Tarefa|#tarefa|Tarefa|tarefa|#TAREFA|TAREFA|#)"
				+ "(:| |) "
				+ "(\\d+)");
    	
    	Pattern pattern2 = Pattern.compile("(#)(\\d+)");
		
		if(comment != null && !comment.isEmpty()){
			Matcher matcher = pattern.matcher(comment);
			if (matcher.find()){
				return Integer.parseInt(matcher.group(3));
			}
			matcher = pattern2.matcher(comment);
			if (matcher.find()){
				return Integer.parseInt(matcher.group(2));
			}
		}    	
    	return null;
    }
}
