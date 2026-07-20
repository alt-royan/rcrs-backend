#!/bin/bash
set -e

# Wait for mongod to be ready
sleep 3

# Initialize replica set
mongosh --quiet --eval '
  try {
    rs.status();
  } catch (e) {
    rs.initiate({ _id: "rs0", members: [{ _id: 0, host: "localhost:27017" }] });
  }
'

echo "Replica set initialized"