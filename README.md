# event-log-handler
This is a coding challange solution for a interview process

## Problem Statement
#### Requirements for this coding assignment:
* Java 8
* Use of any open-source library is allowed
* Your program must use either the Gradle or Maven build system to resolve dependencies, build
and test
### Summary of task
Our custom-build server logs different events to a file named logfile.txt. Every event has 2 entries in the file - one entry when the event was started and another when the event was finished. The entries in the file have no specific order (a finish event could occur before a start event for a given id)
Every line in the file is a JSON object containing the following event data:
* id - the unique event identifier
* state - whether the event was started or finished (can have values "STARTED" or "FINISHED"
* timestamp - the timestamp of the event in milliseconds

Application Server logs also have the following additional attributes:
* type - type of log
* host - hostname

Example contents of logfile.txt:

```json
{"id":"scsmbstgra", "state":"STARTED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495212}
{"id":"scsmbstgrb", "state":"STARTED", "timestamp":1491377495213}
{"id":"scsmbstgrc", "state":"FINISHED", "timestamp":1491377495218}
{"id":"scsmbstgra", "state":"FINISHED", "type":"APPLICATION_LOG", "host":"12345",
"timestamp":1491377495217}
{"id":"scsmbstgrc", "state":"STARTED", "timestamp":1491377495210}
{"id":"scsmbstgrb", "state":"FINISHED", "timestamp":1491377495216}
```
In the example above, the event scsmbstgrb duration is 1491377495216 - 1491377495213 = 3ms
The longest event is scsmbstgrc (1491377495218 - 1491377495210 = 8ms)
### The program should:
* Take the path to logfile.txt as an input argument
* Parse the contents of logfile.txt
* Flag any long events that take longer than 4ms
* Write the found event details to file-based HSQLDB (http://hsqldb.org/) in the working folder
* The application should create a new table if necessary and store the following values:
 Event id
 Event duration
 Type and Host if applicable
 Alert (true if the event took longer than 4ms, otherwise false)

Additional points will be granted for:
* Proper use of info and debug logging
* Proper use of Object Oriented programming
* Unit test coverage
* Multi-threaded solution
* Program that can handle very large files (gigabytes)

## Solution Approach
As per the problem statement the task seems to be a batch process, as it may have to handle very large files (gigabytes). So I have priotized to control memory consumption than the execution time of the program.


1. I have created a Spring boot application with 3 REST services.
    * (POST) /events/storeLogFileEvents?fileName={fileName}
        This API will read all events from the provided file, calculates duration and alert for all events, stores in db table and returns all the events. It can throw an error if 
            - the fileName provided is not a valid file
            - if one event is already processed and stored in db, and user tries to process the same event again.(Database unique constraint violation, as event id has been used for db primary key)
    * (GET) /events
        This API will return all stored events in event_details table
	* (DELETE) /events
		This API will delete all stored events in event_details table
2. In order to read the file `java.util.Scanner` has been used for line-by-line reading. This will consume very less memory than loading the whole file content at once.
2. All data from logFile will be stored in a temp db table. After the operation, temp table will be dropped. This will consume a little more time for extra db operations, but it will surely handle large files with millions of records without creating any memory issue. This temp table name should be unique to each request, so that we can drop the temp table after the operation and other concurrent requests are not affected.
3. To maintain the uniqueness timestamp is appended to the temp table name, considering 2 requests will never come at same instant. However, in practical scenario timestamp should be replaced by requestId/orderId - which is unique to each request.
4. To make the db operations faster, I have used `org.springframework.jdbc.core.JdbcTemplate` for batch inserts(max 200 rows at a time).
5. Furthermore, batch insert operations are executed in Callable threads, so that the main file processing does not wait. One main reason for choosing Callable over Runnable is that, all batch inserts threads should be completed before final processing.
6. After batch inserts are done, I am using sql to calculate the `alert` flag and insert into new table `event_details`.
7. Finally the temp table is deleted.

### Scopes for improvement
1. Need to add unit test cases
2. Need to add proper exception handling
3. Dao layer coding can be improved to handle multiple errors
4. Use of properties to make code more dynamic

## Running the Project
To run this project **maven/IDE with maven plugin** is needed.
1. If maven is installed in system, then start the application using `mvn spring-boot:run` from command line inside the project root folder.
2. If using eclipse, then import the project as Maven project. Once imported and all dependencies are resolved, start the application by running the main method of  `com.test.dpg.DpgCodingAssignmentApplication`.
## Testing the APIs
For testing the api, we can use POSTMAN, or else we can use `curl` commands to call the api from commandline. Below is the API details if application is started in 8080 port and accessed from localhost.
Sample file location for testing: C:\Users\barun\Documents\Study\Coding\Inputs\log.txt
* (POST) http://localhost:8080/events/storeLogFileEvents?fileName=C:/Users/barun/Documents/Study/Coding/Inputs/log.txt
* (GET) http://localhost:8080/events
#### Curl commands to call the API:
* (POST) Store log files in DB
```
curl --data-urlencode "fileName=C:/Users/barun/Documents/Study/Coding/Inputs/log.txt" http://localhost:8080/events/storeLogFileEvents
```
* (GET) Fetch stored events from DB
```
curl http://localhost:8080/events
```
* (DELETE) Delete all stored events
```
curl -X DELETE http://localhost:8080/events
```
