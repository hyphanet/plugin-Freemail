Thanks for trying Freemail!

This is the first release of Freemail, and so may (read: does) have bugs that I haven't found yet. Please do report them at http://bugs.freenetproject.org/.

Using Freemail
==============

You can compile from source:

compile: (however you compile Java, an ant buildfile is supplied)

To build with ant, create a file override.properties with content similar to
the following:

bcprov.location = <your home dir>/.gradle/caches/modules-2/files-2.1/org.bouncycastle/bcprov-jdk15on/1.59/2507204241ab450456bdb8e8c0a8f986e418bd99/bcprov-jdk15on-1.59.jar
junit = <your home dir>/.gradle/caches/modules-2/files-2.1/junit/junit/4.13.2/8ac9e16d933b6fb43bc7f576336b8f4d7eb5ba12/junit-4.13.2.jar
hamcrest = <your home dir>/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest/3.0/8fd9b78a8e6a6510a078a9e30e9e86a6035cfaf7/hamcrest-3.0.jar

Now run `ant clean; ant`

Once you've done one of those steps, run:

java -cp ~/.gradle/caches/modules-2/files-2.1/org.bouncycastle/bcprov-jdk15on/1.59/2507204241ab450456bdb8e8c0a8f986e418bd99/bcprov-jdk15on-1.59.jar:~/.gradle/caches/modules-2/files-2.1/org.freenetproject/freenet-ext/29/507ab3f6ee91f47c187149136fb6d6e98f9a8c7f/freenet-ext-29.jar:./hyphanet-fred/build/libs/freenet.jar:./build/classes org.freenetproject.freemail.FreemailCli

(You can also specify the address (host) and port of your Freenet node
using -h and -p respectively, if they are not the defaults).

Set up your email client to point at IMAP port 3143 and SMTP port 3025.

Feel free to Freemail me on dave@dbkr.freemail! If that doesn't work, my real email address is dbkr@freenetproject.org.

Good luck!
