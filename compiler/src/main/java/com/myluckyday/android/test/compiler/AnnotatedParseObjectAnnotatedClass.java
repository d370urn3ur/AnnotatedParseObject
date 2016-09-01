package com.myluckyday.android.test.compiler;


import com.google.common.collect.Sets;
import com.myluckyday.android.test.api.AnnotatedParseObject;
import com.myluckyday.android.test.api.ParseKey;
import com.parse.ParseClassName;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;

/**
 * Created by jpierce on 30/08/16.
 */
public class AnnotatedParseObjectAnnotatedClass {

    private static final String SUFFIX = "Schema";

    private TypeElement annotatedClassElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;
    private String parseClassName;
    private Set<VariableElement> parseKeyFields = Sets.newHashSet();

    public AnnotatedParseObjectAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {

        annotatedClassElement = classElement;

        AnnotatedParseObject annotation = classElement.getAnnotation(AnnotatedParseObject.class);
        parseClassName = annotation.parseClassName();
        if(StringUtils.isEmpty(parseClassName)) {
            throw new IllegalArgumentException(
                    String.format(
                            "id() in @%s for class %s is null or empty! that's not allowed",
                            AnnotatedParseObject.class.getSimpleName(),
                            classElement.getQualifiedName().toString()
                    )
            );
        }

        try {
            Class<?> superClass = annotation.superClass();
            qualifiedSuperClassName = superClass.getCanonicalName();
            simpleTypeName = superClass.getSimpleName();
        } catch(MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
//            DeclaredType classTypeMirror = (DeclaredType) annotatedClassElement.getSuperclass();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }

        addFields(annotatedClassElement.getEnclosedElements());
    }

    private void addFields(List<? extends Element> fields) {
        for(Element field : fields) {
            if(field.getKind().isField()) {
                VariableElement elem = (VariableElement)field;
                if(elem.getEnclosingElement() == annotatedClassElement) {
                    // this field is enclosed in current class
                    parseKeyFields.add(elem);
                }
            }
        }
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {

        TypeElement superClassName = elementUtils.getTypeElement(qualifiedSuperClassName);
        String schemaName = annotatedClassElement.getSimpleName().toString();
        String className = schemaName.replace(SUFFIX, ""); // (ex: ArticleSchema become Article)
        PackageElement pkg = elementUtils.getPackageOf(annotatedClassElement);
        String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
                .superclass(TypeName.get(superClassName.asType()))
                .addModifiers(Modifier.PUBLIC);

        AnnotationSpec annoSpec = AnnotationSpec.builder(ParseClassName.class)
                .addMember("value", "$S", parseClassName)
                .build();

        typeSpecBuilder.addAnnotation(annoSpec);

        for(VariableElement field : parseKeyFields) {

            String parseKey;
            if(field.getAnnotation(ParseKey.class) != null) {
                parseKey = field.getAnnotation(ParseKey.class).value();
            } else {
                parseKey = field.getSimpleName().toString();
            }
            String fieldName = field.getSimpleName().toString();

            MethodSpec getMethodSpec = MethodSpec.methodBuilder("get" + StringUtils.capitalize(fieldName))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(field.asType()))
                    .addStatement("return ($T)get($S)", TypeName.get(field.asType()), parseKey)
                    .build();

            MethodSpec setMethodSpec = MethodSpec.methodBuilder("set" + StringUtils.capitalize(fieldName))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addParameter(TypeName.get(field.asType()), "value")
                    .addStatement("put($S, value)", parseKey)
                    .build();

            typeSpecBuilder.addMethods(Arrays.asList(getMethodSpec, setMethodSpec));
        }

        JavaFile.builder(packageName, typeSpecBuilder.build()).build().writeTo(filer);
    }

    /**
     * Get the id as specified in {@link AnnotatedParseObject}.
     * return the id
     */
    public String getParseClassName() {
        return parseClassName;
    }

    /**
     * Get the full qualified name of the type specified in  {@link AnnotatedParseObject}.
     *
     * @return qualified name
     */
    public String getQualifiedFactoryGroupName() {
        return qualifiedSuperClassName;
    }


    /**
     * Get the simple name of the type specified in  {@link AnnotatedParseObject}.
     *
     * @return qualified name
     */
    public String getSimpleFactoryGroupName() {
        return simpleTypeName;
    }

    /**
     * The original element that was annotated with @AnnotatedParseClass
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
