import socket

import sys
import time
from grove.adc import ADC
import math

HOST = "100.71.88.14" # IP address of your Raspberry PI
PORT = 65432          # Port to listen on (non-privileged ports are > 1023)

class GroveGSRSensor:

    def __init__(self, channel):
        self.channel = channel
        self.adc = ADC()

    @property
    def GSR(self):
        value = self.adc.read(self.channel)
        return value

Grove = GroveGSRSensor

def main():
    if len(sys.argv) < 2:
        print('Usage: {} adc_channel'.format(sys.argv[0]))
        sys.exit(1)

    sensor = GroveGSRSensor(int(sys.argv[1]))

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()
        delimiter = '\n'
        try:
            while True:
                client, clientInfo = s.accept()
                print("server recv from: ", clientInfo)
                #data = client.recv(1024);      # receive 1024 Bytes of message in binary format

                print(sensor.GSR)
                client.sendall(bytes(str(sensor.GSR), 'utf8'))
                time.sleep(1)
                client.close()
        except Exception as e:
            print("Closing socket reason ", e)
            client.close()
            s.close()

if __name__ == '__main__':
     main()
