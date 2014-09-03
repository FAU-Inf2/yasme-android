YASME for Android 
=================

What is YASME
-------------

Yasme (Yet Another Secure MEssenger) is a secure and free open source messenger. It is currently developed by students in a university project.

We believe that everybody has the right to retain his/her privacy and security, therefore we focus on these issues.                                     
YASME supports real end-to-end encryption using RSA, giving protection against interception by unwanted parties.
Your data is symmetrically encrypted using AES on your own device. The encrypted message is stored on our server, even we can not read your content. We will store your email address (to identify your account) and your group affiliation as well as all data needed for a working communication.
Note that encryption is not limited to one-to-one chats, each group chat has it's own symmetrical key - you have to be part of the group to read the messages.
By focussing on data privacy, we guarantee that we will never misuse your data by analysing or selling it, now or in the future.

We believe that trust in security products can only be gained by openess and clarity about the product's inner workings, contrary to most popular messenger apps out there. We decided to publish YASME as an open source project. YASME's sources will be open to everyone. Remember that this version is an alpha release. The app's code as well as it's capabilities will change and improve in the future.

Version
-------
Current version is: **0.7.5**

Where to get YASME
------------------

* [Google Play Store][gp]
* [GitHub][gh]

How to build and run YASME from source
--------------------------------------

###With Android Studio
* Get and Open the latest version of [Android Studio][as]
* Choose **Check out from Version Control**
* Choose **git**
* Fill in the **Vcs Repository URL:** 'https://github.com/FAU-Inf2/yasme-android'
* Choose a local Project folder in **Parent Directory** and a **Directory Name**
* Press **Clone**
* In the upcoming window choose **User default gradle wrapper** and press **OK**
* Go To **Tools &rarr; Android &rarr; SDK Manager**
	* Install Android SDK Platform 19
	* Install Android SDK Build-Tools 19.1
	* Install Google Support Repository
* Press Run

###Via command-line
* Clone the project: `git clone https://github.com/FAU-Inf2/yasme-android.git`
* Get [gradle][g1] [1.12][g2]
* Get [Android SDK Platform 19 and Android SDK Build-Tools 19.1][at], as well as [Google Support Repository][gs]
* Change directory to the android folder: `cd yasme-android`
* If `local.properties` file does not exists then create
* Add the sdk path to `local.properties`: `sdk_dir=/path/to/your/sdk`
* Build the YASME apk: `gradle assembleDebug`
* Plugin your Android Device to your PC - Make sure USB Debugging is turned on!
* Install YASME: `adb install -r yasme/build/outputs/apk/yasme-debug.apk`

Contribution
------------
Contributions are welcome provided all contributions carry the [MIT license][ml].

To contribute to our project:

1. [Fork][fk] YASME
2. Create a topic branch - `git checkout -b my_branch`
3. Push to your branch - `git push origin my_branch`
4. Create an [Issue][ie] with a link to your branch
5. That's it!

License
-------

Copyright 2014 Yasme.

Licensed under the [MIT license][ml].

Libraries used for YASME
------------------------
* [**ORMLite**][orm] "Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that this permission notice appear in all copies." http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_9.html#License 
*  [**Apache**][apache] [Apache License, v2.0][apl] 
* [**Jackson**][js] [Apache License, v2.0][apl2] like all official Jackson components." "[FasterXML][fxml] also explicitly allows users to alternatively license component under Lesser GPL (LGPL) 2.1 up to and including version 2.2. If so, they may want to repackage artifacts to reflect their choice of license to use." 
* [**android support v4:20**][ans] [Apache License Version 2.0][apl2] 


Future plans
------------
The App will have the following features with the next release:

* Correct adding and deleting of participants in group chats
* Sending and receiving media (pictures/videos/...)
* Contact- and key-exchange via QR code scanning
* Authentic perfect forward secrecy using DHE-RSA
* More platforms
* And much, much more!


Contact us
----------
Feel free to contact us: <yasme@i2.cs.fau.de>

[as]: https://developer.android.com/sdk/installing/studio.html
[at]: https://developer.android.com/sdk/index.html
[fk]: https://help.github.com/forking/
[g1]: http://www.gradle.org/downloads
[g2]: https://services.gradle.org/distributions/gradle-1.12-bin.zip
[gp]: https://play.google.com/store/apps/details?id=de.fau.cs.mad.yasme
[gh]: https://github.com/FAU-Inf2/yasme-android
[gs]: https://developer.android.com/tools/support-library/setup.html
[ie]: https://github.com/FAU-Inf2/yasme-android/issues
[ml]: http://opensource.org/licenses/MIT
[orm]:http://ormlite.com/
[apache]:http://www.apache.org/
[js]:https://github.com/FasterXML/jackson
[ans]:http://developer.android.com/reference/android/support/v4/app/package-summary.html
[apl]:http://hc.apache.org/httpcomponents-client-4.3.x/license.html
[apl2]:http://www.apache.org/licenses/LICENSE-2.0.txt
[fxml]:https://github.com/FasterXML/jackson-core/wiki 

