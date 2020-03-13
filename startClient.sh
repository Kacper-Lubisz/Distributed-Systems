#!/usr/bin/env bash

#java "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 201.6073.9\lib\idea_rt.jar=51098:C:\Program Files\JetBrains\IntelliJ IDEA 201.6073.9\bin" -Dfile.encoding=UTF-8 -classpath "C:\Users\kacpe\Desktop\Distributed Systems\out\production\Distributed Systems;C:\Users\kacpe\.m2\repository\org\jetbrains\annotations\19.0.0\annotations-19.0.0.jar;C:\Users\kacpe\Desktop\Distributed Systems\lib\json-simple-1.1.1.jar" uk.ac.dur.client.Client

mkdir -p out
echo "Compiling"
javac -source 12 -classpath "./lib/*" -d "./out" $(find ./src | grep ".java")
echo "Compilation finished"

echo "The standard output of the front end and client is redirected to log files in /log"
mkdir -p log

java -cp "./out:./lib/*" uk.ac.dur.backend.middleware.BasicFrontEnd >./log/frontend.txt &

echo "Done"
echo "Starting 10 Replicas"

for I in {1..10}; do

  java -cp "./out:./lib/*" uk.ac.dur.backend.middleware.RestaurantServerState .5 > "./log/replica${I}.txt" &

done
echo "Done"

echo "Starting Client"
java -cp "./out:./lib/*" uk.ac.dur.client.Client
