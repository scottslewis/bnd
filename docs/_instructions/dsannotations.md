---
layout: default
title: -dsannotations SELECTORS
class: Builder
summary: |
   Selects the packages that need processing for standard OSGi DS annotations.
note: AUTO-GENERATED FILE - DO NOT EDIT. You can add manual content via same filename in ext folder. 
---

- Example: `-dsannotations: *`

- Pattern: `.*`

<!-- Manual content from: ext/dsannotations.md --><br /><br />

The `-dsannotations` instruction tells **bnd** which bundle classes, if any, to search for [Declarative Services (DS)](https://osgi.org/specification/osgi.cmpn/7.0.0/service.component.html) annotations. **bnd** will then process those classes into DS XML descriptors.

The value of this instruction is a comma delimited list of fully qualified class names.

The default value of this instruction is `*`, which means that by default **bnd** will process all bundle classes looking for DS annotations.

The behavior of DS annotation processing can be further configured using the [-dsannotations-options](dsannotations-options.html) instruction.

[source](https://github.com/bndtools/bnd/blob/master/biz.aQute.bndlib/src/aQute/bnd/component/DSAnnotations.java)
