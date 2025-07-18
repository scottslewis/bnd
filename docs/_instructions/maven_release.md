---
layout: default
title: -maven-release ('local'|'remote') ( ',' option )*
class: Project
summary: |
   Set the Maven release options for the Maven Bnd Repository
note: AUTO-GENERATED FILE - DO NOT EDIT. You can add manual content via same filename in ext folder. 
---

- Example: `-maven-release: local`

- Values: `(local|remote)`

- Pattern: `.*`

<!-- Manual content from: ext/maven_release.md --><br /><br />

The `-maven-release` instruction provides the context for a release to Maven repository. In the Maven world it is customary that a release has a JAR with sources and a JAR with Javadoc. In the OSGi world this is unnecessary because the sources can be packaged inside the bundle. (Since the source is placed at a standard location, the IDEs can take advantage of this.) However, putting an artifact on Maven Central requires that these extra JARs are included. This instruction allows you to specify additional parameters for this release process.

Though this instruction is not specific for a plugin, it was developed in conjunction with the [Maven Bnd Repository Plugin][1].


    -maven-release ::= ( 'local'|'remote' ( ';' snapshot )? ) ( ',' option )*
    snapshot       ::= <value to be used for timestamp>
    option         ::= sources | javadoc | pom | sign | archive*
    archive          ::= 'archive' 
                       ( ';path=' ( PATH | '{' PATH '}' )?
                       ( ';classifier=' maven-classifier )?
    sources        ::= 'sources' 
                       ( ';path=' ( 'NONE' | PATH ) )?
                       ( ';force=' ( 'true' | 'false' ) )?
                       ( ';-sourcepath=' PATH ( ',' PATH )* )?
    javadoc        ::= 'javadoc'
                       ( ';path=' ( 'NONE' | PATH ) )?
                       ( ';packages=' ( 'EXPORTS' | 'ALL' ) )?
                       ( ';force=' ( 'true' | 'false' ) )?
                       ( ';' javadoc-option )*
    javadoc-option ::= '-' NAME '=' VALUE
    pom            ::= 'pom'
                       ( ';path=' ( 'JAR' | PATH ) )?
    sign            ::= 'sign'
                       ( ';passphrase=' VALUE )?

If `sources` or `javadoc` has the attribute `force=true`, either one will be release to the maven repository even if no `releaseUrl` or `snapshotUrl` is set or `maven-release=local`. 

The `aQute.maven.bnd.MavenBndRepository` is a bnd plugin that represent the local and a remote Maven repository. The locations of both repositories can be configured. The local repository is always used as a cache for the remote repository.

For a detailed configuration of the [Maven Bnd Repository Plugin][1], please look at the documentation page.

If the Maven Bnd Repository is asked to put a file, it will look up the `-maven-release` instruction using merged properties. The property is looked up from the bnd file that built the artifact. However, it should in general be possible to define this header in the workspace using macros like `${project}` to specify relative paths.

The `archive` option provides a way to add additional files/archives to release. A Maven release always has a pom and then a number of files/archives that are separated by a _classifier_. The default classifier is generally the jar file. Special classifiers are reserved for the sources and the javadoc. 

The `archive` option takes the following parameters:

* `path` : The path to the file that will be placed in the release directory. If the path is surrounded by curly braces, it will be pre-processed.
* `classifier` : The classifier of the file. This is the maven classifier used.

For example:

     -maven-release \
           archive;\
            path=files/feature.json;
            classifier=feature

# Signing

If the instruction contains the sign attribute  and release build is detected the repository tries to apply [gnupg](https://gnupg.org/) via a command process to create `.asc` files for all deployed artifacts. This requires a Version of [gnupg](https://gnupg.org/) installed on your build system. By default it uses the `gpg` command. If the `passphrase` is configured, it will hand it over to the command as standard input. The command will be constructed as follows: `gpg --batch --passphrase-fd 0 --output <filetosign>.asc --detach-sign --armor <filetosign>`. Some newer gnupg versions will ignore the passphrase via standard input for the first try and ask again with password screen. This will crash the process. Have a look [here](https://stackoverflow.com/questions/19895122/how-to-use-gnupgs-passphrase-fd-argument) to teach gnupg otherwise. The command can be exchanged or amended with additional options by defining a property named `gpg` in your workspace (e.g. `build.bnd` or somewhere in the ext directory).

Example config could look like:

```
# use the env macro to avoid to set the passphrase somehwere in your project
-maven-release: pom,sign;passphrase=${env;GNUPG_PASSPHRASE}
gpg: gpg --homedir /mnt/n/tmp/gpg/.gnupg --pinentry-mode loopback
```



 

[1]: /plugins/maven
