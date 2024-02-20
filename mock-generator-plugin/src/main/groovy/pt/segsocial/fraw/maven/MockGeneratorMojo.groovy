package pt.segsocial.fraw.maven

import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import pt.segsocial.iies.fraw.mock.mapper.MockRegistry

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.jar.JarEntry
import java.util.jar.JarFile

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class MockGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        log.info("Generating Default Mocks for project: " + project.getGroupId())

        Set<Artifact> artifacts = project.getArtifacts()
        List<URL> resolvedDependencies = new ArrayList<>()
        List<File> resolvedFiles = new ArrayList<>()
        for(Artifact artifact : artifacts) {
            //log.info("artifact: " + artifact.toString())
            if(artifact.getGroupId() == project.getGroupId() && "jar" == artifact.getType()) {
                File artifactFile = artifact.getFile()
                //log.info("file: " + artifactFile.toString())
                resolvedDependencies.add(artifactFile.toURI().toURL())
                resolvedFiles.add(artifactFile)
            }
        }
        //log.info("URLS: " + resolvedDependencies.toString())
        ClassLoader cl = new URLClassLoader((URL[])resolvedDependencies.toArray(new URL[resolvedDependencies.size()]))
        def generatedSourceRoot = project.getBuild().getDirectory() + File.separator + "ii-generated-sources"
        if(generatedSourceRoot == null) {
            throw new MojoExecutionException("No generated source folder was set.")
        }
        project.addCompileSourceRoot(generatedSourceRoot)

        log.info("generatedRoot: " + generatedSourceRoot)

        Path sourceRootPath = Paths.get(generatedSourceRoot)
        if(!Files.exists(sourceRootPath)) {
            Files.createDirectories(sourceRootPath)
        }

        for (File file : resolvedFiles) {

            if (Files.exists(file.toPath())) {
                JarFile jarFile = new JarFile(file)
                Enumeration<JarEntry> entries = jarFile.entries()


                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement()

                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {

                        String className = entry.getName().replace(".class", "")
                        className = className.replace("/", ".")
                        //log.info("jar entry: " + className)
                        Class<?> clazz = cl.loadClass(className)
                        if (!clazz.isAnnotation() && !clazz.isAnonymousClass() && !clazz.isEnum()) {
                            List<Annotation> annotations = Arrays.asList(clazz.getAnnotations())
                            //log.info("annotations: " + annotations.toString())
                            // def toBeMocked = clazz.isAnnotationPresent(MockMe.class)  // dont know why this wont return true when the annotation is present.....
                            def toBeMocked = annotations.toString().contains("MockMe")
                            //log.info("class: " + clazz.toString() + "  MockMe is present: " + toBeMocked)
                            if (toBeMocked) {
                                generateJavaFile(sourceRootPath, clazz)
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateJavaFile(Path sourceRootPath, Class clazz) {
        log.info("generating code for: " + clazz.getName())

        Package classPackage = clazz.getPackage()
        log.info("Package: " + classPackage.getName())
        Path packagePath = Paths.get(sourceRootPath.toString(), classPackage.getName().replace(".", "/"))
        if(!Files.exists(packagePath)) {
            Files.createDirectories(packagePath)
        }
        def mockClassName = "IIMock_" +  clazz.getSimpleName()
        Path mockJavaFile = Paths.get(packagePath.toString(), mockClassName + ".java")
        mockJavaFile = Files.createFile(mockJavaFile)

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out)
        ps.println("// Generated II MOCK")
        ps.println()
        ps.println("package " + classPackage.getName() + ";")
        ps.println()
        ps.println("import javax.ejb.LocalBean;")
        ps.println("import " + clazz.getName() + ";")
        ps.println("import static pt.segsocial.iies.fraw.mock.mapper.MockRegistry.*;")
        ps.println()
        ps.println("@LocalBean")
        ps.println("public class " + mockClassName )
        ps.println("\t implements " + clazz.getName())
        ps.println("{")
        generateClassMethods(ps, clazz)
        ps.println()

        ps.println("}")
        ps.println("// END OF FILE")

        Files.write(mockJavaFile, out.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)

        ps.close()

    }

    private void generateClassMethods(PrintStream mainPrintStream, Class clazz) {

        String mockClassName = clazz.getPackage().getName() + ".IIMock_" + clazz.getSimpleName();

        ByteArrayOutputStream outMethod = new ByteArrayOutputStream()
        PrintStream methodPrintStream = new PrintStream(outMethod)

        Method[] methods = clazz.getDeclaredMethods();
        for(Method m : methods) {
            if(!Modifier.isPublic(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            methodPrintStream.println()
            String methodSig = MockRegistry.fetchMethodSignature(m)
            methodPrintStream.print("\tpublic " + methodSig)

            generateMethodThrows(methodPrintStream, m)

            methodPrintStream.println()

            methodPrintStream.println("\t{")
            methodPrintStream.println("\t\tObject _mock = fetchMock(" + clazz.getName() + ".class, \"" + methodSig + "\");")
            methodPrintStream.println("\t\tif(_mock != null && !this.getClass().equals(_mock.getClass())) {")
            methodPrintStream.print("\t\t\t " + (void.class != m.getReturnType() ? " return" : ""))
            methodPrintStream.print(" execute(_mock, \"" + m.getName() + "\", " + fetchArgClasses(m))
            if(m.getParameterTypes().length > 0)
                methodPrintStream.print(", ")
            for(int i = 0; i < m.getParameterTypes().length; i++) {
                if(i > 0)
                    methodPrintStream.print(", ")
                methodPrintStream.print("_arg_" + i)
            }
            methodPrintStream.println(");")
            methodPrintStream.println("\t\t}")
            generateMethodReturn(methodPrintStream, clazz, methodSig)
            methodPrintStream.println("\t}")
        }
        mainPrintStream.print(outMethod.toString())
    }

    private String fetchArgClasses(Method m) {
        Class<?>[] params = m.getParameterTypes()
        StringBuilder sb = new StringBuilder()
        sb.append("new Class[]{")
        int count = 0
        for(Class<?> p : params) {
            if(count > 0)
                sb.append(", ")
            sb.append(p.getName()).append(".class")
            count += 1
        }
        sb.append("}")
        return params.length != 0 ? sb.toString() : "new Class[0]"
    }

    private void generateMethodThrows(PrintStream ps, Method method) {
        int count = 0;
        for(Class exceptionType: method.getExceptionTypes()) {
            if(count > 0)
                ps.print(", ")
            else if(count == 0)
                ps.print(" throws ")
            ps.print(exceptionType.getName())
            count += 1
        }
    }

    private void generateMethodReturn(PrintStream ps, Class clazz, String methodSig) {
        ps.println("\t\tthrow new java.lang.RuntimeException(\"II_MOCK default implementation for: " + clazz.getSimpleName() + " -> " + methodSig + "\");")
/*
        Class returnMethodType = method.getReturnType()
        TypeDecorator decorator = TypeDecorator.getDecorator(returnMethodType)
        decorator.decorate(ps, returnMethodType)
*/
    }

    private void generateMethodParameters(PrintStream ps, Method method) {
        def first = 0
        for(Class paramClass : method.getParameterTypes()) {
            if(first > 0) ps.print(", ")
            ps.print(paramClass.getName() + " " + "_param_" + (first+1) + "_" + paramClass.getSimpleName())
            first += 1
        }
    }
}

