Pref benchmark - Zinc tunig
====

This workshop contains 2 parts:
 * Fix actual peromance problem inside zinc compiler
 * Optimize further classes in xsbt package from compilerBridge project

## Setup

In order to setup workshop please clone this branch and run `short-test` (first one will be really long due to downloading all reqiired deps).

## How can I run things?

There are two sbt aliases provided to run benchmarks:
 * short-test - useful mostly during first part since it is close to minimal reproduction of problem.
 It should take less then 30 sec to finish
 * long-test - design mostly for second part contains bigger project compilation (core shapeless on jvm).

## Tools

For both parts visualvm provided with jdk should be sufficient however some form of test editor with debugger will be usefull (e.g. Intellij).
Since visualvm is not that powerful in terms of functionaly, I suggest using yourkit (my preference) or jprofiler (both has trial periods).


