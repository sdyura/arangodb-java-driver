package arch;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;


@AnalyzeClasses(packages = "com.arangodb..", importOptions = {DoNotIncludeTests.class})
public class PkgDepsTest {

    @ArchTest
    public static final ArchRule commonsPkg = noClasses().that()
            .resideInAPackage("com.arangodb.commons..")
            .should().dependOnClassesThat()
            .resideOutsideOfPackages(
                    "com.arangodb.commons..",
                    "java..",
                    "javax..",
                    "org.slf4j..");

    @ArchTest
    public static final ArchRule protocolPkg = noClasses().that()
            .resideInAPackage("com.arangodb.protocol..")
            .should().dependOnClassesThat()
            .resideOutsideOfPackages(
                    "com.arangodb.protocol..",
                    "com.arangodb.serde..",
                    "com.arangodb.commons..",
                    "com.arangodb.velocypack..",
                    "java..",
                    "io.vertx..",
                    "io.netty..",
                    "javax..",
                    "org.slf4j..");

}
