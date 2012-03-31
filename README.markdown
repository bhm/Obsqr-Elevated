# obsqr - minimalistic QR scanner for Android

obsqr is a fast and lightweight QR scanner application for Android.

### Changes from 2.4

* minSdkVersion escalated to 5
* permission rights escalated to 
	
    GET_ACCOUNTS
    WRITE_CONTACTS
    READ_CONTACTS

* vCardParser class to convert raw text into a suitable structures.
* ContactManager class to plug converted info into a contact database.
* Succesful adding of a contact is signaled by a Toast message.

------------

### Needs work

------------

* MECARD reimplementation for new classes, and cohesive adding contacts.

## Requirements



------------



To make it run on your Android device you need to have Andoird 1.6 or higher.

Both, ARM and x86 architectures are supported.



## Build



-----



obsqr uses zbar library to decode QR images, so before building obsqr you need

to install zbar first. 



To make easier, there is a helper script called 'fetch-zbar.sh':



    $ bash fetch-zbar.sh



This will fetch zbar sources from the official repository and

create symlinks to the zbar sources inside `jni` directory.



Then, update project accodring to your SDK version:



    $ android update project -p . -t <your-target>



Edit local.properties by specifying path to the NDK:


    sdk.path=/path/to/sdk

    ndk.path=/path/to/ndk


Optionaly export paths to 



    $ANDROID_SDK_DIR/tools

    $ANDROID_SDK_DIR/platform-tools

    $ANDROID_NDK_DIR/



Finally, run ant to build obsqr:



    $ ant debug



After this step, you should get an *.apk inside the 'bin' directory.

Install it normal way

   $ adb install -r ./bin/obsqr-debug.apk





## License



-------



obsqr is free software distributed under the terms of the MIT license.

See LICENSE file for more details.


