import java.net.*

int port = args.length == 1 ? Integer.parseInt(args[0]) : 1317

def generateError(output, msgID) {
   println("WASM ERROR")
   output.write(("msgID:" + msgID + "\n").bytes)
   output.write("status:NO\n".bytes)
   output.write("statusDesc:Invalid iKey\n".bytes)
   output.write("\n".bytes)
}

def generateOutput(output, iKey, sKey, msgID) {
   def key = iKey != '' ? iKey : sKey
   def f = new File('db/'+key+'.txt')
   if (!f.exists()) {
      generateError(output, msgID)
      return
   }
   println("WASM OK")
   output.write(("msgID:" + msgID + "\n").bytes)
   output << new FileInputStream(f)
}

println("WASM Server listening to " + port + ". Control-C to finish")
server = new ServerSocket(port)
while(true) {
  server.accept({ socket ->
    socket.withStreams { input, output ->
      try {
        def msgID = ''
        def iKey = ''
        def sKey = ''
        def lines = 0
        input.eachLine() { line ->
          lines = lines + 1
          println("Received line " + line);
          if (line.startsWith("msgID:")) {
             msgID = line.substring(6)
             return
          }
          if (line.startsWith("iKey:")) {
             iKey = line.substring(5)
             return
          }
          if (line.startsWith("sKey:")) {
              sKey = line.substring(5)
              return
           }
          if (line.length() == 0) {
            print("WASM > " + msgID + ", " + iKey + ", " + sKey + "? ")
            generateOutput(output, iKey, sKey, msgID)
            throw new GroovyRuntimeException()
          }
          if (lines > 20) {
            throw new GroovyRuntimeException()
          }
        }
      } 
      catch (GroovyRuntimeException b) { }
      catch (Exception e) { e.printStackTrace(System.out) }
    }
  })
}

