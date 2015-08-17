package com.sutromedia.android.lib.html;

import android.test.AndroidTestCase;
import org.stringtemplate.v4.*;

import com.sutromedia.android.lib.model.DataException;
import com.sutromedia.android.lib.model.Group;
 
public class HtmlTemplateTest extends AndroidTestCase {

    public void testSimple() {
        String htmlTemplate = "Hello, $name$\n$groups:{g|<a class='SMTag' href='SMTag:$g.id$'>$g.name$</a>}; separator=\"\\n\"$";
        HtmlTemplate template = new HtmlTemplate(htmlTemplate);
        template.setAttribute("name", "World");
        assertEquals("Hello, World\n", template.getResult());
    }
    
    public void testGroups() throws DataException {
        doTestGroup(null, "");

        doTestGroup(
            new Group[] {
                new Group("0001", "Manhatan")
            }, 
            "<a class='SMTag' href='SMTag:0001'>Manhatan</a>");
        
        doTestGroup(
            new Group[] {
                new Group("0001", "Manhatan"),
                new Group("0002", "Top 25")
            }, 
            "<a class='SMTag' href='SMTag:0001'>Manhatan</a>\n" + 
            "<a class='SMTag' href='SMTag:0002'>Top 25</a>");
        
    }
    
    private void doTestGroup(Group[] groups, String expected) {
        String template = "$groups:{g|<a class='SMTag' href='SMTag:$g.id$'>$g.name$</a>}; separator=\"\\n\"$";
        ST st = new ST(template, '$', '$');
        if (groups!=null) {
            st.add("groups", groups);
        }
        assertEquals(expected, st.render());
    }
}