# Brandeis LAPPS Grid wrapper for CoreNLP

This project is to build CoreNLP wrappers based on LAPPS Grid I/O specification, namely using LAPPS Interchange Format and LAPPS Vocabulary. 
Two wrappers are available: a simple commandline interface and lapps webservice. By default (`mvn package`), the webservice module is build into a war artifact. To bulid the CLI application, use "cli" profile (`mvn pacakge -Pcli`).

## For developers

### parent POM and base artifact

The parent POM used in the project ([`edu.brandeis.lapps.parent-pom`](https://github.com/brandeis-llc/lapps-parent-pom)) and the base java artifact ([`edu.brandeis.lapps.app-scaffolding`](https://github.com/brandeis-llc/lapps-app-scaffolding)) are not available on the maven central. Their source code are available at [`brandeis-llc`](https://github.com/brandeis-llc) github organization, and the maven artifacts are availble via [Brandeis LLC nexus repository](http://morbius.cs-i.brandeis.edu:8081/).

### releasing with maven-release-plugin

Don't forget to use `release` profile to include all submodules during the release preparation as well as performing release.

```
mvn release:preprare release:perform -Prelease
```
