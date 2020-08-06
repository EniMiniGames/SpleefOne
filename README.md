# SpleefOne  
######A spleef plugin for Minecraft servers via the Spigot API  

#### Building  

Assuming you have Java and Git installed:  
Assuming an Ubuntu 18.04 machine:  
Run these commands:

```sh
# Clone repo
git clone https://github.com/EniMiniGames/SpleefOne.git

# Mark gradle wrapper script as executable
chmod +x ./gradlew

# Just checking if code checks out
./gradlew compileJava
./gradlew build

# Build jars, will be availible in build/libs/SpleefOne-*.jar
./gradlew jar
```

#### Debugging Remotely  
Assuming you have a server, and connect to it with Putty, and you've configured your putty so that you can login to the server with no user prompts:  

- Edit send_to_server.bat, and change do-Flax to the name of your saved Putty session.  

- Edit send_to_server.script to whatever your plugin folder is.  

- Run send_to_server.bat  




