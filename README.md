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

License
=======

This source code is being made available under the Revised BSD or The BSD 3-Clause License.

Copyright (c) 2012, Benetech Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
- Neither the name of Benetech nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.