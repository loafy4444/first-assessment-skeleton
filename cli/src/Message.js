export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

// TODO add timestamp functionality
// add fuction to check message type and set cli.chalk to appropriate color here?

  constructor ({ timestamp, username, command, contents }) {
    // console.log(timestamp)
    this.timestamp = timestamp
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    return this.contents
  }
}
