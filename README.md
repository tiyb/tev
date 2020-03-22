# Tumblr Export Viewer

The **Tumblr Export Viewer**, or **TEV**, is intended to be a standalone viewer of Tumblr content, from Tumblr's export process. 

The application can be downloaded from the [Releases](https://github.com/tiyb/tev/releases/latest) page, and help for the application can be found at the [GitHub wiki](https://github.com/tiyb/tev/wiki/Users).

Additional development help is also welcome. Translation, for example, or tweaks of the UI, would be *wonderful* additions to the base created here.

## Overview

This tool is used to view content exported from Tumblr. There are three main pieces that get exported as part of Tumblr's export process:

1. A `media` folder of images and videos (and potentially other media?)
1. An XML document with all of the Tumblr user's posts
1. An XML document with all of the Tumblr user's messages

Some other things get exported as well (such as HTML versions of all of the user's posts), but these are ignored for the purposes of the TEV application. Likes/comments on posts -- either the user's own, or on the user's posts -- are *not* exported, and therefore not available in TEV.

## System Requirements

The application is fairly lightweight, but a **Java Runtime Environment** (**JRE**) is required.

## Usage

Some technical knowhow is, unfortunately, required to use the application. Full documentation can be found in the [wiki](https://github.com/tiyb/tev/wiki/Users), but at a high level, the user must perform the following steps:

1. Export their blog's content
1. Download the TEV application (a `JAR` file)
1. Run the application
1. Import the content
1. View it

Later, a batch file/script/PowerShell script may be created, but for the time being the application has to be launched from the command line. Open the command prompt, navigate to the directory that contains the `JAR` file, and execute the following:

```
java -jar <jarname>.jar
```

Again, further detail is given in the wiki.

## Technical Details

More detail is given in the [developer wiki](https://github.com/tiyb/tev/wiki/Developers), but the application was developed using **Spring Boot**, with an **HSQLDB** data source.
