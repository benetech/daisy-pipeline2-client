daisy-pipeline2-client
======================

This is a simple Java client for communicating with the DAISY Pipeline2 web service API. See http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI for a description of their API.

It consists of an interface definition (DAISYPipeline2Client) and an implementation of that interface (RestTemplatePipeline2Client) that relies on the following libraries:
* Spring's RestTemplate for calls on the service
* Apache HttpComponents HttpClient for the HTTP layer of the RestTemplate
* JDOM for parsing the responses
* Apache Commons IO for file and I/O utilities
* JUnit and EasyMock for unit testing

You can build and unit test it with Maven with "mvn clean install." 

A Spring configuration file (client-context.xml) contains a set of bean definitions for a standard instance of the implementation class.
