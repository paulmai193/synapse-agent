# Google Java Style Guide

## Table of Contents
1. Introduction  
   1.1 Terminology notes  
   1.2 Guide notes  
2. Source file basics  
   2.1 File name  
   2.2 File encoding: UTF-8  
   2.3 Special characters  
       2.3.1 Whitespace characters  
       2.3.2 Special escape sequences  
       2.3.3 Non-ASCII characters  
3. Source file structure  
   3.1 License or copyright information, if present  
   3.2 Package declaration  
   3.3 Imports  
       3.3.1 No wildcard imports  
       3.3.2 No line-wrapping  
       3.3.3 Ordering and spacing  
       3.3.4 No static import for classes  
   3.4 Class declaration  
       3.4.1 Exactly one top-level class declaration  
       3.4.2 Ordering of class contents  
           3.4.2.1 Overloads: never split  
   3.5 Module declaration  
       3.5.1 Ordering and spacing of module directives  
4. Formatting  
   4.1 Braces  
       4.1.1 Use of optional braces  
       4.1.2 Nonempty blocks: K&R style  
       4.1.3 Empty blocks: may be concise  
   4.2 Block indentation: +2 spaces  
   4.3 One statement per line  
   4.4 Column limit: 100  
   4.5 Line-wrapping  
       4.5.1 Where to break  
       4.5.2 Indent continuation lines at least +4 spaces  
   4.6 Whitespace  
       4.6.1 Vertical whitespace (blank lines)  
       4.6.2 Horizontal whitespace  
       4.6.3 Horizontal alignment: never required  
   4.7 Grouping parentheses: recommended  
   4.8 Specific constructs  
       4.8.1 Enum classes  
       4.8.2 Variable declarations  
           4.8.2.1 One variable per declaration  
           4.8.2.2 Declared when needed  
       4.8.3 Arrays  
           4.8.3.1 Array initializers: can be “block‑like”  
           4.8.3.2 No C‑style array declarations  
       4.8.4 Switch statements and expressions  
           4.8.4.1 Indentation  
           4.8.4.2 Fall-through: commented  
           4.8.4.3 Exhaustiveness and presence of the `default` label  
           4.8.4.4 Switch expressions  
       4.8.5 Annotations  
           4.8.5.1 Type-use annotations  
           4.8.5.2 Class, package, and module annotations  
           4.8.5.3 Method and constructor annotations  
           4.8.5.4 Field annotations  
           4.8.5.5 Parameter and local variable annotations  
       4.8.6 Comments  
           4.8.6.1 Block comment style  
           4.8.6.2 TODO comments  
       4.8.7 Modifiers  
       4.8.8 Numeric Literals  
       4.8.9 Text Blocks  
5. Naming  
   5.1 Rules common to all identifiers  
   5.2 Rules by identifier type  
       5.2.1 Package and module names  
       5.2.2 Class names  
       5.2.3 Method names  
       5.2.4 Constant names  
       5.2.5 Non-constant field names  
       5.2.6 Parameter names  
       5.2.7 Local variable names  
       5.2.8 Type variable names  
   5.3 Camel case: defined  
6. Programming Practices  
   6.1 `@Override`: always used  
   6.2 Caught exceptions: not ignored  
   6.3 Static members: qualified using class  
   6.4 Finalizers: not used  
7. Javadoc  
   7.1 Formatting  
       7.1.1 General form  
       7.1.2 Paragraphs  
       7.1.3 Block tags  
   7.2 The summary fragment  
   7.3 Where Javadoc is used  
       7.3.1 Exception: self‑explanatory members  
       7.3.2 Exception: overrides  
       7.3.4 Non‑required Javadoc

---

## 1. Introduction

This document is the complete definition of Google’s coding standards for source code in the Java™ Programming Language. A Java source file is in **Google Style** if and only if it follows these rules.

Like other style guides, it spans formatting and other conventions. It focuses on universally applied, enforceable rules (by human or tool).

### 1.1 Terminology notes
1. **Class** includes normal classes, record classes, enum classes, interfaces, and annotation types (`@interface`).
2. **Member** (of a class) includes nested classes, fields, methods, and constructors (everything top-level in a class except initializers).
3. **Comment** refers to implementation comments. Documentation comments are called **Javadoc** (not “documentation comments”).

### 1.2 Guide notes
Example code is **non‑normative**: examples follow Google Style but don’t enforce optional choices as rules.

---

## 2. Source file basics

### 2.1 File name
For a file containing classes, the file name is the **case‑sensitive** name of the single top-level class plus the `.java` extension.

### 2.2 File encoding: UTF‑8
Source files are encoded in **UTF‑8**.

### 2.3 Special characters

#### 2.3.1 Whitespace characters
Only ASCII horizontal space (`0x20`) appears in source files (besides line terminators). Therefore:
- All other whitespace characters are **escaped** in char/string literals and text blocks.
- **Tabs** are not used for indentation.

#### 2.3.2 Special escape sequences
Use the language’s special escapes (`\b, \t, \n, \f, \r, \s, \", \', \\`) rather than octal (e.g., `\012`) or Unicode (e.g., `\u000a`) escapes.

#### 2.3.3 Non‑ASCII characters
Use either the actual Unicode character (e.g., `∞`) or its Unicode escape (e.g., `\u221e`), whichever improves readability. Avoid Unicode escapes outside literals and comments.

**Examples:**
```java
String unitAbbrev = "µs";                  // Best: clear without a comment
String unitAbbrev = "\u03bcs";             // Allowed, but unnecessary
String unitAbbrev = "\u03bcs"; // Greek letter mu, "s"   // Allowed, but awkward
String unitAbbrev = "\u03bcs";             // Poor: unclear to reader

return '\ufeff' + content; // byte order mark            // Good: escape + comment
