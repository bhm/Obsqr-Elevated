#Obsqr 

##Branches

###Obsqr Elevated
Elevated version makes use of android permissions to read and write contacts. Now you only need two taps to automatically add a contact form a QR code.

###Master branch
Master is being kept as close as to original that is being developed by Trikita.
Obsqr Master branch should stay as close to updates as possible over at this repo.

###Changelog
####2.5-1.3
* Empty WiFi properties fix
* Warning while no password while connection specified is encrypted in WiFi
* Delay of obnoxious rate dialog until way later ( min 25 back pressed which does not occur always)
* New graphics

####2.5-1.2
* WiFi support with accordance to proposed standard [here](http://code.google.com/p/zxing/wiki/BarcodeContents)

####2.5-1.1
* Changes in ContactManager and QrParser.
* Change to naming conversion Trikita Version-Elevated version

####2.5
* Update to latest Trikita version.
* Primer for future versions
* Split into branches

###Requirements
----

To make it run on your Android device you need to have Andoird 1.6 or higher.
Both, ARM and x86 architectures are supported.

####Build
----

obsqr uses zbar library to decode QR images, so before building obsqr you need
to install zbar first. 

To make easier, there is a helper script called 'fetch-zbar.sh':

    $ bash fetch-zbar.sh

This will fetch zbar sources from the official repository and
create symlinks to the zbar sources inside `jni` directory.

Then, update project accodring to your SDK version:

	android update project -p . -t <your-target>

Edit local.properties by specifying path to the NDK:

	...
	sdk.path=/path/to/sdk
	ndk.path=/path/to/ndk
	...

Finally, run ant to build obsqr:

	$ ant debug

After this step, you should get an *.apk inside the 'bin' directory.

##License
----

obsqr is free software distributed under the terms of the MIT license.
See LICENSE file for more details.

