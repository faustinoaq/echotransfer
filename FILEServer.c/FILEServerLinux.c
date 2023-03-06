#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define BUFFER_SIZE 256
#define PORT        9000

void error(const char *msg) {
    printf("%s\n", msg);
    exit(1);
}

int main() {
    struct sockaddr_in serv_addr, cli_addr;
    int sockfd, newsockfd, nbytes, mbytes, tbytes = 0, sbytes, wbytes = 0, flagTemp;
    unsigned char buffer[ BUFFER_SIZE];
    socklen_t clilen;
    FILE *temporal;
    FILE *final;
    char str[ BUFFER_SIZE], filename[ BUFFER_SIZE], time[ BUFFER_SIZE], continuar = 'n', tmpfile[ BUFFER_SIZE] = "";

    sockfd =  socket(AF_INET, SOCK_STREAM, 0);

    if (sockfd < 0) {
        error("Error al abrir socket");
    }

    bzero((char *) &serv_addr, sizeof(serv_addr));

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);

    if (bind(sockfd, (struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) {
        error("Error al hacer bind");
    }

    listen(sockfd,5);

    clilen = sizeof(cli_addr);

    while (1) {

        printf("FILE Server...\n");

        newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
        if (newsockfd < 0) {
            error("Error al hacer accept");
        }


        bzero(buffer, BUFFER_SIZE);

        nbytes = read(newsockfd,buffer,BUFFER_SIZE - 1);
        if (nbytes < 0) {
            error("Error al leer socket\n");
        }

        sscanf(buffer, "%s %d %s", filename, &sbytes, time);
        printf("Datos del cliente:\nName: %s\nSize: %d\nTime: %s\n", filename, sbytes, time);

        bzero(tmpfile,  BUFFER_SIZE);
        strcat(tmpfile, filename);
        strcat(tmpfile, ".tmp");

        temporal = fopen(tmpfile, "r");

        // tbytes = 0;
        if(temporal != NULL) {
            fseek(temporal, 0L, SEEK_END);
            tbytes = ftell(temporal);
            fseek(temporal, 0, SEEK_SET);
            if (tbytes > sbytes) {
                tbytes = 0;
            }
            sprintf(str, "%d\n", tbytes);
            fclose(temporal);
        } else {
            tbytes = 0;
            printf("No hay archivo temporal\n");
            strcpy(str, "0\n");
        }


        // printf("%s", str);
        send(newsockfd, str, sizeof(str), 0);

        if (tbytes > 0) {
            bzero(buffer, BUFFER_SIZE);
            read(newsockfd, buffer, BUFFER_SIZE - 1);
            continuar = buffer[0];
            if (continuar == 'n') {
                tbytes = 0;
                printf("Temporal borrado!\n");
                remove(tmpfile);
            }
        } else {
            continuar = 'n';
        }

        temporal = fopen(tmpfile,"ab");
        if(temporal == NULL) {
            error("Error al abrir archivo temporal");
        }

        printf("Recibiendo archivo %s...\n", filename);

        flagTemp = 0;
        wbytes = 0;
        while (1) {
            bzero(buffer, BUFFER_SIZE);
            nbytes = read(newsockfd, buffer, BUFFER_SIZE - 1);
            if (nbytes > 0) {
                wbytes = wbytes + nbytes;
            }
            if (nbytes > 0) {
                mbytes = fwrite(buffer, 1, nbytes,temporal); 
            } else {
                printf("w=%d s=%d t=%d, c=%c\n", wbytes, sbytes, tbytes, continuar);
                if (((wbytes != sbytes) && (tbytes == 0)) || ((tbytes > 0) && (tbytes + wbytes < sbytes))) {
                    printf("Escritos temporalmente %d bytes\n", wbytes);
                    flagTemp = 1;
                }
                break;
            }
            if (nbytes != mbytes) {
                printf("Error al escribir byte n=%d m=%d\n", nbytes, mbytes);
            }
        }
        
        fclose(temporal);

        if (flagTemp) {
            continue;
        }

        temporal = fopen(tmpfile, "rb");
        if(temporal == NULL) {
            error("Error al abrir archivo temporal");
        }
        final = fopen(filename, "wb");
        if(final == NULL) {
            error("Error al abrir archivo temporal");
        }

        wbytes = 0;
        while (1) {
            nbytes = fread(buffer, 1, sizeof(buffer), temporal);
            if (nbytes > 0) {
                wbytes = wbytes + nbytes;
                mbytes = fwrite(buffer, 1, nbytes, final); 
            } else {
                break;
            }
            if (nbytes != mbytes) {
                printf("Error al escribir byte n=%d m=%d\n",nbytes, mbytes);
            }
        }

        fclose(temporal);
        fclose(final);
        remove(tmpfile);
        printf("Escritos correctamente total bytes: %d\n", wbytes);

        close(newsockfd);
    }
    close(sockfd);
    return 0;
}
