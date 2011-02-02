package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.CounterAbstractPolicy;

public class RateLimiterUtils extends CounterAbstractPolicy {
	private String ipOrSubjectGroup = null;

	public RateLimiterUtils(String ipOrSubjectGroup) {
		super();
		this.ipOrSubjectGroup = ipOrSubjectGroup;
	}



	public Boolean getFinalresult(String val) throws Exception {
		List<String> expression = simplifyExpression(val,
				new LinkedList<String>());
		int cnt = 0;
		Boolean lastoutput = null;
		String lastlogicalOperation = null;
		for (String a : expression) {
			if (a != null) {
				a = a.trim();
				if ("true".equalsIgnoreCase(a)) {
					if (cnt == 0) {
						lastoutput = true;
						cnt++;
						continue;
					} else {
						lastoutput = proccesOperation(lastoutput, true,
								lastlogicalOperation);
						lastlogicalOperation = null;
					}
				} else if ("false".equalsIgnoreCase(a)) {
					if (cnt == 0) {
						lastoutput = false;
						cnt++;
						continue;
					} else {
						lastoutput = proccesOperation(lastoutput, false,
								lastlogicalOperation);
						lastlogicalOperation = null;
					}
				} else if (("||").equalsIgnoreCase(a)) {
					if (cnt == 0) {
						System.out
								.println("error expression cannot start with ||");
						break;
					} else {
						if (lastlogicalOperation == null) {
							lastlogicalOperation = "||";
						} else {
							System.out.println("Invalid exprestion "
									+ expression);
						}
					}
				} else if (("&&").equalsIgnoreCase(a)) {
					if (cnt == 0) {
						System.out
								.println("error expression can not start with &&");
						break;
					} else {
						if (lastlogicalOperation == null) {
							lastlogicalOperation = "&&";
						} else {
							System.out.println("Invalid exprestion "
									+ expression);
						}
					}
				}
			}
			cnt++;
		}
		return lastoutput;
	}

	private Boolean proccesOperation(boolean lastoutput, boolean current,
			String lastlogicalOperation) {
		if (("||").equalsIgnoreCase(lastlogicalOperation)) {
			lastoutput = lastoutput || current;
		} else if (("&&").equalsIgnoreCase(lastlogicalOperation)) {
			lastoutput = lastoutput && current;
		} else {
			System.out.println(" not supported");
		}

		return lastoutput;

	}

	//
	private List<String> simplifyExpression(String eval, List<String> expression)
			throws Exception {
		expression = (expression == null) ? new LinkedList<String>()
				: expression;
		int andindx = eval.indexOf("&&");
		int orindx = eval.indexOf("||");
		StringTokenizer token;
		if (andindx >= 0 && orindx >= 0) {
			// wow complex do both
			// not yet supported

			if (andindx > orindx) {
				// or 1st

			} else {
				// and 1st
			}
			throw new Exception("not supported");
		} else if (andindx >= 0) {
			// do logical and here
			token = new StringTokenizer(eval, "&&");

			while (token.hasMoreTokens()) {
				// processLogicalExpression(token.nextToken(),"&&");
				expression
						.add(evaluateSimpleExpression(token.nextToken()) + "");
				if (token.hasMoreTokens()) {
					expression.add("&&");
				}
			}

		} else if (orindx >= 0) {
			// do logical or here
			// a > 1 || b > 3

			token = new StringTokenizer(eval, "||");

			while (token.hasMoreTokens()) {
				// processLogicalExpression(token.nextToken(),"&&");
				expression
						.add(evaluateSimpleExpression(token.nextToken()) + "");
				if (token.hasMoreTokens()) {

					expression.add("||");

				}
			}
		} else {
			// simple expression
			expression.add(evaluateSimpleExpression(eval) + "");
		}
		return expression;
	}

	// evaluateSimpleExpression
	private Boolean evaluateSimpleExpression(String str) throws Exception {
		String[] tokenizer = null;
		Boolean found = null;
		if (str.indexOf(">=") > -1) {
			// not supported yet catch for now
			tokenizer = createTokens(str, ">=");
			found = (getActualvalue(tokenizer[0]) >= getActualvalue(tokenizer[1]));
		} else if (str.indexOf("<=") > -1) {
			// not supported yet catch for now
			tokenizer = createTokens(str, "<=");
			found = (getActualvalue(tokenizer[0]) <= getActualvalue(tokenizer[1]));
		} else if (str.indexOf(">") > -1) {
			tokenizer = createTokens(str, ">");
			found = (getActualvalue(tokenizer[0]) > getActualvalue(tokenizer[1]));
	
		} else if (str.indexOf("<") > -1) {
			tokenizer = createTokens(str, "<");
			found = (getActualvalue(tokenizer[0]) < getActualvalue(tokenizer[1]));

		}
		return found;
	}

	// get int value if not get from variable
	private Integer getActualvalue(String str) throws Exception {
		Integer val = null;
		try {
			val = Integer.valueOf(str);
		} catch (NumberFormatException e) {
			// not number it must be a variable
			val = super.getVariable(str, ipOrSubjectGroup);
		}
		return val;
	}

	// split string
	private String[] createTokens(String str, String delim) throws Exception {
		// remove spaces
		str = str.replaceAll(" ", "");
		String[] result = str.split(delim);

		if (result == null || result.length > 2 || result.length < 1) {

			System.err.println("error invalid ex" + str);
			// since not valid return null
			throw new Exception(" invalid expression" + str);
		}

		return result;
	}

}
