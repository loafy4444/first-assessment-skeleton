import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

// const _ = require('lodash')

export const cli = vorpal()

let username
let host
let server
let msg
let commands = ['echo', 'broadcast', '@', 'users', 'disconnect']
let commandPersist

cli
  .delimiter(cli.chalk['yellow']('ChatBox~$'))

cli
  .mode('connect <username> [host]', 'Connect to the host server provided (default \'localhost\' if left out) with username provided.')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    if (args.host === undefined) { // TODO check working properly
      host = args.host
    } else {
      host = 'localhost'
    }

    server = connect({ host: host, port: 8080 }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      msg = Message.fromJSON(buffer)
      switch (msg.command) {
        case 'echo':
          this.log(cli.chalk['bgRed'](msg.toString()))
          break
        case 'broadcast':
          this.log(cli.chalk['bgGreen'](msg.toString()))
          break
        case '@':
          this.log(cli.chalk['bgYellow'](msg.toString()))
          break
        default:
          this.log(cli.chalk['inverse'](msg.toString()))
      }
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input, /\S+/g) // Parses out words based on non-whitespace characters
    let contents

    if (!commands.includes(command)) {
      contents = (command + ' ' + rest.join(' '))
      command = commandPersist
    } else {
      contents = rest.join(' ') // TODO does not yet function correctly with @user
    }

    // const contents = rest.join(' ')

    // this.log(command)
    // this.log(contents)

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
      case 'users':
        server.write(new Message({ username, command }).toJSON() + '\n')
        commandPersist = command
        break
      default:
        if (command.charAt(0) === '@') {
          server.write(new Message({ username, command, contents }).toJSON() + '\n')
          commandPersist = command
        } else {
          this.log(`Command <${command}> was not recognized`)
        }
    }

    callback()
  })
