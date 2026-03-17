# Deployment to Maven Central

This project already attaches Javadoc and sources using the `maven-javadoc-plugin` and
`maven-source-plugin` configured in `pom.xml` (including the `deployment` profile). The
steps below explain what is required to publish the artifact to Maven Central and how
to run the deployment.

## 1. Prerequisites

1. A Sonatype OSSRH account with permissions for the `net.ajaramillo.artifacts` groupId.
2. A GPG key pair published to a public key server.
3. Java 17+ and Maven installed.

## 2. Maven settings

Add your Sonatype credentials and GPG configuration to your Maven settings file
(`~/.m2/settings.xml`):

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>ossrh</id>
      <properties>
        <gpg.keyname>YOUR_GPG_KEY_ID</gpg.keyname>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>ossrh</activeProfile>
  </activeProfiles>
</settings>
```

## 3. POM requirements for Maven Central

Maven Central requires:

1. Signed artifacts (GPG).
2. Sources and Javadoc JARs (already configured in this project).
3. A distribution repository configured for Sonatype staging.

If these pieces are not present yet, add them to `pom.xml`:

```xml
<distributionManagement>
  <repository>
    <id>ossrh</id>
    <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
  </repository>
  <snapshotRepository>
    <id>ossrh</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
  </snapshotRepository>
</distributionManagement>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-gpg-plugin</artifactId>
      <version>3.2.7</version>
      <executions>
        <execution>
          <id>sign-artifacts</id>
          <phase>verify</phase>
          <goals>
            <goal>sign</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.sonatype.plugins</groupId>
      <artifactId>nexus-staging-maven-plugin</artifactId>
      <version>1.7.0</version>
      <extensions>true</extensions>
      <configuration>
        <serverId>ossrh</serverId>
        <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
        <autoReleaseAfterClose>true</autoReleaseAfterClose>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## 4. Build and deploy

Use the deployment profile defined in `pom.xml` to attach Javadocs and sources:

```bash
mvn -Pdeployment clean deploy
```

If you want to skip tests during the first deployment iteration:

```bash
mvn -Pdeployment -DskipTests clean deploy
```

## 5. Troubleshooting

1. Javadoc failures (doclint): If `maven-javadoc-plugin` fails with doclint errors,
   update your Javadoc comments or add a `doclint` configuration in the plugin.
2. GPG errors: Ensure the key is available locally and the passphrase is correct.
3. OSSRH authorization: Verify your `ossrh` server credentials and groupId ownership.
