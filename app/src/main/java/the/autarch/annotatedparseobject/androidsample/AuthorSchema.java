package the.autarch.annotatedparseobject.androidsample;

import com.myluckyday.android.test.api.AnnotatedParseObject;
import com.parse.ParseObject;

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
