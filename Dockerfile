FROM percona/percona-server-mongodb:4.4

COPY ./mongodb-keyfile /usr/shared/mongod/

USER root
RUN chown mongodb ./usr/shared/mongod/mongodb-keyfile
RUN chmod 600 /usr/shared/mongod/mongodb-keyfile

EXPOSE 27017:27017

USER mongodb

#CMD ["mongod", "--enableEncryption", "--encryptionKeyFile",  "/usr/shared/mongod/mongodb-keyfile"]