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