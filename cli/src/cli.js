import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let host
let server
let msg
let commands = ['echo', 'broadcast', '@', 'users', 'disconnect',
      'kirby', 'kirby1', 'kirby2', 'kirbyparty', 'partypooper']
let commandPersist
let kirbycount = 0

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host]', 'Connect to the host server provided (default \'localhost\' if left out) with username provided.')
  .delimiter(cli.chalk['green']('ChatBox>'))
  .init(function (args, callback) {
    username = args.username

    if (args.host !== undefined) {
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
      switch (msg.command) {  // These colors may look terrible on other clients so feel free to change them.
        case 'echo':          // My client has a dark background which almost requires the bg colors to be able to read them all.
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
    } else if (command.toLowerCase() === 'kirby') {
      server.write(new Message({ username, command: 'broadcast', contents: '^(\' - \')^ ^(\' - \')^ ^(\' - \')^' }).toJSON() + '\n')
      commandPersist = command
    } else if (command.toLowerCase() === 'kirby1') {
      server.write(new Message({ username, command: 'broadcast', contents: ' (>\'-\')> ^(\' - \')^ <(\'-\'<) ^(\' - \')^ (>\'-\')>' }).toJSON() + '\n')
      commandPersist = command
    } else if (command.toLowerCase() === 'kirby2') {
      server.write(new Message({ username, command: 'broadcast', contents: '<(\'-\'<) ^(\' - \')^ (>\'-\')> ^(\' - \')^ <(\'-\'<)' }).toJSON() + '\n')
      commandPersist = command
    } else if (command.toLowerCase() === 'kirbyparty') {  // Best command ever.  Just trust me.
      kirbyFunc()
      server.write(new Message({ username, command: 'broadcast', contents: 'KIRBY PARTY!!!!' }).toJSON() + '\n')
    } else if (command.toLowerCase() === 'partypooper') {  // Lamest command ever.  You have been warned.
      clearInterval(myKirby)
    } else {
      this.log('Not sure what the crap happened but I should look into it.')
    }
    callback()
  })

let myKirby
function kirbyFunc () {
  myKirby = setInterval(kirbyPartyFunc, 750)
}

function kirbyPartyFunc () {
  switch (kirbycount % 4) {
    case 0:
      server.write(new Message({ username, command: 'broadcast', contents: '^(\' - \')^ (>\'-\')> ^(\' - \')^ <(\'-\'<) ^(\' - \')^ (>\'-\')>' }).toJSON() + '\n')
      break
    case 1:
      server.write(new Message({ username, command: 'broadcast', contents: '<(\'-\'<) ^(\' - \')^ (>\'-\')> ^(\' - \')^ <(\'-\'<) ^(\' - \')^' }).toJSON() + '\n')
      break
    case 2:
      server.write(new Message({ username, command: 'broadcast', contents: '^(\' - \')^ <(\'-\'<) ^(\' - \')^ (>\'-\')> ^(\' - \')^ <(\'-\'<)' }).toJSON() + '\n')
      break
    case 3:
      server.write(new Message({ username, command: 'broadcast', contents: '(>\'-\')> ^(\' - \')^ <(\'-\'<) ^(\' - \')^ (>\'-\')> ^(\' - \')^' }).toJSON() + '\n')
      break
  }
  kirbycount++
}
