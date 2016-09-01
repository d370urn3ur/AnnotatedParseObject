package the.autarch.annotatedparseobject.api;

/**
 * Created by jpierce on 31/08/16.
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface AnnotatedParseObject {
    String parseClassName();
    Class superClass();
}
