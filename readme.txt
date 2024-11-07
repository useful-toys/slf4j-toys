*******************************************************************************
*** SETUP *********************************************************************
*******************************************************************************
Set following properties at $GRADLE_USER_HOME/gradle.properties
   ossrhUsername = danielferber
   ossrhPassword = ?
   signing.keyId=C106B759
   signing.password=?
   signing.secretKeyRingFile=C:\\Users\\dffwe\\AppData\\Roaming\\gnupg\\secring.gpg

Export public and secret key and ownertrust (backup):
   gpg -a --export danielferber@gmail.com > gpg/danielferber-public-gpg.key
   gpg -a --export-secret-keys danielferber@gmail.com > gpg/danielferber-secret-gpg.key
   gpg --export-ownertrust > gpg/danielferber-ownertrust-gpg.txt

Import secret key (which contains the public key) and ownertrust (restore)
   gpg --import gpg/danielferber-secret-gpg.key
   gpg --import-ownertrust gpg/danielferber-ownertrust-gpg.txt

Remember: The keys were generated running:
   gpg --full-generate-key (all default options)
   gpg --list-keys
   gpg --keyserver hkp://pgp.mit.edu --send-keys <Key-Hash>

Create the secring.gpg file, as it is not maintained anymore by gpg 2.1:
   gpg --export-secret-keys -o C:\Users\dffwe\AppData\Roaming\gnupg\secring.gpg

To list avaiable keys, using keyId compatible with Gradle:
   gpg --list-keys --keyid-format SHORT

*******************************************************************************
*** DEPLOY ********************************************************************
*******************************************************************************
1) Upgrade gradle wrapper:
   .\gradlew.bat wrapper --gradle-version <Version>
   Download new gradle.
   .\gradlew.bat
   Commit changes with message: [bld] Upgrade gradle wrapper.

2) Update license on file headers:
   .\gradlew.bat licenseFormat
   Commit changes with message: [doc] Update license headers.

3) Update version in slf4j-toys/build.gradle.
   Ensure that compileJava.options.debug is set to false.
   Commit with version message with message: [ver] X.X.X
   Create tag with name: X.X.X
   Push to Github.
   Github will create a release for the new tag. Edit the new tag with the
   changelog message.

4) Create binaries.
   .\gradlew.bat clean build uploadArchives

5) Publish to Github.
   Upload binaries to the release.

6) Publish to Maven.
   .\gradlew.bat uploadArchives
   Open: https://oss.sonatype.org/
   Sign in using the same credentials as for ossrhUsername/ossrhPassword above.
   Click link "Staging Repositories"
   Select last repository in the list, usually named like "orgusefultoys-10XX".
   On the toolbar above the list, select "close".
   Wait some minutes.
   Select again the same repository in the list.
   On the toolbar above the list, select "release".

Reference:
http://central.sonatype.org/pages/releasing-the-deployment.html

