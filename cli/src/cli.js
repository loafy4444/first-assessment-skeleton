import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

//  Breaks the command line.
//  Adding is not advised.
// const exit = cli.find('exit')
// if (exit) {
//   exit.remove()
// }

let username
let host
let server
let msg
let commands = ['echo', 'broadcast', '@', 'users', 'disconnect']
let commandPersist

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host]', 'Connect to the host server provided (default \'localhost\' if left out) with username provided.')
  .delimiter(cli.chalk['green']('ChatBox>'))
  .init(function (args, callback) {
    username = args.username
    if (args.host !== undefined) { // TODO check working properly
      if (Number.isNaN(args.host)) {
        host = args.host
      } else {
        host = '10.1.1.' + args.host
      }
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
    let [ command, ...rest ] = words(input, /\S+/g)
    let contents

    if (!commands.includes(command.toLowerCase()) && command.charAt(0) !== '@') {
      contents = (command + ' ' + rest.join(' '))
      command = commandPersist
    } else {
      contents = rest.join(' ')
    }

    if (command === undefined) {
      this.log(`Command was not recognized.  Valid Commands Include: ${commands}`)
    } else if (command.toLowerCase() === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      commandPersist = command
    } else if (command.toLowerCase() === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      commandPersist = command
    } else if (command.toLowerCase() === 'users') {
      server.write(new Message({ username, command }).toJSON() + '\n')
    } else if (command.toLowerCase() === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command.toLowerCase().charAt(0) === '@') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      commandPersist = command
    } else {
      this.log('Not sure what the crap happened but I should look into it.')
    }
    callback()
  })
