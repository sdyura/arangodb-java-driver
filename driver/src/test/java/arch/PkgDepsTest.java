package arch;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "com.arangodb..", importOptions = {DoNotIncludeTests.class})
public class PkgDepsTest {

    @ArchTest
    public static final ArchRule httpPkg = noClasses().that()
            .resideInAPackage("com.arangodb.protocol.http..")
            .should().dependOnClassesThat()
            .resideOutsideOfPackages("com.arangodb.protocol..", "com.arangodb.serde..", "java..", "io.vertx..", "io.netty..", "javax..", "org.slf4j..");

    @ArchTest
    public static final ArchRule vstCrossPkg = noClasses().that()
            .resideInAPackage("com.arangodb.protocol.vst..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.protocol.http..");

    @ArchTest
    public static final ArchRule httpCrossPkg = noClasses().that()
            .resideInAPackage("com.arangodb.protocol.http..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.arangodb.protocol.vst..");

}
