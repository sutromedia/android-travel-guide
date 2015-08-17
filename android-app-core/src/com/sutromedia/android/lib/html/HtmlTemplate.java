package com.sutromedia.android.lib.html;
import org.stringtemplate.v4.*;

import java.util.List;
import com.sutromedia.android.lib.model.IGroup;
import com.sutromedia.android.lib.model.IEntryComment;

public class HtmlTemplate {

    private ST         mHtmlTemplate;
    private boolean    mUseComments;

    public HtmlTemplate(String html) {
        mHtmlTemplate = new ST(html, '$', '$');

        mHtmlTemplate.add("cssLinkColor", "#3678BF");
        mHtmlTemplate.add("externalWebsiteColor", "#4688CF");
        mHtmlTemplate.add("bodyTextFontSize", "15");
        mHtmlTemplate.add("rightMargin", "10");
        mHtmlTemplate.add("leftMargin", "10");
        mHtmlTemplate.add("cssTextColor", "#424242");

        mUseComments = false;
    }

    public void setAttribute(String attribute, Object value) {
        mHtmlTemplate.add(attribute, value);
    }

    public void setGroups(List<IGroup> groups) {
        if (groups.size()>0) {
            mHtmlTemplate.add("groups", groups.toArray());
        }
    }

    public void setComments(final List<IEntryComment> comments) {
        int count = comments.size();
        mHtmlTemplate.add("comments", Integer.toString(count));
        if (count>1) {
             mHtmlTemplate.add("many_comments", true);
        }

        if (count==1) {
             mHtmlTemplate.add("single_comment", true);
        }

        if (count>0) {
            mHtmlTemplate.add("first_comment", comments.get(0));
        }
    }

    public void useComments(boolean useComments) {
        mUseComments = useComments;
    }

    public String getResult() {
        mHtmlTemplate.add("use_comments", mUseComments);
        return mHtmlTemplate.render();
    }
}