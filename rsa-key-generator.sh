#!/bin/bash
# Generate RSA Private Key
openssl genrsa -out private_key.pem 2048
# Generate RSA Public Key
openssl rsa -in private_key.pem -outform PEM -pubout -out public_key.pem
echo "RSA keys generated: private_key.pem and public_key.pem"
