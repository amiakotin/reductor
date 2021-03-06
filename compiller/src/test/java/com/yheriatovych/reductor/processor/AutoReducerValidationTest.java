package com.yheriatovych.reductor.processor;

import com.google.testing.compile.JavaFileObjects;
import com.yheriatovych.reductor.processor.ReductorAnnotationProcessor;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class AutoReducerValidationTest {
    @Test
    public void testFailIfReducerIsInterace() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public interface FoobarReducer extends Reducer<String> {\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("You can apply AutoReducer only to classes")
                .in(source).onLine(7);
    }

    @Test
    public void testFailIfReducerIsInnerClass() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "public class Test {\n" +
                "    @AutoReducer\n" +
                "    public abstract class FoobarReducer implements Reducer<String> {\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("AutoReducer annotated reducers should not be inner classes. Probably 'static' modifier missing")
                .in(source).onLine(8);
    }

    @Test
    public void testCompilesIfReducerIsNestedClass() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "public class Test {\n" +
                "    @AutoReducer\n" +
                "    public static abstract class FoobarReducer implements Reducer<String> {\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .compilesWithoutWarnings();
    }

    @Test
    public void testFailIfReducerDoNotImplementReducer() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer {\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("test.FoobarReducer should implement Reducer interface")
                .in(source).onLine(6);
    }

    @Test
    public void testFailIfReturnTypeIsNotTheSameAsReducerTypeParameter() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<String>{\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    int handleAction(String state, int number) {\n" +
                "        return number;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Method handleAction(java.lang.String,int) should return type assignable to state type java.lang.String")
                .in(source).onLine(9);
    }

    @Test
    public void testFailIfHandlerHasNoArguments() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<String>{\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    String handleAction() {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Method handleAction() should have at least 1 arguments: state of type java.lang.String")
                .in(source).onLine(9);
    }

    @Test
    public void testFailIfFirstArgumentIsNotStateType() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<String>{\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    String handleAction(int action) {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("First parameter action of method handleAction(int) should have the same type as state (java.lang.String)")
                .in(source).onLine(9);
    }

    @Test
    public void testGeneratedReducerWithMatchingConstructor() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<String>{\n" +
                "    private FoobarReducer(int foo, String bar) {\n" +
                "        \n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("No accessible constructors available for class test.FoobarReducer")
                .in(source)
                .onLine(7);
    }

    @Test
    public void testFailIfHandlerIsPrivate() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<String>{\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    private String handleAction(String state) {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("handleAction(java.lang.String) has 'private' modifier and is not accessible from child classes")
                .in(source).onLine(9);
    }

    @Test
    public void testUnboxReturnType() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer>{\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    int handleAction(Integer state) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .compilesWithoutError();
    }

    @Test
    public void testUnboxArgType() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer>{\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    Integer handleAction(int state) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .compilesWithoutError();
    }

    @Test
    public void testFailIfAnnotatedBothWithInitialStateAndAction() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer> {\n" +
                "    @AutoReducer.InitialState\n" +
                "    @AutoReducer.Action(\"ACTION_1\")\n" +
                "    int init() {\n" +
                "        return 42;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Method init() should be may be annotated " +
                        "with either @AutoReducer.InitialState or @AutoReducer.Action but not both")
                .in(source)
                .onLine(10);
    }

    @Test
    public void testFailIfTwoInitMethodsExists() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer> {\n" +
                "    @AutoReducer.InitialState\n" +
                "    int init1() {\n" +
                "        return 42;\n" +
                "    }\n" +
                "\n" +
                "    @AutoReducer.InitialState\n" +
                "    int init2() {\n" +
                "        return 42;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Methods init1() and init2() are both annotated with @AutoReducer.InitialState." +
                        " Only one @AutoReducer.InitialState method is allowed")
                .in(source)
                .onLine(14);
    }

    @Test
    public void testFailIfInitMethodIsPrivate() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer> {\n" +
                "    @AutoReducer.InitialState\n" +
                "    private int init() {\n" +
                "        return 42;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("init() has 'private' modifier and is not accessible from child classes")
                .in(source)
                .onLine(9);
    }

    @Test
    public void testFailIfInitMethodReturnsNotStateType() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer> {\n" +
                "    @AutoReducer.InitialState\n" +
                "    String init() {\n" +
                "        return \"\";\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Method init() should return type assignable to state type java.lang.Integer")
                .in(source)
                .onLine(9);
    }

    @Test
    public void testFailIfInitMethodHasParameters() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.FoobarReducer", "// Generated by com.yheriatovych.reductor.processor.ReductorAnnotationProcessor (https://github.com/Yarikx/reductor)\n" +
                "package test;" +
                "\n" +
                "import com.yheriatovych.reductor.Reducer;\n" +
                "import com.yheriatovych.reductor.annotations.AutoReducer;\n" +
                "\n" +
                "@AutoReducer\n" +
                "public abstract class FoobarReducer implements Reducer<Integer> {\n" +
                "    @AutoReducer.InitialState\n" +
                "    int init(int foobar) {\n" +
                "        return 42;\n" +
                "    }\n" +
                "}");

        assertAbout(javaSource()).that(source)
                .withCompilerOptions("-Xlint:-processing")
                .processedWith(new ReductorAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Method init(int) should not have any parameters")
                .in(source)
                .onLine(9);
    }
}
