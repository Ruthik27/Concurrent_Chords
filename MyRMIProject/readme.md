lsof -i:1099   

kill 12345

doesn't require to have a rmi hosted manually

javac -d bin src/com/myname/rmi/*.java    

java -cp bin com.myname.rmi.RemoteStringArrayClient resources/client_config.properties

java -cp bin com.myname.rmi.RemoteStringArrayServer resources/server_config.properties

