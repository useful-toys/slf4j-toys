How to build:

Set following properties at $GRADLE_USER_HOME/gradle.properties

ossrhUsername = danielferber
ossrhPassword = ?

signing.keyId=3E892DA7
signing.password=?
signing.secretKeyRingFile=C:\\Users\\Daniel\\AppData\\Roaming\\gnupg\\secring.gpg

Run:
gradle clean build uploadArchives

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

