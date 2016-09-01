package the.autarch.annotatedparseobject.androidsample;

import com.parse.ParseObject;

import the.autarch.annotatedparseobject.api.AnnotatedParseObject;
import the.autarch.annotatedparseobject.api.ParseKey;

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
