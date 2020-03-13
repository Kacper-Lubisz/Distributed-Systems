# Distributed Systems
## Notes

UML and Transaction diagrams can be found in diagrams.pdf

The project is targeting java 12 and doesn't compile with java 8 

The system has a mechanism throwing errors randomly when a command line argument is passed.  Note that replica processes
are terminated after an error occurs and are not restated (like they would in a proper system).

## Run instructions

###Start the whole system on localhost

This includes the following steps:

    + Compile
    + 1 x Front End
    + 1 x Client
    + 10 x Replica
    + (without added errors)

run (bash):

```shell script
$ ./startSystem.sh
```

To run with randomly added errors, you must edit the start system file.

Line 21 has a commented out version of line 21 which passes the command line argument which introduces a 20% error rate 
on all requests.

## Granular run instructions (copied from ./startSystem.sh)

###Compilation
```shell script
$ mkdir -p out
$ javac -source 12 -classpath "./lib/*" -d "./out" $(find ./src | grep ".java")
```

###Start Front End, log to stdout:
```shell script
$ java -cp "./out:./lib/*" uk.ac.dur.backend.middleware.BasicFrontEnd
```

###Start Replica Server error rate of 10%, log to stdout:
```shell script
$ java -cp "./out:./lib/*" uk.ac.dur.backend.middleware.RestaurantServerState 0.10
```

###Start Client,
```shell script
java -cp "./out:./lib/*" uk.ac.dur.client.Client
```