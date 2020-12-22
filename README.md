# Credhub Sample

This application can be used to test CredHub connectivity by deploying it to a Cloud Foundry platform that has the CredHub broker installed.

The demo application includes a deployment manifest that makes it simple to deploy the application. 

## Prerequisites

Required

* Access to a foundation with Pivotal Application Service 2.8 or better installed
  * and [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) account credentials
    * e.g., an account on [PCFOne](https://apps.run.pcfone.io/) should suffice 


## Tools

* [git](https://git-scm.com/downloads) 2.20.1 or better
* [JDK](http://openjdk.java.net/install/) 8 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 6.41.0 or better


## Clone

```
git clone https://github.com/pacphi/credhub-sample.git
```

## Build

```bash
cd credhub-sample
../gradlew assemble
```

## Authenticate

If you're using PCFOne to demo, then

```bash
cf api api.run.pcfone.io
cf login --sso
```
> Follow the link and authenticate via Workspace One.  Use the token returned as your password.

## Deploy

### The simple case

Target an organization and space.

```bash
cf target -o {org} -s {space}
```

Deploy

```bash
cf push --no-start
```
Be aware that you may have to change the application name.  You can override the one supplied in the manifest by adding a name, like so

```bash
cf push my-credhub-demo --no-start
```
> If you choose to do this, then make sure you replace all occurrences of `spring-credhub-demo` below with the name you specified.


Create a Credhub service instance and seed with initial configuration

```bash
cf create-service credhub default mych -c stuff.json
```

Bind service instance to application, then startup the application

```bash
cf bind-service spring-credhub-demo mych
cf start spring-credhub-demo
```

### Update configuration in service instance and restage application

Edit the contents of `stuff.json`, then

```bash
cf update-service mych -c stuff.json
cf restage spring-credhub-demo
```

### Emulate a blue-green deployment

// TODO We will fire up multiple instances of the application and orchestrate calls to `cf map-route` and `cf unmap-route`

