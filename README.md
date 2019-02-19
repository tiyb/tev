# Tumblr Export Viewer

The **Tumblr Export Viewer**, or **TEV**, is intended to be a standalone viewer of Tumblr content, from Tumblr's export process. There are three main pieces that get exported from Tumblr:

1. A `media` folder of images and videos (and potentially other media?)
1. An XML document with all of the Tumblr user's posts
1. An XML document with all of the Tumblr user's messages

Some other things get exported as well (such as HTML versions of all of the user's posts), but these are ignored for the purposes of the TEV application.

## System Requirements

The application is *somewhat* lightweight, but a **Java Runtime Environment** (**JRE**) is required.

## Usage

Some technical knowhow is, unfortunately, required to use the application. Users must perform the following steps:

1. Export their blog's content
1. Download the TEV application (a `JAR` file)
1. Run the application
1. Import the content
1. View it

Each of these is described in better detail in the [wiki](https://github.com/tiyb/tev/wiki), but running the `JAR` file is described here. Later, a batch file/script/PowerShell script may be created, but for the time being, the application has to be launched from the command line. Open the command prompt, navigate to the directory that contains the `JAR` file, and execute the following:

```
java -jar <jarname>.jar
```

## Technical Details

More detail is given in the [wiki](https://github.com/tiyb/tev/wiki), but the application was developed using **Spring Boot**, with an **HSQLDB** data source.
