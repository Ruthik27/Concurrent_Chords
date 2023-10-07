lsof -i:1099   
javac -d bin src/com/myname/rmi/*.java  
rmiregistry -J-Djava.class.path=./bin 1099 &  
java -cp bin com.myname.rmi.RemoteStringArrayServer resources/server_config.properties
java -cp bin com.myname.rmi.RemoteStringArrayClient  
