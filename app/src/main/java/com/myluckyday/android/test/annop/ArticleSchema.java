package com.myluckyday.android.test.annop;

import com.myluckyday.android.test.api.AnnotatedParseObject;
import com.myluckyday.android.test.api.ParseKey;
import com.parse.ParseObject;

/**
 * Created by jpierce on 30/08/16.
 */
@AnnotatedParseObject(
        parseClassName = "Article",
        superClass = ParseObject.class
)
public class ArticleSchema {

//    @ParseKey("title")
    public String title;

    @ParseKey("link")
    public String url;

    public Author author;

}
