import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import utils.UtilsWater;

public class TestXPathFormat {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFormatXPath() {

		String xpath = "html/body/div[2]/table/tbody/tr/td[2]/form/fieldset/input";

		assertTrue("/html[1]/body[1]/div[2]/table[1]/tbody[1]/tr[1]/td[2]/form[1]/fieldset[1]/input[1]"
				.equals(UtilsWater.formatXPath(xpath)));

	}

}
