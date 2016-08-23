import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let host
let server
let commands = ['echo', 'broadcast', '@', 'users']
let commandPersist

cli
  .delimiter(cli.chalk['yellow']('ChatBox~$'))

cli
  .mode('connect <username> [host]', 'Connect to the host server provided (default \'localhost\' if left out) with username provided.')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    if (args.host !== undefined) { // TODO check working properly
      host = args.host
    } else {
      host = 'localhost'
    }

    server = connect({ host: host, port: 8080 }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      this.log(cli.chalk['red'](Message.fromJSON(buffer).toString()))
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input)
    const contents = rest.join(' ')

    switch (command) {
      case 'disconnect':
        server.end(new Message({ username, command }).toJSON() + '\n')
        break
      case 'echo':
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        commandPersist = command
        break
      case 'broadcast':
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        commandPersist = command
        break
      case '@':
        this.log(`Command <${command}> not yet implemented`)
        commandPersist = command
        break
      case 'users':
        server.write(new Message({ username, command }).toJSON() + '\n')
        commandPersist = command
        break
      default:
        this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
