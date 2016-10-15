package cn.com.akl.bq;

import java.util.HashMap;

import com.actionsoft.awf.commons.expression.ExpressionAbst;

public class TheDepartmentOfImpl extends ExpressionAbst {

	public TheDepartmentOfImpl(HashMap paramMaps, String expressionValue) {
		super(paramMaps, expressionValue);
	}
	
	@Override
	public String expressionParse(String expression) {
		String field = getParameter(expression, 1).trim();
		String departFullIds = getParameter("@thedepartmentof(@departmentFullId)", 1).trim();
		
		String[] split = departFullIds.split("/");
		StringBuilder whereSql = new StringBuilder(" 1=1 ");
		whereSql.append(" AND (").append(field).append("='' OR ").append(field).append(" is null");
		for(int i=0; i<split.length; i++){
			if("".equals(split[i])){
				continue;
			}
			
			whereSql.append(" OR ");
			whereSql.append(field).append("=").append(split[i]);
		}
		whereSql.append(")");
		return whereSql.toString();
	}

}
