[Micronaut Guides](https://guides.micronaut.io) or [Guides Index](https://guides.micronaut.io/latest/)

--- 

## Working on a single guide 

To work on a single guide use the system property `micronaut.guide`

E.g. 

````bash
%> ./gradlew -Dmicronaut.guide=micronaut-oauth2-github build
````

----

## Examples

See examples of guides in the `guides` folder.

----

## New Guide Template

To create a new guide create a new asciidoc file under `src/guides` with the following template: 
 

```asciidoc
include::{commondir}/common-header.adoc[]

include::{commondir}/common-requirements.adoc[]

include::{commondir}/common-completesolution.adoc[]

include::{commondir}/common-create-app.adoc[]

TODO: Describe the user step by step how to write the app. Use includes to reference real code: 

Example of a Controller

source:HelloController[]

Example of a Test

test:HelloControllerTest[]

include::{commondir}/common-testApp.adoc[]

include::{commondir}/common-runapp.adoc[]

include::{commondir}/common-graal-with-plugins.adoc[]

:exclude-for-languages:groovy

TODO describe how you consume the endpoints exposed by the native image with curl

:exclude-for-languages:

TODO Use the generic next step 

include::{commondir}/common-next.adoc[]

TODO or a personalized guide for the guide:

== Next steps

TODO: link to the documentation modules you used in the guide

include::{commondir}/common-helpWithMicronaut.adoc[]
```

----

## Deployment

Guides are published to [gh-pages](https://pages.github.com).

