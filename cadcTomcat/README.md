# cadcTomcat

This module contains two plugins for Tomcat 7:
- A authentication realm plugin
- An SSL plugin to enable x509 Client Certificates to work directly with tomcat

## Dependencies
- catalina.jar (catalina-7.0.33.jar)
- tomcat-util.jar (tomcat-util-7.0.33.jar)
- tomcat-juli.jar (tomcat-juli-7.0.33.jar)
- tomcat-coyote.jar (tomcat-coyote-7.0.33.jar)

## CADC Tomcat Realm Plugin
 
This plugin will authenticate users who enter with a userid and password.  The authentication mechanism will call the access control web service (in module cadcAccessControl-Server) to see if the credentials are correct.
 
To use this plugin, add the following line to the <Host> element (within the <Service> element) in the tomcat 7 server.xml file:
 
> <Realm className="ca.nrc.cadc.tomcat.CadcBasicAuthenticator" />
 
## CADC SSL Plugin
 
The SSL plugin is a custom custom trust management implementation for apache tomcat (version 7) that overrides the default tomcat trust behaviour by adding trust to valid proxy certificates.

cadcTomcat Installation Steps:
1. Create / identify keystore file (serves as server identity)
2. Create / identify truststore file (list of CAs that server trusts)
3. Checkout cadcTomcat source and build
4. Include cadcTomcat.jar in $CATALINA_HOME/server/lib
5. Configure server.xml to use custom trust store

### Step 1: Create / identify keystore file (serves as server identity)

Steps to create a development version of a keystore file.

Notes:
- Common name (first & last name) must be the fully qualified name of the server.
- Keystore password MUST match key password (only hit enter on last step)
  - Record name/location of keystore and password for use in Step 5.

```
> keytool -keystore $KEYSTORE_DIR/tomcatkeystore.ks --genkey -alias tomcat
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  server.cadc.nrc.ca
What is the name of your organizational unit?
  [Unknown]:  CADC
What is the name of your organization?
  [Unknown]:  NRC
What is the name of your City or Locality?
  [Unknown]:  Victoria
What is the name of your State or Province?
  [Unknown]:  British Columbia
What is the two-letter country code for this unit?
  [Unknown]:  CA
Is CN=server.cadc.nrc.ca OU=CADC, O=NRC, L=Victoria, ST=British Columbia, C=CA correct?
  [no]:  yes

Enter key password for <tomcat>
        (RETURN if same as keystore password):
```


### Step 2: Create / identify truststore file (list of CAs that server trusts)

Steps to create a development version of a truststore file.

Notes:
- Only one truststore file can be used.  This means that the common list of CAs needs to be merged with any internal CAs. 
- The common list of java trusted CAs is: $JAVA_HOME/jre/lib/security/cacerts Note the location / name of the truststore file.  The password is 'changeit'.

If no internal CAs need to be identified, then the default java trust store
file can be used: $JAVA_HOME/jre/lib/security/cacerts

Otherwise, follow these steps to combine the common set of CAs with internal
CAs:
```
> cp $JAVA_HOME/jre/lib/security/cacerts $KEYSTORE_DIR/tomcattruststore.ks
> chmod 664 $KEYSTORE_DIR/tomcattruststore.ks
> keytool -import -alias root -keystore $KEYSTORE_DIR/tomcattruststore.ks -trustcacerts -file <path to internal CA public key file .crt>
```

Repeat the third command for each internal CA that needs importing.

### Step 3: Checkout cadcTomcat source and build

```
> svn checkout http://opencadc.googlecode.com/svn/trunk/projects/cadcTomcat $WORK_DIR/cadcTomcat
> ant clean build
```

### Step 4: Include cadcTomcat.jar in $CATALINA_HOME/server/lib

```
> ln -s $WORK_DIR/cadcTomcat/build/lib/cadcTomcat.jar $CATALINA_HOME/server/lib/cadcTomcat.jar
```

### Step 5: Configure tomcat's conf/server.xml to use custom trust store

Add a connector in tomcat's server.xml file.  Relevant elements are:

keyStoreFile      - Points to the created / identified keystore
keystorePass      - The keystore password
truststoreFile    - Points to the created / identified truststore
truststorePass    - The truststore password
SSLImplementation - The CADC Custom implementation of TrustManagers that
                    accepts proxy certificates (default tomcat trust
                    manager does not.)

```
    <Connector port="443" protocol="org.apache.coyote.http11.Http11Protocol"
            maxThreads="600"
            scheme="https"
            secure="true"
            SSLEnabled="true"
            keystoreFile="$KEYSTORE_DIR/tomcatkeystore.ks"
            keystorePass="changeit"
            keyAlias="tomcat"
            clientAuth="true"
            truststoreFile="$KEYSTORE_DIR/tomcattruststore.ks" 
            truststorePass="changeit"
            truststoreType="JKS"
            sslProtocol="TLS"
            SSLImplementation="ca.nrc.cadc.auth.CadcSSLImplementation"/>
```
