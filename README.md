# Credhub Sample

This application can be used to test CredHub connectivity by deploying it to a Cloud Foundry platform that has the CredHub broker installed.

The demo application includes a deployment manifest that makes it simple to deploy the application. 

## Prerequisites

Required

* Access to a foundation with Pivotal Application Service 2.6 or better installed
  * and [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) account credentials
    * e.g., an account on [PCFOne](https://apps.run.pcfone.io/) should suffice 
* Targeted foundation should have the [Credhub Broker for PCF](https://docs.pivotal.io/credhub-service-broker/) service tile installed and the service enabled in the `cf marketplace`


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

## Simple Deployment

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

### Test the application

If you have `curl`, then

```bash
curl -k https://spring-credhub-demo.apps.pcfone.io
```

Better yet, with [httpie](https://httpie.io/)

```
http https://spring-credhub-demo.apps.pcfone.io
```

### Update configuration in service instance and restart application

Edit the contents of `stuff.json`, then

```bash
cf update-service mych -c stuff.json
cf restart spring-credhub-demo
```
> Note we'll take down time while we do this.  We'll explore how to update configuration in a zero-down time fashion later on.


### Test the application again to see that the updates took effect

```bash
http https://spring-credhub-demo.apps.pcfone.io
```

### Teardown the application and service instance

```bash
cf unbind-service spring-credhub-demo mych
cf delete-service -f mych
cf delete -r -f spring-credhub-demo
```

## Blue-green deployment

Let's start with pushing a `blue-version` of the application, stamp it `blue`, create a service instance, bind it to the application and start the application.

```bash
cf push spring-credhub-demo-blue --random-route --no-start
cf set-env spring-credhub-demo-blue VERSION blue
cf create-service credhub default credhub-blue -c stuff-blue.json
cf bind-service spring-credhub-demo-blue credhub-blue
cf start spring-credhub-demo-blue
```

Next, we will map a shared host name `spring-credhub-demo` onto the same domain as the `blue-version` of the application as this will be the host name we will want users to visit to interact with the application.

```bash
BLUE_DOMAIN=$(cf app spring-credhub-demo-blue | grep routes | awk {'print $2'} | cut -d'.' -f 2,3,4,5,6)
echo $BLUE_DOMAIN
cf map-route spring-credhub-demo-blue $BLUE_DOMAIN --hostname spring-credhub-demo
```

Let's test that the result of transacting the shared host name and domain matches what we expect in [stuff-blue.json](stuff-blue.json).

```bash
http https://spring-credhub-demo.apps.pcfone.io
```

Let's pretend that we've made an update to the implementation and/or the configuration and we want to roll-out the update without disruption.  We'll push a `green-version` of the application, stamp it `green`, create a service instance, bind it to the application and start the application.

```bash
cf push spring-credhub-demo-green --random-route --no-start
cf set-env spring-credhub-demo-green VERSION green
cf create-service credhub default credhub-green -c stuff-green.json
cf bind-service spring-credhub-demo-green credhub-green
cf start spring-credhub-demo-green
```

Next, let's map the aforementioned shared host name on the `green-version` of the application.  When we do this, both applications will take on traffic at the shared host name and domain. Requests will be serviced in a random fashion from each versioned application.

```bash
GREEN_DOMAIN=$(cf app spring-credhub-demo-green | grep routes | awk {'print $2'} | cut -d'.' -f 2,3,4,5,6)
echo $GREEN_DOMAIN
cf map-route spring-credhub-demo-green $GREEN_DOMAIN --hostname spring-credhub-demo
```

To complete the cut over to the `green-version` of the application (and configuration) we will `unmap` the shared host name from the `blue-version` of the application (and configuration).

```bash
cf unmap-route spring-credhub-demo-blue $BLUE_DOMAIN --hostname spring-credhub-demo
```

Let's test that the result of transacting the shared host name and domain matches what we expect in [stuff-green.json](stuff-green.json).

```bash
http https://spring-credhub-demo.apps.pcfone.io
```

At this point the `blue-version` of the application will no longer take on new requests at the shared host name and domain.  You may decide to tear it down or roll back if there are noted issues with the `green-version` of the application (and configuration).  Rolling back is as simple as re-mapping the shared host name to the `blue-version` of the application with the `cf map-route` command above. Then you could `cf unmap-route` the `green-version` of the application.  Tearing down the `blue-version` is accomplished with:

```bash
cf unbind-service spring-credhub-demo-blue credhub-blue
cf delete-service -f credhub-blue
cf delete -r -f spring-credhub-demo-blue
```

Note: if you don't want to allow direct public access to either the blue or green versions of the application you could swap `--random-route` with `--no-route`. However, the method we use above to obtain the domain would have to change.  (But then you should already know the domain, shouldn't you, for the target foundation in your operating environment?)
 
## Credits

* [Credhub Service Broker for PCF](https://docs.pivotal.io/credhub-service-broker/)
* [cf update-service](https://cli.cloudfoundry.org/en-US/v6/update-service.html)
* [cf map-route](https://cli.cloudfoundry.org/en-US/v6/map-route.html)
* [cf unmap-route](https://cli.cloudfoundry.org/en-US/v6/unmap-route.html)
* [Java CFEnv](https://github.com/pivotal-cf/java-cfenv)
* [CloudFoundry: Using Blue-Green deployment and route mapping for continuous deployment](https://fabianlee.org/2018/02/20/cloudfoundry-using-blue-green-deloyment-and-route-mapping-for-continuous-deployment/)

