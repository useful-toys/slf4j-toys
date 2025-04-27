# Deploy Maven Central

*Note:* The Sonartype OSSRH (OSS Repository Hosting) will be deprecated in Jun 2025.

### Update copyright headers

If needed, update the copyright headers in the source code files to reflect the new year.
However, I was not able to configure some maven plugin to do this automatically.
I tried org.codehaus.mojo:license-maven-plugin, but frustrated that is uses placeholders 
that introduce noise in the generated headers.
I tried com.mycila:license-maven-plugin, but it got stuck trying to find a gpg file
that should not be needed for this purpose.

Meanwhile, here's how to do it manually in IntelliJ IDEA:
 - Go to Settings > Editor > Copyright > Copyright Profiles.
 - Click the + button to create a new profile.
 - Enter the Apache 2.0 license text.
 - Under Settings > Editor > Copyright, select the profile you just created as the default.
 - Configure the scope to define which files should receive the header.
 - Right-click the root of your project and go to Analyse > Update Copyright.

### Update version

Update the version in the `pom.xml` file.
The version should be in the format `x.y.z-SNAPSHOT`, where `x`, `y`, and `z` are integers. 
The `-SNAPSHOT` suffix indicates that this is a development version. Remove the prefix if you are
ready to release a new version.

```bash
mvn versions:set -DnewVersion=x.y.z-SNAPSHOT -DgenerateBackupPoms=false
```

### Deploy to Maven Central

```bash
cd sl4j-toys
mvn clean deploy -P sonatype
```