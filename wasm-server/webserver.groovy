// Groovy web server using only the JDK in 8 lines please
import com.sun.net.httpserver.*

int port = args.length == 1 ? Integer.parseInt(args[0]) : 8888
String statics = '/login.cgi_files/'

def replaceSub(base, part, value) {
   int p = base.indexOf(part)
   StringBuilder sb = new StringBuilder()
   while (p != -1) {
      if (p > 0) 
        sb.append(base[0..p-1])
      sb.append(value)
      base = base.substring(p+part.length())
      p = base.indexOf(part)
   }
   sb.append(base)
   return sb.toString()
}

def parseParams(String q) {
    def resp = [:]
    q?.split('&').each { frag -> 
       def p = frag.indexOf('=')
       def l = frag.length()
       def k = p == -1 ? frag : frag[0..p-1]
       def v = p == -1 ? '' : (p < l-1 ? frag[p+1..l-1] : '')
       resp[k]=URLDecoder.decode(v)
    }
    resp    
}

def extractPost(HttpExchange exchange) {
    def query = exchange.requestBody.text
    parseParams(query)
}

def send302(HttpExchange exchange, String uri) {
    exchange.getResponseHeaders().set("Location",uri)
    exchange.sendResponseHeaders(302,0);
    exchange.responseBody.close()
}

def send404(HttpExchange exchange, String uri) {
    exchange.sendResponseHeaders(404,0);
    exchange.responseBody.write(("Not found " + uri).bytes)
    exchange.responseBody.close()
}

def send500(HttpExchange exchange) {
    exchange.sendResponseHeaders(500,0);
    exchange.responseBody.write("FATAL ERROR".bytes)
    exchange.responseBody.close()
}

def sendForm(HttpExchange exchange, parameters) {
    def is = new FileInputStream('static/login.cgi.html')
    exchange.getResponseHeaders().add("Content-type","text/html")
    exchange.sendResponseHeaders(200,0);
    is.eachLine { line ->
        if (line.indexOf('${') != -1) {
            parameters.each { k, v ->
                line = replaceSub(line,'${'+k+'}',v)
            }
        }
        exchange.responseBody.write(line.bytes)
        exchange.responseBody.write("\n".bytes)
    }
    is.close()
    exchange.responseBody.close()
}

def sendStatic(HttpExchange exchange, String path) {
    def mimes = ['gif':'image/gif', 'css':'text/css']
    def ext = path[-3..-1]
    if (mimes[ext] == null) {
        send404(exchange,path)
        return
    }
    def file = new File('static' + path)
    if (!file.exists()) {
        send404(exchange,path)
        return
    }
    exchange.getResponseHeaders().add("Content-type",mimes[ext])
    exchange.sendResponseHeaders(200,0);
    def is = new FileInputStream(file)
    exchange.responseBody << is
    exchange.responseBody.close()
    is.close()
}

def validLogin(username, password) {
    def f = new File('db/'+username+'.txt')
    return f.exists() && f.text.indexOf('password:'+password+"\n") != -1
}

HttpServer server = HttpServer.create(new InetSocketAddress(port),0)
server.createContext('/', { HttpExchange exchange ->
    def method = exchange.getRequestMethod()
    def uri = exchange.getRequestURI()
    def uriPath = uri.getPath()
    def queryPart = uri.getQuery()
    println method + ": " + uriPath
    try {
        if ('POST'.equals(method)) {
           def parameters = extractPost(exchange)           
           def username = parameters['credential_0']
           def password = parameters['credential_1']
           if (validLogin(username, password)) {
              def destURL = parameters['destURL']
              if (destURL.charAt(destURL.length()-1) != '/')
              {
                  destURL += '/'
              }
              def redirectUrl = destURL + '?wasmIkey=' + username
              println('destURL=' + redirectUrl)
              send302(exchange, redirectUrl)
           } else {
              parameters['error'] = 'Invalid login'
              sendForm(exchange,parameters)
           }
           return
        }
        if ('/login.cgi'.equals(uriPath)) {
            def parameters = parseParams(queryPart)
            if (parameters['error'] == null) parameters['error'] = ''
            sendForm(exchange, parameters)
        } else if (uriPath.startsWith('/login.cgi_files/') || uriPath.startsWith('/images/')) {
            sendStatic(exchange, uriPath)
        } else {
            send404(exchange, uriPath)
        }
    } catch(Exception e) {
       e.printStackTrace(System.out)
       send500(exchange)
    }
}  as HttpHandler)
println "Serving port " + port + ". Control-C to finish"
server.start();
