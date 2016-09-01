package the.autarch.annotatedparseobject.androidsample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.myluckyday.android.test.annop.Article;
import com.myluckyday.android.test.annop.Author;
import com.parse.ParseObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseObject.registerSubclass(Article.class);
        ParseObject.registerSubclass(Author.class);

        Article a = new Article();
        a.setTitle("Text");
        a.setUrl("http://www.google.com");
        a.setAuthor(new Author());

        Log.e(getClass().getName(), "" + a);
    }
}
