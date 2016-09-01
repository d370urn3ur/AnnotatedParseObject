package the.autarch.annotatedparseobject.androidsample;

import com.parse.ParseObject;

import the.autarch.annotatedparseobject.api.AnnotatedParseObject;

/**
 * Created by jpierce on 31/08/16.
 */
@AnnotatedParseObject(
        parseClassName = "Author",
        superClass = ParseObject.class
)
public class AuthorSchema {

    public String name;

}
