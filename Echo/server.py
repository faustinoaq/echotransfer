import sys
import http.server
import socketserver

ip = '0.0.0.0'
try:
    puerto = int(sys.argv[1])
except:
    puerto = 8000

class Handler(http.server.BaseHTTPRequestHandler):

    def do_POST(self):
        body = self.rfile.read(int(self.headers['Content-Length']))
        self.send_response(200)
        self.end_headers()
        message = 'Hola, Cliente: {0}'.format(body.decode('utf-8'))
        self.wfile.write(message.encode('utf-8'))

class MyServer(socketserver.TCPServer):
    allow_reuse_address = True

with MyServer((ip, puerto), Handler) as httpd:
    try:
        print('Ejecutando Servidor...')
        httpd.allow_reuse_address = True
        httpd.serve_forever()
    except:
        print('Deteniendo Servidor...')
        httpd.shutdown()
