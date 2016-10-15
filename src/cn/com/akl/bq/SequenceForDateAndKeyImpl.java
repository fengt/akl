package cn.com.akl.bq;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.actionsoft.awf.commons.expression.ExpressionAbst;

public class SequenceForDateAndKeyImpl extends ExpressionAbst{

	public SequenceForDateAndKeyImpl(HashMap paramMaps, String expressionValue) {
		super(paramMaps, expressionValue);
	}

	@Override
	public String expressionParse(String expression) {
		String parameter = getParameter(expression, 1).trim();
		Calendar instance = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String dateFormat = format.format(instance.getTime());//@sequence:(#key)
		StringBuilder sb = new StringBuilder("@sequencefordateandkey(@sequence:(#");
		sb.append(parameter).append(dateFormat).append("))");
		 String trim = getParameter(sb.toString(), 1).trim();
		 return trim;
	}
	
}
