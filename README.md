M2E Code Style Maven Plugin
===========================

[![Build Status](https://travis-ci.org/trajano/m2e-codestyle-maven-plugin.svg?branch=master)](https://travis-ci.org/trajano/m2e-codestyle-maven-plugin) [![Quality Gate](https://sonarcloud.io/api/badges/gate?key=net.trajano.mojo:m2e-codestyle-maven-plugin)](https://sonarcloud.io/dashboard?id=net.trajano.mojo:m2e-codestyle-maven-plugin)

This was originally a fork of the [maven-m2e-codestyle-plugin][1] but is
now rewritten from scratch using more modern Maven plugin API techniques.

It removes a large number of external dependencies in favor of using what
is provided by the Maven and Plexus APIs.

It is primarily used with the [Coding Standards][2] project to set up the
Eclipse IDE, but it also supports remote coding standard files like the
original.

In addition it adds a `format` goal that utilizes the Eclipse JDT 
formatter to format the code.  It is not recommended that the `format`
goal be executed inside M2E and is ignored by default.

[1]: https://github.com/germanklf/maven-m2e-codestyle
[2]: http://site.trajano.net/coding-standards