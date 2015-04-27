M2E Code Style Maven Plugin
===========================

This was originally a fork of the [maven-m2e-codestyle-plugin][1] but is
now rewritten from scratch using more modern Maven plugin API techniques.

It removes a large number of external dependencies in favor of using what
is provided by the Maven and Plexus APIs.

It is primarily used with the [Coding Standards][2] project to set up the
Eclipse IDE, but it also supports remote coding standard files like the
original.

[1]: https://github.com/germanklf/maven-m2e-codestyle
[2]: http://site.trajano.net/coding-standards