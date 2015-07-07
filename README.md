# rpiIncubator
rpiIncubator is a demo project to show the Java / microservice (Vert.x) capabilities of Raspberry PI using sensors, MQTT or WebSockets

## Quickstart
To run the demo locally build the project (mvn clean package) and execute:
```java
java -jar target/rpiVertx-sensors-1.0-SNAPSHOT-fat.jar -testmode true
```
This will execute the MQTT Verticle, when you want to try the WebSocket implementation open the pom.xml and replace:
```xml
 <main-verticle>ch.trivadis.com.verticle.UltrasonicMQTTSensor</main-verticle>
```
by 
```xml
 <main-verticle>ch.trivadis.com.verticle.UltrasonicWebSocketSensor</main-verticle>
```
## Configuration
The application can be configured with command-line arguments or by providing a configuration json file (see serviceConfig.json). Possible values are:
- testmode true/false
- scheduleTime (in ms)
- mqttProtocol
- mqttIp
- mqttPort

If no configuration is provided, the application uses default values.

## deploy on Raspberry PI
The used UltrasonicSensor is part of the GroovePi package, so download and install the GroovePi debain on your Raspberry Pi.
### install Pi4J
Download the debian package (http://get.pi4j.com/download/pi4j-1.0.deb) and install (sudo dpkg -i pi4j-1.0.deb). Pi4j should now be available in /opt/pi4j
