connector-workproductsubmitter
==============================

A GUI to submit binary work product (.doc, pdf, .docx, .shz) to the XchangeCore and to be associated with an XchangeCore incident. This connector also allow user to submit of a Map Layer to the MapViewContext work product of the selected incident.  

Dependencies:
connector-base-util
connector-base-async

To Build:
1. Use maven and run "mvn clean install" to build the dependencies.
2. Run "mvn clean install" to build workproductSubmitter.

To Run:
1. Copy the workproductsubmitter/src/main/resources/contexts/async-context to the same directory of the ShalefileClient.jar executable jar file.
2. Use an editor to open the async-context file.
3. Look for the webServiceTemplate bean, replace the "defaultUri" to the XchangeCore you are using to run this adapter to create the incidents.
   If not localhost, change http to https, example "https://test4.xchangecore.leidos.com/uicds/core/ws/services"
4. Change the "credentials" to a valid username and password that can access your XchangeCore.
5. A more detail description of this XchangeCore connector, including the command to run, can be found in workproductsubmitter/doc directory.


