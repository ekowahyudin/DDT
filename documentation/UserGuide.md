
## User Guide

*Note:* For an overview of DDT features, see [Features]. This also serves to document which major functionalities are available.

### Eclipse basics

If you are new to Eclipse, you can learn some of the basics of the Eclipse IDE with this short intro article: http://www.ibm.com/developerworks/opensource/library/os-eclipse-master1/ 

Also, to improve Eclipse performance on modern machines, it is recommended you increase the memory available to the JVM. You can do so by modifying the _`eclipse.ini`_ file in your Eclipse installation. The two VM parameters in _`eclipse.ini`_ to note are _-Xms_ (initial Java heap size) and _-Xmx_ (maximum Java heap size). For a machine with 4Gb of RAM or more, the following is recommended as minimum values:
```
-vmargs
-Xms256m
-Xmx1024m
```

### DDT Configuration

Before creating D projects, you should configure a D compiler installation (they are often called 'interpreters' throughout the IDE UI. This is due to a current limitation arising from DDT's usage of _DLTK - the Dynamic Languages ToolKit_). The compiler itself might not be used, but adding a D compiler installation allows the standard library to be part of a project build path.

Open 'Preferences' and go the preference page: 'DDT / Compilers'. Click "Add..", then "Browse.." and then navigate and select the DMD executable for the D compiler installation. Now the "Interpreter system libraries" locations should be filled automatically. You should see something similar to this:

![UserGuide_DDT_EditInterpreter](screenshots/UserGuide_DDT_EditInterpreter.png)

_Only DMD and GDC are supported at the moment._ However other compilers or standard libraries can be partially supported by manually adding or changing the default locations in the "Interpreter system libraries" list.

### Project setup

*Project creation:*
A new D project can be created with the 'New' / 'DDT/D Project' wizard. The D perspective should open after creation. Use the Script Explorer view to work with D projects. (the Project Explorer view is not yet properly supported)

*Build Path:*
A project has a build path configuration. The build path is the set of folders that contain the D files that constitute the D project. Semantic features (such as code completion) will only see the modules contained in the build path. The built-in builder also works by means of the project build path. 
*Note:* In the future all project configuration will be made entirely by means of the [http://code.dlang.org/about Dub tool].

*Build Path configuration:*
The build path can be configured in the "D Build Path" project property page, or in several context menu actions of the Script Explorer view. The simplest element of the build path is a source folder, which is a folder that contains D files (.d or .di), whose module/package declaration must match the filesystem directory hierarchy, rooted at the source folder.

Note: library entries in the build path, with the exception of the standard library, are not supported as build path entries (they can be added in the Libraries UI, but they will have no effect). Project dependencies are also not supported: there is a Project References functionality in the UI but it will have little effect. Source folder exclusions/inclusions are not supported by the builder. 

*Build configuration:*
The default build behavior is to use the simple DDT built-in builder (configuration guide in last section).
You can change this, and configure an external builder instead (it is recommended to use Dub). To do this, open the project 'Properties' page (available in the project context menu), go the 'Builders' page. Disable the existing builders, and add your own, invoking an external program.

### Editor and Navigation

*Editor newline auto-indentation:*
The editor will auto-indent new lines after an Enter is pressed. Pressing Backspace with the cursor after the indent characters in the start of the line will delete the indent and preceding newline, thus joining the rest of the line with the previous line. Pressing Delete before a newline will have an identical effect.
This is unlike most source editors - if instead you want to just remove one level of indent (or delete the preceding Tab), press Shift-Tab. 

*Open Definition:*
The Open Definition functionality is invoked by pressing F3 in the DDT source editor, or by clicking the Open Definition button placed in the toolbar. When using the toolbar button, Open Definition will work in any text editor, however it won't be able to follow imports across modules if the file is not on the build path of a DDT project. Open Definition is also available in the editor context menu and by means of editor *hyper-linking* (hold Ctrl and select a reference with the mouse).

Open Definition functionality should find any definition under basic reference contexts, but references under complex expressions might resolve inaccurately, or not at all.
Particularly: function call overloading, template overloads, template instantiation, IFTI, operator overloading are not currently understood by the semantic engine.

*Code-Completion/Auto-Complete:*
Invoked with Ctrl-Space. This functionality is generally called Content Assist in Eclipse. For DDT, it has the same semantic power as Open Definition to determine completions. Can be used in import statements to list available modules to import.

Content Assist can also present Code Templates. These are predefined parameterized blocks of code that can be automatically inserted in the current source. These can be configured in the preferences, under 'DDT/Editor/Code Templates'.

*Text Hover:*
Text hover shows a text popup over the reference or definition under the mouse cursor. The hover will display the signature of the definition, as well as DDoc, if available. DDoc will be rendered in a graphical way, similar to a standard HTML presentation.

*Open-Type dialog:*
Invoked with Ctrl-Shift-T. This is a dialog that allows one to search for any definitions (types or meta-types) and open an editor on the source of the selected definition. Search works the same as JDT, a simple text filter can be used, or camel-case matching can be used to match the desired element (for example: the `FEx` text will match `FiberException`, `FileException`, `FormatException`, etc.). Wildcards can also be used in the filter text.
 
*Hierarchy View:*
These are not currently supported/implemented, even though they are present in the UI.

*Semantic Search:*
The search dialog allows searching for definitions based on a text pattern. Available in the main menu, under 'Search' / 'D...':
![UserGuide_SearchDialog](screenshots/UserGuide_SearchDialog.png)

It is also possible to search for all references to a given definition. In the editor, select the name of a definition, and use the editor context menu to search for references (shortcut: Ctrl-Shift-G). This can also be invoked on references, invoking a search for all references to the same definition the selected reference resolves to.


### Launch and Debug:
To run a D project that builds to an executable, you will need to create a launch configuration. Locate the main menu, open 'Run' / 'Run Configurations...'. Then double click 'D Application" to create a new D launch, and configure it accordingly. You can run these launches from the 'Run Configurations...', or for quicker access, from the Launch button in the Eclipse toolbar.

Alternatively, to automatically create and run a launch configuration (if a matching one doesn't exist already), you can select a D project in the workspace explorer, open the context menu, and do 'Run As...' / 'D Application'. (or 'Debug As...' for debugging instead). If a matching configuration exists already, that one will be run.

Whenever a launch is requested, a build will be performed beforehand. This behavior can be configured under general Eclipse settings, or in the launch configuration.

D launches can be run in debug mode. You will need a GDB debugger. To configure debug options (in particular, the path to the debugger to use), open the launch under 'Run' / 'Debug Configurations...', and then navigate to the 'Debugger' tab in the desired launch configuration:
![UserGuide_DebuggerLaunchConfiguration](screenshots/UserGuide_DebuggerLaunchConfiguration.png)
GDB debugger integration is achieved by using the CDT plugins. To configure global debugger options, go the 'C/C++'/'Debug'/'GDB' preference page.

### Built-in Build:
*Note* In the near future all project configuration (including building) will be setup by means of the [Dub tool](http://code.dlang.org/about).

There is some support for integrated builder functionality, namely, the IDE can automatically manage some of the build settings according to the IDE's D project configuration. The build settings can be accessed on the "D Compile Options" project properties page. Here's how it works: DDT will create a build.rf file in the project root folder and then invoke a builder according to the "Build Command:" entry. You can, and should, specify arguments to the build command in the entry, such as the response file "build.rf". 

The contents of the build.rf file will be the same as the "Managed Response File" configuration entry, except that some special tokens will be replaced by the IDE. With this functionality it should be possible to use any builder, such as [rebuild](http://www.dsource.org/projects/dsss/wiki/Rebuild), [bud](http://www.dsource.org/projects/build/), or even DMD itself, which is the default (but not recommend for large projects though). The available replace tokens are: 
 * $DEEBUILDER.OUTPUTPATH - Project relative output path
 * $DEEBUILDER.OUTPUTEXE - Project relative output executable path
 * $DEEBUILDER.SRCLIBS.-I - Absolute path of all source libs, each prefixed by "-I" and separated by newlines
 * $DEEBUILDER.SRCFOLDERS.-I - Project relative  path of all source folder, each prefixed by "-I" and separated by newlines
 * $DEEBUILDER.SRCMODULES - All source modules (.d files) found in the project's source folders, separated by newlines

There is also a substitution token performed for the builder command line ('Build Command:' option) :
 * $DEEBUILDER.COMPILEREXEPATH - Full path to the compiler executable, as defined in the project's configured interpreter.
 * $DEEBUILDER.COMPILERPATH - Full path to the directory containing the compiler executable (same as $DEEBUILDER.COMPILEREXEPATH, but without the last segment).


When run, the builder will print its output on the Eclipse console. If by any reason you have problems with the builder, it can be disabled on the Builders page of the project properties.