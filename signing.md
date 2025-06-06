# Signing

The secret key is used to sign artifacts, while the public key is used to verify their signatures.

### Configure the local environment
Add the following configuration to the Maven `settings.xml` file:
```xml
<profiles>
    <profile>
        <id>gpg</id>
        <activation>
            <activeByDefault>true</activeByDefault>	
        </activation>
        <properties>
            <gpg.keyname>7AFA66E152AD868D0DB288DE9B1946E0C106B759</gpg.keyname>
            <gpg.passphrase>****</gpg.passphrase>
        </properties>			
    </profile>
</profiles>
```

### Export public and secret keys, and ownertrust (backup)

To create a backup of the keys for use on another machine, execute the following commands:

```bash
gpg -a --export danielferber@gmail.com > gpg/danielferber-public-gpg.key
gpg -a --export-secret-keys danielferber@gmail.com > gpg/danielferber-secret-gpg.key
gpg --export-ownertrust > gpg/danielferber-ownertrust-gpg.txt
```

### Import secret key (which also includes the public key) and ownertrust (restore)

To restore the keys on another machine, execute the following commands:

```bash
gpg --import gpg/danielferber-secret-gpg.key
gpg --import-ownertrust gpg/danielferber-ownertrust-gpg.txt
```

### Generate secret key

The keys were generated following this procedure. They are stored in the `gpg` folder located in the home directory.

```bash
The keys were generated by executing:
gpg --full-generate-key (using all default options)
gpg --list-keys
gpg --keyserver hkp://pgp.mit.edu --send-keys <Key-Hash>
```

Create the `secring.gpg` file, as it is no longer maintained by GPG version 2.1:

```bash
gpg --export-secret-keys -o C:\Users\dffwe\AppData\Roaming\gnupg\secring.gpg
```

### List installed keys

```bash
To list the available keys using the key ID format compatible with Gradle:
gpg --list-keys --keyid-format SHORT
```
