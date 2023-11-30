# SMTP Client

## Configuration instructions

### Building
This tool uses Maven for building and fetching the dependencies. To compile the code, simply run: 

```mvn clean package```

### Data
The messages and addresses must be stored in separate JSON files as follows: 

#### Messages
```json
[
    {
        "subject": "My subject", 
        "body": "My\nbody"
    },
    {
        "subject": "My subject #2", 
        "body": "My\nBody #2"
    }
]
```
#### Addresses

```json
[
    {
        "address": "myaddress@gmail.com"
    },
    {
        "address": "mysecondaddress@gmail.com"
    }
]
```

### Running
To run the program, simply run the following command in your terminal: 

`java -jar target/client_smtp-1.0-SNAPSHOT.jar <nGroups> <path/to/addressFile> <path/to/messagessFile>`

Where nGroups represents the number of groups to form and send emails to.

## Implementation details
