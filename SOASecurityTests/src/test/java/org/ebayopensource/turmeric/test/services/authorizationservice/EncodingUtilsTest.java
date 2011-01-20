package org.ebayopensource.turmeric.test.services.authorizationservice;

import static org.junit.Assert.*;
import static org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author mpoplacenel
 *
 */
public class EncodingUtilsTest {

	
	@Test
	public void testSeparator() {
		assertEquals("Wrong separator mask", maskString(SEPARATOR), SEPARATOR_MASK);
	}

	public void testEncodeString() throws Exception {
		String string = "abcdef";
		verifyString(string);
	}

	public void testEncodeNull() throws Exception {
		String string = null;
		verifyString(string);
	}

	private void verifyString(String string) {
		String encoded = encodeNull(encodeString(string));
		assertEquals("Wrong encoded string", string == null ? NULL : string, encoded);
		String decoded = decodeString(decodeNull(encoded));
		assertEquals("Loopback failed", string, decoded);
	}

	public void testEncodeKey_Basic() throws Exception {
		String[] strings = new String[] {"abc", "def", "ghi"};
		verify(strings, "abc" + SEPARATOR + "def" + SEPARATOR + "ghi");
	}
	
	public void testEncodeKey_Escaping() throws Exception {
		String[] strings = new String[] {"abc" + SEPARATOR + "ABC", "def", "ghi"};
		verify(strings, "abc" + maskString(SEPARATOR) + "ABC" + SEPARATOR + "def" 
				+ SEPARATOR + "ghi");
	}
	
	public void testEncodeKey_FirstEmpty() throws Exception {
		String[] strings = new String[] {"", "abc", "def"};
		verify(strings, SEPARATOR + "abc" + SEPARATOR + "def");
	}

	public void testEncodeKey_MiddleEmpty() throws Exception {
		String[] strings = new String[] {"abc", "", "def"};
		verify(strings, "abc" + SEPARATOR + SEPARATOR + "def");
	}

	public void testEncodeKey_LastEmpty() throws Exception {
		String[] strings = new String[] {"abc", "def", ""};
		verify(strings, "abc" + SEPARATOR + "def" + SEPARATOR);
	}

	public void testEncodeKey_LastTwoEmpty() throws Exception {
		String[] strings = new String[] {"abc", "", ""};
		verify(strings, "abc" + SEPARATOR + SEPARATOR);
	}

	private void verify(String[] strings, String encodedExp) {
		List<String> stringList = Arrays.asList(strings);
		String encodedOut = encodeKey(strings);
		System.out.println("Strings: " + stringList + " encoded as " + encodedOut);
		assertEquals("Wrong encoded string: ", encodedExp, encodedOut);
		String[] outStrings = decodeKey(encodedOut);
		assertNotNull("Returned array should never be null", outStrings);
		if (outStrings.length < strings.length) {
			String[] tmpStrings = new String[strings.length];
			for (int i = 0; i < strings.length; i++) {
				if (i < outStrings.length) {
					tmpStrings[i] = outStrings[i];
				} else {
					tmpStrings[i] = "";
				}
			}
			outStrings = tmpStrings;
		}
		assertEquals("Wrong decoded strings", stringList, Arrays.asList(outStrings));
	}

}