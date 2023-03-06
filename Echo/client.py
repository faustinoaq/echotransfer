import http.client

print('Ejecutando Cliente...')
try:
    ip = input('IP: ')
    puerto = input('Puerto: ')
    while True:
        mensaje = input('Mensaje: ')
        cliente = http.client.HTTPConnection(ip, puerto, timeout=2)
        cliente.request('POST', '/', mensaje)
        respuesta = cliente.getresponse()
        datos = respuesta.read()
        print(datos.decode('utf-8'))
except:
    print('Sin comunicacion con Servidor. Deteniendo Cliente...')
