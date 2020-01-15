package github.popeen.dsub.domain;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookmarkTest extends TestCase {
	
	/**
	 * tests the set created date
	 * @throws ParseException
	 */
	public void testSetCreated() throws ParseException {
		Bookmark bookmark = new Bookmark();
		bookmark.setCreated((String) null);
		assertEquals(null, bookmark.getCreated());
		
		bookmark.setCreated("");
		assertEquals(null, bookmark.getCreated());
		
		bookmark.setCreated("2014-04-04");
		assertEquals(null, bookmark.getCreated());
		
		bookmark.setCreated("2014/04/04");
		assertEquals(null, bookmark.getCreated());
		
		bookmark.setCreated("18/03/1988");
		assertEquals(null, bookmark.getCreated());
		
		bookmark.setCreated("18/03/88");
		assertEquals(null, bookmark.getCreated());

		Date date =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).parse("2013-10-20T00:00:00");
		bookmark.setCreated("2013-10-20T00:00:00");
		assertEquals(date, bookmark.getCreated());
	}
}