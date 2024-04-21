
# Concurrent Chords: Harmonizing Distributed Strings with Java RMI

## Introduction

This project demonstrates the power of Java Remote Method Invocation (RMI) to manage a distributed string array with sophisticated concurrency controls. By encapsulating a string array within a remote object accessible via Java RMI, it allows multiple clients to perform concurrent read and write operations securely and efficiently.

## Key Features

- **Distributed Access**: Clients from remote locations can access and manipulate the string array as if it were local.
- **Concurrency Controls**: Ensures that multiple clients can safely interact with the string array using read and write locks.
- **Interactive Client Application**: Provides a user-friendly interface for performing array operations remotely.

## Badges

![Java](https://img.shields.io/badge/java-v1.8+-blue.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## Getting Started

These instructions will get your copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

What things you need to install the software and how to install them:

```bash
# Java Development Kit (JDK)
sudo apt update
sudo apt install openjdk-11-jdk
```

### Installation

A step by step series of examples that tell you how to get a development env running:

1. **Clone the repository**:
    ```bash
    git clone [repository-url]
    ```
2. **Compile the Java source files**:
    ```bash
    javac RemoteStringArrayServer.java RemoteStringArrayClient.java RemoteStringArray.java RemoteStringArrayInterface.java
    ```
3. **Start the Java RMI registry**:
    ```bash
    start rmiregistry
    ```

### Running the Server

```bash
java RemoteStringArrayServer server_config.txt
```

### Running the Client

```bash
java RemoteStringArrayClient client_config.txt
```

## Project Structure

Here's a brief overview of the main components:

- **RemoteStringArrayServer.java**: Initializes the server and remote object.
- **RemoteStringArrayClient.java**: Client that provides a UI for user commands.
- **RemoteStringArray.java**: Implements the remote object functionality.
- **RemoteStringArrayInterface.java**: Interface that defines the RMI methods.

## Usage Examples

The following examples showcase how clients can use the commands to interact with the `RemoteStringArray` server. Each command allows the client to perform specific operations on the remote string array.

### Get Array Capacity
This command displays the maximum number of elements in the string array:
```bash
java Client Get_Array_Capacity
```

### Fetch an Element in Read-Only Mode
Fetches the element at the specified index in read-only mode. The client must ensure that a read lock is obtained:
```bash
java Client Fetch_Element_Read <index>
```
Example:
```bash
java Client Fetch_Element_Read 2
```

### Fetch an Element in Read/Write Mode
Fetches the element at the specified index in read/write mode. The client must ensure that a write lock is obtained:
```bash
java Client Fetch_Element_Write <index>
```
Example:
```bash
java Client Fetch_Element_Write 2
```

### Print an Element
Prints the string associated with the specified element. It is assumed that the element has been previously fetched from the server:
```bash
java Client Print_Element <index>
```
Example:
```bash
java Client Print_Element 1
```

### Concatenate String to Element
Concatenates a given string to the contents of the specified element. It is assumed that the element has been previously fetched:
```bash
java Client Concatenate <index> <string>
```
Example:
```bash
java Client Concatenate 3 " additional text"
```

### Write Back Element
Attempts to copy the element back to the server. The command displays "Success" if the operation was successful and "Failure" if not, such as when the client does not hold the write lock:
```bash
java Client Writeback <index>
```
Example:
```bash
java Client Writeback 1
```

### Release a Lock
Releases any locks held by this client on the specified element:
```bash
java Client Release_Lock <index>
```
Example:
```bash
java Client Release_Lock 2
```

These commands represent the basic functionality offered to clients to interact with the remote string array. Make sure to handle the concurrency conditions correctly to ensure the system operates reliably under load.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

