# UDP Error Control

## Protocol Design
### Header
|              0 - 15 bits            |               16 - 32 bits              |
| ----------------------------------- | --------------------------------------- |
|            sequence number               |                window size              |
|            message type             |             payload length              |

- sequence number

The sequence number of the packet (starts from 0)

- window size

The max number of outstanding packets (default set to 4)

- message type
  - S: `Sync` packet sent by sender
  - A: `Ack` packet sent by receiver notifying packet received
  - L: `Lost` packet sent by receiver notifying packet lost

- payload length 

The playload size in byte

### Go-Back-N
**Java** is the programming language
#### What's implemented
- The total number of outstanding packets at a time cannot be greater than `window size`
- Receiver keeps track of the `expected seq #`. If the packet received has sequence number other than `expected seq #', receiver
simply drop the packet
- Upon receiving each packet from sender, receive sends `A(ck)` to sender notifying packet received.
- If receiver sees packet loss, sends `L(ost)` packet with the `sequence number` of the lost packet
- If sender sees `L(ost)` response packet, reset the start point of the current sliding window to `sequence number` in the `L(ost)` packet and
resend all packets from here
- Sender caches packets sent in a `HashMap`

#### What's not implemented
- handle server timeout

#### Build & Run
0. prerequisites
- JDK 1.8
- Gradle

1. Build
```bash
cd <Project Root>
gradle build
# you should see `build` directories are created under each sub projects
```

2. Start the Server

```bash
cd <Project Root>/gbnServer/build/libs/
java -jar gbnServer-1.0.jar --lost <Lost Rate> --output <Path to the output file you want to compare>
```

3. Start the Client

```bash
cd <Project Root>/gbnClient/build/libs/
java -jar gbnClient-1.0.jar
```

4. Sample Terminal Output
```bash
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Lost Rate = 80%<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# Server

RequestHanler is running at localhost:55055 with lost rate 80 %
Done Writing to file!
Generating Server Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| Simulated Lost Rate | # Packets Received | # Packets Lost + | Packets Dropped |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|         80 %        |         1286      |          1088       |      0        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Shutdown signaled...


# Client
Starting the UDP client...
Done reading all the file.
 Total bytes read: 200001
Generating Client Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| # Packets Sent | # Packets Lost + | Time Elapsed (ms) |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|        1286      |        1088       |     76        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Lost Rate = 20%<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# Server
RequestHanler is running at localhost:55055 with lost rate 20 %
Done Writing to file!
Generating Server Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| Simulated Lost Rate | # Packets Received | # Packets Lost + | Packets Dropped |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|         20 %        |         294      |          96       |      0        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Shutdown signaled...


# Client
Starting the UDP client...
Done reading all the file.
 Total bytes read: 200001
Generating Client Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| # Packets Sent | # Packets Lost + | Time Elapsed (ms) |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|        294      |        96       |     40        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Lost Rate = 5%<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# Server
RequestHanler is running at localhost:55055 with lost rate 5 %
Done Writing to file!
Generating Server Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| Simulated Lost Rate | # Packets Received | # Packets Lost + | Packets Dropped |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|         5 %        |         212      |          14       |      0        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Shutdown signaled...


# Client
Starting the UDP client...
Done reading all the file.
 Total bytes read: 200001
Generating Client Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| # Packets Sent | # Packets Lost + | Time Elapsed (ms) |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|        212      |        14       |     36        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Lost Rate = 2%<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# Server
RequestHanler is running at localhost:55055 with lost rate 2 %
Done Writing to file!
Generating Server Stats...
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
| Simulated Lost Rate | # Packets Received | # Packets Lost + | Packets Dropped |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
|         2 %        |         198      |          0       |      0        |
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Shutdown signaled...


# Client
SStarting the UDP client...
 Done reading all the file.
  Total bytes read: 200001
 Generating Client Stats...
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 | # Packets Sent | # Packets Lost + | Time Elapsed (ms) |
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 |        198      |        0       |     35        |
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

```